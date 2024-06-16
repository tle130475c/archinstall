package com.tle130475c.archinstall.partition;

import static com.tle130475c.archinstall.util.DiskUtil.convertGibibyteToByte;
import static com.tle130475c.archinstall.util.DiskUtil.shrinkNTFSPartition;

import java.io.IOException;

import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public class UnencryptedDualBootWindowsAutoResize extends UnencryptedDualBootWindows {
    private Partition windowsPartition;
    private StorageDeviceSize linuxSystemSize;

    public UnencryptedDualBootWindowsAutoResize(
            String diskName,
            StorageDeviceSize xbootldrSize,
            StorageDeviceSize swapSize,
            Partition windowsPartition,
            StorageDeviceSize linuxSystemSize) {
        super(diskName, xbootldrSize, swapSize);
        this.windowsPartition = windowsPartition;
        this.linuxSystemSize = linuxSystemSize;
    }

    @Override
    public void create() throws InterruptedException, IOException {
        shrinkNTFSPartition(windowsPartition, convertGibibyteToByte(linuxSystemSize));
        super.create();
    }
}
