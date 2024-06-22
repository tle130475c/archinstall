package com.tle130475c.archinstall.menu.mainmenu;

import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.ConfigUtil.startService;
import static com.tle130475c.archinstall.util.IOUtil.getConfirmation;
import static com.tle130475c.archinstall.util.IOUtil.confirmDefaultYes;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstallPostfix implements Runnable {
    private static final String ETC_POSTFIX_VIRTUAL = "/etc/postfix/virtual";
    private static final String ETC_POSTFIX_MAIN_CF = "/etc/postfix/main.cf";
    private static final String ETC_POSTFIX_ALIASES = "/etc/postfix/aliases";

    @Override
    public void run() {
        System.console().printf("Username: ");
        final String username = System.console().readLine();

        if (confirmDefaultYes(getConfirmation(":: Proceed with installation? [Y/n] "))) {
            try {
                installMainReposPkgs(List.of("postfix"), null);

                // map all mail addressed to root to another account
                List<String> lines = Files.readAllLines(Paths.get(ETC_POSTFIX_ALIASES));
                Pattern pattern = Pattern.compile("^#root:.*you");
                var result = lines.stream().map(pattern::matcher).filter(Matcher::matches).findFirst().orElse(null);
                if (result != null) {
                    int lineNumber = lines.indexOf(result.group(0));

                    lines.set(lineNumber, lines.get(lineNumber).replace("#", ""));
                    lines.set(lineNumber, lines.get(lineNumber).replace("you", username));

                    try (var writer = new PrintWriter(ETC_POSTFIX_ALIASES)) {
                        for (String line : lines) {
                            writer.println(line);
                        }
                    }

                    runVerbose(List.of("postalias", ETC_POSTFIX_ALIASES));
                    runVerbose(List.of("newaliases"));
                }

                // to only deliver mail to local system users
                try (var writer = new PrintWriter(new FileOutputStream(ETC_POSTFIX_MAIN_CF, true))) {
                    String config = """

                            # to only deliver mail to local system users
                            myhostname = localhost
                            mydomain = localdomain
                            mydestination = $myhostname, localhost.$mydomain, localhost
                            inet_interfaces = $myhostname, localhost
                            mynetworks_style = host
                            default_transport = error: outside mail is not deliverable
                            virtual_alias_maps = lmdb:/etc/postfix/virtual
                            """;
                    writer.append(config);
                }

                // use any email address ending with "@localhost" or "@localhost.com"
                try (var writer = new PrintWriter(new FileOutputStream(ETC_POSTFIX_VIRTUAL, true))) {
                    String config = """

                            # use any email address ending with "@localhost" or "@localhost.com"
                            @localhost %s
                            @localhost.com %s
                            """.formatted(username, username);
                    writer.append(config);
                }

                // rebuild the index file:
                runVerbose(List.of("postmap", ETC_POSTFIX_VIRTUAL));

                // enable and start the postfix service
                enableService("postfix.service", null);
                startService("postfix", null);
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
