package com.tle130475c.archinstall.osinstall.tool;

import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class Ansible implements Installable {
    private final String chrootDir;

    public Ansible(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("ansible", "ansible-lint", "ansible-language-server"), chrootDir);
        return 0;
    }
}
