package com.tle130475c.archinstall.osinstall.driver;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class Thermald implements Installable {
    private final String chrootDir;

    public Thermald(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("thermald"), chrootDir);
        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        enableService("thermald", chrootDir);
        return 0;
    }
}
