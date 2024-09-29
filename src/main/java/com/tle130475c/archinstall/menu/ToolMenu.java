package com.tle130475c.archinstall.menu;

import com.tle130475c.archinstall.osinstall.tool.Browser;
import com.tle130475c.archinstall.osinstall.tool.CoreTool;
import com.tle130475c.archinstall.osinstall.tool.DiskImageTool;
import com.tle130475c.archinstall.osinstall.tool.Font;
import com.tle130475c.archinstall.osinstall.tool.Game;
import com.tle130475c.archinstall.osinstall.tool.Mullvad;
import com.tle130475c.archinstall.osinstall.tool.Multimedia;
import com.tle130475c.archinstall.osinstall.tool.NetworkTool;
import com.tle130475c.archinstall.osinstall.tool.Office;
import com.tle130475c.archinstall.osinstall.tool.RemoteDesktop;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class ToolMenu extends MultiChoiceMenu {
    public ToolMenu(String chrootDir, UserAccount userAccount) {
        super();
        addOption(new Option("Browser", new Browser(chrootDir, userAccount), false));
        addOption(new Option("Core Tools", new CoreTool(chrootDir, userAccount), false));
        addOption(new Option("Disk Image Tools", new DiskImageTool(chrootDir, userAccount), false));
        addOption(new Option("Font", new Font(chrootDir), false));
        addOption(new Option("Multimedia", new Multimedia(chrootDir), false));
        addOption(new Option("Network Tool", new NetworkTool(chrootDir, userAccount), false));
        addOption(new Option("Office", new Office(chrootDir, userAccount), false));
        addOption(new Option("Remote Desktop", new RemoteDesktop(chrootDir), false));
        addOption(new Option("Game", new Game(chrootDir, userAccount), false));
        addOption(new Option("Mullvad", new Mullvad(chrootDir, userAccount), false));
    }
}
