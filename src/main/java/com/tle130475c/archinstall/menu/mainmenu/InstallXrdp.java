package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.ConfigUtil.startService;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.readUsername;
import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.runPipelineSilent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.tle130475c.archinstall.systeminfo.UserAccount;

public class InstallXrdp implements Runnable {
    @Override
    public void run() {
        String username = readUsername("Enter username: ");
        UserAccount userAccount = new UserAccount(null, username, null);

        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                installPkgs(List.of("xrdp", "xorgxrdp"), userAccount, null);

                runPipelineSilent(List.of(
                    List.of("printf", "allowed_users=anybody\nneeds_root_rights=no\n"),
                    List.of("sudo", "tee", "/etc/X11/Xwrapper.config")
                ));

                try (var writer = new PrintWriter(("/home/%s/.xinitrc".formatted(username)))) {
                    writer.println("exec dbus-run-session -- startplasma-x11");
                }

                enableService("xrdp", null);
                startService("xrdp", null);
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
