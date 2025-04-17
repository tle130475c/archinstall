package com.tle130475c.archinstall.menu.mainmenu;

import java.io.IOException;

import com.tle130475c.archinstall.osinstall.networking.KerberosNfsServerInstall;

public class InstallKerberosNfsServer implements Runnable {
    @Override
    public void run() {
        try {
            new KerberosNfsServerInstall().install();
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
