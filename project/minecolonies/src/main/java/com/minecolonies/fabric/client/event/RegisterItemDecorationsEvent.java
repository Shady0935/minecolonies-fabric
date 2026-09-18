package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.client.IItemDecorator;
import com.minecolonies.fabric.event.Event;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

/** Client item-decoration registration bridge. */
public final class RegisterItemDecorationsEvent extends Event
{
    private final Map<Item, IItemDecorator> decorators = new LinkedHashMap<>();

    public void register(final Item item, final IItemDecorator decorator)
    {
        decorators.put(item, decorator);
    }

    public Map<Item, IItemDecorator> getDecorators()
    {
        return Map.copyOf(decorators);
    }
}
