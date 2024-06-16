package com.tle130475c.archinstall.osinstall.programming;

import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class CAndCPP implements Installable {
    private final String chrootDir;

    public CAndCPP(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("clang", "llvm", "lldb", "cmake", "qt6-base", "qt6-doc", "qtcreator", "kdevelop"),
                chrootDir);

        return 0;
    }
}
