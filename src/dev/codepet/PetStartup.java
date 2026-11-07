package dev.codepet;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

/** Non-suspending ProjectActivity bridge for this Java-only plugin. */
public final class PetStartup implements ProjectActivity {
    @Override public Object execute(Project project, Continuation<? super Unit> continuation) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!project.isDisposed()) project.getService(PetRoaming.class).start();
        });
        return Unit.INSTANCE;
    }
}
