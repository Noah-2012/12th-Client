/*
 * 12th Client
 * Copyright (C) 2026 Noadsch12
 *
 * This file is part of the 12th Client project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 */

package com.noadsch12.look;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import java.util.*;

public class ItemHexManager {
    private static final Map<Item, String> itemToHex = new HashMap<>();
    private static final Map<String, Integer> hexHistoryCount = new HashMap<>();
    private static long lastUpdateTime = 0;

    public static void tick() {
        long currentTime = System.currentTimeMillis();
        // Update every 10,000ms (10 seconds)
        if (currentTime - lastUpdateTime > 750) {
            generateNewHexes();
            lastUpdateTime = currentTime;
        }
    }

    private static void generateNewHexes() {
        itemToHex.clear();
        Set<String> assignedHexes = new HashSet<>();
        Random random = new Random();

        for (Item item : Registries.ITEM) {
            String hex;
            do {
                // Generates a random 3-digit hex (000 to FFF)
                hex = String.format("%03X", random.nextInt(0xFFF + 1));
            } while (assignedHexes.contains(hex));

            assignedHexes.add(hex);
            itemToHex.put(item, hex);

            // Track how many times this specific 3-digit code has been used
            hexHistoryCount.put(hex, hexHistoryCount.getOrDefault(hex, 0) + 1);
        }
    }

    public static String getHexForItem(Item item) {
        if (itemToHex.isEmpty()) generateNewHexes();
        return itemToHex.getOrDefault(item, "000");
    }

    public static String getItem(String hex) {
        // Clean the input (remove # and make uppercase)
        String searchHex = hex.startsWith("#") ? hex.substring(1) : hex;
        searchHex = searchHex.toUpperCase();

        for (Map.Entry<Item, String> entry : itemToHex.entrySet()) {
            // If the hex matches the one we generated for this item...
            if (entry.getValue().equals(searchHex)) {
                // Get the actual Name of the Item
                String itemName = entry.getKey().getName().getString();

                // Get the history (total times this hex has appeared minus current)
                int history = hexHistoryCount.getOrDefault(searchHex, 1) - 1;

                // Format: "Your Item is <Item Name (Yellow)> and is the <Count (Red)> Item..."
                return "Your Item is §e" + itemName + " §rand is the §c" + history + "§r Item with this Hex Number.";
            }
        }
        return "§cNo item currently has the hex: " + searchHex;
    }
}