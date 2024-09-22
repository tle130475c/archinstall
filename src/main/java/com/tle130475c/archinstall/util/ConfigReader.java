package com.tle130475c.archinstall.util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.tle130475c.archinstall.partition.Partition;
import com.tle130475c.archinstall.partition.PartitionLayoutInfo;
import com.tle130475c.archinstall.systeminfo.StorageDeviceSize;
import com.tle130475c.archinstall.systeminfo.SystemInfo;
import com.tle130475c.archinstall.systeminfo.UserAccount;
import com.tle130475c.archinstall.systeminfo.WirelessNetwork;

public class ConfigReader {
    private final XMLReader xmlReader;

    public ConfigReader(String path) throws SAXException, IOException, ParserConfigurationException {
        xmlReader = new XMLReader(path, DocumentBuilderFactory.newInstance());
    }

    public SystemInfo getSystemInfo() throws XPathExpressionException {
        String hostname = xmlReader.getValue("//system/hostname");
        String rootPassword = xmlReader.getValue("//system/rootPassword");

        List<String> mirrors = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(xmlReader.getValue("count(//mirrors/mirror)")); i++) {
            mirrors.add(xmlReader.getValue("//mirrors/mirror[" + (i + 1) + "]"));
        }

        return new SystemInfo(hostname, rootPassword, mirrors);
    }

    public UserAccount getUserAccount() throws XPathExpressionException {
        String realName = xmlReader.getValue("//account/realName");
        String username = xmlReader.getValue("//account/username");
        String password = xmlReader.getValue("//account/password");

        List<String> groups = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(xmlReader.getValue("count(//account/groups/group)")); i++) {
            groups.add(xmlReader.getValue("//account/groups/group[" + (i + 1) + "]"));
        }

        return new UserAccount(realName, username, password, groups);
    }

    public String getUsername() throws XPathExpressionException {
        return xmlReader.getValue("//account/username");
    }

    public PartitionLayoutInfo getPartitionLayoutInfo() throws XPathExpressionException {
        String diskName = xmlReader.getValue("//partitionLayout/diskName");
        StorageDeviceSize swapSize = new StorageDeviceSize(
                BigInteger.valueOf(Long.parseLong(xmlReader.getValue("//partitionLayout/swapSize"))), "G");
        StorageDeviceSize rootSize = new StorageDeviceSize(
                BigInteger.valueOf(Long.parseLong(xmlReader.getValue("//partitionLayout/rootSize"))), "G");
        String password = xmlReader.getValue("//partitionLayout/password");
        Partition windowsPartition = new Partition(diskName,
                Integer.parseInt(xmlReader.getValue("//partitionLayout/windowsPartNumber")));
        int option = Integer.parseInt(xmlReader.getValue("//partitionLayout/option"));

        return new PartitionLayoutInfo(diskName, swapSize, rootSize, password, windowsPartition, option);
    }

    public Set<Integer> getDriverOptions() throws NumberFormatException, XPathExpressionException {
        Set<Integer> options = new HashSet<>();
        for (int i = 0; i < Integer.parseInt(xmlReader.getValue("count(//drivers/driver)")); i++) {
            options.add(Integer.parseInt(xmlReader.getValue("//drivers/driver[%d]/option".formatted(i + 1))));
        }

        return options;
    }

    public Set<Integer> getDesktopEnvironmentOptions() throws NumberFormatException, XPathExpressionException {
        Set<Integer> options = new HashSet<>();
        for (int i = 0; i < Integer
                .parseInt(xmlReader.getValue("count(//desktopEnvironments/desktopEnvironment)")); i++) {
            options.add(Integer.parseInt(
                    xmlReader.getValue("//desktopEnvironments/desktopEnvironment[%d]/option".formatted(i + 1))));
        }

        return options;
    }

    public Set<Integer> getVirtualMachineOptions() throws NumberFormatException, XPathExpressionException {
        Set<Integer> options = new HashSet<>();
        for (int i = 0; i < Integer.parseInt(xmlReader.getValue("count(//virtualMachines/virtualMachine)")); i++) {
            options.add(Integer.parseInt(
                    xmlReader.getValue("//virtualMachines/virtualMachine[%d]/option".formatted(i + 1))));
        }

        return options;
    }

    public WirelessNetwork getWirelessNetwork() throws XPathExpressionException {
        String ssid = xmlReader.getValue("//wirelessNetwork/ssid");
        String password = xmlReader.getValue("//wirelessNetwork/password");
        String interfaceName = xmlReader.getValue("//wirelessNetwork/interfaceName");
        boolean isHidden = Boolean.parseBoolean(xmlReader.getValue("//wirelessNetwork/isHidden"));

        return new WirelessNetwork(ssid, password, interfaceName, isHidden);
    }
}
