package com.minecolonies.apiimp.initializer;

import com.minecolonies.coremod.client.gui.containers.*;
import net.minecraft.client.gui.screens.MenuScreens;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.fabric.event.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.minecolonies.api.inventory.ModContainers;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainerInitializers
{
    public static final com.minecolonies.fabric.registry.FabricDeferredRegister<net.minecraft.world.inventory.MenuType<?>> CONTAINERS = ModContainerRegistry.CONTAINERS;
    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        MenuScreens.register(ModContainers.craftingFurnace.get(), WindowFurnaceCrafting::new);
        MenuScreens.register(ModContainers.craftingGrid.get(), WindowCrafting::new);
        MenuScreens.register(ModContainers.craftingBrewingstand.get(), WindowBrewingstandCrafting::new);

        MenuScreens.register(ModContainers.buildingInv.get(), WindowBuildingInventory::new);
        MenuScreens.register(ModContainers.citizenInv.get(), WindowCitizenInventory::new);
        MenuScreens.register(ModContainers.rackInv.get(), WindowRack::new);
        MenuScreens.register(ModContainers.graveInv.get(), WindowGrave::new);
    }
}
