package com.tle130475c.archinstall.menu;

import com.tle130475c.archinstall.osinstall.desktopenvironment.GNOME;
import com.tle130475c.archinstall.osinstall.desktopenvironment.KDEPlasma;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class DesktopEnvironmentMenu extends MultiChoiceMenu {
    public DesktopEnvironmentMenu(String chrootDir, UserAccount userAccount) {
        super();
        addOption(new Option("GNOME", new GNOME(chrootDir, userAccount), false));
        addOption(new Option("KDE Plasma", new KDEPlasma(chrootDir, userAccount), false));
    }
}
