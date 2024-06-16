package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class Multimedia implements Installable {
    private final String chrootDir;

    public Multimedia(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("vlc", "gst-libav", "gst-plugins-good", "gst-plugins-ugly", "gst-plugins-bad",
                "obs-studio", "inkscape", "gimp", "kdenlive", "frei0r-plugins"), chrootDir);

        return 0;
    }
}
