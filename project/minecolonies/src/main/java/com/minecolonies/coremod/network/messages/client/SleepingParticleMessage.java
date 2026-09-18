package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for sleeping particles
 */
public class SleepingParticleMessage implements IMessage
{
    /**
     * Position the particles spawn at
     */
    private double x;
    private double y;
    private double z;

    public SleepingParticleMessage()
    {
        super();
    }

    public SleepingParticleMessage(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(final FriendlyByteBuf byteBuf)
    {
        x = byteBuf.readDouble();
        y = byteBuf.readDouble();
        z = byteBuf.readDouble();
    }

    @Override
    public void toBytes(final FriendlyByteBuf byteBuf)
    {
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
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
        ClientMessageBridge.invoke("handleSleepingParticleMessage",
          new Class<?>[] {double.class, double.class, double.class}, x, y, z);
    }
}
