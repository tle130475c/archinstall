package com.tle130475c.archinstall.util;

import java.util.regex.Pattern;

public final class IOUtil {
    private IOUtil() {
    }

    public static String readPassword(String firstPrompt, String secondPrompt) {
        System.console().printf(firstPrompt);
        String password = String.valueOf(System.console().readPassword());

        System.console().printf(secondPrompt);
        String reEnterPassword = String.valueOf(System.console().readPassword());

        while (!password.equals(reEnterPassword)) {
            System.console().printf("Two password isn't the same. Please try again!%n");

            System.console().printf(firstPrompt);
            password = String.valueOf(System.console().readPassword());

            System.console().printf(secondPrompt);
            reEnterPassword = String.valueOf(System.console().readPassword());
        }

        return password;
    }

    public static String getConfirmation(String promptMessage) {
        System.console().printf(promptMessage);
        return System.console().readLine();
    }

    public static boolean confirmDefaultYes(String answer) {
        Pattern pattern = Pattern.compile("y|yes", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(answer).matches() || answer.isBlank();
    }

    public static boolean confirmDefaultNo(String answer) {
        Pattern pattern = Pattern.compile("y|yes", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(answer).matches();
    }
}
