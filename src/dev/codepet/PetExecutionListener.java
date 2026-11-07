package dev.codepet;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import java.util.Locale;

/** Encourages every launch, and reacts to recognizable test results without granting XP. */
public final class PetExecutionListener implements ExecutionListener {
    static boolean isTest(String className,String typeId){String key=(className+" "+typeId).toLowerCase(Locale.ROOT);return key.contains("test")||key.contains("junit")||key.contains("pytest");}
    @Override public void processStarting(String executorId,ExecutionEnvironment env){
        var app=ApplicationManager.getApplication();app.invokeLater(()->{if(!app.isDisposed()&&!env.getProject().isDisposed())PetState.collection().runSerial++;});
    }
    @Override public void processTerminated(String executorId,ExecutionEnvironment env,ProcessHandler handler,int exitCode){
        var profile=env.getRunProfile();String type=profile instanceof RunConfiguration run?run.getType().getId():"";
        if(!isTest(profile.getClass().getSimpleName(),type))return;
        var app=ApplicationManager.getApplication();
        app.invokeLater(()->{if(!app.isDisposed()&&!env.getProject().isDisposed())for(PetModel pet:PetState.collection().activePets())PetReactions.signal(pet,exitCode==0?"tests-passed":"tests-failed",System.nanoTime());});
    }
}
