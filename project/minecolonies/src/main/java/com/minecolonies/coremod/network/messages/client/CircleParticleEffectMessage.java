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
 * Handles spawning item particle effects in a circle around a target.
 */
public class CircleParticleEffectMessage implements IMessage
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
    private double posX;
    private double posY;
    private double posZ;

    /**
     * The current stage of the circle.
     */
    private int stage;

    /**
     * Empty constructor used when registering the
     */
    public CircleParticleEffectMessage()
    {
        super();
    }

    /**
     * Start a circling particle effect.
     *
     * @param pos   the position.
     * @param type  the particle type.
     * @param stage the stage.
     */
    public CircleParticleEffectMessage(final Vec3 pos, final SimpleParticleType type, final int stage)
    {
        super();
        this.posX = pos.x;
        this.posY = pos.y - 0.5;
        this.posZ = pos.z;
        this.stage = stage;
        this.type = type;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.stage = buf.readInt();
        this.type = (SimpleParticleType) FabricRegistries.PARTICLE_TYPES.getValue(buf.readResourceLocation());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeInt(this.stage);
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
        ClientMessageBridge.invoke("handleCircleParticleEffectMessage",
          new Class<?>[] {SimpleParticleType.class, double.class, double.class, double.class, int.class},
          type, posX, posY, posZ, stage);
    }
}
