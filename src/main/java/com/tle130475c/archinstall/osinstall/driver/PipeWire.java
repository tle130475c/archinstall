package com.tle130475c.archinstall.osinstall.driver;

import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChrootAsUser;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class PipeWire implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public PipeWire(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("pipewire", "pipewire-pulse", "pipewire-alsa", "alsa-utils",
                "gst-plugin-pipewire", "lib32-pipewire", "wireplumber"), chrootDir);

        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        if (userAccount != null) {
            List<String> mkdirCmd = List.of("mkdir", "-p",
                    "/home/%s/.config/pipewire".formatted(userAccount.getUsername()));
            runVerbose(chrootDir != null
                    ? getCommandRunChrootAsUser(mkdirCmd, userAccount.getUsername(), chrootDir)
                    : mkdirCmd);

            List<String> cpCfgCmd = List.of("cp", "-r", "/usr/share/pipewire",
                    "/home/%s/.config/".formatted(userAccount.getUsername()));
            runVerbose(chrootDir != null
                    ? getCommandRunChrootAsUser(cpCfgCmd, userAccount.getUsername(), chrootDir)
                    : cpCfgCmd);

            List<String> cfgHighQuality = List.of("bash", "-c",
                    "sed -i '/resample.quality/s/#//; /resample.quality/s/4/10/'" + " "
                            + "/home/%s/.config/pipewire/{client.conf,pipewire-pulse.conf}"
                                    .formatted(userAccount.getUsername()));
            runVerbose(chrootDir != null
                    ? getCommandRunChrootAsUser(cfgHighQuality, userAccount.getUsername(), chrootDir)
                    : cfgHighQuality);
        }

        return 0;
    }
}
