package com.tle130475c.archinstall.menu;

import com.tle130475c.archinstall.osinstall.BaseSystem;
import com.tle130475c.archinstall.osinstall.bootloader.GRUB;
import com.tle130475c.archinstall.osinstall.bootloader.SystemdBoot;

public class BootloaderMenu extends SingleChoiceMenu {
    public BootloaderMenu(BaseSystem baseSystem) {
        super();
        addOption(new Option("systemd-boot", new SystemdBoot(baseSystem), false));
        addOption(new Option("GRUB", new GRUB(baseSystem), false));
    }
}
