package com.tle130475c.archinstall.partition;

import static com.tle130475c.archinstall.util.DiskUtil.closeLUKSContainer;
import static com.tle130475c.archinstall.util.DiskUtil.createLUKSContainer;
import static com.tle130475c.archinstall.util.DiskUtil.createLVMLogicalVolume;
import static com.tle130475c.archinstall.util.DiskUtil.createLVMPhysicalVolume;
import static com.tle130475c.archinstall.util.DiskUtil.createLVMVolumeGroup;
import static com.tle130475c.archinstall.util.DiskUtil.createPartition;
import static com.tle130475c.archinstall.util.DiskUtil.deactiveVolumeGroup;
import static com.tle130475c.archinstall.util.DiskUtil.formatEXT4;
import static com.tle130475c.archinstall.util.DiskUtil.formatFAT32;
import static com.tle130475c.archinstall.util.DiskUtil.makeSwap;
import static com.tle130475c.archinstall.util.DiskUtil.openLUKSContainer;
import static com.tle130475c.archinstall.util.DiskUtil.wipeDeviceSignature;

import java.io.IOException;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public class LVMOnLUKSDualBootWindows implements LVMOnLUKSPartitionLayout {
    private final String diskName;

    private Partition espPartition;
    private Partition xbootldrPartition;
    private Partition linuxLUKSPartition;
    private LogicalVolume swapVolume;
    private LogicalVolume rootVolume;
    private final String password;

    public LVMOnLUKSDualBootWindows(
            String diskName,
            StorageDeviceSize xbootldrSize,
            StorageDeviceSize swapSize,
            String password) {
        this.diskName = diskName;
        this.password = password;

        espPartition = new Partition(diskName, 1, "/mnt/efi");
        xbootldrPartition = new Partition(diskName, 5, "ea00", "XBOOTLDR", xbootldrSize, "/mnt/boot");
        linuxLUKSPartition = new Partition(diskName, 6, "8309", "luks-encrypted", null, null);
        swapVolume = new LogicalVolume(getVolumeGroupName(), "swap", swapSize, null);
        rootVolume = new LogicalVolume(getVolumeGroupName(), "root", null, "/mnt");
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Partition getLinuxLUKSPartition() {
        return linuxLUKSPartition;
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
    public LogicalVolume getRoot() {
        return rootVolume;
    }

    @Override
    public LogicalVolume getSwap() {
        return swapVolume;
    }

    @Override
    public void create() throws InterruptedException, IOException {
        createPartition(xbootldrPartition);
        wipeDeviceSignature(xbootldrPartition.getPath());

        createPartition(linuxLUKSPartition);
        wipeDeviceSignature(linuxLUKSPartition.getPath());

        createLUKSContainer(linuxLUKSPartition, password);
        openLUKSContainer(linuxLUKSPartition, getLUKSMapperName(), password);
        wipeDeviceSignature(getLUKSMapperDevicePath());

        createLVMPhysicalVolume(getLUKSMapperDevicePath());
        createLVMVolumeGroup(getLUKSMapperDevicePath(), getVolumeGroupName());
        createLVMLogicalVolume(swapVolume);
        createLVMLogicalVolume(rootVolume);

        formatFAT32(xbootldrPartition.getPath());
        makeSwap(swapVolume.getPath());
        formatEXT4(rootVolume.getPath());

        deactiveVolumeGroup(getVolumeGroupName());
        closeLUKSContainer(getLUKSMapperName());
    }
}
