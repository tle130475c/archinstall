package com.tle130475c.archinstall.menu;

import java.util.ArrayList;
import java.util.List;

public abstract class Menu {
    protected static final int EXIT = -1;
    protected static final int MIN_CHOICE = 0;

    protected int optionCount;
    protected List<Option> options;

    protected Menu() {
        optionCount = MIN_CHOICE;
        options = new ArrayList<>();
    }

    public int getMaxChoice() {
        return options.size() - 1;
    }

    public abstract String getPromptMessage();

    public void addOption(Option option) {
        options.add(option);
        option.setOptionNumber(optionCount);
        optionCount++;
    }

    public void clearAll() {
        for (Option option : options) {
            option.setMarked(false);
        }
    }

    public void displayMenu() {
        for (Option option : options) {
            System.console().printf("%s\n", option);
        }

        System.console().printf(getPromptMessage());
    }

    public void doAction() {
        for (Option option : options) {
            option.doAction();
        }
    }

    public String getActionSummary() {
        List<String> markedOptions = new ArrayList<>();

        for (Option option : options) {
            if (option.isMarked()) {
                markedOptions.add(option.getDescription());
            }
        }

        return getClass().getSimpleName() + "=" + markedOptions;
    }

    public abstract void selectOption();
}
