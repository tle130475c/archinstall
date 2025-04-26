package com.tle130475c.archinstall.osinstall.programming;

import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class Python implements Installable {
    private final String chrootDir;

    public Python(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("python", "jupyterlab", "python-nltk", "python-pandas", "python-pip",
                "python-numpy", "python-scikit-learn", "tk", "python-matplotlib", "python-docs", "autopep8",
                "python-requests", "python-beautifulsoup4", "python-pygame", "python-networkx", "uv"), chrootDir);

        return 0;
    }
}
