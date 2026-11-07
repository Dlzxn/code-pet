package dev.codepet;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.wm.ToolWindow;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/** Multiple autonomous companions; a single clock and one background painter per selected editor. */
final class PetPlayScene implements AutoCloseable {
    private static final class Buddy {
        final PetBehavior behavior=new PetBehavior();final PetDialogue dialogue=new PetDialogue();double x,y,vx,vy;long balloonSince,lastJump,runUntil;double balloonX,renderScale;boolean grow;int files;long signal;Rectangle hoverBounds;
        Buddy(PetModel pet){files=pet.filesCreated;signal=pet.ideSignal;behavior.autonomy(pet,false);behavior.startStory(PetStories.choose(pet,new Random(pet.id.hashCode()),false));}
    }
    record Actor(PetModel pet,PetBehavior.Frame frame,Rectangle bounds,double scale,String speech,boolean balloon){}
    record BallFrame(double x,double y,boolean active){}
    private final Map<String,Buddy> buddies=new LinkedHashMap<>();
    private final Random random=new Random();
    private final PetBall ball=new PetBall();
    private final Surface surface=new Surface();
    private Editor editor;private PetEditorBackground background;private JLayeredPane layer;
    private long lastBall,lastBalloon,lastRun;private int speaker;
    private String runSpeech="";private Point runAt;
    private long created=System.nanoTime();
    void update(Editor next,ToolWindow projectWindow,JLayeredPane target,PetCollection collection,PetPlayInput input,long now,double dt){
        List<PetModel> pets=collection.activePets();Set<String> ids=new HashSet<>();pets.forEach(p->ids.add(p.id));buddies.keySet().removeIf(id->!ids.contains(id));
        if(layer!=target){detachSurface();layer=target;layer.add(surface,JLayeredPane.PALETTE_LAYER);}
        if(surface.getWidth()!=layer.getWidth()||surface.getHeight()!=layer.getHeight())surface.setBounds(0,0,layer.getWidth(),layer.getHeight());
        if(editor!=next){if(background!=null)background.close();background=null;editor=next;ball.active=false;buddies.values().forEach(b->{b.x=b.y=0;});}
        Rectangle view=editor==null?new Rectangle(0,0,Math.max(200,layer.getWidth()),Math.max(200,layer.getHeight())):editor.getScrollingModel().getVisibleArea();
        if(editor!=null&&collection.behindText&&background==null)background=new PetEditorBackground(editor);
        if((!collection.behindText||editor==null)&&background!=null){background.close();background=null;}
        if(collection.keyboardGames&&collection.behindText&&input.ballSerial!=lastBall&&editor!=null){lastBall=input.ballSerial;Point at=editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());at.translate(14,editor.getLineHeight());ball.launch(view,at,now,random);}
        if(!collection.keyboardGames||!collection.behindText){ball.active=false;lastBall=input.ballSerial;lastBalloon=input.balloonSerial;buddies.values().forEach(b->b.balloonSince=0);}
        boolean newBalloon=collection.keyboardGames&&collection.behindText&&input.balloonSerial!=lastBalloon&&editor!=null;if(newBalloon)lastBalloon=input.balloonSerial;
        boolean newRun=lastRun!=collection.runSerial;
        if(newRun){lastRun=collection.runSerial;runAt=runButton(layer);speaker=pets.isEmpty()?0:(speaker+1)%pets.size();if(!pets.isEmpty()){List<String> phrases=PetRunPhrases.phrases(pets.get(speaker));runSpeech=phrases.get(random.nextInt(phrases.size()));}}
        ball.update(view,dt,now);
        ArrayList<Actor> back=new ArrayList<>(),front=new ArrayList<>();int index=0;
        Rectangle left=projectFloor(projectWindow);
        Point mouse=null;if(input.mouseScreen!=null&&layer.isShowing()){mouse=new Point(input.mouseScreen);SwingUtilities.convertPointFromScreen(mouse,layer);}
        for(PetModel pet:pets){
            Buddy b=buddies.computeIfAbsent(pet.id,id->new Buddy(pet));
            boolean hovered=mouse!=null&&b.hoverBounds!=null&&b.hoverBounds.contains(mouse);b.behavior.hover(hovered);
            if(b.x==0&&b.y==0){b.x=view.x+50+index*90;b.y=view.getMaxY()-12;}
            b.behavior.autonomy(pet,input.away(now));
            b.dialogue.update(dt,pet);
            if(pet.filesCreated!=b.files){b.files=pet.filesCreated;b.behavior.eat();b.dialogue.say("Новый файл — перекус!");}
            if(pet.ideSignal!=b.signal){b.signal=pet.ideSignal;b.behavior.force(PetReactions.action(pet.ideEvent));b.dialogue.say(PetReactions.text(pet.ideEvent));}
            if(pet.level()>pet.deliveredGiftLevel&&!b.behavior.inStory()){pet.receiveGift();b.behavior.force(PetBehavior.Action.SHOW_GIFT);}
            int complete=b.behavior.consumeCompleted();if(complete>0)b.grow=random.nextDouble()<.28;pet.storiesCompleted=(int)Math.min(Integer.MAX_VALUE,(long)pet.storiesCompleted+complete);
            if(newBalloon&&pet.animated){b.balloonSince=now;b.balloonX=Math.max(view.x+40,Math.min(view.getMaxX()-40,b.x));}
            if(newRun){b.runUntil=now+7_000_000_000L;b.balloonSince=0;}
            double desired=pet.roamingSize/80.0*(pet.growWhenIdle&&input.away(now)&&b.grow?1.6:1);if(b.renderScale<=0)b.renderScale=desired;b.renderScale+=(desired-b.renderScale)*(1-Math.exp(-dt*3));double scale=b.renderScale;String speech=b.dialogue.text(pet);boolean balloon=false,foreground=false;
            PetBehavior.Frame frame;Rectangle bounds;
            if(b.runUntil>now&&runAt!=null){
                scale=Math.max(.42,scale);bounds=new Rectangle(Math.max(4,Math.min(layer.getWidth()-250,runAt.x-125-index*65)),Math.max(8,runAt.y+10+index%2*20),250,170);
                frame=new PetBehavior.Frame(PetBehavior.Action.WAVE,pet.animated?(now-created)/1e9*6:0,.5,1,false);speech=index==speaker&&pet.speech?runSpeech:"";foreground=true;
            }else if(collection.keyboardGames&&input.cinema(now)&&editor!=null&&pet.animated){
                scale*=1.8;int columns=Math.max(1,view.width/165);bounds=new Rectangle(Math.max(view.x,(int)(view.getMaxX()-235-index%columns*165)),Math.max(view.y,(int)(view.getMaxY()-235-index/columns*145)),230,230);frame=new PetBehavior.Frame(PetBehavior.Action.CINEMA,(now-created)/1e9*6,.5,1,false);
            }else if(b.balloonSince>0&&editor!=null&&pet.animated){
                double age=(now-b.balloonSince)/1e9;b.x=b.balloonX+Math.sin(age*1.8+index)*27+Math.sin(age*.73+index)*42;b.y=view.getMaxY()-20-age*25-Math.sin(age*1.3)*12;
                if(age>14||b.y<view.y+55)b.balloonSince=0;
                b.x=Math.max(view.x+45,Math.min(view.getMaxX()-45,b.x));bounds=bodyBounds(b.x,b.y,scale);frame=new PetBehavior.Frame(PetBehavior.Action.BALLOON_RIDE,age*6,.5,1,false);balloon=b.balloonSince>0;
            }else if(ball.active&&editor!=null&&pet.animated){
                double age=(now-ball.launched)/1e9;int facing=ball.x>=b.x?1:-1;
                if(age>.65){
                    double speed=pet.back.equals("Джетпак")?240:145;
                    b.vx+=(facing*speed-b.vx)*(1-Math.exp(-dt*8));b.x+=b.vx*dt;
                    if(pet.back.equals("Джетпак")){b.y+=(ball.y+35-b.y)*(1-Math.exp(-dt*3));}
                    else{
                        if(b.y>=view.getMaxY()-13&&ball.y<b.y-35&&Math.abs(ball.x-b.x)<150&&now-b.lastJump>800_000_000L){b.vy=-PetBall.jump(pet.type);b.lastJump=now;}
                        b.vy+=430*dt;b.y+=b.vy*dt;if(b.y>view.getMaxY()-12){b.y=view.getMaxY()-12;b.vy=0;}
                    }
                }
                b.x=Math.max(view.x+40,Math.min(view.getMaxX()-40,b.x));b.y=Math.max(view.y+70,Math.min(view.getMaxY()-12,b.y));bounds=bodyBounds(b.x,b.y,scale);
                frame=new PetBehavior.Frame(age<.65?PetBehavior.Action.BALL_THINK:pet.back.equals("Джетпак")?PetBehavior.Action.FLOAT:PetBehavior.Action.RUN,age*9,.5,facing,false);
            }else{
                b.behavior.update(dt,Math.max(1,view.width-130),pet.animated);
                if(!b.behavior.inStory()&&b.behavior.action()==PetBehavior.Action.SIT)b.behavior.startStory(PetStories.choose(pet,random,input.away(now)));
                double fraction=Math.floorMod(pet.id.hashCode(),997)/997.0;
                if(left!=null&&((now-created)/22_000_000_000L+index)%3==0){
                    int w=150,h=125;b.x=view.x+view.width*fraction;b.y=view.getMaxY()-12;
                    bounds=new Rectangle(left.x+(int)(Math.max(0,left.width-w)*b.behavior.position()),left.y+left.height-h,w,h);foreground=true;
                }else{
                    b.x=view.x+55+b.behavior.position()*Math.max(1,view.width-110);b.y=view.y+100+fraction*Math.max(0,view.height-160);
                    bounds=bodyBounds(b.x,b.y,scale);
                }
                char glyph=';';if(editor!=null&&editor.getDocument().getTextLength()>0){int offset=editor.xyToLogicalPosition(new Point((int)b.x,(int)b.y)).line;int line=Math.max(0,Math.min(editor.getDocument().getLineCount()-1,offset));int start=editor.getDocument().getLineStartOffset(line),end=editor.getDocument().getLineEndOffset(line);if(start<end)glyph=editor.getDocument().getImmutableCharSequence().charAt(Math.min(end-1,start+2));}b.behavior.glyph(glyph);frame=b.behavior.frame();
            }
            frame=new PetBehavior.Frame(frame.action(),frame.phase(),frame.progress(),frame.direction(),hovered,frame.glyph(),frame.prop());
            b.hoverBounds=foreground?new Rectangle(bounds):editor==null?null:SwingUtilities.convertRectangle(editor.getContentComponent(),bounds,layer);
            if(b.hoverBounds!=null){int width=Math.max(30,(int)(150*scale)),height=Math.max(45,(int)(170*scale));b.hoverBounds=new Rectangle((int)b.hoverBounds.getCenterX()-width/2,(int)b.hoverBounds.getMaxY()-16-height,width,height);}
            if(frame.action()!=PetBehavior.Action.HIDDEN){
                if(foreground)front.add(new Actor(pet,frame,bounds,scale,speech,balloon));
                else if(editor!=null&&collection.behindText)back.add(new Actor(pet,frame,bounds,scale,speech,balloon));
            }
            index++;
        }
        BallFrame ballFrame=new BallFrame(ball.x,ball.y,ball.active);
        if(background!=null){List<Actor> snapshot=List.copyOf(back);background.update(g->paint(g,snapshot,ballFrame),dirty(snapshot,ballFrame));}
        surface.ball=new BallFrame(0,0,false);
        surface.actors=List.copyOf(front);surface.present(dirty(front,surface.ball));surface.setVisible(!front.isEmpty()||surface.ball.active);
    }
    private static Rectangle dirty(List<Actor> actors,BallFrame ball){Rectangle area=ball.active?new Rectangle((int)ball.x-10,(int)ball.y-10,20,20):new Rectangle();for(Actor actor:actors){Rectangle bounds=new Rectangle(actor.bounds);if(actor.balloon)bounds.grow(80,35);area=area.isEmpty()?bounds:area.union(bounds);}return area;}
    private static Rectangle bodyBounds(double x,double foot,double scale){int h=Math.max(125,(int)(255*scale)+36);return new Rectangle((int)x-83,(int)foot-h+16,166,h);}
    static void paint(Graphics2D original,List<Actor> actors,BallFrame ball){
        Graphics2D g=(Graphics2D)original.create();try{
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            for(Actor actor:actors){Rectangle r=actor.bounds;Graphics2D local=(Graphics2D)g.create(r.x,r.y,r.width,r.height);try{PetPainter.paint(local,r.width,r.height,actor.pet,actor.frame,actor.speech,actor.scale);}finally{local.dispose();}
                if(actor.balloon)balloon(g,actor);
            }
            if(ball.active){g.setColor(new Color(196,151,237));g.fill(new Ellipse2D.Double(ball.x-7,ball.y-7,14,14));g.setColor(new Color(244,220,255));g.draw(new Arc2D.Double(ball.x-5,ball.y-6,8,11,50,160,Arc2D.OPEN));}
        }finally{g.dispose();}
    }
    private static void balloon(Graphics2D g,Actor a){double phase=a.frame.phase(),x=a.bounds.getCenterX()+15+Math.sin(phase*.4)*8,y=a.bounds.y+13;g.setStroke(new BasicStroke(1.2f));g.setColor(new Color(192,191,209));g.draw(new CubicCurve2D.Double(x,y+25,x-12,y+47,x+25,y+66,a.bounds.getCenterX()+15,a.bounds.getMaxY()-45));g.setColor(PetCatalog.color(a.pet.accessoryColor));g.fill(new Ellipse2D.Double(x-17,y-20,34,44));g.setColor(new Color(237,235,249,120));g.fill(new Ellipse2D.Double(x-11,y-13,8,15));g.setColor(new Color(157,191,215,110));for(int i=0;i<3;i++){double windY=y+40+i*15;g.draw(new CubicCurve2D.Double(x-70,windY,x-55,windY-8,x-40,windY+7,x-24,windY-3));}}
    private Rectangle projectFloor(ToolWindow tool){
        if(tool==null||!tool.isVisible()||layer==null)return null;
        JTree tree=findTree(tool.getComponent());if(tree==null)return null;
        Rectangle view=tree.getVisibleRect();Rectangle last=tree.getRowBounds(tree.getRowCount()-1);int end=last==null?view.y:last.y+last.height;
        if(view.y+view.height-Math.max(view.y,end)<125)return null;
        return SwingUtilities.convertRectangle(tree,new Rectangle(view.x+5,view.y+view.height-128,Math.max(1,view.width-10),125),layer);
    }
    private JTree findTree(Container root){for(Component c:root.getComponents()){if(!c.isShowing())continue;if(c instanceof JTree tree)return tree;if(c instanceof Container nested){JTree found=findTree(nested);if(found!=null)return found;}}return null;}
    private Point runButton(Container root){
        Point found=findRun(root);return found!=null?found:new Point(Math.max(100,layer.getWidth()-180),10);
    }
    private Point findRun(Container root){for(Component c:root.getComponents()){if(!c.isShowing()||c==surface)continue;
        String text=c instanceof JComponent j?j.getToolTipText():null;if(text==null&&c.getAccessibleContext()!=null)text=c.getAccessibleContext().getAccessibleName();
        if(text!=null&&(text.toLowerCase(Locale.ROOT).startsWith("run")||text.toLowerCase(Locale.ROOT).startsWith("запуск"))){Point at=SwingUtilities.convertPoint(c,c.getWidth()/2,c.getHeight(),layer);if(at.y<150)return at;}
        if(c instanceof Container nested){Point at=findRun(nested);if(at!=null)return at;}}
        return null;
    }
    void discardEvents(PetCollection collection,PetPlayInput input){lastBall=input.ballSerial;lastBalloon=input.balloonSerial;lastRun=collection.runSerial;}
    void suspend(){if(background!=null){background.close();background=null;}editor=null;ball.active=false;buddies.values().forEach(b->{b.balloonSince=0;b.runUntil=0;});surface.setVisible(false);}
    private void detachSurface(){if(surface.getParent()!=null){Container p=surface.getParent();p.remove(surface);p.repaint();}}
    @Override public void close(){suspend();detachSurface();buddies.clear();layer=null;}
    static final class Surface extends JComponent {
        List<Actor> actors=List.of();BallFrame ball=new BallFrame(0,0,false);private Rectangle previous=new Rectangle();
        Surface(){setOpaque(false);setFocusable(false);setEnabled(false);setDoubleBuffered(true);}
        void present(Rectangle actor){Rectangle dirty=previous.union(actor);previous=new Rectangle(actor);repaint(dirty.x,dirty.y,dirty.width,dirty.height);}
        @Override public boolean contains(int x,int y){return false;}
        @Override protected void paintComponent(Graphics graphics){PetPlayScene.paint((Graphics2D)graphics,actors,ball);}
    }
}
