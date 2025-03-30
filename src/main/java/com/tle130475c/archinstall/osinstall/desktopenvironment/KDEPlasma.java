package com.tle130475c.archinstall.osinstall.desktopenvironment;

import static com.tle130475c.archinstall.util.ConfigUtil.createUserEnvironmentDir;
import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChrootAsUser;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class KDEPlasma implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public KDEPlasma(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("xorg-server", "plasma-desktop", "ark", "dolphin",
                "dolphin-plugins", "kate", "konsole", "kdegraphics-thumbnailers", "ffmpegthumbs", "spectacle",
                "gwenview", "bluedevil", "kinfocenter", "kscreen", "plasma-firewall", "plasma-nm",
                "plasma-pa", "plasma-systemmonitor", "powerdevil", "sddm-kcm", "okular", "kcalc", "yakuake", "cryfs",
                "plasma-vault", "discover", "breeze-gtk", "kde-gtk-config", "gnome-keyring", "krusader",
                "kwalletmanager", "krename", "khelpcenter", "gtk2", "xdg-desktop-portal-kde", "ktorrent",
                "gnome-disk-utility", "power-profiles-daemon", "plasma-workspace-wallpapers", "filelight"), chrootDir);
        installFcitx5Bamboo();

        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        createUserEnvironmentDir(userAccount.getUsername(), chrootDir);
        enableService("sddm", chrootDir);
        enableService("bluetooth", chrootDir);
        return 0;
    }

    public int installFcitx5Bamboo() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("fcitx5-bamboo", "fcitx5-qt", "fcitx5-gtk", "fcitx5-configtool"), chrootDir);

        List<String> createEnvironmentDirCmd = List.of("mkdir", "-p",
                "/home/%s/.config/environment.d".formatted(userAccount.getUsername()));
        runVerbose(chrootDir != null
                ? getCommandRunChrootAsUser(createEnvironmentDirCmd, userAccount.getUsername(), chrootDir)
                : createEnvironmentDirCmd);

        List<String> createFcitx5ConfigCmd = List.of("bash", "-c",
                "printf \"XMODIFIERS=@im=fcitx\n\""
                        + " > /home/%s/.config/environment.d/fcitx5.conf".formatted(userAccount.getUsername()));
        runVerbose(chrootDir != null
                ? getCommandRunChrootAsUser(createFcitx5ConfigCmd, userAccount.getUsername(), chrootDir)
                : createFcitx5ConfigCmd);

        return 0;
    }
}
