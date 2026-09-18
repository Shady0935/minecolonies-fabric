package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.network.IMessage;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.dist.Dist;
import com.minecolonies.fabric.dist.OnlyIn;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import com.minecolonies.fabric.registry.FabricRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Asks the client to play a specific music
 */
public class PlayMusicAtPosMessage implements IMessage
{
    /**
     * The sound event to play.
     */
    private SoundEvent soundEvent;

    /**
     * The position to play at
     */
    private BlockPos pos;

    /**
     * The dimension id to play in
     */
    private ResourceKey<Level> dimensionID;

    /**
     * The volume to use
     */
    private float volume;

    /**
     * pitch to use
     */
    private float pitch;

    /**
     * Default constructor.
     */
    public PlayMusicAtPosMessage()
    {
        super();
    }

    /**
     * Create a play music message with a specific sound event.
     *
     * @param event the sound event.
     */
    public PlayMusicAtPosMessage(final SoundEvent event, final BlockPos pos, final Level world, final float volume, final float pitch)
    {
        super();
        this.soundEvent = event;
        this.pos = pos;
        this.dimensionID = world.dimension();
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(FabricRegistries.SOUND_EVENTS.getKey(this.soundEvent));
        buf.writeBlockPos(pos);
        buf.writeUtf(dimensionID.location().toString());
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.soundEvent = FabricRegistries.SOUND_EVENTS.getValue(buf.readResourceLocation());
        this.pos = buf.readBlockPos();
        this.dimensionID = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        ClientMessageBridge.invoke("handlePlayMusicAtPosMessage",
          new Class<?>[] {SoundEvent.class, BlockPos.class, ResourceKey.class, float.class, float.class},
          soundEvent, pos, dimensionID, volume, pitch);
    }
}
