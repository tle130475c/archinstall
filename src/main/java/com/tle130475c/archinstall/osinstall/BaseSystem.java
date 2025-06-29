package com.tle130475c.archinstall.osinstall;

import static com.tle130475c.archinstall.util.ConfigUtil.backupFile;
import static com.tle130475c.archinstall.util.ConfigUtil.commentLine;
import static com.tle130475c.archinstall.util.ConfigUtil.disableService;
import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.ConfigUtil.findAndReplaceInLine;
import static com.tle130475c.archinstall.util.ConfigUtil.getServiceState;
import static com.tle130475c.archinstall.util.ConfigUtil.isServiceStarted;
import static com.tle130475c.archinstall.util.ConfigUtil.stopService;
import static com.tle130475c.archinstall.util.ConfigUtil.uncommentLine;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;
import static com.tle130475c.archinstall.util.PackageUtil.isPackageInstalled;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChroot;
import static com.tle130475c.archinstall.util.ShellUtil.runAppendOutputToFile;
import static com.tle130475c.archinstall.util.ShellUtil.runSetInput;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.tle130475c.archinstall.partition.LVMOnLUKSPartitionLayout;
import com.tle130475c.archinstall.partition.PartitionLayout;
import com.tle130475c.archinstall.systeminfo.SystemInfo;
import com.tle130475c.archinstall.systeminfo.UserAccount;

public class BaseSystem {
    private static final String CHROOT_DIR = "/mnt";
    private static final String PATH_TO_SUDOERS = CHROOT_DIR + "/etc/sudoers";
    private static final String PATH_TO_MKINITCPIO_CONFIG = CHROOT_DIR + "/etc/mkinitcpio.conf";
    private static final String MKINITCPIO_HOOKS_LINE_PATTERN = "^HOOKS=\\(.*\\)$";

    private SystemInfo systemInfo;
    private final UserAccount userAccount;

    public BaseSystem(SystemInfo systemInfo, UserAccount userAccount) {
        this.systemInfo = systemInfo;
        this.userAccount = userAccount;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public void waitUntilKeyringIsInitialized() throws IOException, InterruptedException {
        System.console().printf("Waiting for keyring to be initialized...%n");

        while (!isServiceStarted("archlinux-keyring-wkd-sync.timer", null)) {
            TimeUnit.SECONDS.sleep(1);
        }

        while (!List.of("dead", "failed", "exited").contains(
                getServiceState("archlinux-keyring-wkd-sync.service", null))) {
            TimeUnit.SECONDS.sleep(1);
        }

        System.console().printf("Keyring successfully initialized!%n");
    }

    public void disableAutoGenerateMirrors() throws InterruptedException, IOException {
        stopService("reflector.service", null);
        stopService("reflector.timer", null);
        disableService("reflector.service", null);
        disableService("reflector.timer", null);
    }

    public void enableNetworkTimeSynchronization() throws InterruptedException, IOException {
        runVerbose(List.of("timedatectl", "set-ntp", "true"));
    }

    public void configureMirrors() throws FileNotFoundException {
        try (var writer = new PrintWriter("/etc/pacman.d/mirrorlist")) {
            for (var mirror : systemInfo.getMirrors()) {
                writer.println(mirror);
            }
        }
    }

    public void prepareDisk() throws InterruptedException, IOException {
        systemInfo.getPartitionLayout().create();
        systemInfo.getPartitionLayout().mount();
    }

    public void installEssentialPackages() throws InterruptedException, IOException {
        runVerbose(List.of("pacstrap", CHROOT_DIR, "base", "base-devel", "linux", "linux-headers", "linux-firmware",
                "man-pages", "man-db", "iptables-nft", "pipewire", "pipewire-pulse", "pipewire-alsa", "alsa-utils",
                "gst-plugin-pipewire", "wireplumber", "bash-completion", "nfs-utils", "gvim", "linux-lts",
                "linux-lts-headers"));
    }

    public void disableMakePkgDebug() throws IOException {
        String makePkgConfPath = CHROOT_DIR + "/etc/makepkg.conf";

        backupFile(makePkgConfPath);
        findAndReplaceInLine(makePkgConfPath, "^OPTIONS=\\(.*\\)$", "debug", "!debug");
    }

    public void configureFstab() throws IOException, InterruptedException {
        runAppendOutputToFile(List.of("genfstab", "-U", "/mnt"), CHROOT_DIR + "/etc/fstab");
    }

    public void configureTimeZone() throws InterruptedException, IOException {
        List<String> configureLocaltimeCommand = List.of("ln", "-sf", "/usr/share/zoneinfo/Asia/Ho_Chi_Minh",
                "/etc/localtime");
        runVerbose(getCommandRunChroot(configureLocaltimeCommand, CHROOT_DIR));

        List<String> configureHWClockCommand = List.of("hwclock", "--systohc");
        runVerbose(getCommandRunChroot(configureHWClockCommand, CHROOT_DIR));
    }

    public void configureLocalization() throws IOException, InterruptedException {
        String localeGenPath = "/mnt/etc/locale.gen";

        backupFile(localeGenPath);

        try (var writer = new PrintWriter(localeGenPath)) {
            writer.println("en_US.UTF-8 UTF-8");
        }

        try (var writer = new PrintWriter("/mnt/etc/locale.conf")) {
            writer.println("LANG=en_US.UTF-8");
        }

        List<String> command = List.of("locale-gen");
        runVerbose(getCommandRunChroot(command, CHROOT_DIR));
    }

    public void enableMultilib() throws IOException {
        String pacmanConfigPath = CHROOT_DIR + "/etc/pacman.conf";

        backupFile(pacmanConfigPath);

        // comment out lines contain multilib config
        List<String> lines = Files.readAllLines(Paths.get(pacmanConfigPath));
        int lineNumber = lines.indexOf("#[multilib]");
        lines.set(lineNumber, lines.get(lineNumber).replace("#", ""));
        lines.set(lineNumber + 1, lines.get(lineNumber + 1).replace("#", ""));

        try (var writer = new PrintWriter(pacmanConfigPath)) {
            for (String line : lines) {
                writer.println(line);
            }
        }
    }

    public void configureNetwork() throws IOException, InterruptedException {
        try (var writer = new PrintWriter(CHROOT_DIR + "/etc/hostname")) {
            writer.println(systemInfo.getHostname());
        }

        try (var writer = new PrintWriter(CHROOT_DIR + "/etc/hosts")) {
            writer.append("127.0.0.1\tlocalhost\n");
            writer.append("::1\tlocalhost\n");
            writer.append(
                    "127.0.1.1\t%s.localdomain\t%s%n".formatted(systemInfo.getHostname(), systemInfo.getHostname()));
        }

        installMainReposPkgs(List.of("networkmanager"), CHROOT_DIR);
        enableService("NetworkManager", CHROOT_DIR);
    }

    public void setRootPassword() throws IOException, InterruptedException {
        List<String> command = List.of("passwd");
        runSetInput(getCommandRunChroot(command, CHROOT_DIR),
                List.of(systemInfo.getRootPassword(), systemInfo.getRootPassword()));
    }

    public void addNormalUser() throws InterruptedException, IOException {
        List<String> addUserCommand = List.of("useradd",
                "-G", String.join(",", userAccount.getGroups()),
                "-s", "/bin/bash",
                "-m", userAccount.getUsername(),
                "-d", "/home/%s".formatted(userAccount.getUsername()),
                "-c", userAccount.getRealName());
        runVerbose(getCommandRunChroot(addUserCommand, CHROOT_DIR));

        List<String> changePasswordCommand = List.of("passwd", userAccount.getUsername());
        runSetInput(getCommandRunChroot(changePasswordCommand, CHROOT_DIR),
                List.of(userAccount.getPassword(), userAccount.getPassword()));
    }

    public static void allowUserInWheelGroupExecuteAnyCommand() throws IOException {
        uncommentLine(PATH_TO_SUDOERS, "# %wheel ALL=(ALL:ALL) ALL");
    }

    public static void disallowUserInWheelGroupExecuteAnyCommand() throws IOException {
        commentLine(PATH_TO_SUDOERS, "%wheel ALL=(ALL:ALL) ALL");
    }

    public static void allowUserInWheelGroupExecuteAnyCommandWithoutPassword() throws IOException {
        uncommentLine(PATH_TO_SUDOERS, "# %wheel ALL=(ALL:ALL) NOPASSWD: ALL");
    }

    public static void disallowUserInWheelGroupExecuteAnyCommandWithoutPassword() throws IOException {
        commentLine(PATH_TO_SUDOERS, "%wheel ALL=(ALL:ALL) NOPASSWD: ALL");
    }

    public void disableSudoPasswordPromptTimeout() throws IOException {
        backupFile(PATH_TO_SUDOERS);

        try (var writer = new PrintWriter(new FileOutputStream(PATH_TO_SUDOERS, true))) {
            writer.append("\n## Disable password prompt timeout\n");
            writer.append("Defaults passwd_timeout=0\n");
        }
    }

    public void disableSudoTimestampTimeout() throws IOException {
        backupFile(PATH_TO_SUDOERS);

        try (var writer = new PrintWriter(new FileOutputStream(PATH_TO_SUDOERS, true))) {
            writer.append("\n## Disable sudo timestamp timeout\n");
            writer.append("Defaults timestamp_timeout=-1");
        }
    }

    public void buildInitramfsImageMkinitcpio() throws IOException, InterruptedException {
        runVerbose(getCommandRunChroot(List.of("mkinitcpio", "-p", "linux"), CHROOT_DIR));
    }

    public void configureMkinitcpioForHibernation() throws IOException, InterruptedException {
        backupFile(PATH_TO_MKINITCPIO_CONFIG);
        findAndReplaceInLine(PATH_TO_MKINITCPIO_CONFIG, MKINITCPIO_HOOKS_LINE_PATTERN,
                "filesystems", "filesystems resume");
        buildInitramfsImageMkinitcpio();
    }

    public void configureMkinitcpioForEncryptedRootFileSystem() throws IOException, InterruptedException {
        if (!isPackageInstalled("lvm2", CHROOT_DIR)) {
            installMainReposPkgs(List.of("lvm2"), CHROOT_DIR);
        }

        backupFile(PATH_TO_MKINITCPIO_CONFIG);

        findAndReplaceInLine(PATH_TO_MKINITCPIO_CONFIG, MKINITCPIO_HOOKS_LINE_PATTERN,
                "block", "block encrypt lvm2");

        buildInitramfsImageMkinitcpio();
    }

    private void addSystemdBootEntry(String title, String entryFilename, String kernelPath, String initrdImgPath)
            throws IOException, InterruptedException {
        try (var writer = new PrintWriter(CHROOT_DIR + "/boot/loader/entries/" + entryFilename)) {
            writer.println("title " + title);
            writer.println("linux " + kernelPath);
            writer.println("initrd /intel-ucode.img");
            writer.println("initrd " + initrdImgPath);

            configureMkinitcpioForHibernation();
            if (systemInfo.getPartitionLayout() instanceof LVMOnLUKSPartitionLayout layout) {
                configureMkinitcpioForEncryptedRootFileSystem();
                writer.print("options cryptdevice=UUID=%s:%s"
                        .formatted(layout.getLinuxLUKSPartition().getUUID(), layout.getLUKSMapperName()));
                writer.print(" root=%s".formatted(layout.getRoot().getPath()));
                writer.println(" resume=UUID=%s rw".formatted(layout.getSwap().getUUID()));
            } else if (systemInfo.getPartitionLayout() instanceof PartitionLayout layout) {
                writer.print("options root=UUID=%s".formatted(layout.getRoot().getUUID()));
                writer.println(" resume=UUID=%s rw".formatted(layout.getSwap().getUUID()));
            }
        }
    }

    public void configureSystemdBootloader() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("efibootmgr", "intel-ucode"), CHROOT_DIR);

        List<String> command = List.of("bootctl", "--esp-path=/efi", "--boot-path=/boot", "install");
        runVerbose(getCommandRunChroot(command, CHROOT_DIR));

        try (var writer = new PrintWriter(CHROOT_DIR + "/efi/loader/loader.conf")) {
            writer.println("default archlinux");
            writer.println("timeout 5");
            writer.println("console-mode keep");
            writer.println("editor no");
        }

        // Add entry for standard kernel
        addSystemdBootEntry("Arch Linux", "archlinux.conf", "/vmlinuz-linux", "/initramfs-linux.img");

        // Add entry for LTS kernel
        addSystemdBootEntry("Arch Linux LTS", "archlinux-lts.conf", "/vmlinuz-linux-lts", "/initramfs-linux-lts.img");
    }

    public void configureGRUBBootloader() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("efibootmgr", "intel-ucode", "grub", "grub-customizer", "os-prober"), CHROOT_DIR);

        configureMkinitcpioForHibernation();

        String text = "";
        backupFile(CHROOT_DIR + "/etc/default/grub");
        if (systemInfo.getPartitionLayout() instanceof LVMOnLUKSPartitionLayout layout) {
            configureMkinitcpioForEncryptedRootFileSystem();
            text = "cryptdevice=UUID=%s:%s root=%s resume=UUID=%s rw".formatted(
                    layout.getLinuxLUKSPartition().getUUID(), layout.getLUKSMapperName(),
                    layout.getRoot().getPath(), layout.getSwap().getUUID());
        } else if (systemInfo.getPartitionLayout() instanceof PartitionLayout layout) {
            text = "resume=UUID=%s rw".formatted(layout.getSwap().getUUID());
        }

        findAndReplaceInLine(CHROOT_DIR + "/etc/default/grub", "^GRUB_CMDLINE_LINUX=\"\"$", "\"\"",
                "\"%s\"".formatted(text));

        List<String> grubInstallCommand = List.of("grub-install", "--target=x86_64-efi", "--efi-directory=/efi",
                "--bootloader-id=archlinux", "--recheck");
        List<String> grubMkConfigCommand = List.of("grub-mkconfig", "-o", "/boot/grub/grub.cfg");
        runVerbose(getCommandRunChroot(grubInstallCommand, CHROOT_DIR));
        runVerbose(getCommandRunChroot(grubMkConfigCommand, CHROOT_DIR));
    }

    public void install() throws InterruptedException, IOException {
        disableAutoGenerateMirrors();
        enableNetworkTimeSynchronization();
        configureMirrors();
        prepareDisk();
        waitUntilKeyringIsInitialized();
        installEssentialPackages();
        disableMakePkgDebug();
        configureFstab();
        configureTimeZone();
        configureLocalization();
        enableMultilib();
        configureNetwork();
        setRootPassword();
        addNormalUser();
        disableSudoPasswordPromptTimeout();
        disableSudoTimestampTimeout();
    }
}
