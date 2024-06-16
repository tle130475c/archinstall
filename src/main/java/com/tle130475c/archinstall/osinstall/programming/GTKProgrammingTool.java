package com.tle130475c.archinstall.osinstall.programming;

import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class GTKProgrammingTool implements Installable {
    private final String chrootDir;

    public GTKProgrammingTool(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("devhelp", "glade", "gnome-builder", "gnome-code-assistance", "gnome-devel-docs"),
                chrootDir);

        return 0;
    }
}
