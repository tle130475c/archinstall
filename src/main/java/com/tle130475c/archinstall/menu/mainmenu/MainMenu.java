package com.tle130475c.archinstall.menu.mainmenu;

import com.tle130475c.archinstall.menu.Option;
import com.tle130475c.archinstall.menu.SingleChoiceMenu;

public class MainMenu extends SingleChoiceMenu {
    public MainMenu() {
        super();
        addOption(new Option("Install System Full (live system)", new InstallSystem(), false));
        addOption(new Option("Install System Selective (live system)", new InstallSystemSelective(), false));
        addOption(new Option("Configure System After Install (installed system)", new ConfigureSystem(), false));
        addOption(new Option("Configure As A VirtualBox Guest (sudo)", new ConfigureAsVBGuest(null, null), false));
        addOption(new Option("Enable DNSCryptProxy (installed system, sudo)", new EnableDNSCryptProxy(), false));
        addOption(new Option("Disable DNSCryptProxy (installed system, sudo)", new DisableDNSCryptProxy(), false));
        addOption(new Option("Encrypt Disk (sudo)", new EncryptDisk(), false));
        addOption(new Option("Create Linux LUKS Partition (sudo)", new CreateLinuxLUKSPartition(), false));
        addOption(new Option("Encrypt Partition (sudo)", new EncryptedPartition(), false));
        addOption(new Option("Create Encrypted Partition Using LUKS (sudo)",
                new CreateEncryptedPartitionUsingLUKS(), false));
        addOption(new Option("Erase Disk (sudo)", new EraseDisk(), false));
        addOption(new Option("Install Yay AUR Helper (installed system)", new InstallYayAURHelper(), false));
        addOption(new Option("Install fcitx5-bamboo (installed system)", new InstallFcitx5Bamboo(), false));
        addOption(new Option("Install flatpak packages (installed system)", new InstallFlatpakPackages(), false));
        addOption(new Option("Install Lutris (installed system)", new InstallLutris(), false));
        addOption(new Option("Install VirtualBox (installed system)", new InstallVirtualBox(), false));
        addOption(new Option("Make Retail Windows ISO (installed system)", new MakeRetailWindowsISO(), false));
        addOption(new Option("Install Postfix (installed system, sudo)", new InstallPostfix(), false));
        addOption(new Option("Write Hybrid ISO to USB (sudo)", new WriteHybridISOToUSB(), false));
        addOption(new Option("Configure LUKS Auto Mount (installed system, sudo)",
                new ConfigureLUKSAutoMount(), false));
        addOption(new Option("Configure GNOME (GNOME, installed system)", new ConfigureGNOME(), false));
        addOption(new Option("Install Xrdp (installed system)", new InstallXrdp(), false));
        addOption(new Option("Setup FTP Server (installed system, sudo)", new SetupFTPServer(), false));
        addOption(new Option("Install Kerberos NFS Server (installed system, sudo)", new InstallKerberosNfsServer(), false));
        addOption(new Option("Install Kerberos NFS Client (installed system, sudo)", new InstallKerberosNfsClient(), false));
    }
}
