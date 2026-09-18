package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.network.IMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minecolonies.fabric.network.ClientMessageBridge;

/**
 * Message to update the recipes on the client side.
 */
public class UpdateClientWithCompatibilityMessage implements IMessage
{
    private FriendlyByteBuf buffer;

    /**
     * Empty public constructor.
     */
    public UpdateClientWithCompatibilityMessage()
    {
        super();
    }

    /**
     * Message creation.
     *
     * @param dummy just pass true to initialize the message for sending.
     */
    public UpdateClientWithCompatibilityMessage(final boolean dummy)
    {
        super();

        this.buffer = new FriendlyByteBuf(Unpooled.buffer());
        IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().serialize(this.buffer);
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.buffer = new FriendlyByteBuf(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.buffer.resetReaderIndex();
        buf.writeBytes(this.buffer);
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
        try
        {
            ClientMessageBridge.invoke("handleUpdateClientWithCompatibilityMessage",
              new Class<?>[] {FriendlyByteBuf.class}, this.buffer);
        }
        finally
        {
            if (this.buffer != null) this.buffer.release();
        }
    }
}
