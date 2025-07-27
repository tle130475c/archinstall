package com.tle130475c.archinstall.partition;

import static com.tle130475c.archinstall.util.DiskUtil.createEFIPartition;
import static com.tle130475c.archinstall.util.DiskUtil.createLinuxRootPartition;
import static com.tle130475c.archinstall.util.DiskUtil.createSwapPartition;
import static com.tle130475c.archinstall.util.DiskUtil.createXBOOTLDRPartition;
import static com.tle130475c.archinstall.util.DiskUtil.eraseDisk;
import static com.tle130475c.archinstall.util.DiskUtil.formatEXT4;
import static com.tle130475c.archinstall.util.DiskUtil.formatFAT32;
import static com.tle130475c.archinstall.util.DiskUtil.getPathToDisk;
import static com.tle130475c.archinstall.util.DiskUtil.makeSwap;
import static com.tle130475c.archinstall.util.DiskUtil.wipeDeviceSignature;

import java.io.IOException;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public class Unencrypted implements PartitionLayout {
    private final String diskName;
    private final StorageDeviceSize espSize;
    private final StorageDeviceSize xbootldrSize;
    private final StorageDeviceSize swapSize;

    private Partition espPartition;
    private Partition xbootldrPartition;
    private Partition swapPartition;
    private Partition rootPartition;

    public Unencrypted(
            String diskName,
            StorageDeviceSize espSize,
            StorageDeviceSize xbootldrSize,
            StorageDeviceSize swapSize) {
        this.diskName = diskName;
        this.espSize = espSize;
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

    public void create() throws InterruptedException, IOException {
        eraseDisk(getPathToDisk(diskName));

        espPartition = createEFIPartition(diskName, 1, espSize, "/mnt/efi");
        xbootldrPartition = createXBOOTLDRPartition(diskName, 2, xbootldrSize, "/mnt/boot");
        swapPartition = createSwapPartition(diskName, 3, swapSize, null);
        rootPartition = createLinuxRootPartition(diskName, 4, null, "/mnt");

        wipeDeviceSignature(espPartition.getPath());
        wipeDeviceSignature(xbootldrPartition.getPath());
        wipeDeviceSignature(swapPartition.getPath());
        wipeDeviceSignature(rootPartition.getPath());

        formatFAT32(espPartition.getPath());
        formatFAT32(xbootldrPartition.getPath());
        makeSwap(swapPartition.getPath());
        formatEXT4(rootPartition.getPath());
    }
}
