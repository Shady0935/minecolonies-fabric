package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.event.Event;
import com.minecolonies.fabric.event.level.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Emits the retained Forge farmland-trample event at the block mutation point. */
@Mixin(FarmBlock.class)
public abstract class FarmBlockMixin
{
    @Redirect(
      method = "fallOn",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/FarmBlock;turnToDirt(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void minecolonies$onFarmlandTrample(final Entity entity, final BlockState state, final Level level, final BlockPos pos)
    {
        if (level.isClientSide())
        {
            FarmBlock.turnToDirt(entity, state, level, pos);
            return;
        }

        final BlockEvent.FarmlandTrampleEvent event = new BlockEvent.FarmlandTrampleEvent(level, pos, state, entity);
        if (!MinecraftForge.EVENT_BUS.post(event) && event.getResult() != Event.Result.DENY)
        {
            FarmBlock.turnToDirt(entity, state, level, pos);
        }
    }
}
