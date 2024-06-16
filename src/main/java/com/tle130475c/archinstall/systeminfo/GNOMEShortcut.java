package com.tle130475c.archinstall.systeminfo;

public class GNOMEShortcut {
    private final String packageName;
    private final String name;
    private final String keybinding;
    private final String command;

    public GNOMEShortcut(String packageName, String name, String keybinding, String command) {
        this.packageName = packageName;
        this.name = name;
        this.keybinding = keybinding;
        this.command = command;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getKeybinding() {
        return keybinding;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "GNOMEShortcut(packageName=%s, name=%s, keybinding=%s, command=%s)"
                .formatted(packageName, name, keybinding, command);
    }
}
