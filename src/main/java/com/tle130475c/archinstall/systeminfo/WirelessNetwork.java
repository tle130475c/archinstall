package com.tle130475c.archinstall.systeminfo;

import java.io.IOException;
import java.util.List;

import com.tle130475c.archinstall.util.ShellUtil;

public class WirelessNetwork {
    private final String ssid;
    private final String password;
    private final String interfaceName;
    private final boolean isHidden;

    public WirelessNetwork(String ssid, String password, String interfaceName, boolean isHidden) {
        this.ssid = ssid;
        this.password = password;
        this.interfaceName = interfaceName;
        this.isHidden = isHidden;
    }

    public void connect() throws InterruptedException, IOException {
        ShellUtil.runVerbose(List.of("iwctl", "--passphrase=%s".formatted(password), "station", interfaceName,
                isHidden ? "connect-hidden" : "connect", ssid));
    }
}
