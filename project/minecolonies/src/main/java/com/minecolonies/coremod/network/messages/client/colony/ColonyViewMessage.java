package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.network.IMessage;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import com.minecolonies.fabric.dist.Dist;
import com.minecolonies.fabric.dist.OnlyIn;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewMessage implements IMessage
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * If this is a new subscription.
     */
    private boolean isNewSubscription;

    /**
     * The buffer with the data.
     */
    private FriendlyByteBuf colonyBuffer;

    /**
     * The dimension of the colony.
     */
    private ResourceKey<Level> dim;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewMessage()
    {
        super();
    }

    /**
     * Add or Update a ColonyView on the client.
     *
     * @param colonyId colony id of the view to update.
     * @param dimension dimension of the colony.
     * @param buf       the bytebuffer.
     */
    public ColonyViewMessage(final int colonyId, final ResourceKey<Level> dimension, final FriendlyByteBuf buf)
    {
        this.colonyId = colonyId;
        this.dim = dimension;
        this.colonyBuffer = new FriendlyByteBuf(buf.copy());
    }

    /**
     * Set whether the message is a new subscription(full view)
     *
     * @param newSubscription
     */
    public void setIsNewSubscription(boolean newSubscription)
    {
        isNewSubscription = newSubscription;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        isNewSubscription = buf.readBoolean();
        dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        colonyBuffer = new FriendlyByteBuf(buf.readBytes(buf.readableBytes()));
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyBuffer.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeBoolean(isNewSubscription);
        buf.writeUtf(dim.location().toString());
        buf.writeBytes(colonyBuffer);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        try
        {
            final Class<?> bridge = Class.forName("com.minecolonies.fabric.client.network.ClientNetworkHooks");
            bridge.getMethod("handleColonyViewMessage", int.class, FriendlyByteBuf.class, boolean.class, ResourceKey.class)
              .invoke(null, colonyId, colonyBuffer, isNewSubscription, dim);
        }
        catch (ClassNotFoundException ignored)
        {
            // The message is client-bound and is never executed on a dedicated server.
        }
        catch (NoSuchMethodException | IllegalAccessException exception)
        {
            throw new IllegalStateException("Unable to dispatch the MineColonies colony view to Fabric client hooks", exception);
        }
        catch (InvocationTargetException exception)
        {
            final Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            if (cause instanceof RuntimeException runtimeException)
            {
                throw runtimeException;
            }
            throw new IllegalStateException("Fabric client colony view dispatch failed", cause);
        }
        finally
        {
            colonyBuffer.release();
        }
    }
}
