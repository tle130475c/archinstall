package com.tle130475c.archinstall.osinstall.driver;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;

public class IntelDriver implements Installable {
    private final String chrootDir;

    public IntelDriver(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("mesa", "lib32-mesa", "ocl-icd", "lib32-ocl-icd", "intel-compute-runtime",
                "vulkan-intel", "lib32-vulkan-intel", "vulkan-icd-loader", "lib32-vulkan-icd-loader",
                "intel-media-driver", "libva-utils", "intel-media-sdk", "thermald", "sof-firmware"), chrootDir);

        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        enableService("thermald", chrootDir);
        return 0;
    }
}
