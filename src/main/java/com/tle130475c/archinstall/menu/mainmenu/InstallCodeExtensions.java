package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;

import com.tle130475c.archinstall.osinstall.programming.CodeEditor;

public class InstallCodeExtensions implements Runnable {
    @Override
    public void run() {
        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            CodeEditor codeEditor = new CodeEditor(null, null);
            codeEditor.installCodeStableExtensions();
        }
    }
}
