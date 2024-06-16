package com.tle130475c.archinstall.osinstall.driver;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.ConfigUtil.startService;
import static com.tle130475c.archinstall.util.PackageUtil.installAutoAnswerYes;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class TLP implements Installable {
    private String chrootDir;

    public TLP(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installAutoAnswerYes(List.of("tlp"), chrootDir);
        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        enableService("tlp", chrootDir);

        if (chrootDir == null) {
            startService("tlp", chrootDir);
        }

        return 0;
    }
}
