package com.tle130475c.archinstall.osinstall.desktopenvironment;

import static com.tle130475c.archinstall.util.ConfigUtil.createUserEnvironmentDir;
import static com.tle130475c.archinstall.util.ConfigUtil.enableService;
import static com.tle130475c.archinstall.util.PackageUtil.installAURPkgs;
import static com.tle130475c.archinstall.util.PackageUtil.installMainReposPkgs;
import static com.tle130475c.archinstall.util.PackageUtil.installPkgs;
import static com.tle130475c.archinstall.util.PackageUtil.isInMainRepos;
import static com.tle130475c.archinstall.util.PackageUtil.isPackageInstalled;
import static com.tle130475c.archinstall.util.ShellUtil.runGetOutput;
import static com.tle130475c.archinstall.util.ShellUtil.runVerbose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.tle130475c.archinstall.osinstall.Installable;
import com.tle130475c.archinstall.systeminfo.GNOMEShortcut;
import com.tle130475c.archinstall.systeminfo.UserAccount;
import com.tle130475c.archinstall.util.Pair;

public class GNOME implements Installable {
    private static final String GSETTINGS = "gsettings";

    private static final String GSETTINGS_CUSTOM_KEYBINDINGS_KEY = "custom-keybindings";
    private static final String SCHEMA_TO_LIST = "org.gnome.settings-daemon.plugins.media-keys";
    private static final String SCHEMA_TO_ITEM = "org.gnome.settings-daemon.plugins.media-keys.custom-keybinding";
    private static final String PATH_TO_CUSTOM_KEY = "/org/gnome/settings-daemon/plugins/media-keys/custom-keybindings/custom";

    private final String chrootDir;
    private final UserAccount userAccount;

    public GNOME(String chrootDir, UserAccount userAccount) {
        this.chrootDir = chrootDir;
        this.userAccount = userAccount;
    }

    @Override
    public int install() throws InterruptedException, IOException {
        installMainReposPkgs(List.of("xorg-server", "baobab", "eog", "evince", "file-roller", "gdm", "gnome-calculator",
                "gnome-calendar", "gnome-clocks", "gnome-color-manager", "gnome-control-center",
                "gnome-font-viewer", "gnome-keyring", "gnome-screenshot", "gnome-shell-extensions",
                "gnome-system-monitor", "ptyxis", "gnome-themes-extra", "gnome-video-effects", "nautilus",
                "sushi", "gnome-tweaks", "totem", "xdg-user-dirs-gtk", "gnome-usage", "endeavour", "dconf-editor",
                "gnome-shell-extension-appindicator", "alacarte", "gnome-text-editor", "gnome-sound-recorder",
                "seahorse", "seahorse-nautilus", "gnome-browser-connector", "xdg-desktop-portal",
                "xdg-desktop-portal-gnome", "gnome-disk-utility", "libappindicator-gtk3",
                "transmission-gtk", "power-profiles-daemon", "gvfs-smb", "gvfs-google", "gvfs-mtp", "gvfs-nfs",
                "gnome-logs", "evolution", "evolution-ews", "evolution-on", "gnome-software",
                "gnome-boxes", "gnome-remote-desktop", "gnome-connections", "gedit", "gedit-plugins"), chrootDir);
        installMainReposPkgs(List.of("fcitx5-bamboo", "fcitx5-configtool"), chrootDir);
        installPkgs(List.of("gnome-shell-extension-kimpanel-git"), userAccount, chrootDir);

        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
        createUserEnvironmentDir(userAccount.getUsername(), chrootDir);
        enableService("gdm", chrootDir);
        enableService("bluetooth", chrootDir);
        return 0;
    }

    public int gSettingsSet(String schema, String key, String value) throws IOException, InterruptedException {
        return runVerbose(List.of(GSETTINGS, "set", schema, key, value));
    }

    public int gSettingsReset(String schema, String key) throws IOException, InterruptedException {
        return runVerbose(List.of(GSETTINGS, "reset", schema, key));
    }

    public int enableExtension(String uuid) throws IOException, InterruptedException {
        return runVerbose(List.of("gnome-extensions", "enable", uuid));
    }

    public int enableExtension(List<String> uuidList) throws IOException, InterruptedException {
        for (String uuid : uuidList) {
            enableExtension(uuid);
        }

        return 0;
    }

    public void configureDesktopInterface() throws InterruptedException, IOException {
        final String GNOME_DESKTOP_INTERFACE_SCHEMA = "org.gnome.desktop.interface";
        final String GNOME_POWER_SCHEMA = "org.gnome.settings-daemon.plugins.power";

        // set font-antialiasing to rgba
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "font-antialiasing", "rgba");

        // show weekday
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "clock-show-weekday", "true");

        // schedule night light
        gSettingsSet("org.gnome.settings-daemon.plugins.color", "night-light-enabled", "true");
        gSettingsSet("org.gnome.settings-daemon.plugins.color", "night-light-schedule-from", "18.0");

        // set dark theme
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "color-scheme", "prefer-dark");

        // configure nautilus
        gSettingsSet("org.gnome.nautilus.preferences", "default-folder-viewer", "list-view");

        // disable suspend
        gSettingsSet(GNOME_POWER_SCHEMA, "sleep-inactive-battery-type", "nothing");
        gSettingsSet(GNOME_POWER_SCHEMA, "sleep-inactive-ac-type", "nothing");

        // disable dim screen
        gSettingsSet(GNOME_POWER_SCHEMA, "idle-dim", "false");

        // disable screen blank
        gSettingsSet("org.gnome.desktop.session", "idle-delay", "uint32 0");

        // show battery percentage
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "show-battery-percentage", "true");

        // set clock format 24-hour
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "clock-format", "24h");
    }

    public void createCustomShortcut(List<GNOMEShortcut> shortcuts) throws IOException, InterruptedException {
        resetCustomShortcuts();

        for (GNOMEShortcut shortcut : shortcuts) {
            if (!isPackageInstalled(shortcut.getPackageName(), chrootDir)) {
                if (isInMainRepos(shortcut.getPackageName(), chrootDir)) {
                    installMainReposPkgs(List.of(shortcut.getPackageName()), chrootDir);
                } else {
                    installAURPkgs(List.of(shortcut.getPackageName()), userAccount, chrootDir);
                }
            }

            createCustomShortcut(shortcut);
        }
    }

    public void createCustomShortcut(GNOMEShortcut shortcut)
            throws IOException, InterruptedException {
        Pair<String, List<Integer>> pathListAndIndexes = getGNOMEShortcutPathListAndIndexes();
        String pathList = pathListAndIndexes.getFirst();
        int index = pathListAndIndexes.getSecond().size();

        final String CUSTOM_SHORTCUT_SCHEMA = "%s:%s%d/".formatted(SCHEMA_TO_ITEM, PATH_TO_CUSTOM_KEY, index);
        gSettingsSet(CUSTOM_SHORTCUT_SCHEMA, "name", shortcut.getName());
        gSettingsSet(CUSTOM_SHORTCUT_SCHEMA, "binding", shortcut.getKeybinding());
        gSettingsSet(CUSTOM_SHORTCUT_SCHEMA, "command", shortcut.getCommand());

        // determine new pathList
        if (index == 0) {
            pathList = "['%s%d/']".formatted(PATH_TO_CUSTOM_KEY, index);
        } else {
            pathList = pathList.substring(0, pathList.length() - 1) + ", '%s%d/']".formatted(PATH_TO_CUSTOM_KEY, index);
        }

        gSettingsSet(SCHEMA_TO_LIST, GSETTINGS_CUSTOM_KEYBINDINGS_KEY, pathList);
    }

    public List<GNOMEShortcut> readShortcutsFromFile(String fileName) {
        try (var reader = new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName))) {
            Stream<String> rawData = new BufferedReader(reader).lines();
            return rawData.map(line -> {
                String[] parts = line.split(",");
                return new GNOMEShortcut(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
            }).toList();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void resetCustomShortcuts() throws IOException, InterruptedException {
        List<Integer> indexes = getGNOMEShortcutPathListAndIndexes().getSecond();

        for (int index : indexes) {
            final String CUSTOM_SHORTCUT_SCHEMA = "%s:%s%d/".formatted(SCHEMA_TO_ITEM, PATH_TO_CUSTOM_KEY, index);

            gSettingsReset(CUSTOM_SHORTCUT_SCHEMA, "name");
            gSettingsReset(CUSTOM_SHORTCUT_SCHEMA, "binding");
            gSettingsReset(CUSTOM_SHORTCUT_SCHEMA, "command");
        }

        gSettingsReset(SCHEMA_TO_LIST, GSETTINGS_CUSTOM_KEYBINDINGS_KEY);
    }

    private Pair<String, List<Integer>> getGNOMEShortcutPathListAndIndexes() throws IOException, InterruptedException {
        String pathList = runGetOutput(
                List.of(GSETTINGS, "get", SCHEMA_TO_LIST, GSETTINGS_CUSTOM_KEYBINDINGS_KEY));
        List<Integer> indexes = pathList.equals("@as []") ? List.of()
                : Pattern.compile("\\d+").matcher(pathList).results().map(MatchResult::group)
                        .map(Integer::valueOf).toList();

        return new Pair<>(pathList, indexes);
    }
}
