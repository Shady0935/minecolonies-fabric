package com.ldtteam.domumornamentum.network.messages;

import com.ldtteam.domumornamentum.client.screens.ArchitectsCutterScreen;
import com.ldtteam.domumornamentum.container.ArchitectsCutterContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Sets an architect's cutter input slot from the creative-only client helper. */
public final class CreativeSetArchitectCutterSlotMessage implements IMessage
{
    private final int slot;
    private final ItemStack stack;

    public CreativeSetArchitectCutterSlotMessage(final int slot, @NotNull final ItemStack stack)
    {
        this.slot = slot;
        this.stack = stack.copy();
    }

    public CreativeSetArchitectCutterSlotMessage(@NotNull final FriendlyByteBuf buf)
    {
        this.slot = buf.readVarInt();
        this.stack = buf.readItem();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeVarInt(slot);
        buf.writeItem(stack);
    }

    public void apply(final ServerPlayer player)
    {
        if (player.isCreative() && player.containerMenu instanceof ArchitectsCutterContainer menu
            && slot >= 0 && slot < menu.slots.size())
        {
            final Slot menuSlot = menu.slots.get(slot);
            if (menuSlot.isActive() && menuSlot.allowModification(player) && menuSlot.mayPlace(stack))
            {
                menuSlot.setByPlayer(stack);
            }
        }
    }
}
