package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Opens the Town Hall screen chosen by the authoritative server state. */
public class OpenTownHallWindowMessage implements IMessage
{
    private BlockPos pos;
    private boolean registeredBuilding;
    private boolean openInventory;

    public OpenTownHallWindowMessage()
    {
    }

    public OpenTownHallWindowMessage(final BlockPos pos, final boolean registeredBuilding, final boolean openInventory)
    {
        this.pos = pos;
        this.registeredBuilding = registeredBuilding;
        this.openInventory = openInventory;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        registeredBuilding = buf.readBoolean();
        openInventory = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeBoolean(registeredBuilding);
        buf.writeBoolean(openInventory);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context context, final boolean isLogicalServer)
    {
        ClientMessageBridge.invoke("handleOpenTownHallWindowMessage",
          new Class<?>[] {BlockPos.class, boolean.class, boolean.class}, pos, registeredBuilding, openInventory);
    }
}
