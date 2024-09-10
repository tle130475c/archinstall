package com.tle130475c.archinstall.menu;

import com.tle130475c.archinstall.osinstall.driver.IntelDriver;
import com.tle130475c.archinstall.osinstall.driver.PipeWire;
import com.tle130475c.archinstall.osinstall.driver.TLP;
import com.tle130475c.archinstall.osinstall.driver.Thermald;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class DriverMenu extends MultiChoiceMenu {
    public DriverMenu(String chrootDir, UserAccount userAccount) {
        super();
        addOption(new Option("Intel Driver", new IntelDriver(chrootDir), false));
        addOption(new Option("PipeWire", new PipeWire(chrootDir, userAccount), false));
        addOption(new Option("TLP", new TLP(chrootDir), false));
        addOption(new Option("Thermald", new Thermald(chrootDir), false));
    }
}
