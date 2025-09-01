package com.tle130475c.archinstall.partition;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartitionLayoutInfo {
    private String diskName;
    private StorageDeviceSize swapSize;
    private StorageDeviceSize rootSize;
    private String password;
    private Partition windowsPartition;
    private Partition xbootldrPartition;
    private Partition linuxLUKSPartition;
    private int option;
}
