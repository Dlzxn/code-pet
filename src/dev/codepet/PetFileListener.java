package dev.codepet;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import java.util.List;

/** A newly created project file triggers a snack, never a care chore or XP reward. */
public final class PetFileListener implements BulkFileListener {
    @Override public void after(List<? extends VFileEvent> events){
        if(!ApplicationManager.getApplication().isDispatchThread())return;
        Project project=CommandProcessor.getInstance().getCurrentCommandProject();
        if(project==null||project.isDisposed())return;
        for(VFileEvent event:events){
            if(!(event instanceof VFileCreateEvent create)||create.isDirectory()||event.isFromRefresh())continue;
            VirtualFile file=create.getFile();
            if(!PetEligibility.projectFile(project,file))continue;
            PetCollection.FileProgress f=PetState.collection().file(PetCodingTracker.key(file.getUrl()),0,0);
            f.creationSeen=true;for(PetModel pet:PetState.collection().activePets())pet.filesCreated=Math.min(Integer.MAX_VALUE-1,pet.filesCreated)+1;
        }
    }
}
