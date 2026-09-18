package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.SimpleParticleType;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import com.minecolonies.fabric.registry.FabricRegistries;
import org.jetbrains.annotations.Nullable;


/**
 * Message for vanilla particles around a citizen, in villager-like shape.
 */
public class VanillaParticleMessage implements IMessage
{
    /**
     * Citizen Position
     */
    private double x;
    private double y;
    private double z;

    /**
     * Particle id
     */
    private SimpleParticleType type;

    public VanillaParticleMessage() {super();}

    public VanillaParticleMessage(final double x, final double y, final double z, final SimpleParticleType type)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    @Override
    public void fromBytes(final FriendlyByteBuf byteBuf)
    {
        x = byteBuf.readDouble();
        y = byteBuf.readDouble();
        z = byteBuf.readDouble();
        this.type = (SimpleParticleType) FabricRegistries.PARTICLE_TYPES.getValue(byteBuf.readResourceLocation());
    }

    @Override
    public void toBytes(final FriendlyByteBuf byteBuf)
    {
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
        byteBuf.writeResourceLocation(FabricRegistries.PARTICLE_TYPES.getKey(this.type));
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
        ClientMessageBridge.invoke("handleVanillaParticleMessage",
          new Class<?>[] {double.class, double.class, double.class, SimpleParticleType.class}, x, y, z, type);
    }
}
