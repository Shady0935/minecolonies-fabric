package com.minecolonies.fabric.common;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

import java.util.List;

/** Fabric-side replacement for Forge's item action extension. */
public final class ToolActions
{
    public static final ToolAction AXE_DIG = new ToolAction("axe_dig");
    public static final ToolAction SHOVEL_DIG = new ToolAction("shovel_dig");
    public static final ToolAction PICKAXE_DIG = new ToolAction("pickaxe_dig");
    public static final ToolAction HOE_DIG = new ToolAction("hoe_dig");
    public static final ToolAction HOE_TILL = new ToolAction("hoe_till");
    public static final ToolAction HOE_HARVEST = new ToolAction("hoe_harvest");
    public static final ToolAction SWORD_SWEEP = new ToolAction("sword_sweep");
    public static final ToolAction FISHING_ROD_CAST = new ToolAction("fishing_rod_cast");
    public static final ToolAction SHEARS_DIG = new ToolAction("shears_dig");
    public static final ToolAction SHEARS_HARVEST = new ToolAction("shears_harvest");
    public static final ToolAction SHIELD_BLOCK = new ToolAction("shield_block");
    public static final List<ToolAction> DEFAULT_HOE_ACTIONS = List.of(HOE_DIG, HOE_TILL, HOE_HARVEST);

    private ToolActions()
    {
    }

    public static boolean canPerformAction(final ItemStack stack, final ToolAction action)
    {
        if (stack == null || stack.isEmpty()) return false;
        final var item = stack.getItem();
        return action == AXE_DIG && item instanceof AxeItem
          || action == SHOVEL_DIG && item instanceof ShovelItem
          || action == PICKAXE_DIG && item instanceof PickaxeItem
          || (action == HOE_DIG || action == HOE_TILL || action == HOE_HARVEST) && item instanceof HoeItem
          || action == SWORD_SWEEP && item instanceof SwordItem
          || action == FISHING_ROD_CAST && item instanceof FishingRodItem
          || (action == SHEARS_DIG || action == SHEARS_HARVEST) && item instanceof ShearsItem;
    }
}
