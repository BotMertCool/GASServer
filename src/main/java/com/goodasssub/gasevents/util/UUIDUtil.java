package com.goodasssub.gasevents.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDUtil {

    public static UUID getOfflineUUID(String playerName) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            String offlinePlayerPrefix = "OfflinePlayer:" + playerName;
            byte[] nameBytes = offlinePlayerPrefix.getBytes(StandardCharsets.UTF_8);

            byte[] hash = md5.digest(nameBytes);

            hash[6] &= 0x0f;
            hash[6] |= 0x30;
            hash[8] &= 0x3f;
            hash[8] |= 0x80;

            long mostSigBits = 0;
            long leastSigBits = 0;

            for (int i = 0; i < 8; i++) {
                mostSigBits = (mostSigBits << 8) | (hash[i] & 0xff);
            }

            for (int i = 8; i < 16; i++) {
                leastSigBits = (leastSigBits << 8) | (hash[i] & 0xff);
            }

            return new UUID(mostSigBits, leastSigBits);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
