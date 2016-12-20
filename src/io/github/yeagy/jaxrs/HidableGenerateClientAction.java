package io.github.yeagy.jaxrs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;

public class HidableGenerateClientAction extends GenerateClientAction {
    @Override
    public void update(AnActionEvent event) {
        VirtualFile file = event.getData(DataKeys.VIRTUAL_FILE);
        if (file != null && (file.getName().endsWith(".java") || file.getName().endsWith(".class"))) {
            event.getPresentation().setEnabledAndVisible(true);
        } else {
            event.getPresentation().setEnabledAndVisible(false);
        }
    }
}
