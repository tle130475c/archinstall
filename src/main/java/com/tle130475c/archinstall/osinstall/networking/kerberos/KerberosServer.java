package com.tle130475c.archinstall.osinstall.networking.kerberos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KerberosServer {
    private String ip;
    private String domain;
    private String hostname;
    private String realm;
    private String masterKey;
    private String adminUser;
    private String adminPassword;
    private String user;
    private String userPassword;
}
