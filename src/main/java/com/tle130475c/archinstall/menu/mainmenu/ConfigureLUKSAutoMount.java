package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.DiskUtil.configureAutoMountLUKSPartition;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;

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
        if (confirmDefaultYes(getConfirmation(":: Proceed with configuring LUKS partitions auto mount? [Y/n] "))) {
            try {
                ConfigReader configReader = new ConfigReader("install_info.xml");
                List<LUKSPartition> partitions = configReader.getLUKSAutoMountPartitions();
                String username = configReader.getUsername();

                for (LUKSPartition partition : partitions) {
                    configureAutoMountLUKSPartition(partition, username);
                }

                System.console().printf("LUKS partitions auto mount configured!%n");
            } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
                System.console().printf("No LUKS partition information!%n");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
