package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Handles spawning item particle effects on top of a block..
 */
public class LocalizedParticleEffectMessage implements IMessage
{
    /**
     * Random obj.
     */
    /**
     * The itemStack for the particles.
     */
    private ItemStack stack;

    /**
     * The entity position.
     */
    private double posX;
    private double posY;
    private double posZ;

    /**
     * Empty constructor used when registering the
     */
    public LocalizedParticleEffectMessage()
    {
        super();
    }

    /**
     * Constructor to trigger an item particle message for crushing.
     *
     * @param stack the stack.
     * @param pos   the pos.
     */
    public LocalizedParticleEffectMessage(final ItemStack stack, final BlockPos pos)
    {
        super();
        this.stack = stack;
        this.posX = pos.getX() + 0.5;
        this.posY = pos.getY() + 0.5;
        this.posZ = pos.getZ() + 0.5;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        stack = buf.readItem();
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(stack);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
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
        ClientMessageBridge.invoke("handleLocalizedParticleEffectMessage",
          new Class<?>[] {ItemStack.class, double.class, double.class, double.class}, stack, posX, posY, posZ);
    }
}
