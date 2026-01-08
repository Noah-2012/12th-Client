package com.noadsch12.ui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Optional;

public class ModConfigScreen {

    public static Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("MyMod Config"));

        ConfigCategory general = builder.getOrCreateCategory(Text.of("General"));

        MyModConfig config = MyModConfig.get();

        // Boolean Entry
        BooleanListEntry enableFeatureEntry = new BooleanListEntry(
                Text.of("Enable Feature"),
                config.enableFeature,
                Text.of("Reset"),
                () -> config.enableFeature,
                value -> config.enableFeature = value
        );
        general.addEntry(enableFeatureEntry);

        // Integer Entry (angepasst an neuen Konstruktor)
        IntegerListEntry featureLevelEntry = new IntegerListEntry(
                Text.of("Feature Level"),                // Feldname
                config.featureLevel,                    // aktueller Wert
                Text.of("Reset"),                        // Reset-Button Text
                () -> 5,                                 // Default-Wert
                value -> config.featureLevel = value,    // Save-Consumer
                () -> Optional.of(new Text[]{Text.of("Set feature level 0-10")}), // Tooltip
                false                                     // requiresRestart
        );

        IntegerSliderEntry featureLevelSlider = new IntegerSliderEntry(
                Text.of("Feature Level but as slider"),
                0,
                10,
                config.featureLevel,
                Text.of("Reset"),
                () -> 5,
                value -> config.featureLevel = value,
                () -> Optional.of(new Text[]{Text.of("Set feature level 0-10")}),
                false
        );

        general.addEntry(featureLevelEntry);

        general.addEntry(featureLevelSlider);

        return builder.build();
    }
}
