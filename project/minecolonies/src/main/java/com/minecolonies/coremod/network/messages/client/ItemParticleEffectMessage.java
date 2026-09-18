package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Handles spawning item particle effects close to an entity.
 */
public class ItemParticleEffectMessage implements IMessage
{
    /**
     * Random obj.
     */
    /**
     * The itemStack for the particles.
     */
    private ItemStack stack;

    /**
     * The entity rotation pitch.
     */
    private double rotationPitch;

    /**
     * The entity rotation yaw.
     */
    private double rotationYaw;

    /**
     * The entity eye height.
     */
    private double eyeHeight;

    /**
     * The entity position.
     */
    private double posX;
    private double posY;
    private double posZ;

    /**
     * Empty constructor used when registering the
     */
    public ItemParticleEffectMessage()
    {
        super();
    }

    /**
     * Constructor to trigger an item particle message for eating.
     *
     * @param stack         the stack.
     * @param posX          the double x pos.
     * @param posY          the double y pos.
     * @param posZ          the double z pos.
     * @param rotationPitch the rotation pitch.
     * @param rotationYaw   the rotation yaw.
     * @param eyeHeight     the eye height.
     */
    public ItemParticleEffectMessage(
      final ItemStack stack,
      final double posX,
      final double posY,
      final double posZ,
      final double rotationPitch,
      final double rotationYaw,
      final double eyeHeight)
    {
        this.stack = stack;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.rotationPitch = rotationPitch;
        this.rotationYaw = rotationYaw;
        this.eyeHeight = eyeHeight;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        stack = buf.readItem();
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
        rotationPitch = buf.readDouble();
        rotationYaw = buf.readDouble();
        eyeHeight = buf.readDouble();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(stack);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
        buf.writeDouble(rotationPitch);
        buf.writeDouble(rotationYaw);
        buf.writeDouble(eyeHeight);
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
        ClientMessageBridge.invoke("handleItemParticleEffectMessage",
          new Class<?>[] {ItemStack.class, double.class, double.class, double.class, double.class, double.class, double.class},
          stack, posX, posY, posZ, rotationPitch, rotationYaw, eyeHeight);
    }
}
