package com.tle130475c.archinstall.partition;

import static com.tle130475c.archinstall.util.DiskUtil.createEFIPartition;
import static com.tle130475c.archinstall.util.DiskUtil.createLUKSContainer;
import static com.tle130475c.archinstall.util.DiskUtil.createLVMLogicalVolume;
import static com.tle130475c.archinstall.util.DiskUtil.createLVMPhysicalVolume;
import static com.tle130475c.archinstall.util.DiskUtil.createLVMVolumeGroup;
import static com.tle130475c.archinstall.util.DiskUtil.createLinuxLUKSPartition;
import static com.tle130475c.archinstall.util.DiskUtil.createXBOOTLDRPartition;
import static com.tle130475c.archinstall.util.DiskUtil.eraseDisk;
import static com.tle130475c.archinstall.util.DiskUtil.formatEXT4;
import static com.tle130475c.archinstall.util.DiskUtil.formatFAT32;
import static com.tle130475c.archinstall.util.DiskUtil.getPathToDisk;
import static com.tle130475c.archinstall.util.DiskUtil.makeSwap;
import static com.tle130475c.archinstall.util.DiskUtil.openLUKSContainer;
import static com.tle130475c.archinstall.util.DiskUtil.wipeDeviceSignature;

import java.io.IOException;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public class LVMOnLUKS implements LVMOnLUKSPartitionLayout {
    private final String diskName;
    private final StorageDeviceSize espSize;
    private final StorageDeviceSize xbootldrSize;
    private final StorageDeviceSize swapSize;

    private Partition espPartition;
    private Partition xbootldrPartition;
    private Partition linuxLUKSPartition;

    private static final String LUKS_MAPPER_NAME = "encrypt-lvm";
    private static final String VG_NAME = "vg-system";
    private static final String LUKS_MAPPER_DEVICE_PATH = "/dev/mapper/%s".formatted(LUKS_MAPPER_NAME);
    private LogicalVolume swapVolume;
    private LogicalVolume rootVolume;
    private final String password;

    public LVMOnLUKS(
            String diskName,
            StorageDeviceSize espSize,
            StorageDeviceSize xbootldrSize,
            StorageDeviceSize swapSize,
            String password) {
        this.diskName = diskName;
        this.espSize = espSize;
        this.xbootldrSize = xbootldrSize;
        this.swapSize = swapSize;
        this.password = password;
    }

    @Override
    public String getLUKSMapperName() {
        return LUKS_MAPPER_NAME;
    }

    @Override
    public Partition getLinuxLUKSPartition() {
        return linuxLUKSPartition;
    }

    @Override
    public LogicalVolume getRoot() {
        return rootVolume;
    }

    @Override
    public LogicalVolume getSwap() {
        return swapVolume;
    }

    @Override
    public void create() throws InterruptedException, IOException {
        eraseDisk(getPathToDisk(diskName));

        espPartition = createEFIPartition(diskName, 1, espSize, "/mnt/efi");
        xbootldrPartition = createXBOOTLDRPartition(diskName, 2, xbootldrSize, "/mnt/boot");
        linuxLUKSPartition = createLinuxLUKSPartition(diskName, 3, null);
        swapVolume = new LogicalVolume(VG_NAME, "swap", swapSize, null);
        rootVolume = new LogicalVolume(VG_NAME, "root", null, "/mnt");

        wipeDeviceSignature(espPartition.getPath());
        wipeDeviceSignature(xbootldrPartition.getPath());
        wipeDeviceSignature(linuxLUKSPartition.getPath());

        createLUKSContainer(linuxLUKSPartition, password);
        openLUKSContainer(linuxLUKSPartition, LUKS_MAPPER_NAME, password);
        wipeDeviceSignature(LUKS_MAPPER_DEVICE_PATH);

        createLVMPhysicalVolume(LUKS_MAPPER_DEVICE_PATH);
        createLVMVolumeGroup(LUKS_MAPPER_DEVICE_PATH, VG_NAME);
        createLVMLogicalVolume(swapVolume);
        createLVMLogicalVolume(rootVolume);

        formatFAT32(espPartition.getPath());
        formatFAT32(xbootldrPartition.getPath());
        makeSwap(swapVolume.getPath());
        formatEXT4(rootVolume.getPath());
    }

    @Override
    public void mount() throws InterruptedException, IOException {
        rootVolume.mount();
        espPartition.mount();
        xbootldrPartition.mount();
    }
}
