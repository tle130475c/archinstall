package com.tle130475c.archinstall.osinstall.networking;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.ConfigUtil.maskService;
import static com.tle130475c.archinstall.util.ConfigUtil.restartService;
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
import com.tle130475c.archinstall.osinstall.networking.kerberos.KerberosClient;
import com.tle130475c.archinstall.osinstall.networking.kerberos.KerberosServer;
import com.tle130475c.archinstall.osinstall.networking.nfs.NfsShareDirectory;
import com.tle130475c.archinstall.util.ConfigReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KerberosNfsServerInstall implements Installable {
    private static final String CONFIG_XML = "install_info.xml";
    private static final String KRB5_KDC_SERVICE = "krb5-kdc.service";
    private static final String KRB5_KADMIND_SERVICE = "krb5-kadmind.service";
    private static final String KADMIN_LOCAL_COMMAND = "kadmin.local";
    private final String chrootDir;

    private KerberosServer kerberosServer;
    private List<KerberosClient> kerberosClients;
    private List<NfsShareDirectory> nfsShareDirectories;

    public KerberosNfsServerInstall() {
        this.chrootDir = null;
    }

    public KerberosNfsServerInstall(String chrootDir) {
        this.chrootDir = chrootDir;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        try {
            installRequiredPackages();
            getInstallInfo();
            configureHostsFile();
            configureKerberosServer();
            configureNfsServer();
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

    private void getInstallInfo()
            throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        log.info("Reading install info from {}...", CONFIG_XML);
        ConfigReader configReader = new ConfigReader(CONFIG_XML);
        kerberosServer = configReader.getKerberosServer();
        kerberosClients = configReader.getKerberosClients();
        nfsShareDirectories = configReader.getNfsShareDirectories();
    }

    private void configureHostsFile() throws FileNotFoundException {
        log.info("Configuring hosts file...");
        final String path = chrootDir != null ? chrootDir + "/etc/hosts" : "/etc/hosts";

        try (var writer = new PrintWriter(new FileOutputStream(path, true))) {
            for (KerberosClient client : kerberosClients) {
                writer.println("%s\t%s\t%s".formatted(client.getIp(), client.getDomain(), client.getHostname()));
            }
        }
    }

    private void configureKerberosServer() throws IOException, InterruptedException {
        log.info("Configuring Kerberos server...");
        editKrb5Configuration(kerberosServer, chrootDir);
        createDatabase();
        enableAndStartKerberosServices();
        configureKadminACL();
        createUserPrincipal();
        createNfsPrincipals();
    }

    public static void editKrb5Configuration(KerberosServer kerberosServer, String chrootDir)
            throws FileNotFoundException {
        log.info("Editing krb5.conf file...");
        final String path = chrootDir != null ? chrootDir + "/etc/krb5.conf" : "/etc/krb5.conf";

        try (var writer = new PrintWriter(path)) {
            writer.write("""
                    [libdefaults]
                    \tdefault_realm = %s

                    [realms]
                    \t%s = {
                    \t\tadmin_server = %s
                    \t\tkdc = %s
                    \t\tdefault_principal_flags = +preauth
                    \t}

                    [domain_realm]
                    \t%s = %s
                    \t.%s = %s

                    [logging]
                    \tkdc          = SYSLOG:NOTICE
                    \tadmin_server = SYSLOG:NOTICE
                    \tdefault      = SYSLOG:NOTICE
                    """.formatted(
                    kerberosServer.getRealm(),
                    kerberosServer.getRealm(),
                    kerberosServer.getDomain(),
                    kerberosServer.getDomain(),
                    kerberosServer.getRealm().toLowerCase(),
                    kerberosServer.getRealm(),
                    kerberosServer.getRealm().toLowerCase(),
                    kerberosServer.getRealm()));
        }
    }

    private void createDatabase() throws IOException, InterruptedException {
        log.info("Creating Kerberos database...");
        List<String> command = List.of("kdb5_util",
                "-P", kerberosServer.getMasterKey(),
                "-r", kerberosServer.getRealm(),
                "create", "-s");
        runVerbose(chrootDir != null ? getCommandRunChroot(command, chrootDir) : getCommandRunSudo(command));
    }

    private void enableAndStartKerberosServices() throws InterruptedException, IOException {
        log.info("Enabling and starting Kerberos services...");
        // enable services
        enableService(KRB5_KDC_SERVICE, chrootDir);
        enableService(KRB5_KADMIND_SERVICE, chrootDir);

        // start services
        if (chrootDir == null) {
            startService(KRB5_KDC_SERVICE, chrootDir);
            startService(KRB5_KADMIND_SERVICE, chrootDir);
        }
    }

    private void configureKadminACL() throws InterruptedException, IOException {
        log.info("Configuring kadmin ACL...");
        final String kadm5AclPath = chrootDir != null ? chrootDir + "/var/lib/krb5kdc/kadm5.acl"
                : "/var/lib/krb5kdc/kadm5.acl";
        final String kdcConfPath = chrootDir != null ? chrootDir + "/var/lib/krb5kdc/kdc.conf"
                : "/var/lib/krb5kdc/kdc.conf";

        // create principal for administration
        List<String> command = List.of(KADMIN_LOCAL_COMMAND, "-q",
                "add_principal -pw %s %s/admin@%s".formatted(
                        kerberosServer.getAdminPassword(),
                        kerberosServer.getAdminUser(),
                        kerberosServer.getRealm()));
        runVerbose(chrootDir != null ? getCommandRunChroot(command, chrootDir) : getCommandRunSudo(command));

        // Add the user to the kadm5.acl file
        try (var writer = new PrintWriter(kadm5AclPath)) {
            writer.println("%s/admin@%s *".formatted(kerberosServer.getAdminUser(), kerberosServer.getRealm()));
        }

        // Configure kdc.conf
        try (var writer = new PrintWriter(kdcConfPath)) {
            writer.write("""
                    [kdcdefaults]
                    \tkdc_ports = 750,88

                    [realms]
                    \t%s = {
                    \t\tdatabase_name = /var/lib/krb5kdc/principal
                    \t\tacl_file = /var/lib/krb5kdc/kadm5.acl
                    \t\tkey_stash_file = /var/lib/krb5kdc/.k5.%s
                    \t\tkdc_ports = 750,88
                    \t\tmax_life = 10h 0m 0s
                    \t\tmax_renewable_life = 7d 0h 0m 0s
                    \t}
                    """.formatted(kerberosServer.getRealm(), kerberosServer.getRealm()));
        }

        restartService(KRB5_KDC_SERVICE, chrootDir);
        restartService(KRB5_KADMIND_SERVICE, chrootDir);
    }

    private void createUserPrincipal() throws IOException, InterruptedException {
        log.info("Creating user principal...");
        List<String> command = List.of(KADMIN_LOCAL_COMMAND, "-q",
                "add_principal -pw %s %s@%s".formatted(
                        kerberosServer.getUserPassword(),
                        kerberosServer.getUser(),
                        kerberosServer.getRealm()));
        runVerbose(chrootDir != null ? getCommandRunChroot(command, chrootDir) : getCommandRunSudo(command));
    }

    private void createNfsPrincipals() throws IOException, InterruptedException {
        log.info("Creating NFS principals...");
        createNfsPrincipalForServer();
        createNfsPrincipalForClients();
    }

    private void createNfsPrincipalForServer() throws IOException, InterruptedException {
        log.info("Creating NFS principal for server...");
        // create nfs principal for server
        List<String> addNfsServerPrincipalCommand = List.of(KADMIN_LOCAL_COMMAND, "-q",
                "add_principal -randkey nfs/%s@%s".formatted(
                        kerberosServer.getDomain(),
                        kerberosServer.getRealm()));
        runVerbose(chrootDir != null ? getCommandRunChroot(addNfsServerPrincipalCommand, chrootDir)
                : getCommandRunSudo(addNfsServerPrincipalCommand));

        // add server nfs principal to keytab
        List<String> addServerNfsPrincipalToKeytabCommand = List.of(KADMIN_LOCAL_COMMAND, "-q",
                "ktadd nfs/%s@%s".formatted(
                        kerberosServer.getDomain(),
                        kerberosServer.getRealm()));
        runVerbose(chrootDir != null ? getCommandRunChroot(addServerNfsPrincipalToKeytabCommand, chrootDir)
                : getCommandRunSudo(addServerNfsPrincipalToKeytabCommand));
    }

    private void createNfsPrincipalForClients() throws IOException, InterruptedException {
        log.info("Creating NFS principals for clients...");
        for (KerberosClient client : kerberosClients) {
            // create nfs principal for clients
            List<String> addNfsClientPrincipalCommand = List.of(KADMIN_LOCAL_COMMAND, "-q",
                    "add_principal -randkey nfs/%s@%s".formatted(
                            client.getDomain(),
                            kerberosServer.getRealm()));
            runVerbose(chrootDir != null ? getCommandRunChroot(addNfsClientPrincipalCommand, chrootDir)
                    : getCommandRunSudo(addNfsClientPrincipalCommand));

            // add client nfs principal to keytab
            List<String> addClientNfsPrincipalToKeytabCommand = List.of(KADMIN_LOCAL_COMMAND, "-q",
                    "ktadd nfs/%s@%s".formatted(
                            client.getDomain(),
                            kerberosServer.getRealm()));
            runVerbose(chrootDir != null ? getCommandRunChroot(addClientNfsPrincipalToKeytabCommand, chrootDir)
                    : getCommandRunSudo(addClientNfsPrincipalToKeytabCommand));
        }
    }

    private void configureNfsServer() throws InterruptedException, IOException {
        log.info("Configuring NFS server...");
        String exportsPath = chrootDir != null ? chrootDir + "/etc/exports" : "/etc/exports";
        try (var writer = new PrintWriter(new FileOutputStream(exportsPath, true))) {
            for (NfsShareDirectory directory : nfsShareDirectories) {
                writer.println("%s *(%s)".formatted(directory.getPath(), directory.getOptions()));
            }
        }

        // enable nfs 4 service
        enableService("nfsv4-server.service", chrootDir);
        if (chrootDir == null) {
            startService("nfsv4-server.service", chrootDir);
        }

        // mask unnecessary services
        maskService("rpcbind.socket", chrootDir);
        maskService("rpcbind.service", chrootDir);
        maskService("nfs-server.service", chrootDir);
    }
}
