package com.minecolonies.fabric.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Adapter for the small tag lookup surface used by the upstream code. */
public final class FabricRegistryTags<T>
{
    private final Registry<T> registry;

    FabricRegistryTags(final Registry<T> registry)
    {
        this.registry = registry;
    }

    public List<T> getTag(final TagKey<T> tag)
    {
        return registry.getTag(tag)
          .map(named -> named.stream().map(Holder::value).toList())
          .orElseGet(List::of);
    }

    public TagKey<T> createTagKey(final ResourceLocation id)
    {
        return TagKey.create(registry.key(), id);
    }

    public Optional<ReverseTag<T>> getReverseTag(final T value)
    {
        // Reverse tag lookup is diagnostic-only in the upstream auditor.  The
        // vanilla registry exposes forward tag lookup, so leave the optional
        // empty until that audit is migrated to Fabric's tag entry API.
        return Optional.empty();
    }

    public record ReverseTag<T>(Stream<TagKey<T>> tagKeys)
    {
        public Stream<TagKey<T>> getTagKeys()
        {
            return tagKeys;
        }
    }
}
