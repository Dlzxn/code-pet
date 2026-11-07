package dev.codepet;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;

/** Tiny click-through scenes only in verified empty editor space, with a 48px code clearance. */
@Service(Service.Level.PROJECT)
public final class PetRoaming implements Disposable {
    private final Project project;
    private final Timer timer;
    private final Overlay overlay=new Overlay();
    private PetBehavior behavior=new PetBehavior();
    private final PetDialogue dialogue=new PetDialogue();
    private JLayeredPane layer;
    private Editor cachedEditor;
    private Rectangle cachedViewport;
    private Point cachedCaret;
    private Rectangle safeLane;
    private long cachedStamp=-1,cacheTime,lastFrame;
    private String shownId="";
    private int previousXp,previousFiles;
    private volatile boolean disposed;

    public PetRoaming(Project project){this.project=project;timer=new Timer(40,e->update());overlay.valid=this::stillSafe;}
    public void start(){if(!disposed&&!timer.isRunning()){lastFrame=System.nanoTime();timer.start();}}
    public void invalidate(){cachedStamp=-1;overlay.setVisible(false);}
    private void update(){
        if(disposed||project.isDisposed()){timer.stop();detach();return;}
        PetModel pet=PetState.model();Editor editor=FileEditorManager.getInstance(project).getSelectedTextEditor();
        if(!pet.roaming||editor==null||editor.isDisposed()||editor.getEditorKind()!=EditorKind.MAIN_EDITOR||!editor.getContentComponent().isShowing()){idle();return;}
        JComponent content=editor.getContentComponent();Window window=SwingUtilities.getWindowAncestor(content);JRootPane root=SwingUtilities.getRootPane(content);
        if(root==null||window==null||!window.isActive()){idle();return;}
        JLayeredPane target=root.getLayeredPane();if(layer!=target){detach();layer=target;layer.add(overlay,JLayeredPane.PALETTE_LAYER);}
        if(!shownId.equals(pet.id)){shownId=pet.id;behavior=new PetBehavior();previousXp=pet.xp;previousFiles=pet.filesCreated;cachedStamp=-1;}
        long now=System.nanoTime();double delta=Math.min(.1,Math.max(0,(now-lastFrame)/1e9));lastFrame=now;
        Rectangle viewport=editor.getScrollingModel().getVisibleArea();
        Point caret=editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
        int width=pet.speech?156:116,height=120+(pet.roamingSize-30)*2;
        Dimension sprite=new Dimension(width,height);
        if(cachedEditor!=editor||cachedStamp!=editor.getDocument().getModificationStamp()||!viewport.equals(cachedViewport)||!caret.equals(cachedCaret)||now-cacheTime>200_000_000L||overlay.getHeight()!=height||overlay.getWidth()!=width){
            cachedEditor=editor;cachedViewport=new Rectangle(viewport);cachedCaret=new Point(caret);cachedStamp=editor.getDocument().getModificationStamp();cacheTime=now;
            safeLane=findLane(editor,viewport,sprite,caret);
        }
        if(safeLane==null){overlay.setVisible(false);timer.setDelay(120);return;}
        if(previousFiles!=pet.filesCreated){previousFiles=pet.filesCreated;behavior.eat();dialogue.say("Новый файл — перекус!");}
        if(previousXp!=pet.xp){int gain=pet.xp-previousXp;previousXp=pet.xp;if(gain>0)dialogue.say("+"+gain+" XP за код");}
        boolean hover=hovered();behavior.hover(hover);behavior.temperament(pet.temperament);
        behavior.update(delta,safeLane.width-width,pet.animated);dialogue.update(delta,pet);
        int x=safeLane.x+(int)((safeLane.width-width)*behavior.position());
        Point location=SwingUtilities.convertPoint(content,x,safeLane.y,layer);
        overlay.pet=pet;overlay.frame=behavior.frame();overlay.speech=dialogue.text(pet);
        overlay.setBounds(location.x,location.y,width,height);overlay.setVisible(behavior.action()!=PetBehavior.Action.HIDDEN);overlay.repaint();
        timer.setDelay(pet.animated?40:160);
    }
    private Rectangle findLane(Editor editor,Rectangle viewport,Dimension sprite,Point caret){
        ArrayList<Rectangle> blocked=new ArrayList<>();Document doc=editor.getDocument();
        int first=Math.max(0,editor.xyToLogicalPosition(new Point(viewport.x,viewport.y)).line-1);
        int last=Math.min(doc.getLineCount()-1,editor.xyToLogicalPosition(new Point(viewport.x+viewport.width,viewport.y+viewport.height)).line+1);
        if(last-first>1200)return null;
        CharSequence text=doc.getImmutableCharSequence();
        for(int line=first;line<=last;line++){
            int from=doc.getLineStartOffset(line),to=doc.getLineEndOffset(line);
            while(from<to&&Character.isWhitespace(text.charAt(from)))from++;
            while(to>from&&Character.isWhitespace(text.charAt(to-1)))to--;
            if(from==to)continue;
            Point start=editor.offsetToXY(from),end=editor.offsetToXY(to);
            if(start.y!=end.y)blocked.add(new Rectangle(viewport.x-48,Math.min(start.y,end.y)-48,viewport.width+96,Math.abs(end.y-start.y)+editor.getLineHeight()+96));
            else blocked.add(new Rectangle(Math.min(start.x,end.x)-48,start.y-48,Math.abs(end.x-start.x)+96,editor.getLineHeight()+96));
        }
        blocked.add(new Rectangle(caret.x-80,caret.y-48,160,editor.getLineHeight()+96));
        return PetSafeArea.lane(viewport,sprite,blocked);
    }
    private boolean stillSafe(){
        return !disposed&&cachedEditor!=null&&!cachedEditor.isDisposed()&&cachedEditor.getContentComponent().isShowing()&&
                cachedEditor.getDocument().getModificationStamp()==cachedStamp&&cachedEditor.getScrollingModel().getVisibleArea().equals(cachedViewport)&&
                cachedEditor.visualPositionToXY(cachedEditor.getCaretModel().getVisualPosition()).equals(cachedCaret);
    }
    private boolean hovered(){
        if(!overlay.isShowing())return false;
        try{PointerInfo pointer=MouseInfo.getPointerInfo();if(pointer==null)return false;Point p=pointer.getLocation();SwingUtilities.convertPointFromScreen(p,overlay);
            return new Rectangle(overlay.getWidth()/2-36,overlay.getHeight()-86,72,80).contains(p);
        }catch(HeadlessException|SecurityException ignored){return false;}
    }
    private void idle(){detach();timer.setDelay(300);lastFrame=System.nanoTime();}
    private void detach(){
        if(layer!=null){Rectangle old=overlay.getBounds();layer.remove(overlay);layer.repaint(old);layer=null;}
        cachedEditor=null;cachedStamp=-1;safeLane=null;
    }
    @Override public void dispose(){disposed=true;Runnable cleanup=()->{timer.stop();detach();};if(SwingUtilities.isEventDispatchThread())cleanup.run();else SwingUtilities.invokeLater(cleanup);}
    static final class Overlay extends JComponent {
        PetModel pet;PetBehavior.Frame frame;String speech="";BooleanSupplier valid=()->true;
        Overlay(){setOpaque(false);setFocusable(false);setEnabled(false);}
        @Override public boolean contains(int x,int y){return false;}
        @Override protected void paintComponent(Graphics graphics){if(pet!=null&&frame!=null&&valid.getAsBoolean())PetPainter.paint((Graphics2D)graphics,getWidth(),getHeight(),pet,frame,speech,pet.roamingSize/80.0);}
    }
}
