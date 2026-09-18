package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.network.IMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

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
            final Class<?> bridge = Class.forName("com.minecolonies.fabric.client.network.ClientNetworkHooks");
            bridge.getMethod("handleUpdateClientWithCompatibilityMessage", FriendlyByteBuf.class)
              .invoke(null, this.buffer);
        }
        catch (final ClassNotFoundException ignored)
        {
            // Client-bound packet; the client bridge is intentionally absent from dedicated-server execution.
        }
        catch (final NoSuchMethodException | IllegalAccessException exception)
        {
            throw new IllegalStateException("MineColonies client compatibility bridge is unavailable", exception);
        }
        catch (final InvocationTargetException exception)
        {
            final Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException)
            {
                throw runtimeException;
            }
            if (cause instanceof Error error)
            {
                throw error;
            }
            throw new IllegalStateException("MineColonies client compatibility update failed", cause);
        }
        finally
        {
            if (this.buffer != null)
            {
                this.buffer.release();
            }
        }
    }
}
