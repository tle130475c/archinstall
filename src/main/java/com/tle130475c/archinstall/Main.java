package com.tle130475c.archinstall;

import com.tle130475c.archinstall.menu.mainmenu.MainMenu;

public class Main {
    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
        mainMenu.selectOption();
        mainMenu.doAction();
    }
}
