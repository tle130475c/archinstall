package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.DiskUtil.configureAutoMountLUKSPartition;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.tle130475c.archinstall.partition.LUKSPartition;
import com.tle130475c.archinstall.util.ConfigReader;

public class ConfigureLUKSAutoMount implements Runnable {
    @Override
    public void run() {
        try {
            ConfigReader configReader = new ConfigReader("install-info.xml");
            List<LUKSPartition> partitions = configReader.getLUKSAutoMountPartitions();
            String username = configReader.getUsername();

            for (LUKSPartition partition : partitions) {
                configureAutoMountLUKSPartition(partition, username);
            }
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
            System.console().printf("No LUKS partition information!%n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
