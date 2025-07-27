package com.tle130475c.archinstall.partition;

import static com.tle130475c.archinstall.util.DiskUtil.swapOff;
import static com.tle130475c.archinstall.util.DiskUtil.swapOn;

import java.io.IOException;

public interface PartitionLayout {
    String getDiskName();

    Mountable getESP();

    Mountable getXbootldr();

    Mountable getRoot();

    Mountable getSwap();

    void create() throws InterruptedException, IOException;

    default void mount() throws InterruptedException, IOException {
        getESP().mount();
        getXbootldr().mount();
        getRoot().mount();
        swapOn(getSwap().getPath());
    }

    default void unmount() throws InterruptedException, IOException {
        getESP().unmount();
        getXbootldr().unmount();
        getRoot().unmount();
        swapOff(getSwap().getPath());
    }
}
