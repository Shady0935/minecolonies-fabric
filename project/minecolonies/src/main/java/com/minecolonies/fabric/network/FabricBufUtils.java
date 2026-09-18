package com.minecolonies.fabric.network;

import com.minecolonies.fabric.registry.FabricRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Registry-id packet helpers replacing Forge's FriendlyByteBuf extensions. */
public final class FabricBufUtils
{
    private FabricBufUtils()
    {
    }

    public static <T> void writeRegistryId(final FriendlyByteBuf buffer, final FabricRegistry<T> registry, final T value)
    {
        final ResourceLocation id = registry.getKey(value);
        buffer.writeResourceLocation(id == null ? registry.getDefaultKey() : id);
    }

    public static <T> void writeRegistryIdUnsafe(final FriendlyByteBuf buffer, final FabricRegistry<T> registry, final T value)
    {
        writeRegistryId(buffer, registry, value);
    }

    public static <T> T readRegistryId(final FriendlyByteBuf buffer, final FabricRegistry<T> registry)
    {
        return registry.getValue(buffer.readResourceLocation());
    }

    public static <T> T readRegistryIdUnsafe(final FriendlyByteBuf buffer, final FabricRegistry<T> registry)
    {
        return readRegistryId(buffer, registry);
    }

    public static <T> T readRegistryIdSafe(final FriendlyByteBuf buffer, final FabricRegistry<T> registry)
    {
        return readRegistryId(buffer, registry);
    }
}
