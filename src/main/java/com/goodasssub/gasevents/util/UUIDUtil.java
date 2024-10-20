package com.goodasssub.gasevents.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodasssub.gasevents.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Pattern;

public class UUIDUtil {
    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static UUID uuidFromName(String playerName) {
        if (!Main.getInstance().getConfigManager().getConfig().getMojangAuth())
            return getOfflineUuid(playerName);

        return getOnlineUuid(playerName);
    }

    public static UUID getOnlineUuid(String playerName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOJANG_API_URL + playerName))
                .GET()
                .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.body());
                String id = node.get("id").asText();

                String formattedUUID = UUID_PATTERN.matcher(id).replaceFirst("$1-$2-$3-$4-$5");
                return UUID.fromString(formattedUUID);
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().error("Error: {}", e.getMessage());
        }

        return null;
    }

    public static UUID getOfflineUuid(String playerName) {
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
