package dev.codepet;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;

public final class ToggleRoamingAction extends ToggleAction implements DumbAware {
    @Override public boolean isSelected(AnActionEvent event) { return PetState.model().roaming; }
    @Override public void setSelected(AnActionEvent event, boolean selected) { PetState.model().roaming = selected; }
    @Override public ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.EDT; }
}
