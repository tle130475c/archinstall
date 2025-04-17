package com.tle130475c.archinstall.menu.mainmenu;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.tle130475c.archinstall.osinstall.networking.KerberosNfsClientInstall;
import com.tle130475c.archinstall.osinstall.networking.kerberos.KerberosClient;
import com.tle130475c.archinstall.util.ConfigReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InstallKerberosNfsClient implements Runnable {
    private static final String CONFIG_XML = "install_info.xml";

    @Override
    public void run() {

        try {
            ConfigReader configReader = new ConfigReader(CONFIG_XML);
            KerberosClient kerberosClient = configReader.getKerberosClients().get(0);
            new KerberosNfsClientInstall(kerberosClient.getDomain()).install();
        } catch (XPathExpressionException | SAXException | IOException | ParserConfigurationException
                | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
