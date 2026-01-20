package com.noadsch12;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public class HotbarHelper {
    private static boolean lastRightShiftState = false;
    private static Field selectedField;

    private static void initSelectedField() {
        if (selectedField != null) return; // Search only once

        try {
            // Try different field names (for 1.21.10: Yarn Mappings)
            String[] possibleNames = {"field_7545", "selectedSlot", "m", "selected", "f_19829_", "currentItem", "p_19829_"};
            for (String name : possibleNames) {
                try {
                    Field field = net.minecraft.entity.player.PlayerInventory.class.getDeclaredField(name);
                    field.setAccessible(true);
                    selectedField = field;
                    TwelfthClient.LOGGER.info("Found selected field: " + name);
                    return;
                } catch (NoSuchFieldException ignored) {}
            }

            TwelfthClient.LOGGER.warn("Could not find selected field in PlayerInventory class!");
            // Log all fields for debugging
            TwelfthClient.LOGGER.info("Available fields in PlayerInventory:");
            for (Field f : net.minecraft.entity.player.PlayerInventory.class.getDeclaredFields()) {
                TwelfthClient.LOGGER.info("  - " + f.getName() + " (" + f.getType().getSimpleName() + ")");
            }
        } catch (Exception e) {
            TwelfthClient.LOGGER.error("Error initializing selectedField", e);
        }
    }

    public static void register() {
        TwelfthClient.LOGGER.info("HotbarHelper registered!");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player == null) return;

                // Initialize the selected field on the first tick in the world
                initSelectedField();

                // Check if Right Shift is pressed
                boolean rightShiftDown = GLFW.glfwGetKey(
                        GLFW.glfwGetCurrentContext(),
                        GLFW.GLFW_KEY_RIGHT_SHIFT
                ) == GLFW.GLFW_PRESS;

                // Check if J is pressed
                boolean jDown = GLFW.glfwGetKey(
                        GLFW.glfwGetCurrentContext(),
                        GLFW.GLFW_KEY_J
                ) == GLFW.GLFW_PRESS;

                if (rightShiftDown || jDown) {
                    TwelfthClient.LOGGER.info("RightShift: " + rightShiftDown + ", J: " + jDown);
                }

                // Only when pressing (edge), not when holding
                boolean jWasPressed = false;
                if (jDown && !lastRightShiftState) {
                    jWasPressed = true;
                }

                if (rightShiftDown && jWasPressed) {
                    TwelfthClient.LOGGER.info("Key combination detected! Jumping to food...");
                    jumpToNextFood(player);
                }

                lastRightShiftState = jDown;
            } catch (Exception e) {
                TwelfthClient.LOGGER.error("Error in HotbarHelper tick", e);
            }
        });
    }

    private static void jumpToNextFood(PlayerEntity player) {
        if (!ClientSettingsScreen.jumpToFoodEnabled) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (isFood(stack)) {
                setSelectedSlot(player, i);
                TwelfthClient.LOGGER.info("Jumped to food at slot " + (i + 1));
                return;
            }
        }
        TwelfthClient.LOGGER.warn("No food found in hotbar!");
    }

    private static void setSelectedSlot(PlayerEntity player, int slot) {
        try {
            if (selectedField != null) {
                selectedField.setInt(player.getInventory(), slot);
            } else {
                // Fallback: try using different field names
                Field field = null;
                for (String name : new String[]{"selectedSlot", "m", "selected"}) {
                    try {
                        field = net.minecraft.entity.player.PlayerInventory.class.getDeclaredField(name);
                        field.setAccessible(true);
                        field.setInt(player.getInventory(), slot);
                        selectedField = field;
                        return;
                    } catch (NoSuchFieldException ignored) {}
                }
                TwelfthClient.LOGGER.warn("Could not set selected slot!");
            }
        } catch (IllegalAccessException e) {
            TwelfthClient.LOGGER.error("Failed to set selected slot via reflection", e);
        }
    }

    private static boolean isFood(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // Prüfe bekannte Food-Items
        return stack.getItem() == Items.APPLE ||
                stack.getItem() == Items.BAKED_POTATO ||
                stack.getItem() == Items.BEEF ||
                stack.getItem() == Items.BREAD ||
                stack.getItem() == Items.CARROT ||
                stack.getItem() == Items.CHICKEN ||
                stack.getItem() == Items.COD ||
                stack.getItem() == Items.COOKIE ||
                stack.getItem() == Items.DRIED_KELP ||
                stack.getItem() == Items.GOLDEN_APPLE ||
                stack.getItem() == Items.GOLDEN_CARROT ||
                stack.getItem() == Items.MELON_SLICE ||
                stack.getItem() == Items.MUTTON ||
                stack.getItem() == Items.PORKCHOP ||
                stack.getItem() == Items.POTATO ||
                stack.getItem() == Items.PUFFERFISH ||
                stack.getItem() == Items.PUMPKIN_PIE ||
                stack.getItem() == Items.RABBIT ||
                stack.getItem() == Items.RABBIT_STEW ||
                stack.getItem() == Items.SALMON ||
                stack.getItem() == Items.TROPICAL_FISH ||
                stack.getItem() == Items.SWEET_BERRIES ||
                stack.getItem() == Items.GLOW_BERRIES ||
                stack.getItem() == Items.HONEY_BOTTLE;
    }
}