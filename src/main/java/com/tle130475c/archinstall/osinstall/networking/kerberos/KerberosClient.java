package com.tle130475c.archinstall.osinstall.networking.kerberos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KerberosClient {
    private String ip;
    private String domain;
    private String hostname;
}
