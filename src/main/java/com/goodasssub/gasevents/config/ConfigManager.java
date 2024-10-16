package com.goodasssub.gasevents.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.json";
    private final Gson gson;
    @Getter private Config config;

    public ConfigManager() {
        gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

        loadConfig();
    }

    private void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = gson.fromJson(reader, Config.class);
            } catch (IOException e) {
                System.err.println("Error reading config file: " + e.getMessage());
                config = new Config();
            }
        } else {
            config = new Config();
            saveConfig();
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Error writing config file: " + e.getMessage());
        }
    }
}
