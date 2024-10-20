package com.goodasssub.gasevents.profile.whitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WhitelistHandler {
    private static final String CONFIG_FILE = "whitelist.json";
    private final Gson gson;
    @Getter private Whitelist whitelist;

    public WhitelistHandler() {
        gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

        loadJson();
    }

    private void loadJson() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                whitelist = gson.fromJson(reader, Whitelist.class);
            } catch (IOException e) {
                System.err.println("Error reading config file: " + e.getMessage());
                whitelist = new Whitelist();
            }
        } else {
            whitelist = new Whitelist();
            saveJson();
        }
    }

    public void saveJson() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(whitelist, writer);
        } catch (IOException e) {
            System.err.println("Error writing config file: " + e.getMessage());
        }
    }
}
