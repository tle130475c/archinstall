package com.tle130475c.archinstall.partition;

import static com.tle130475c.archinstall.util.DiskUtil.activeVolumeGroup;
import static com.tle130475c.archinstall.util.DiskUtil.closeLUKSContainer;
import static com.tle130475c.archinstall.util.DiskUtil.deactiveVolumeGroup;
import static com.tle130475c.archinstall.util.DiskUtil.openLUKSContainer;
import static com.tle130475c.archinstall.util.DiskUtil.swapOff;
import static com.tle130475c.archinstall.util.DiskUtil.swapOn;

import java.io.IOException;

public interface LVMOnLUKSPartitionLayout extends PartitionLayout {
    Partition getLinuxLUKSPartition();

    String getPassword();

    default String getLUKSMapperName() {
        return "encrypt-lvm";
    }

    default String getVolumeGroupName() {
        return "vg-system";
    }

    default String getLUKSMapperDevicePath() {
        return "/dev/mapper/%s".formatted(getLUKSMapperName());
    }

    @Override
    default void mount() throws InterruptedException, IOException {
        openLUKSContainer(getLinuxLUKSPartition(), getLUKSMapperName(), getPassword());
        activeVolumeGroup(getVolumeGroupName());

        getRoot().mount();
        getESP().mount();
        getXbootldr().mount();
        swapOn(getSwap().getPath());
    }

    @Override
    default void unmount() throws InterruptedException, IOException {
        getESP().unmount();
        getXbootldr().unmount();
        getRoot().unmount();
        swapOff(getSwap().getPath());

        deactiveVolumeGroup(getVolumeGroupName());
        closeLUKSContainer(getLUKSMapperName());
    }
}
