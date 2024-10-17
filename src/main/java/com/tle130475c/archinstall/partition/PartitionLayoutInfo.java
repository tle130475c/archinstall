package com.tle130475c.archinstall.partition;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public class PartitionLayoutInfo {
    private String diskName;
    private StorageDeviceSize swapSize;
    private StorageDeviceSize rootSize;
    private String password;
    private Partition windowsPartition;
    private int option;

    public PartitionLayoutInfo(String diskName, StorageDeviceSize swapSize, StorageDeviceSize rootSize, String password,
            Partition windowsPartition, int option) {
        this.diskName = diskName;
        this.swapSize = swapSize;
        this.rootSize = rootSize;
        this.password = password;
        this.windowsPartition = windowsPartition;
        this.option = option;
    }

    public String getDiskName() {
        return diskName;
    }

    public void setDiskName(String diskName) {
        this.diskName = diskName;
    }

    public StorageDeviceSize getSwapSize() {
        return swapSize;
    }

    public void setSwapSize(StorageDeviceSize swapSize) {
        this.swapSize = swapSize;
    }

    public StorageDeviceSize getRootSize() {
        return rootSize;
    }

    public void setRootSize(StorageDeviceSize rootSize) {
        this.rootSize = rootSize;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Partition getWindowsPartition() {
        return windowsPartition;
    }

    public void setWindowsPartition(Partition windowsPartition) {
        this.windowsPartition = windowsPartition;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }
}
