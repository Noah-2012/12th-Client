package com.noadsch12;

import com.noadsch12.util.TwelfthCommand;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwelfthClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("12thClient");

    @Override
    public void onInitializeClient() {
        LOGGER.info("12th Client - Client initialized!");
        HotbarHelper.register();
        TwelfthCommand.register();
    }
}
