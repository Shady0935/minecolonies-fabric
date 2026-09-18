package com.minecolonies.api.tileentities;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.minecolonies.fabric.registry.FabricRegistryObject;

public class MinecoloniesTileEntities
{
    public static FabricRegistryObject<BlockEntityType<? extends AbstractTileEntityScarecrow>> SCARECROW;

    public static FabricRegistryObject<BlockEntityType<? extends AbstractTileEntityPlantationField>> PLANTATION_FIELD;

    public static FabricRegistryObject<BlockEntityType<? extends AbstractTileEntityBarrel>> BARREL;

    public static FabricRegistryObject<BlockEntityType<? extends AbstractTileEntityColonyBuilding>> BUILDING;

    public static FabricRegistryObject<BlockEntityType<? extends BlockEntity>> DECO_CONTROLLER;

    public static FabricRegistryObject<BlockEntityType<TileEntityRack>> RACK;

    public static FabricRegistryObject<BlockEntityType<TileEntityGrave>> GRAVE;

    public static FabricRegistryObject<BlockEntityType<? extends TileEntityNamedGrave>> NAMED_GRAVE;

    public static FabricRegistryObject<BlockEntityType<? extends AbstractTileEntityWareHouse>> WAREHOUSE;

    public static FabricRegistryObject<BlockEntityType<? extends BlockEntity>> COMPOSTED_DIRT;

    public static FabricRegistryObject<BlockEntityType<TileEntityEnchanter>> ENCHANTER;

    public static FabricRegistryObject<BlockEntityType<TileEntityStash>> STASH;

    public static FabricRegistryObject<BlockEntityType<TileEntityColonyFlag>> COLONY_FLAG;
}