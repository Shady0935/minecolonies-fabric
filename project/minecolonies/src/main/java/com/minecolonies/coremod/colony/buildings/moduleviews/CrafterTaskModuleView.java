package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.modules.WindowHutCrafterTaskModule;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.fabric.dist.Dist;
import com.minecolonies.fabric.dist.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Crafter task module to display tasks in the UI.
 */
public class CrafterTaskModuleView extends AbstractBuildingModuleView
{
    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new WindowHutCrafterTaskModule(buildingView,Constants.MOD_ID + ":gui/layouthuts/layouttasklist.xml");
    }

    @Override
    public String getIcon()
    {
        return "info";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.crafter.tasks";
    }
}
