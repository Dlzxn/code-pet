package dev.codepet;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.util.Disposer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** One event observer and clock per project, shared by every enabled companion. */
@Service(Service.Level.PROJECT)
public final class PetRoaming implements Disposable {
    private final Project project;
    private final Timer timer;
    private final PetPlayInput input=new PetPlayInput(System.nanoTime());
    private final PetPlayScene scene=new PetPlayScene();
    private final AWTEventListener observer=this::observe;
    interface KeySource { Disposable listen(Consumer<KeyEvent> observer); }
    private Disposable keyHook;
    private Supplier<Editor> selectedEditor;
    private volatile boolean disposed;private boolean listening,queued;private long previous;
    public PetRoaming(Project project){this.project=project;timer=new Timer(40,e->update());selectedEditor=()->{var manager=FileEditorManager.getInstance(project);return manager==null?null:manager.getSelectedTextEditor();};}
    private static Disposable listenKeys(Consumer<KeyEvent> listener){
        Disposable lifetime=Disposer.newDisposable("Code Pet editor keys");
        IdeEventQueue.getInstance().addPreprocessor(event->{if(event instanceof KeyEvent key)listener.accept(key);return false;},lifetime);
        return ()->Disposer.dispose(lifetime);
    }
    public void start(){start(PetRoaming::listenKeys,selectedEditor);}
    void start(KeySource keys,Supplier<Editor> editorSource){if(disposed||timer.isRunning())return;selectedEditor=editorSource;keyHook=keys.listen(key->observe(key));Toolkit.getDefaultToolkit().addAWTEventListener(observer,AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK|AWTEvent.MOUSE_WHEEL_EVENT_MASK);listening=true;previous=System.nanoTime();timer.start();}
    public void typing(){input.lastTyping=System.nanoTime();invalidate();}
    public void invalidate(){if(!disposed&&!queued){queued=true;SwingUtilities.invokeLater(()->{queued=false;if(!disposed&&timer.isRunning())update();});}}
    private Editor editor(){return selectedEditor.get();}
    private void observe(AWTEvent event){
        if(disposed||project.isDisposed())return;long now=System.nanoTime();
        if(event instanceof KeyEvent key){
            if(key.getID()==KeyEvent.KEY_RELEASED){input.released(key.getKeyCode(),now);return;}
            if(key.getID()!=KeyEvent.KEY_PRESSED)return;
            Editor editor=editor();if(!accepts(editor,key,KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()))return;
            if(key.isControlDown()||key.isMetaDown()||key.isAltDown()||key.isShiftDown()){typing();return;}
            input.pressed(key.getKeyCode(),now,PetState.collection().keyboardGames);invalidate();
            // Deliberately never consume the event: Enter, Tab and Delete keep their IDE meaning.
        }else if(event instanceof MouseEvent mouse){int id=mouse.getID();if(id==MouseEvent.MOUSE_MOVED||id==MouseEvent.MOUSE_DRAGGED||id==MouseEvent.MOUSE_PRESSED||id==MouseEvent.MOUSE_WHEEL){input.lastMouse=now;input.mouseScreen=mouse.getLocationOnScreen();}}
    }
    static boolean accepts(Editor editor,KeyEvent event,Component focus){
        if(editor==null||editor.isDisposed()||editor.isViewer()||editor.getEditorKind()!=EditorKind.MAIN_EDITOR)return false;
        JComponent content=editor.getContentComponent();
        return focus!=null?SwingUtilities.isDescendingFrom(focus,content):SwingUtilities.isDescendingFrom(event.getComponent(),content);
    }
    private void update(){
        if(disposed||project.isDisposed()){dispose();return;}long now=System.nanoTime();double dt=Math.min(.06,Math.max(0,(now-previous)/1e9));previous=now;
        PetCollection collection=PetState.collection();Editor editor=editor();if(editor!=null&&(editor.isDisposed()||!editor.getContentComponent().isShowing()))editor=null;
        ToolWindow tool=ToolWindowManager.getInstance(project).getToolWindow("Project");JComponent context=editor!=null?editor.getContentComponent():tool!=null&&tool.isVisible()?tool.getComponent():null;
        Window window=context==null?null:SwingUtilities.getWindowAncestor(context);JRootPane root=context==null?null:SwingUtilities.getRootPane(context);
        if(root==null||window==null||!window.isActive()||collection.quietUntilMillis>System.currentTimeMillis()){scene.suspend();scene.discardEvents(collection,input);input.reset(now);timer.setDelay(250);return;}
        scene.update(editor,tool,root.getLayeredPane(),collection,input,now,dt);timer.setDelay(40);
    }
    @Override public void dispose(){if(disposed)return;disposed=true;Runnable cleanup=()->{timer.stop();if(keyHook!=null){keyHook.dispose();keyHook=null;}if(listening){Toolkit.getDefaultToolkit().removeAWTEventListener(observer);listening=false;}scene.close();};if(SwingUtilities.isEventDispatchThread())cleanup.run();else SwingUtilities.invokeLater(cleanup);}
}
