package com.goodasssub.gasevents.util;

import java.security.SecureRandom;

public class SyncUtil {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateSyncCode() {
        final int LENGTH = 6;

        StringBuilder result = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }
}
