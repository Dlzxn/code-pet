package dev.codepet;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.util.xmlb.XmlSerializer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** CI regressions for multiplayer XP, passive keys, viewport physics and the SDK paint boundary. */
public final class GameplayChecks {
    static int checks;
    static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        collection();input();physics();phrases();lounge();
        SwingUtilities.invokeAndWait(()->{background();scene();});
        System.out.println("PASS: "+checks+" gameplay checks (collection, input, ball, phrases, editor background, lifecycle)");
    }
    static void collection(){
        PetCollection c=new PetCollection();PetModel cat=c.active(),fox=c.create("fox","Люми");
        check(c.activePets().size()==2,"Creating a companion retains the previous one");
        c.select(1);check(c.active()==cat&&c.enabled(fox.id),"Customization selection does not change enabled companions");
        c.file("source",0,0);c.recordFor("source",100,1,List.of(cat.id,fox.id,cat.id,"missing"));
        check(cat.xp==7&&fox.xp==7,"Every recipient earns the same growth exactly once");
        c.recordFor("source",0,0,List.of(cat.id,fox.id));c.recordFor("source",100,1,List.of(cat.id,fox.id));
        check(cat.xp==7&&fox.xp==7,"Deletion and retyping do not replay multiplayer XP");
        c.enable(cat.id,false);check(!c.enabled(cat.id)&&c.enabled(fox.id),"Disabling is per companion");
        c.recordFor("source",200,2,c.activePets().stream().map(p->p.id).toList());
        check(cat.xp==7&&fox.xp==14,"Disabled companions receive no new batch");
        c.behindText=false;c.keyboardGames=false;
        PetCollection restored=XmlSerializer.deserialize(XmlSerializer.serialize(c.snapshot()),PetCollection.class);restored.normalize();
        check(!restored.enabled(cat.id)&&restored.enabled(fox.id)&&!restored.behindText&&!restored.keyboardGames,"Enabled IDs and global settings survive SDK XML serialization");
        c.enable(cat.id,true);check(!restored.enabled(cat.id),"Save snapshot owns an independent enabled-ID list");
        PetCollection legacy=new PetCollection();legacy.active();legacy.activeIds=null;legacy.activeIdsInitialized=false;legacy.normalize();
        check(legacy.activePets().size()==1,"Single-companion saves migrate to one enabled companion");
        c.enable(cat.id,false);c.enable(fox.id,false);check(c.activePets().isEmpty(),"The last companion can be put away");
        PetCollection empty=XmlSerializer.deserialize(XmlSerializer.serialize(c.snapshot()),PetCollection.class);empty.normalize();check(empty.activePets().isEmpty(),"An intentionally empty screen stays empty after restart");
        empty.enable(cat.id,true);check(empty.activePets().size()==1,"A stored companion can be released again");
    }
    static void input(){
        long t=1_000_000_000L;PetPlayInput input=new PetPlayInput(t);
        input.pressed(KeyEvent.VK_ENTER,t,true);input.pressed(KeyEvent.VK_ENTER,t+100_000_000L,true);
        check(input.ballSerial==1,"Held Enter is bounded");input.pressed(KeyEvent.VK_ENTER,t+300_000_000L,true);check(input.ballSerial==2,"Later Enter launches a new ball");
        input.pressed(KeyEvent.VK_TAB,t,true);input.pressed(KeyEvent.VK_TAB,t+900_000_000L,true);check(input.balloonSerial==1,"Held Tab cannot create unlimited balloons");
        input.pressed(KeyEvent.VK_TAB,t+1_000_000_000L,true);check(input.balloonSerial==2,"Tab cooldown expires");
        input.pressed(KeyEvent.VK_DELETE,t,true);input.pressed(KeyEvent.VK_DELETE,t+500_000_000L,true);
        check(!input.cinema(t+649_000_000L)&&input.cinema(t+650_000_000L),"Delete auto-repeat preserves the initial hold threshold");
        input.released(KeyEvent.VK_A,t+700_000_000L);check(input.deleteStart==t,"Unrelated release cannot end a held Delete");
        input.released(KeyEvent.VK_DELETE,t+800_000_000L);check(input.cinema(t+1_000_000_000L)&&!input.cinema(t+5_000_000_000L),"Cinema briefly remains after release, then ends");
        input.reset(t);input.pressed(KeyEvent.VK_BACK_SPACE,t,true);input.released(KeyEvent.VK_BACK_SPACE,t+200_000_000L);check(!input.cinema(t+700_000_000L),"A quick deletion has no cinema scene");
        long balls=input.ballSerial,balloons=input.balloonSerial;input.pressed(KeyEvent.VK_ENTER,t,false);input.pressed(KeyEvent.VK_TAB,t,false);input.pressed(KeyEvent.VK_DELETE,t,false);
        check(input.ballSerial==balls&&input.balloonSerial==balloons&&!input.cinema(t+2_000_000_000L),"Disabled keyboard games create no toys or chair");
        input.reset(t);check(!input.away(t+45_000_000_000L)&&input.away(t+46_000_000_000L),"Absence requires both mouse and keyboard inactivity");
        input.pressed(KeyEvent.VK_A,t+46_000_000_000L,true);check(!input.away(t+47_000_000_000L),"Typing ends absence");
    }
    static void physics(){
        PetBall ball=new PetBall();Random random=new Random(9);long t=1_000_000_000L;
        Rectangle view=new Rectangle(100,200,640,480);ball.launch(view,new Point(-1000,9999),t,random);
        check(view.contains(ball.x,ball.y),"Launch clamps an offscreen caret to the viewport");
        boolean bounded=true;for(int i=1;i<1500;i++){if(i==500)view=new Rectangle(300,600,320,220);ball.update(view,.016,t+i*16_000_000L);bounded&=Double.isFinite(ball.x)&&Double.isFinite(ball.y)&&view.contains(ball.x,ball.y);}
        check(bounded,"Bounces remain finite and inside a scrolled, resized viewport");
        ball.x=view.x+7;ball.vx=-200;ball.update(view,.04,t+24_000_000_000L);check(ball.vx>0,"Wall collision reverses horizontal velocity");
        ball.update(view,.04,t+26_000_000_000L);check(!ball.active,"Old ball is retired");
        ball.launch(view,new Point(350,650),t,random);ball.update(new Rectangle(0,0,10,10),.04,t);check(!ball.active,"Tiny editors suspend physics");
        check(PetBall.jump("frog")>PetBall.jump("cat")&&PetBall.jump("cat")>PetBall.jump("turtle"),"Jump strength follows anatomy");
        for(PetCatalog.Species s:PetCatalog.SPECIES)check(PetBall.jump(s.id())>0,"Every species has a jump strength");
    }
    static void phrases(){
        Set<String> all=new HashSet<>();for(PetCatalog.Species s:PetCatalog.SPECIES){PetModel pet=new PetModel();pet.type=s.id();pet.name="Спутник";List<String> phrases=PetRunPhrases.phrases(pet);
            check(phrases.size()>=10&&phrases.size()<=15&&new HashSet<>(phrases).size()==phrases.size(),"Each species has 10–15 distinct launch phrases");
            for(String phrase:phrases)check(phrase.contains(pet.name)&&phrase.contains(s.name().toLowerCase(Locale.ROOT))&&all.add(phrase),"Launch phrase identifies the companion and its species");
        }
    }
    static void lounge(){
        for(Rectangle area:List.of(new Rectangle(12,60,960,600),new Rectangle(20,40,280,220),new Rectangle(0,0,140,100)))for(int count:new int[]{1,2,6,15,30,100}){
            PetLounge.Layout layout=PetLounge.arrange(area,count);check(layout!=null&&layout.seats().size()==count&&area.contains(layout.bounds()),"Sofa layout includes every companion inside the available area");
            check(!layout.hit(null)&&!layout.hit(new Point(area.x-1,area.y-1)),"Hover outside companions does not scatter the group");
        }
        PetLounge.Layout layout=PetLounge.arrange(new Rectangle(10,60,900,500),4);Rectangle seat=layout.seats().get(0);
        check(layout.hit(new Point((int)seat.getCenterX(),(int)seat.getMaxY()-35)),"Hover on a seated companion scatters the group");
        check(!layout.hit(new Point((int)layout.television().getCenterX(),(int)layout.television().getCenterY())),"Television itself is not a pet hover target");
        check(PetLounge.arrange(new Rectangle(0,0,80,50),4)==null,"Tiny windows omit a sofa that cannot fit");
    }
    static final class EditorFixture {
        final String source="source_value = 123456789\n";
        final Rectangle view=new Rectangle(0,0,800,500);
        final JPanel content=new JPanel();final JLayeredPane layer=new JLayeredPane(){@Override public boolean isShowing(){return true;}@Override public Point getLocationOnScreen(){return new Point();}};
        CustomHighlighterRenderer renderer;int markers,disposed;boolean greedyLeft,greedyRight,valid;
        final RangeHighlighter marker=SdkChecks.proxy(RangeHighlighter.class,(p,m,a)->switch(m.getName()){
            case "setCustomRenderer"->{renderer=(CustomHighlighterRenderer)a[0];yield null;}
            case "setGreedyToLeft"->{greedyLeft=(boolean)a[0];yield null;}
            case "setGreedyToRight"->{greedyRight=(boolean)a[0];yield null;}
            case "isValid"->valid;case "dispose"->{valid=false;disposed++;yield null;}
            default->SdkChecks.fallback(p,m,a);
        });
        final Document document=SdkChecks.proxy(Document.class,(p,m,a)->switch(m.getName()){
            case "setText","insertString","deleteString","replaceString"->throw new AssertionError("Animation attempted to edit source");
            case "getTextLength"->source.length();case "getImmutableCharSequence","getText","getCharsSequence"->source;case "getLineCount"->2;
            case "getLineStartOffset"->(int)a[0]==0?0:source.length();case "getLineEndOffset"->(int)a[0]==0?source.length()-1:source.length();
            default->SdkChecks.fallback(p,m,a);
        });
        final MarkupModel markup=SdkChecks.proxy(MarkupModel.class,(p,m,a)->{
            if(m.getName().equals("addRangeHighlighter")){check((int)a[0]==0&&(int)a[1]==source.length()&&a[3]==null,"Background covers the document without text attributes");markers++;valid=true;return marker;}return SdkChecks.fallback(p,m,a);
        });
        final ScrollingModel scrolling=SdkChecks.proxy(ScrollingModel.class,(p,m,a)->m.getName().equals("getVisibleArea")?new Rectangle(view):SdkChecks.fallback(p,m,a));
        final CaretModel caret=SdkChecks.proxy(CaretModel.class,(p,m,a)->m.getName().equals("getVisualPosition")?new VisualPosition(0,5):SdkChecks.fallback(p,m,a));
        final Editor editor=SdkChecks.proxy(Editor.class,(p,m,a)->switch(m.getName()){
            case "getDocument"->document;case "getMarkupModel"->markup;case "getScrollingModel"->scrolling;case "getCaretModel"->caret;case "getContentComponent"->content;
            case "visualPositionToXY"->new Point(45,20);case "xyToLogicalPosition"->new LogicalPosition(0,1);case "getLineHeight"->20;case "getEditorKind"->EditorKind.MAIN_EDITOR;
            default->SdkChecks.fallback(p,m,a);
        });
        EditorFixture(){layer.setSize(1000,700);content.setBounds(80,60,800,500);layer.add(content,JLayeredPane.DEFAULT_LAYER);}
        void paint(){BufferedImage image=new BufferedImage(1000,700,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();try{renderer.paint(editor,marker,g);}finally{g.dispose();}}
    }
    static void background(){
        EditorFixture f=new EditorFixture();PetEditorBackground background=new PetEditorBackground(f.editor);
        check(f.renderer.getOrder()==CustomHighlighterOrder.AFTER_BACKGROUND&&f.greedyLeft&&f.greedyRight,"SDK renderer runs after background and before glyphs, and follows document growth");
        boolean[] painted={false};background.update(g->{painted[0]=true;g.fillRect(5,5,10,10);},new Rectangle(5,5,10,10));f.paint();check(painted[0],"Editor hook invokes the current frame painter");
        background.close();check(f.disposed==1&&f.source.equals(f.document.getText()),"Closing releases the highlighter without changing source");
    }
    static void scene(){
        EditorFixture f=new EditorFixture();PetCollection c=new PetCollection();PetModel cat=c.active(),frog=c.create("frog","Ква");cat.back="Джетпак";
        long t=System.nanoTime();PetPlayInput input=new PetPlayInput(t);PetPlayScene scene=new PetPlayScene();
        scene.update(f.editor,null,f.layer,c,input,t,.04);f.paint();check(f.markers==1,"Multiple companions share one editor background hook");
        input.pressed(KeyEvent.VK_ENTER,t,true);for(int i=1;i<100;i++){scene.update(f.editor,null,f.layer,c,input,t+i*40_000_000L,.04);f.paint();}
        input.pressed(KeyEvent.VK_TAB,t+4_000_000_000L,true);scene.update(f.editor,null,f.layer,c,input,t+4_000_000_000L,.04);f.paint();
        input.pressed(KeyEvent.VK_BACK_SPACE,t+5_000_000_000L,true);scene.update(f.editor,null,f.layer,c,input,t+6_000_000_000L,.04);f.paint();
        c.runSerial++;scene.update(f.editor,null,f.layer,c,input,t+7_000_000_000L,.04);f.paint();
        // A held Delete takes priority over the Run lounge; start a fresh launch after release.
        input.reset(t+8_000_000_000L);c.runSerial++;scene.update(f.editor,null,f.layer,c,input,t+8_000_000_000L,.04);
        PetPlayScene.Surface surface=(PetPlayScene.Surface)Arrays.stream(f.layer.getComponents()).filter(component->component instanceof PetPlayScene.Surface).findFirst().orElseThrow();
        check(surface.lounge!=null&&surface.actors.size()==2&&surface.actors.stream().allMatch(a->a.frame().action()==PetBehavior.Action.LOUNGE),"Run seats all enabled companions in a shared lounge");
        Rectangle seat=surface.lounge.seats().get(0);input.mouseScreen=new Point((int)seat.getCenterX(),(int)seat.getMaxY()-35);scene.update(f.editor,null,f.layer,c,input,t+8_040_000_000L,.04);
        check(surface.lounge==null&&surface.actors.stream().allMatch(a->a.frame().action()==PetBehavior.Action.RUN),"Hover dismisses the sofa and scatters every companion");
        scene.update(f.editor,null,f.layer,c,input,t+9_200_000_000L,.04);scene.update(f.editor,null,f.layer,c,input,t+9_240_000_000L,.04);check(surface.actors.isEmpty(),"After scattering, companions resume their editor activities");
        check(cat.xp==0&&frog.xp==0&&f.source.equals(f.document.getText()),"Ball, jetpack, balloon, chair and launch scenes change neither XP nor document");
        c.keyboardGames=false;scene.update(f.editor,null,f.layer,c,input,t+10_000_000_000L,.04);c.behindText=false;scene.update(f.editor,null,f.layer,c,input,t+11_000_000_000L,.04);
        check(f.disposed==1,"Disabling editor presence releases the background hook");
        c.keyboardGames=true;input.pressed(KeyEvent.VK_ENTER,t+12_000_000_000L,true);scene.update(f.editor,null,f.layer,c,input,t+12_000_000_000L,.04);check(f.markers==2,"Games remain visible with autonomous editor walks disabled");
        check(PetRoaming.accepts(f.editor,new KeyEvent(new JPanel(),KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_ENTER,'\n'),f.content),"Actual editor focus handles keys whose source is retargeted by the IDE");
        check(!PetRoaming.accepts(f.editor,new KeyEvent(f.content,KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_ENTER,'\n'),new JTextField()),"Popup settings do not trigger games in the background editor");
        scene.suspend();scene.close();check(f.layer.getComponentCount()==1,"Closing removes the foreground surface and leaves editor components intact");
    }
}
