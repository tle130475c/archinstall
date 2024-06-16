package com.tle130475c.archinstall.osinstall.virtualmachine;

import static com.tle130475c.archinstall.util.ConfigUtil.addUserToGroup;
import static com.tle130475c.archinstall.util.ConfigUtil.backupFile;
import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class KVM implements Installable {
    private final String chrootDir;
    private final UserAccount userAccount;

    public KVM(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("virt-manager", "qemu", "vde2", "dnsmasq", "bridge-utils", "virt-viewer",
                "dmidecode", "edk2-ovmf", "iptables-nft", "swtpm", "qemu-hw-usb-host"), chrootDir);

        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        enableService("libvirtd", chrootDir);

        String libvirtdConfigPath = chrootDir + "/etc/libvirt/libvirtd.conf";
        backupFile(libvirtdConfigPath);

        List<String> lines = Files.readAllLines(Paths.get(libvirtdConfigPath));
        int lineNumber = lines.indexOf("#unix_sock_group = \"libvirt\"");
        lines.set(lineNumber, lines.get(lineNumber).replace("#", ""));

        lineNumber = lines.indexOf("#unix_sock_rw_perms = \"0770\"");
        lines.set(lineNumber, lines.get(lineNumber).replace("#", ""));

        try (var writer = new PrintWriter(libvirtdConfigPath)) {
            for (String line : lines) {
                writer.println(line);
            }
        }

        if (userAccount != null) {
            addUserToGroup(userAccount.getUsername(), "libvirt", chrootDir);
            addUserToGroup(userAccount.getUsername(), "kvm", chrootDir);
        }

        return 0;
    }
}
