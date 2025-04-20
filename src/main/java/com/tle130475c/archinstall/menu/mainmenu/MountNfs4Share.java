package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunSudo;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

public class MountNfs4Share implements Runnable {
    @Override
    public void run() {
        System.console().printf("Enter hostname or IP address of NFS server: ");
        String server = System.console().readLine();

        System.console().printf("Enter NFS share path: ");
        String sharePath = System.console().readLine();

        System.console().printf("Enter mount point: ");
        String mountPoint = System.console().readLine();

        List<String> command = List.of("mount",
                "-t", "nfs",
                "-o", "vers=4",
                "%s:%s".formatted(server, sharePath),
                mountPoint);
        try {
            runVerbose(getCommandRunSudo(command));
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
