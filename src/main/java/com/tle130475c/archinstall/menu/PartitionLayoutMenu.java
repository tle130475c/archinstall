package com.tle130475c.archinstall.menu;

import static com.tle130475c.archinstall.util.IOUtil.readPassword;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import com.tle130475c.archinstall.partition.LVMOnLUKS;
import com.tle130475c.archinstall.partition.LVMOnLUKSDualBootWindows;
import com.tle130475c.archinstall.partition.LVMOnLUKSDualBootWindowsAutoResize;
import com.tle130475c.archinstall.partition.Partition;
import com.tle130475c.archinstall.partition.PartitionLayout;
import com.tle130475c.archinstall.partition.PartitionLayoutInfo;
import com.tle130475c.archinstall.partition.Unencrypted;
import com.tle130475c.archinstall.partition.UnencryptedDualBootWindows;
import com.tle130475c.archinstall.partition.UnencryptedDualBootWindowsAutoResize;
import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public class PartitionLayoutMenu extends SingleChoiceMenu {
    private static final StorageDeviceSize ESP_SIZE = new StorageDeviceSize(BigInteger.valueOf(550L), "M");
    private static final StorageDeviceSize XBOOTLDR_SIZE = new StorageDeviceSize(BigInteger.valueOf(550L), "M");

    private PartitionLayout partitionLayout;

    public PartitionLayoutMenu() throws IOException, InterruptedException {
        super();

        runVerbose(List.of("lsblk"));
        System.console().printf("Enter disk's name (e.g. nvme0n1, sda): ");
        String diskName = System.console().readLine();

        System.console().printf("Enter swap size: ");
        long swapSizeInput = Long.parseLong(System.console().readLine());
        final StorageDeviceSize swapSize = new StorageDeviceSize(BigInteger.valueOf(swapSizeInput), "G");

        Runnable setUnencrypted = () -> partitionLayout = new Unencrypted(diskName, ESP_SIZE, XBOOTLDR_SIZE, swapSize);

        Runnable setUnencryptedDualBootWindows = () -> partitionLayout = new UnencryptedDualBootWindows(
                diskName, XBOOTLDR_SIZE, swapSize);

        Runnable setUnencryptedDualBootWindowsAutoResize = () -> {
            System.console().printf("Enter Windows's partition number: ");
            int partitionNumber = Integer.parseInt(System.console().readLine());

            System.console().printf("Enter Linux's system size in GiB: ");
            StorageDeviceSize linuxSystemSize = new StorageDeviceSize(
                    BigInteger.valueOf(Long.parseLong(System.console().readLine())), "GiB");

            Partition windowsPartition = new Partition(diskName, partitionNumber);
            partitionLayout = new UnencryptedDualBootWindowsAutoResize(diskName, XBOOTLDR_SIZE, swapSize,
                    windowsPartition, linuxSystemSize);
        };

        Runnable setLVMOnLUKS = () -> {
            String password = getLUKSPassword();
            partitionLayout = new LVMOnLUKS(diskName, ESP_SIZE, XBOOTLDR_SIZE, swapSize, password);
        };

        Runnable setLVMOnLUKSDualBootWindows = () -> {
            System.console().printf("Enter XBOOTLDR partition number: ");
            int xbootldrPartNumber = Integer.parseInt(System.console().readLine().trim());

            System.console().printf("Enter Linux LUKS partition number: ");
            int linuxLUKSPartNumber = Integer.parseInt(System.console().readLine().trim());

            String password = getLUKSPassword();

            partitionLayout = new LVMOnLUKSDualBootWindows(diskName, XBOOTLDR_SIZE, swapSize, password,
                    xbootldrPartNumber, linuxLUKSPartNumber);
        };

        Runnable setLVMOnLUKSDualBootWindowsAutoResize = () -> {
            String password = getLUKSPassword();

            System.console().printf("Enter Windows's partition number: ");
            int partitionNumber = Integer.parseInt(System.console().readLine());

            System.console().printf("Enter Linux's system size in GiB: ");
            StorageDeviceSize linuxSystemSize = new StorageDeviceSize(
                    BigInteger.valueOf(Long.parseLong(System.console().readLine())), "GiB");

            Partition windowsPartition = new Partition(diskName, partitionNumber);
            partitionLayout = new LVMOnLUKSDualBootWindowsAutoResize(diskName, XBOOTLDR_SIZE, swapSize, password,
                    windowsPartition, linuxSystemSize);
        };

        addOptions(setUnencrypted, setUnencryptedDualBootWindows, setUnencryptedDualBootWindowsAutoResize, setLVMOnLUKS,
                setLVMOnLUKSDualBootWindows, setLVMOnLUKSDualBootWindowsAutoResize);
    }

    public PartitionLayoutMenu(PartitionLayoutInfo info) {
        super();

        Runnable setUnencrypted = () -> partitionLayout = new Unencrypted(info.getDiskName(), ESP_SIZE, XBOOTLDR_SIZE,
                info.getSwapSize());

        Runnable setUnencryptedDualBootWindows = () -> partitionLayout = new UnencryptedDualBootWindows(
                info.getDiskName(), XBOOTLDR_SIZE, info.getSwapSize());

        Runnable setUnencryptedDualBootWindowsAutoResize = () -> partitionLayout = new UnencryptedDualBootWindowsAutoResize(
                info.getDiskName(), XBOOTLDR_SIZE, info.getSwapSize(), info.getWindowsPartition(),
                info.getRootSize());

        Runnable setLVMOnLUKS = () -> partitionLayout = new LVMOnLUKS(info.getDiskName(), ESP_SIZE, XBOOTLDR_SIZE,
                info.getSwapSize(), info.getPassword());

        Runnable setLVMOnLUKSDualBootWindows = () -> partitionLayout = new LVMOnLUKSDualBootWindows(info.getDiskName(),
                XBOOTLDR_SIZE, info.getSwapSize(), info.getPassword(),
                info.getXbootldrPartition().getPartitionNumber(),
                info.getLinuxLUKSPartition().getPartitionNumber());

        Runnable setLVMOnLUKSDualBootWindowsAutoResize = () -> partitionLayout = new LVMOnLUKSDualBootWindowsAutoResize(
                info.getDiskName(), XBOOTLDR_SIZE, info.getSwapSize(), info.getPassword(), info.getWindowsPartition(),
                info.getRootSize());

        addOptions(setUnencrypted, setUnencryptedDualBootWindows, setUnencryptedDualBootWindowsAutoResize, setLVMOnLUKS,
                setLVMOnLUKSDualBootWindows, setLVMOnLUKSDualBootWindowsAutoResize);
    }

    private void addOptions(Runnable setUnencrypted, Runnable setUnencryptedDualBootWindows,
            Runnable setUnencryptedDualBootWindowsAutoResize, Runnable setLVMOnLUKS,
            Runnable setLVMOnLUKSDualBootWindows, Runnable setLVMOnLUKSDualBootWindowsAutoResize) {
        addOption(new Option("Unencrypted partition layout", setUnencrypted, false));
        addOption(new Option("Unencrypted dual boot Windows partition layout", setUnencryptedDualBootWindows, false));
        addOption(new Option("Unencrypted dual boot Windows partition layout (auto resize)",
                setUnencryptedDualBootWindowsAutoResize, false));
        addOption(new Option("LVM on LUKS partition layout", setLVMOnLUKS, false));
        addOption(new Option("LVM on LUKS dual boot Windows partition layout", setLVMOnLUKSDualBootWindows, false));
        addOption(new Option("LVM on LUKS dual boot Windows partition layout (auto resize)",
                setLVMOnLUKSDualBootWindowsAutoResize, false));
    }

    public PartitionLayout selectPartitionLayout() {
        selectOption();
        doAction();
        return partitionLayout;
    }

    public PartitionLayout setPartitionLayout(int option) {
        setOption(option);
        doAction();
        return partitionLayout;
    }

    private String getLUKSPassword() {
        return readPassword(
                "LUKS's password: ",
                "Re-enter LUKS's password: ");
    }
}
