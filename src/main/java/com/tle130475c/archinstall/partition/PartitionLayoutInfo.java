package com.tle130475c.archinstall.partition;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PartitionLayoutInfo {
    private String diskName;
    private StorageDeviceSize swapSize;
    private StorageDeviceSize rootSize;
    private String password;
    private Partition windowsPartition;
    private int option;
}
