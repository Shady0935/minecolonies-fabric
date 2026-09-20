package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.inventory.ContainerUpdateHooks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Emits MineColonies request updates when a vanilla loot-capable container receives an item. */
@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizableContainerBlockEntityMixin
{
    @Inject(method = "setItem", at = @At("TAIL"))
    private void minecolonies$onSetItem(final int slot, final ItemStack stack, final CallbackInfo callbackInfo)
    {
        ContainerUpdateHooks.notifyRegisteredContainerChanged((BlockEntity) (Object) this);
    }
}
