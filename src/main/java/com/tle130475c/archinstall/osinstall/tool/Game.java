package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgsWithOptionalDeps;
import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class Game implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public Game(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgsWithOptionalDeps(List.of("lutris", "wine", "winetricks"), chrootDir);
        installPkgs(List.of("discord", "steam", "steam-native-runtime",
                "minecraft-launcher", "retroarch", "libretro", "gnome-chess",
                "gnuchess", "kigo", "quadrapassel", "wesnoth"),
                userAccount, chrootDir);

        return 0;
    }
}
