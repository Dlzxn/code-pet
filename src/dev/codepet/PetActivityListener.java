package dev.codepet;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.*;
import javax.swing.SwingUtilities;

public final class PetActivityListener implements DocumentListener {
    private Project eligible(DocumentEvent event){
        if(!ApplicationManager.getApplication().isDispatchThread()||event.getDocument().isInBulkUpdate()||
                event.getDocument().getTextLength()>500_000)return null;
        CommandProcessor commands=CommandProcessor.getInstance();
        if(commands.getCurrentCommand()==null||commands.isUndoTransparentActionInProgress())return null;
        Project project=commands.getCurrentCommandProject();
        if(project==null||project.isDisposed()||UndoManager.getInstance(project).isUndoOrRedoInProgress())return null;
        VirtualFile file=FileDocumentManager.getInstance().getFile(event.getDocument());
        if(!PetEligibility.projectFile(project,file)||!PetEligibility.source(file))return null;
        Component focus=KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if(focus==null)return null;
        for(Editor editor:EditorFactory.getInstance().getEditors(event.getDocument(),project))
            if(!editor.isDisposed()&&!editor.isViewer()&&editor.getEditorKind()==EditorKind.MAIN_EDITOR&&
                    SwingUtilities.isDescendingFrom(focus,editor.getContentComponent()))return project;
        return null;
    }
    @Override public void beforeDocumentChange(DocumentEvent event){
        Project p=eligible(event);
        if(p!=null){
            PetRoaming roaming=p.getServiceIfCreated(PetRoaming.class);if(roaming!=null)roaming.typing();
            p.getService(PetCodingTracker.class).before(event.getDocument(),FileDocumentManager.getInstance().getFile(event.getDocument()));
        }
    }
    @Override public void documentChanged(DocumentEvent event){
        Project p=eligible(event);
        if(p!=null){p.getService(PetCodingTracker.class).changed(event.getDocument());if(event.getNewLength()>1200)PetReactions.signal(PetState.model(),"large-paste",System.nanoTime());}
        else if(ApplicationManager.getApplication().isDispatchThread())for(Project open:com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()){
            PetCodingTracker tracker=open.getServiceIfCreated(PetCodingTracker.class);if(tracker!=null)tracker.discard(event.getDocument());
        }
    }
}
