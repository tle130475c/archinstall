package com.tle130475c.archinstall.partition;

public class LUKSPartition {
    private String partitionUUID;
    private String luksUUID;
    private String mapperName;
    private String luksPassword;

    public LUKSPartition(String partitionUUID, String luksUUID, String mapperName, String luksPassword) {
        this.partitionUUID = partitionUUID;
        this.luksUUID = luksUUID;
        this.mapperName = mapperName;
        this.luksPassword = luksPassword;
    }

    public String getPartitionUUID() {
        return partitionUUID;
    }

    public void setPartitionUUID(String partitionUUID) {
        this.partitionUUID = partitionUUID;
    }

    public String getLuksUUID() {
        return luksUUID;
    }

    public void setLuksUUID(String luksUUID) {
        this.luksUUID = luksUUID;
    }

    public String getMapperName() {
        return mapperName;
    }

    public void setMapperName(String mapperName) {
        this.mapperName = mapperName;
    }

    public String getLuksPassword() {
        return luksPassword;
    }

    public void setLuksPassword(String luksPassword) {
        this.luksPassword = luksPassword;
    }
}
