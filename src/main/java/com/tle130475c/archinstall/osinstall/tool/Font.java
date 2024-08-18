package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class Font implements Installable {
    private final String chrootDir;

    public Font(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("ttf-dejavu", "ttf-liberation", "noto-fonts-emoji", "ttf-cascadia-code",
                "ttf-fira-code", "ttf-roboto-mono", "ttf-hack", "noto-fonts-cjk"), chrootDir);

        return 0;
    }
}
