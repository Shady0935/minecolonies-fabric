package com.minecolonies.api.inventory;

import com.minecolonies.api.inventory.container.*;
import net.minecraft.world.inventory.MenuType;
import com.minecolonies.fabric.registry.FabricRegistryObject;

public class ModContainers
{
    public static FabricRegistryObject<MenuType<ContainerCraftingFurnace>> craftingFurnace;

    public static FabricRegistryObject<MenuType<ContainerBuildingInventory>> buildingInv;

    public static FabricRegistryObject<MenuType<ContainerCitizenInventory>> citizenInv;

    public static FabricRegistryObject<MenuType<ContainerRack>> rackInv;

    public static FabricRegistryObject<MenuType<ContainerGrave>> graveInv;

    public static FabricRegistryObject<MenuType<ContainerCrafting>> craftingGrid;

    public static FabricRegistryObject<MenuType<ContainerCraftingBrewingstand>> craftingBrewingstand;
}
