package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.fabric.LogicalSide;
import com.minecolonies.fabric.network.ClientMessageBridge;
import com.minecolonies.fabric.network.NetworkEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Opens a citizen interaction window after its server-authoritative view has arrived. */
public class OpenCitizenWindowMessage implements IMessage
{
    private int colonyId;
    private int citizenId;
    private ResourceKey<Level> dimension;

    public OpenCitizenWindowMessage()
    {
    }

    public OpenCitizenWindowMessage(final int colonyId, final int citizenId, final ResourceKey<Level> dimension)
    {
        this.colonyId = colonyId;
        this.citizenId = citizenId;
        this.dimension = dimension;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
        buf.writeUtf(dimension.location().toString());
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
        ClientMessageBridge.invoke("handleOpenCitizenWindowMessage",
          new Class<?>[] {int.class, int.class, ResourceKey.class}, colonyId, citizenId, dimension);
    }
}
