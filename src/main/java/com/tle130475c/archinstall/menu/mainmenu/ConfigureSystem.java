package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.isAnswerYes;
import static com.tle130475c.archinstall.util.PackageUtil.installFlatpakPackages;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.tle130475c.archinstall.osinstall.desktopenvironment.GNOME;
import com.tle130475c.archinstall.systeminfo.UserAccount;
import com.tle130475c.archinstall.util.ConfigReader;

public class ConfigureSystem implements Runnable {
    private UserAccount userAccount;

    private void getInfo() {
        String username = null;

        try {
            ConfigReader configReader = new ConfigReader("install-info.xml");
            username = configReader.getUsername();
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
            System.console().printf("Username: ");
            username = System.console().readLine();
        }

        userAccount = new UserAccount(null, username, null);
    }

    @Override
    public void run() {
        getInfo();

        if (isAnswerYes(getConfirmation(":: Proceed with configuration? [Y/n] "))) {
            try {
                configureGNOME();
                installFlatpakPkgs();
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void configureGNOME() throws InterruptedException, IOException {
        GNOME gnomeInstall = new GNOME(null, userAccount);
        gnomeInstall.configureDesktopInterface();
        gnomeInstall.createCustomShortcut(gnomeInstall.readShortcutsFromFile("gnome-shortcuts.txt"));
        gnomeInstall.configureIbusBamboo();
        gnomeInstall.enableExtension("appindicatorsupport@rgcjonas.gmail.com");
    }

    public static void installFlatpakPkgs() throws InterruptedException, IOException {
        installFlatpakPackages(List.of("org.goldendict.GoldenDict"));
    }
}
