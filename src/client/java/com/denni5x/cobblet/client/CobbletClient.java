package com.denni5x.cobblet.client;

import com.moulberry.axiom.tools.ToolManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.moulberry.axiom.utils.Authorization;

public class CobbletClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblet");
    


    @Override
    public void onInitializeClient() {
        LOGGER.info("Registering Cobblet...");
        ToolManager.addTool(new CobbletTool());
    }
}
