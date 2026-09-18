package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.common.MinecraftForge;
import com.minecolonies.fabric.event.Event;
import com.minecolonies.fabric.event.entity.living.MobSpawnEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Emits the retained Forge position-check event at vanilla's spawn-rule gate. */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin
{
    @Redirect(
      method = {
        "isValidPositionForMob(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;D)Z",
        "spawnMobsForChunkGeneration(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/util/RandomSource;)V"
      },
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Mob;checkSpawnRules(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"))
    private static boolean minecolonies$onPositionCheck(final Mob mob, final LevelAccessor level,
      final MobSpawnType spawnType)
    {
        final MobSpawnEvent.PositionCheck event = new MobSpawnEvent.PositionCheck(
          mob, level, mob.getX(), mob.getY(), mob.getZ(), spawnType);
        if (MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Event.Result.DENY)
        {
            return false;
        }
        return mob.checkSpawnRules(level, spawnType);
    }
}
