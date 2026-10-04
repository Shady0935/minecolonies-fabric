package com.minecolonies.fabric.mixin;

import com.minecolonies.fabric.capability.CapabilityHooks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces Forge's automatic chunk-capability NBT persistence with a namespaced chunk tag. */
@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin
{
    private static final String MINECOLONIES_CLAIMS = "minecolonies:claims";

    @Inject(method = "write", at = @At("RETURN"))
    private static void minecolonies$writeClaims(final ServerLevel level, final ChunkAccess access,
      final CallbackInfoReturnable<CompoundTag> cir)
    {
        final LevelChunk chunk = access instanceof LevelChunk full ? full
          : access instanceof ImposterProtoChunk wrapped ? wrapped.getWrapped() : null;
        if (chunk != null)
        {
            cir.getReturnValue().put(MINECOLONIES_CLAIMS, CapabilityHooks.writeChunkData(chunk));
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void minecolonies$readClaims(final ServerLevel level, final PoiManager poi,
      final ChunkPos pos, final CompoundTag tag, final CallbackInfoReturnable<ProtoChunk> cir)
    {
        // Full chunks are wrapped during disk loading; restore before Fabric's CHUNK_LOAD callback.
        if (cir.getReturnValue() instanceof ImposterProtoChunk wrapped && tag.contains(MINECOLONIES_CLAIMS, Tag.TAG_COMPOUND))
        {
            CapabilityHooks.readChunkData(wrapped.getWrapped(), tag.getCompound(MINECOLONIES_CLAIMS));
        }
    }
}
