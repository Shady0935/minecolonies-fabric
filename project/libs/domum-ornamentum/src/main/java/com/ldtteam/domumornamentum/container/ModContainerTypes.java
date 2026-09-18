package com.ldtteam.domumornamentum.container;

import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

/** Eager Fabric menu registrations. */
public final class ModContainerTypes
{
    public static final Supplier<MenuType<ArchitectsCutterContainer>> ARCHITECTS_CUTTER = register(
        "architects_cutter", new MenuType<>(ArchitectsCutterContainer::new, FeatureFlagSet.of()));

    private ModContainerTypes()
    {
    }

    public static void init()
    {
    }

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> Supplier<MenuType<T>> register(
        final String name, final MenuType<T> type)
    {
        final MenuType<T> value = Registry.register(BuiltInRegistries.MENU, new ResourceLocation(Constants.MOD_ID, name), type);
        return () -> value;
    }
}
