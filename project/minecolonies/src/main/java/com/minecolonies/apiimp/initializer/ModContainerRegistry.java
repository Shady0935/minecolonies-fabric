package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.inventory.container.ContainerBuildingInventory;
import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.api.inventory.container.ContainerGrave;
import com.minecolonies.api.inventory.container.ContainerRack;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.fabric.common.extensions.IForgeMenuType;
import com.minecolonies.fabric.registry.FabricDeferredRegister;
import com.minecolonies.fabric.registry.FabricRegistries;
import net.minecraft.world.inventory.MenuType;

/**
 * Common menu registration.  Keeping this separate from the client screen
 * registration prevents a dedicated server from resolving client GUI classes.
 */
public final class ModContainerRegistry
{
    public static final FabricDeferredRegister<MenuType<?>> CONTAINERS =
      FabricDeferredRegister.create(FabricRegistries.MENU_TYPES, Constants.MOD_ID);

    static
    {
        ModContainers.craftingFurnace = CONTAINERS.register("crafting_furnace", () -> IForgeMenuType.create(ContainerCraftingFurnace::fromFriendlyByteBuf));
        ModContainers.buildingInv = CONTAINERS.register("building_inv", () -> IForgeMenuType.create(ContainerBuildingInventory::fromFriendlyByteBuf));
        ModContainers.citizenInv = CONTAINERS.register("citizen_inv", () -> IForgeMenuType.create(ContainerCitizenInventory::fromFriendlyByteBuf));
        ModContainers.craftingGrid = CONTAINERS.register("crafting_building", () -> IForgeMenuType.create(ContainerCrafting::fromFriendlyByteBuf));
        ModContainers.rackInv = CONTAINERS.register("rack_inv", () -> IForgeMenuType.create(ContainerRack::fromFriendlyByteBuf));
        ModContainers.graveInv = CONTAINERS.register("grave_inv", () -> IForgeMenuType.create(ContainerGrave::fromFriendlyByteBuf));
        ModContainers.craftingBrewingstand = CONTAINERS.register("crafting_brewingstand", () -> IForgeMenuType.create(ContainerCraftingBrewingstand::fromFriendlyByteBuf));
    }

    private ModContainerRegistry()
    {
    }
}
