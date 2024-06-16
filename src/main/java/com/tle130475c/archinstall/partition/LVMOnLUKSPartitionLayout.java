package com.tle130475c.archinstall.partition;

public interface LVMOnLUKSPartitionLayout extends PartitionLayout {
    Partition getLinuxLUKSPartition();

    String getLUKSMapperName();
}
