package com.tle130475c.archinstall.osinstall.networking;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.ConfigUtil.startService;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunChroot;
import static com.tle130475c.archinstall.util.ShellUtil.getCommandRunSudo;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.osinstall.networking.kerberos.KerberosServer;
import com.tle130475c.archinstall.util.ConfigReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KerberosNfsClientInstall implements Installable {
    private static final String CONFIG_XML = "install_info.xml";
    private final String chrootDir;

    private KerberosServer kerberosServer;
    private String clientDomain;

    public KerberosNfsClientInstall(String clientDomain) {
        this(clientDomain, null);
    }

    public KerberosNfsClientInstall(String clientDomain, String chrootDir) {
        this.clientDomain = clientDomain;
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        try {
            installRequiredPackages();
            getServerInfo();
            configureHostsFile();
            KerberosNfsServerInstall.editKrb5Configuration(kerberosServer, chrootDir);
            createNfsPrincipal();

            // enable and start nfs-client.target
            enableService("nfs-client.target", chrootDir);
            if (chrootDir == null) {
                startService("nfs-client.target", chrootDir);
            }
        } catch (SAXException | ParserConfigurationException | XPathExpressionException e) {
            log.error("Error reading from file {}!%n", CONFIG_XML);
            return -1;
        }

        return 0;
    }

    private void installRequiredPackages() throws InterruptedException, IOException {
        log.info("Installing required packages...");
        installMainReposPkgs(List.of("krb5", "nfs-utils"), chrootDir);
    }

    private void getServerInfo()
            throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        log.info("Reading server info from file {}...", CONFIG_XML);
        ConfigReader configReader = new ConfigReader(CONFIG_XML);
        kerberosServer = configReader.getKerberosServer();
    }

    private void configureHostsFile() throws FileNotFoundException {
        log.info("Configuring hosts file...");
        String hostsFilePath = chrootDir != null ? chrootDir + "/etc/hosts" : "/etc/hosts";

        try (var writer = new PrintWriter(new FileOutputStream(hostsFilePath, true))) {
            writer.println("%s\t%s\t%s".formatted(
                    kerberosServer.getIp(), kerberosServer.getDomain(), kerberosServer.getHostname()));
        }
    }

    private void createNfsPrincipal() throws IOException, InterruptedException {
        log.info("Creating NFS principal...");
        List<String> command = List.of("kadmin",
                "-p", "%s/admin@%s".formatted(kerberosServer.getAdminUser(), kerberosServer.getRealm()),
                "-w", kerberosServer.getAdminPassword(),
                "-q", "ktadd nfs/%s@%s".formatted(clientDomain, kerberosServer.getRealm()));
        runVerbose(chrootDir != null ? getCommandRunChroot(command, chrootDir) : getCommandRunSudo(command));
    }
}
