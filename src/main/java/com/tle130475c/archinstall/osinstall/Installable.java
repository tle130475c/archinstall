package com.tle130475c.archinstall.osinstall;

import java.io.IOException;

public interface Installable extends Runnable {

    @Override
    default void run() {
        try {
            install();
            config();
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }

    int install() throws InterruptedException, IOException;

    default int config() throws IOException, InterruptedException {
        return 0;
    }
}
