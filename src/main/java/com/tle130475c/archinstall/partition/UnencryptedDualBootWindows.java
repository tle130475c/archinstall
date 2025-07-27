package com.tle130475c.archinstall.partition;

import static com.tle130475c.archinstall.util.DiskUtil.createLinuxRootPartition;
import static com.tle130475c.archinstall.util.DiskUtil.createSwapPartition;
import static com.tle130475c.archinstall.util.DiskUtil.createXBOOTLDRPartition;
import static com.tle130475c.archinstall.util.DiskUtil.formatEXT4;
import static com.tle130475c.archinstall.util.DiskUtil.formatFAT32;
import static com.tle130475c.archinstall.util.DiskUtil.makeSwap;
import static com.tle130475c.archinstall.util.DiskUtil.wipeDeviceSignature;

import java.io.IOException;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public class UnencryptedDualBootWindows implements PartitionLayout {
    private final String diskName;
    private final StorageDeviceSize xbootldrSize;
    private final StorageDeviceSize swapSize;

    private Partition espPartition;
    private Partition xbootldrPartition;
    private Partition swapPartition;
    private Partition rootPartition;

    public UnencryptedDualBootWindows(String diskName, StorageDeviceSize xbootldrSize,
            StorageDeviceSize swapSize) {
        this.diskName = diskName;
        this.xbootldrSize = xbootldrSize;
        this.swapSize = swapSize;
    }

    @Override
    public String getDiskName() {
        return diskName;
    }

    @Override
    public Partition getESP() {
        return espPartition;
    }

    @Override
    public Partition getXbootldr() {
        return xbootldrPartition;
    }

    @Override
    public Partition getRoot() {
        return rootPartition;
    }

    @Override
    public Partition getSwap() {
        return swapPartition;
    }

    @Override
    public void create() throws InterruptedException, IOException {
        espPartition = new Partition(diskName, 1, "/mnt/efi");
        xbootldrPartition = createXBOOTLDRPartition(diskName, 5, xbootldrSize, "/mnt/boot");
        swapPartition = createSwapPartition(diskName, 6, swapSize, null);
        rootPartition = createLinuxRootPartition(diskName, 7, null, "/mnt");

        wipeDeviceSignature(xbootldrPartition.getPath());
        wipeDeviceSignature(swapPartition.getPath());
        wipeDeviceSignature(rootPartition.getPath());

        formatFAT32(xbootldrPartition.getPath());
        makeSwap(swapPartition.getPath());
        formatEXT4(rootPartition.getPath());
    }
}
