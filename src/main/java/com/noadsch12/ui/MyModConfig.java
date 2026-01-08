package com.noadsch12.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MyModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/mymod.json");

    public boolean enableFeature = true;
    public int featureLevel = 5;

    private static MyModConfig instance;

    public static MyModConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static MyModConfig load() {
        try {
            if (CONFIG_FILE.exists()) {
                return GSON.fromJson(new FileReader(CONFIG_FILE), MyModConfig.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MyModConfig(); // Standardwerte
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
