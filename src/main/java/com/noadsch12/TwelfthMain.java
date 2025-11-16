package com.noadsch12;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwelfthMain implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("12thClient");

    @Override
    public void onInitialize() {
        LOGGER.info("12th Client - Main initialized!");
    }
}