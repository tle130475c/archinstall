package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;
import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class CoreTool implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public CoreTool(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installPkgs(List.of("keepassxc", "expect", "pacman-contrib", "dosfstools", "p7zip", "unarchiver",
                "bash-completion", "flatpak", "tree", "archiso", "rclone", "rsync", "lm_sensors",
                "ntfs-3g", "gparted", "exfatprogs", "pdftk", "youtube-dl", "ufw", "ufw-extras", "filezilla",
                "texlive", "texlive-lang", "krusader", "gptfdisk", "ventoy-bin", "kio5-extras",
                "gnome-characters", "reflector", "dislocker"), userAccount, chrootDir);

        installMainReposPkgs(List.of("ktexteditor5"), chrootDir); // dependency of krusader, can be removed in the future

        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        enableService("ufw", chrootDir);
        return 0;
    }
}
