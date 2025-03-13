package com.tle130475c.archinstall.osinstall;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Installable extends Runnable {
    private Logger log() {
        return LoggerFactory.getLogger(Installable.class);
    }

    @Override
    default void run() {
        try {
            log().info("Install {}", getClass().getName());
            install();

            log().info("Configure {}", getClass().getName());
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
