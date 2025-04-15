package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.ConfigUtil.startService;
import static com.tle130475c.archinstall.util.ConfigUtil.uncommentLine;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.readUsername;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SetupFTPServer implements Runnable {
    private static final String VSFTPD = "vsftpd";
    private static final String PATH_TO_VSFTPD_CONF = "/etc/vsftpd.conf";

    @Override
    public void run() {
        String username = readUsername("Enter username: ");

        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                installMainReposPkgs(List.of(VSFTPD), null);
                uncommentLine(PATH_TO_VSFTPD_CONF, "#write_enable=YES");
                uncommentLine(PATH_TO_VSFTPD_CONF, "#local_enable=YES");
                uncommentLine(PATH_TO_VSFTPD_CONF, "#chroot_local_user=YES");
                Files.writeString(Paths.get(PATH_TO_VSFTPD_CONF),
                        "%nlocal_root=/home/%s/ftp_root%n".formatted(username), StandardOpenOption.APPEND);
                runVerbose(List.of("mkdir", "-p", "/home/%s/ftp_root/upload".formatted(username)));
                runVerbose(List.of("chmod", "550", "/home/%s/ftp_root".formatted(username)));
                runVerbose(List.of("chmod", "750", "/home/%s/ftp_root/upload".formatted(username)));
                runVerbose(List.of("chown", "-R", "%s:%s".formatted(username, username),
                        "/home/%s/ftp_root".formatted(username)));
                enableService(VSFTPD, null);
                startService(VSFTPD, null);
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
