package com.noadsch12.render.items;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ItemLabelManager {

    /**
     * Updates the custom name of an ItemEntity to show its name and count.
     * Call this during the ItemEntity's tick or upon spawning.
     */
    public static void updateItemLabel(ItemEntity itemEntity, boolean clear) {
        ItemStack stack = itemEntity.getStack();

        if (stack.isEmpty()) {
            return;
        }

        // Create the text: "64x Diamond" or "1x Iron Sword"
        Text label = Text.literal(stack.getCount() + "x ")
                    .append(stack.getName())
                    .formatted(Formatting.YELLOW);

        // Apply the name to the entity
        itemEntity.setCustomName(label);
        // Ensure the name is always visible (like a name tag)
        itemEntity.setCustomNameVisible(clear);
    }
}