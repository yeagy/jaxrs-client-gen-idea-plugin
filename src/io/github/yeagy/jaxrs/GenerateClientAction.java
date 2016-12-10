package io.github.yeagy.jaxrs;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.ws.rs.Path;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * id like to generate whole packages of clients in a single call,
 * but i need to find a better way than to blindly go through the module dependencies trying to string match paths
 * one possibility is to have a dropdown of dependency jars and let the user specify where to look...
 */
public class GenerateClientAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(GenerateClientAction.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        new GenerateClientDialog(anActionEvent.getProject()).show();
    }

    private void generateModuleClient(Project project, String moduleName, String className, boolean async) {
        Set<URL> urls = new HashSet<URL>();
        Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
        extractUrlsFromModule(project, module, urls);
        generateClient(module, className, urls, async);
    }

    private void extractUrlsFromModule(Project project, Module module, Set<URL> urls) {
        extractModuleOutputUrl(module, urls);
        for (VirtualFile root : LibraryUtil.getLibraryRoots(new Module[]{module}, false, false)) {
            extractJarUrlFromLibraryRoot(root, urls);
        }
        extractUrlsFromDependentModules(project, module, urls);
    }

    private void extractUrlsFromDependentModules(Project project, Module module, Set<URL> urls) {
        for (Module dependentModule : ModuleManager.getInstance(project).getModuleDependentModules(module)) {
            extractUrlsFromModule(project, dependentModule, urls);
        }
    }

    private void extractModuleOutputUrl(Module module, Set<URL> urls) {
        CompilerModuleExtension compiler = CompilerModuleExtension.getInstance(module);
        if (compiler != null && compiler.getCompilerOutputPath() != null) {
            try {
                urls.add(new File(compiler.getCompilerOutputPath().getPath()).toURI().toURL());
            } catch (MalformedURLException e) {
                LOG.error("could not extract module output path " + compiler.getCompilerOutputPath().getPath(), e);
            }
        }
    }

    private void extractJarUrlFromLibraryRoot(VirtualFile root, Set<URL> urls) {
        if (root.getPath().endsWith(".jar!/")) {
            File jarFile = new File(root.getPath().substring(0, root.getPath().length() - 2));
            try {
                urls.add(jarFile.toURI().toURL());
            } catch (MalformedURLException e) {
                LOG.error("could not extract library root path " + root.getPath(), e);
            }
        }
    }

    private void generateClient(Module module, String className, Set<URL> urls, boolean async) {
        try {
            URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
            Class<?> klass = classLoader.loadClass(className);
            if (klass.getAnnotation(Path.class) != null) {
                VirtualFile moduleRoot = ModuleRootManager.getInstance(module).getContentRoots()[0];
                new ClientGenerator(async).generate(klass, new File(moduleRoot.getPath(), "jaxrs-client-generated-source"));
            }
        } catch (Exception e) {
            LOG.error("could not generate client for class " + className, e);
        }
    }

    private class GenerateClientDialog extends DialogWrapper {
        private final Project project;
        private final JTextField targetInput = new JTextField();
        private final ComboBox moduleDropdown;
        private final Checkbox asyncBox = new Checkbox("Async clients?");

        GenerateClientDialog(Project project) {
            super(project);
            this.project = project;
            setTitle("Generate JAX-RS Client");
            moduleDropdown = createModuleDropdown(project);
            init();//call at the end!!!
        }

        private ComboBox createModuleDropdown(Project project) {
            Module[] modules = ModuleManager.getInstance(project).getModules();
            List<String> names = new ArrayList<String>();
            for (Module module : modules) {
                //todo find a better way to filter out unwanted modules
                if (!module.getName().endsWith("_test")) {
                    VirtualFile[] libraryRoots = LibraryUtil.getLibraryRoots(new Module[]{module}, false, false);
                    if (libraryRoots.length > 0) {
                        names.add(module.getName());
                    }
                }
            }
            return new ComboBox(names.toArray(), -1);
        }

        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel(new VerticalFlowLayout());
            panel.add(new JLabel("Generate JAX-RS Client v0.2.1"));
            panel.add(new JLabel("Select Module"));
            panel.add(moduleDropdown);
            panel.add(new JLabel("Full Class Name"));
            panel.add(targetInput);
            panel.add(asyncBox);
            return panel;
        }

        @Override
        protected void doOKAction() {
            String moduleName = (String) moduleDropdown.getSelectedItem();
            generateModuleClient(project, moduleName, targetInput.getText(), asyncBox.getState());
            super.doOKAction();
        }
    }
}
