package com.tle130475c.archinstall.util;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public final class IOUtil {
    private IOUtil() {
    }

    public static String readUsername(String promptMessage) {
        String username = null;

        try {
            ConfigReader configReader = new ConfigReader("install-info.xml");
            username = configReader.getUsername();
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
            System.console().printf(promptMessage);
            username = System.console().readLine();
        }

        return username;
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
