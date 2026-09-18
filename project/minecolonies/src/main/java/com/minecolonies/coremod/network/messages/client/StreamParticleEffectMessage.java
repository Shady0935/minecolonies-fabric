package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import com.minecolonies.fabric.registry.FabricRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Handles spawning item particle effects in a stream between two targets.
 */
public class StreamParticleEffectMessage implements IMessage
{
    /**
     * Random obj.
     */
    /**
     * The itemStack for the particles.
     */
    private SimpleParticleType type;

    /**
     * The start position.
     */
    private double sPosX;
    private double sPosY;
    private double sPosZ;

    /**
     * The end position.
     */
    private double ePosX;
    private double ePosY;
    private double ePosZ;

    /**
     * The stage of the transfer.
     */
    private int stage;

    /**
     * The max stage of the transfer.
     */
    private int maxStage;

    /**
     * Empty constructor used when registering the
     */
    public StreamParticleEffectMessage()
    {
        super();
    }

    /**
     * Start a particle stream.
     *
     * @param start    the starting position.
     * @param end      the end position.
     * @param type     the particle type.
     * @param stage    the stage we're at
     * @param maxStage the max stage
     */
    public StreamParticleEffectMessage(final Vec3 start, final Vec3 end, final SimpleParticleType type, final int stage, final int maxStage)
    {
        super();
        this.sPosX = start.x;
        this.sPosY = start.y - 0.5;
        this.sPosZ = start.z;

        this.ePosX = end.x;
        this.ePosY = end.y - 0.5;
        this.ePosZ = end.z;

        this.stage = stage;
        this.maxStage = maxStage;

        this.type = type;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.sPosX = buf.readDouble();
        this.sPosY = buf.readDouble();
        this.sPosZ = buf.readDouble();

        this.ePosX = buf.readDouble();
        this.ePosY = buf.readDouble();
        this.ePosZ = buf.readDouble();

        this.stage = buf.readInt();
        this.maxStage = buf.readInt();
        this.type = (SimpleParticleType) FabricRegistries.PARTICLE_TYPES.getValue(buf.readResourceLocation());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeDouble(this.sPosX);
        buf.writeDouble(this.sPosY);
        buf.writeDouble(this.sPosZ);

        buf.writeDouble(this.ePosX);
        buf.writeDouble(this.ePosY);
        buf.writeDouble(this.ePosZ);

        buf.writeInt(this.stage);
        buf.writeInt(this.maxStage);
        buf.writeResourceLocation(FabricRegistries.PARTICLE_TYPES.getKey(this.type));
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
        ClientMessageBridge.invoke("handleStreamParticleEffectMessage",
          new Class<?>[] {SimpleParticleType.class, double.class, double.class, double.class, double.class, double.class,
            double.class, int.class, int.class},
          type, sPosX, sPosY, sPosZ, ePosX, ePosY, ePosZ, stage, maxStage);
    }
}
