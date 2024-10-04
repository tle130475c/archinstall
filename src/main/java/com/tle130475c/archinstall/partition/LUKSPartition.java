package com.tle130475c.archinstall.partition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LUKSPartition {
    private String partitionUUID;
    private String luksUUID;
    private String mapperName;
    private String luksPassword;
}
