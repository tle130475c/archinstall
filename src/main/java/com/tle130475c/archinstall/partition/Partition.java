package com.tle130475c.archinstall.partition;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public class Partition implements Mountable {
    private String diskName;
    private int partitionNumber;
    private String type;
    private String gptName;
    private StorageDeviceSize size;
    private String mountPoint;

    public Partition(String diskName, int partitionNumber) {
        this.diskName = diskName;
        this.partitionNumber = partitionNumber;
    }

    public Partition(String diskName, int partitionNumber, String mountPoint) {
        this.diskName = diskName;
        this.partitionNumber = partitionNumber;
        this.mountPoint = mountPoint;
    }

    public Partition(String type, String gptName, StorageDeviceSize size) {
        this.type = type;
        this.gptName = gptName;
        this.size = size;
    }

    public Partition(String diskName, String type, String gptName) {
        this.diskName = diskName;
        this.type = type;
        this.gptName = gptName;
    }

    public Partition(
            String diskName,
            int partitionNumber,
            String type,
            String gptName,
            StorageDeviceSize size,
            String mountPoint) {
        this.diskName = diskName;
        this.partitionNumber = partitionNumber;
        this.type = type;
        this.gptName = gptName;
        this.size = size;
        this.mountPoint = mountPoint;
    }

    public String getDiskName() {
        return diskName;
    }

    public void setDiskName(String diskName) {
        this.diskName = diskName;
    }

    public int getPartitionNumber() {
        return partitionNumber;
    }

    public void setPartitionNumber(int partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGptName() {
        return gptName;
    }

    public void setGptName(String gptName) {
        this.gptName = gptName;
    }

    public StorageDeviceSize getSize() {
        return size;
    }

    public void setSize(StorageDeviceSize size) {
        this.size = size;
    }

    @Override
    public String getMountPoint() {
        return mountPoint;
    }

    public String getPathToDisk() {
        return "/dev/%s".formatted(diskName);
    }

    @Override
    public String getPath() {
        return diskName.startsWith("nvme") || diskName.startsWith("mmcblk")
                ? "%sp%d".formatted(getPathToDisk(), partitionNumber)
                : getPathToDisk() + partitionNumber;
    }
}
