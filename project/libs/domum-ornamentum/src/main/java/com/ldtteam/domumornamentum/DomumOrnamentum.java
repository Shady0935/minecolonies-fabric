package com.ldtteam.domumornamentum;

import com.ldtteam.domumornamentum.api.DomumOrnamentumAPI;
import com.ldtteam.domumornamentum.block.ModBlocks;
import com.ldtteam.domumornamentum.block.ModCreativeTabs;
import com.ldtteam.domumornamentum.container.ModContainerTypes;
import com.ldtteam.domumornamentum.entity.block.ModBlockEntityTypes;
import com.ldtteam.domumornamentum.event.handlers.ModBusEventHandler;
import com.ldtteam.domumornamentum.recipe.ModRecipeSerializers;
import com.ldtteam.domumornamentum.recipe.ModRecipeTypes;
import com.ldtteam.domumornamentum.util.Constants;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Common Fabric entrypoint. */
public final class DomumOrnamentum implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);
    private static boolean initialized;

    @Override
    public synchronized void onInitialize()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        IDomumOrnamentumApi.Holder.setInstance(DomumOrnamentumAPI.getInstance());
        ModBlocks.init();
        ModBlockEntityTypes.init();
        ModContainerTypes.init();
        ModRecipeTypes.init();
        ModRecipeSerializers.init();
        ModCreativeTabs.init();
        ModBusEventHandler.registerCommon();
    }
}
