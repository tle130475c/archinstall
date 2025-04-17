package com.tle130475c.archinstall.osinstall.networking.nfs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NfsShareDirectory {
    private String name;
    private String path;
    private String options;
}
