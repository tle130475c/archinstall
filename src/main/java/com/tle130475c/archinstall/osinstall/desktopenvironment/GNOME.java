package com.tle130475c.archinstall.osinstall.desktopenvironment;

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

    private static final String IBUS_BAMBOO_PACKAGE = "ibus-bamboo";

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
                "gnome-system-monitor", "gnome-terminal", "gnome-themes-extra", "gnome-video-effects", "nautilus",
                "sushi", "gnome-tweaks", "totem", "xdg-user-dirs-gtk", "gnome-usage", "endeavour", "dconf-editor",
                "gnome-shell-extension-appindicator", "alacarte", "gnome-text-editor", "gnome-sound-recorder",
                "seahorse", "seahorse-nautilus", "gnome-browser-connector", "xdg-desktop-portal",
                "xdg-desktop-portal-gnome", "gnome-remote-desktop", "gnome-disk-utility", "libappindicator-gtk3",
                "transmission-gtk", "power-profiles-daemon", "gvfs-smb", "gvfs-google", "gvfs-mtp", "gvfs-nfs",
                "gnome-user-share", "gnome-logs", "evolution", "evolution-ews", "evolution-on"),
                chrootDir);
        installPkgs(List.of(IBUS_BAMBOO_PACKAGE), userAccount, chrootDir);

        return 0;
    }

    @Override
    public int config() throws IOException, InterruptedException {
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

    public void configureIbusBamboo() throws InterruptedException, IOException {
        if (!isPackageInstalled(IBUS_BAMBOO_PACKAGE, chrootDir)) {
            installPkgs(List.of(IBUS_BAMBOO_PACKAGE), userAccount, chrootDir);
            System.console().printf("Please restart then run the configure for ibus-bamboo again.");
            return;
        }

        gSettingsSet("org.gnome.desktop.input-sources", "sources", "[('xkb', 'us'), ('ibus', 'Bamboo')]");
    }

    public void configureDesktopInterface() throws InterruptedException, IOException {
        final String GNOME_DESKTOP_INTERFACE_SCHEMA = "org.gnome.desktop.interface";
        final String CASCADIA_CODE_MONO_12_VALUE = "Cascadia Mono 12";
        final String GNOME_POWER_SCHEMA = "org.gnome.settings-daemon.plugins.power";

        if (!isPackageInstalled("ttf-cascadia-code", chrootDir)) {
            installMainReposPkgs(List.of("ttf-cascadia-code"), chrootDir);
        }

        // set default monospace font
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "monospace-font-name", CASCADIA_CODE_MONO_12_VALUE);

        // set default interface font
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "font-name", CASCADIA_CODE_MONO_12_VALUE);

        // set default legacy windows titles font
        gSettingsSet("org.gnome.desktop.wm.preferences", "titlebar-font", "Cascadia Mono Bold 12");

        // set default document font
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "document-font-name", CASCADIA_CODE_MONO_12_VALUE);

        // set font-antialiasing to rgba
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "font-antialiasing", "rgba");

        // show weekday
        gSettingsSet(GNOME_DESKTOP_INTERFACE_SCHEMA, "clock-show-weekday", "true");

        // schedule night light
        gSettingsSet("org.gnome.settings-daemon.plugins.color", "night-light-enabled", "true");
        gSettingsSet("org.gnome.settings-daemon.plugins.color", "night-light-schedule-from", "18.0");

        // empty favorite apps
        gSettingsSet("org.gnome.shell", "favorite-apps", "[]");

        // configure nautilus
        gSettingsSet("org.gnome.nautilus.preferences", "default-folder-viewer", "list-view");
        gSettingsSet("org.gnome.nautilus.list-view", "default-zoom-level", "large");

        // disable suspend
        gSettingsSet(GNOME_POWER_SCHEMA, "sleep-inactive-battery-type", "nothing");
        gSettingsSet(GNOME_POWER_SCHEMA, "sleep-inactive-ac-type", "nothing");

        // disable dim screen
        gSettingsSet(GNOME_POWER_SCHEMA, "idle-dim", "false");

        // disable screen blank
        gSettingsSet("org.gnome.desktop.session", "idle-delay", "uint32 0");

        // show battery percentage
        gSettingsSet("org.gnome.desktop.interface", "show-battery-percentage", "true");

        // set clock format 24-hour
        gSettingsSet("org.gnome.desktop.interface", "clock-format", "24h");

        // disable automount for removable media
        gSettingsSet("org.gnome.desktop.media-handling", "automount", "false");
        gSettingsSet("org.gnome.desktop.media-handling", "automount-open", "false");

        // disable tap-to-click
        gSettingsSet("org.gnome.desktop.peripherals.touchpad", "tap-to-click", "false");
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

    private Pair<String, List<Integer>> getGNOMEShortcutPathListAndIndexes() throws IOException {
        String pathList = runGetOutput(
                List.of(GSETTINGS, "get", SCHEMA_TO_LIST, GSETTINGS_CUSTOM_KEYBINDINGS_KEY));
        List<Integer> indexes = pathList.equals("@as []") ? List.of()
                : Pattern.compile("\\d+").matcher(pathList).results().map(MatchResult::group)
                        .map(Integer::valueOf).toList();

        return new Pair<>(pathList, indexes);
    }
}
