package com.minecolonies.fabric.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the vanilla container origin to optional JEI ghost-slot integration. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor
{
    @Accessor("leftPos")
    int minecolonies$getLeftPos();

    @Accessor("topPos")
    int minecolonies$getTopPos();
}
