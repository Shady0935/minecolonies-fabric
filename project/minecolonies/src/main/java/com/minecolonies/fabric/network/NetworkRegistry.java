package com.minecolonies.fabric.network;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.fabric.network.simple.SimpleChannel;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NetworkRegistry
{
    private NetworkRegistry()
    {
    }

    public static SimpleChannel newSimpleChannel(final ResourceLocation id,
                                                  final Supplier<String> protocol,
                                                  final Predicate<String> clientAccepted,
                                                  final Predicate<String> serverAccepted)
    {
        return new SimpleChannel(id);
    }
}
