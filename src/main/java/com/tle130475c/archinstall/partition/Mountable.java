package com.tle130475c.archinstall.partition;

import static com.tle130475c.archinstall.util.ShellUtil.runGetOutput;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public interface Mountable {
    String getMountPoint();

    String getPath();

    default void mount() throws IOException, InterruptedException {
        Files.createDirectories(Paths.get(getMountPoint()));
        runVerbose(List.of("mount", getPath(), getMountPoint()));
    }

    default String getUUID() throws IOException, InterruptedException {
        return runGetOutput(List.of("blkid", "-s", "UUID", "-o", "value", getPath()));
    }
}
