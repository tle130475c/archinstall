package com.tle130475c.archinstall.menu;

import com.tle130475c.archinstall.osinstall.virtualmachine.KVM;
import com.tle130475c.archinstall.osinstall.virtualmachine.VMware;
import com.tle130475c.archinstall.osinstall.virtualmachine.VirtualBox;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class VirtualMachineMenu extends MultiChoiceMenu {
    public VirtualMachineMenu(String chrootDir, UserAccount userAccount) {
        super();
        addOption(new Option("KVM", new KVM(chrootDir, userAccount), false));
        addOption(new Option("VirtualBox", new VirtualBox(chrootDir, userAccount), false));
        addOption(new Option("VMware", new VMware(chrootDir, userAccount), false));
    }
}
