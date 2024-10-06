package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.tle130475c.archinstall.osinstall.desktopenvironment.GNOME;
import com.tle130475c.archinstall.systeminfo.UserAccount;
import com.tle130475c.archinstall.util.ConfigReader;

public class ConfigureGNOME implements Runnable {
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

        if (confirmDefaultYes(getConfirmation(":: Proceed with GNOME configuration? [Y/n] "))) {
            try {
                configureGNOME();
                System.console().printf("GNOME configuration completed!%n");
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
}
