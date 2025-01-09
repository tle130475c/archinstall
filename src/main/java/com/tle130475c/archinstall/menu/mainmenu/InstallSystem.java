package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.readPassword;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.tle130475c.archinstall.menu.BootloaderMenu;
import com.tle130475c.archinstall.menu.DesktopEnvironmentMenu;
import com.tle130475c.archinstall.menu.DriverMenu;
import com.tle130475c.archinstall.menu.PartitionLayoutMenu;
import com.tle130475c.archinstall.menu.ProgrammingMenu;
import com.tle130475c.archinstall.menu.ToolMenu;
import com.tle130475c.archinstall.menu.VirtualMachineMenu;
import com.tle130475c.archinstall.osinstall.BaseSystem;
import com.tle130475c.archinstall.partition.PartitionLayout;
import com.tle130475c.archinstall.partition.PartitionLayoutInfo;
import com.tle130475c.archinstall.systeminfo.SystemInfo;
import com.tle130475c.archinstall.systeminfo.UserAccount;
import com.tle130475c.archinstall.systeminfo.WirelessNetwork;
import com.tle130475c.archinstall.util.ConfigReader;
import com.tle130475c.archinstall.util.NetworkUtil;

public class InstallSystem implements Runnable {
    private static final String CONFIG_XML = "install-info.xml";
    private SystemInfo systemInfo;
    private UserAccount userAccount;

    private BaseSystem baseSystem;

    protected BootloaderMenu bootloaderMenu;
    protected DesktopEnvironmentMenu desktopEnvironmentMenu;
    protected DriverMenu driverMenu;
    protected ProgrammingMenu programmingMenu;
    protected ToolMenu toolMenu;
    protected VirtualMachineMenu virtualMachineMenu;

    @Override
    public void run() {
        System.console().printf("%n");

        try {
            if (Files.exists(Paths.get(CONFIG_XML))) {
                ConfigReader configReader = new ConfigReader(CONFIG_XML);
                getSystemInfoFromFile(configReader);
            } else {
                getSystemInfo();
            }

            getInstallInfo();

            if (Files.exists(Paths.get(CONFIG_XML))) {
                ConfigReader configReader = new ConfigReader(CONFIG_XML);
                selectInstallSoftwareFromFile(configReader);
            } else {
                selectInstallSoftwares();
            }

            System.console().printf("%n");
            getInstallSummary();

            if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
                install();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    private void getSystemInfoFromFile(ConfigReader configReader) throws XPathExpressionException {
        PartitionLayoutInfo partitionLayoutInfo = configReader.getPartitionLayoutInfo();

        systemInfo = configReader.getSystemInfo();
        systemInfo.setPartitionLayout(
                new PartitionLayoutMenu(partitionLayoutInfo).setPartitionLayout(partitionLayoutInfo.getOption()));

        userAccount = configReader.getUserAccount();
    }

    private void getSystemInfo() throws IOException, InterruptedException {
        String[] mirrorsArray = new String[3];
        Arrays.fill(mirrorsArray, "Server = https://mirror.xtom.com.hk/archlinux/$repo/os/$arch");
        List<String> mirrors = Arrays.asList(mirrorsArray);

        System.console().printf("Hostname: ");
        final String hostname = System.console().readLine();

        final String rootPassword = readPassword(
                "Root's password: ",
                "Re-enter root's password: ");

        System.console().printf("User's real name: ");
        final String realName = System.console().readLine();

        System.console().printf("Username: ");
        final String username = System.console().readLine();

        final String userPassword = readPassword(
                "User's password: ",
                "Re-enter User's password: ");

        final PartitionLayout partitionLayout = new PartitionLayoutMenu().selectPartitionLayout();

        systemInfo = new SystemInfo(hostname, rootPassword, mirrors, partitionLayout);
        userAccount = new UserAccount(realName, username, userPassword);
    }

    private void getInstallInfo() {
        final String chrootDir = "/mnt";

        baseSystem = new BaseSystem(systemInfo, userAccount);
        bootloaderMenu = new BootloaderMenu(baseSystem);
        desktopEnvironmentMenu = new DesktopEnvironmentMenu(chrootDir, userAccount);
        driverMenu = new DriverMenu(chrootDir, userAccount);
        programmingMenu = new ProgrammingMenu(chrootDir, userAccount);
        toolMenu = new ToolMenu(chrootDir, userAccount);
        virtualMachineMenu = new VirtualMachineMenu(chrootDir, userAccount);
    }

    protected void selectInstallSoftwares() {
        bootloaderMenu.selectOption();
        desktopEnvironmentMenu.selectOption();
        driverMenu.selectOption();
        programmingMenu.selectAll();
        toolMenu.selectAll();
        virtualMachineMenu.selectAll();
    }

    protected void selectInstallSoftwareFromFile(ConfigReader configReader)
            throws NumberFormatException, XPathExpressionException {
        bootloaderMenu.setOption(configReader.getBootloaderOption());
        desktopEnvironmentMenu.setOptions(configReader.getDesktopEnvironmentOptions());
        driverMenu.setOptions(configReader.getDriverOptions());
        programmingMenu.setOptions(configReader.getProgrammingOptions());
        toolMenu.setOptions(configReader.getToolOptions());
        virtualMachineMenu.setOptions(configReader.getVirtualMachineOptions());
    }

    private void getInstallSummary() {
        System.console().printf("Install summary:%n");
        System.console().printf("%s%n", "[Base System]");
        System.console().printf("%s%n", bootloaderMenu.getActionSummary());
        System.console().printf("%s%n", desktopEnvironmentMenu.getActionSummary());
        System.console().printf("%s%n", driverMenu.getActionSummary());
        System.console().printf("%s%n", programmingMenu.getActionSummary());
        System.console().printf("%s%n", toolMenu.getActionSummary());
        System.console().printf("%s%n", virtualMachineMenu.getActionSummary());
    }

    private void install() throws InterruptedException, IOException, XPathExpressionException, SAXException,
            ParserConfigurationException {
        if (!NetworkUtil.isConnectedToInternet()) {
            if (Files.exists(Paths.get(CONFIG_XML))) {
                ConfigReader configReader = new ConfigReader(CONFIG_XML);
                WirelessNetwork network = configReader.getWirelessNetwork();
                NetworkUtil.connectToWifi(network);
            } else {
                NetworkUtil.connectToWifi();
            }
        }

        baseSystem.install();
        bootloaderMenu.doAction();

        BaseSystem.allowUserInWheelGroupExecuteAnyCommandWithoutPassword();

        desktopEnvironmentMenu.doAction();
        driverMenu.doAction();
        programmingMenu.doAction();
        toolMenu.doAction();
        virtualMachineMenu.doAction();

        BaseSystem.disallowUserInWheelGroupExecuteAnyCommandWithoutPassword();
        BaseSystem.allowUserInWheelGroupExecuteAnyCommand();
    }
}
