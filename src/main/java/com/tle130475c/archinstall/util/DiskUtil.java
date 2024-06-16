package com.tle130475c.archinstall.util;

import static com.tle130475c.archinstall.util.ShellUtil.runGetOutput;
import static com.tle130475c.archinstall.util.ShellUtil.runSetInput;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.tle130475c.archinstall.partition.LogicalVolume;
import com.tle130475c.archinstall.partition.Partition;
import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;

public final class DiskUtil {
    private static final String CRYPTSETUP = "cryptsetup";

    private DiskUtil() {
    }

    public static void eraseDisk(String pathToDisk) throws InterruptedException, IOException {
        runVerbose(List.of("wipefs", "-a", pathToDisk));
        runVerbose(List.of("sgdisk", "-Z", pathToDisk));
        TimeUnit.SECONDS.sleep(2);
    }

    public static void wipeDeviceSignature(String pathToDevice) throws InterruptedException, IOException {
        runVerbose(List.of("wipefs", "-a", pathToDevice));
    }

    public static Partition createPartition(Partition partition)
            throws InterruptedException, IOException {
        runVerbose(List.of("sgdisk",
                "-n", "0:0:" + (partition.getSize() == null ? "0"
                        : "+%s%s".formatted(partition.getSize().getValueInString(), partition.getSize().getUnit())),
                "-t", "0:%s".formatted(partition.getType()),
                "-c", "0:%s".formatted(partition.getGptName()),
                partition.getPathToDisk()));

        return partition;
    }

    public static void resizeNTFSFilesystem(Partition partition, StorageDeviceSize sizeInByte)
            throws IOException, InterruptedException {
        runVerbose(List.of("ntfsresize", "-f",
                "--size", "%s".formatted(sizeInByte.getValueInString()), partition.getPath()));

        // remove dirty flag, so the filesystem will not be checked on next Windows boot
        runVerbose(List.of("ntfsfix", "-d", partition.getPath()));
    }

    public static StorageDeviceSize getPartitionSizeInByte(Partition partition) throws IOException {
        String sizeInString = runGetOutput(List.of("blockdev", "--getsize64", partition.getPath()));
        return new StorageDeviceSize(new BigInteger(sizeInString), "B");
    }

    public static StorageDeviceSize convertByteToMebibyte(StorageDeviceSize sizeInByte) {
        return new StorageDeviceSize(sizeInByte.getValue().divide(BigInteger.valueOf(1024L * 1024L)), "MiB");
    }

    public static StorageDeviceSize convertGibibyteToMebibyte(StorageDeviceSize sizeInGibibyte) {
        return new StorageDeviceSize(sizeInGibibyte.getValue().multiply(BigInteger.valueOf(1024L)), "MiB");
    }

    public static StorageDeviceSize convertGibibyteToByte(StorageDeviceSize sizeInGibibyte) {
        return new StorageDeviceSize(
                sizeInGibibyte.getValue().multiply(BigInteger.valueOf(1024L * 1024L * 1024L)), "B");
    }

    public static void shrinkPartition(Partition partition, StorageDeviceSize size)
            throws IOException, InterruptedException {
        runSetInput(List.of("parted", partition.getPathToDisk(), "resizepart", "---pretend-input-tty"),
                List.of(String.valueOf(partition.getPartitionNumber()),
                        "-%s%s".formatted(size.getValueInString(), size.getUnit()), "Yes"));
    }

    public static void shrinkNTFSPartition(Partition partition, StorageDeviceSize sizeInByte)
            throws IOException, InterruptedException {
        var partitionSize = getPartitionSizeInByte(partition);
        var newPartitionSize = new StorageDeviceSize(partitionSize.getValue().subtract(sizeInByte.getValue()), "B");
        resizeNTFSFilesystem(partition, newPartitionSize);
        shrinkPartition(partition, sizeInByte);
    }

    public static Partition createEFIPartition(
            String diskName,
            int partitionNumber,
            StorageDeviceSize size,
            String mountPoint)
            throws InterruptedException, IOException {
        return createPartition(new Partition(diskName, partitionNumber, "ef00", "esp", size, mountPoint));
    }

    public static Partition createXBOOTLDRPartition(
            String diskName,
            int partitionNumber,
            StorageDeviceSize size,
            String mountPoint)
            throws InterruptedException, IOException {
        return createPartition(new Partition(diskName, partitionNumber, "ea00", "XBOOTLDR", size, mountPoint));
    }

    public static Partition createSwapPartition(
            String diskName,
            int partitionNumber,
            StorageDeviceSize size,
            String mountPoint)
            throws InterruptedException, IOException {
        return createPartition(new Partition(diskName, partitionNumber, "8200", "swap", size, mountPoint));
    }

    public static Partition createLinuxRootPartition(
            String diskName,
            int partitionNumber,
            StorageDeviceSize size,
            String mountPoint)
            throws InterruptedException, IOException {
        return createPartition(new Partition(diskName, partitionNumber, "8304", "root", size, mountPoint));
    }

    public static Partition createLinuxLUKSPartition(String diskName, int partitionNumber, StorageDeviceSize size)
            throws InterruptedException, IOException {
        return createPartition(new Partition(diskName, partitionNumber, "8309", "luks-encrypted", size, null));
    }

    public static void createLVMPhysicalVolume(String pathToDevice) throws IOException, InterruptedException {
        runVerbose(List.of("pvcreate", pathToDevice));
    }

    public static void createLVMVolumeGroup(String pathToDevice, String vgName)
            throws IOException, InterruptedException {
        runVerbose(List.of("vgcreate", vgName, pathToDevice));
    }

    public static LogicalVolume createLVMLogicalVolume(LogicalVolume logicalVolume)
            throws IOException, InterruptedException {
        runVerbose(List.of("lvcreate", logicalVolume.getSize() != null ? "-L" : "-l",
                logicalVolume.getSize() != null
                        ? "%s%s".formatted(logicalVolume.getSize().getValueInString(),
                                logicalVolume.getSize().getUnit())
                        : "+100%FREE",
                logicalVolume.getVgName(), "-n", logicalVolume.getLvName()));

        return logicalVolume;
    }

    public static Partition createLUKSContainer(Partition partition, String password)
            throws IOException, InterruptedException {
        runSetInput(List.of(CRYPTSETUP, "luksFormat", "--type", "luks2", partition.getPath()),
                List.of(password));

        return partition;
    }

    public static Partition openLUKSContainer(Partition partition, String luksMapperName, String password)
            throws IOException, InterruptedException {
        runSetInput(List.of(CRYPTSETUP, "open", partition.getPath(), luksMapperName), List.of(password));

        return partition;
    }

    public static Partition encryptDiskUsingLUKS(String diskName, String luksMapperName, String luksPassword,
            String username)
            throws InterruptedException, IOException {
        final String LUKS_MAPPER_DEVICE_PATH = "/dev/mapper/%s".formatted(luksMapperName);
        final String tmpMountDir = "/tmp/%s".formatted(luksMapperName);

        eraseDisk("/dev/%s".formatted(diskName));

        Partition linuxLUKSPartition = createLinuxLUKSPartition(diskName, 1, null);
        wipeDeviceSignature(linuxLUKSPartition.getPath());

        createLUKSContainer(linuxLUKSPartition, luksPassword);
        openLUKSContainer(linuxLUKSPartition, luksMapperName, luksPassword);

        wipeDeviceSignature(LUKS_MAPPER_DEVICE_PATH);
        formatEXT4(LUKS_MAPPER_DEVICE_PATH, luksMapperName);

        mount("/dev/mapper/%s".formatted(luksMapperName), tmpMountDir);
        runVerbose(List.of("chown", "%s:%s".formatted(username, username), tmpMountDir));
        runVerbose(List.of("umount", tmpMountDir));
        FileUtils.deleteDirectory(new File(tmpMountDir));

        runVerbose(List.of(CRYPTSETUP, "close", luksMapperName));

        return linuxLUKSPartition;
    }

    public static void mount(String pathToDevice, String mountPoint) throws IOException, InterruptedException {
        Files.createDirectories(Paths.get(mountPoint));
        runVerbose(List.of("mount", pathToDevice, mountPoint));
    }

    public static void makeSwap(String pathToDevice) throws InterruptedException, IOException {
        runVerbose(List.of("mkswap", pathToDevice));
        runVerbose(List.of("swapon", pathToDevice));
    }

    public static void formatFAT32(String pathToDevice) throws InterruptedException, IOException {
        runVerbose(List.of("mkfs.vfat", "-F32", pathToDevice));
    }

    public static void formatEXT4(String pathToDevice) throws InterruptedException, IOException {
        runVerbose(List.of("mkfs.ext4", pathToDevice));
    }

    public static void formatEXT4(String pathToDevice, String label) throws IOException, InterruptedException {
        runVerbose(List.of("mkfs.ext4", pathToDevice, "-L", label));
    }

    public static String getPathToDisk(String diskName) {
        return "/dev/%s".formatted(diskName);
    }
}
