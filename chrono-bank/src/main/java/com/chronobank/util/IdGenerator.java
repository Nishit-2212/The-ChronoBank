package com.chronobank.util;

import java.util.UUID;

public class IdGenerator {
    /**
     * @return A string representing the unique user ID.
     */
    public static String generateUserId() {
        return UUID.randomUUID().toString();
    }

    /**
     * @return A string representing the unique ID.
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}

