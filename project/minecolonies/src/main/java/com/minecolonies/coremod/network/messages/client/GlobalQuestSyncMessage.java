package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * The message used to synchronize global quest data from a server to a remote client.
 */
public class GlobalQuestSyncMessage implements IMessage
{

    /**
     * The buffer with the data.
     */
    private FriendlyByteBuf questBuffer;

    /**
     * Empty constructor used when registering the message
     */
    public GlobalQuestSyncMessage()
    {
        super();
    }

    /**
     * Add or Update QuestData on the client.
     *
     * @param buf the bytebuffer.
     */
    public GlobalQuestSyncMessage(final FriendlyByteBuf buf)
    {
        this.questBuffer = new FriendlyByteBuf(buf.copy());
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        questBuffer = new FriendlyByteBuf(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        questBuffer.resetReaderIndex();
        buf.writeBytes(questBuffer);
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
            bridge.getMethod("handleGlobalQuestSyncMessage", FriendlyByteBuf.class)
              .invoke(null, questBuffer);
        }
        catch (final ClassNotFoundException ignored)
        {
            // Client-bound packet; the client bridge is intentionally absent from dedicated-server execution.
        }
        catch (final NoSuchMethodException | IllegalAccessException exception)
        {
            throw new IllegalStateException("MineColonies client quest bridge is unavailable", exception);
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
            throw new IllegalStateException("MineColonies client quest sync failed", cause);
        }
        finally
        {
            if (questBuffer != null)
            {
                questBuffer.release();
            }
        }
    }
}
