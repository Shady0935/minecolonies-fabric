package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minecolonies.fabric.network.ClientMessageBridge;

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
            ClientMessageBridge.invoke("handleGlobalQuestSyncMessage",
              new Class<?>[] {FriendlyByteBuf.class}, questBuffer);
        }
        finally
        {
            if (questBuffer != null) questBuffer.release();
        }
    }
}
