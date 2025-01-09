package com.tle130475c.archinstall.osinstall.bootloader;

import java.io.IOException;

import com.tle130475c.archinstall.osinstall.BaseSystem;
import com.tle130475c.archinstall.osinstall.Installable;

public class GRUB implements Installable {
    private final BaseSystem baseSystem;

    public GRUB(BaseSystem baseSystem) {
        this.baseSystem = baseSystem;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        baseSystem.configureGRUBBootloader();
        return 0;
    }
}
