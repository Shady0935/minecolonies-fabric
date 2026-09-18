package com.ldtteam.domumornamentum.client.model.data;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Map;
import java.util.Objects;

/** NBT-backed replacement for Forge's {@code INBTSerializable} model data. */
public class MaterialTextureData
{
    public static final MaterialTextureData EMPTY = new MaterialTextureData();

    private final Map<ResourceLocation, Block> texturedComponents;

    public MaterialTextureData()
    {
        this.texturedComponents = Maps.newHashMap();
    }

    public MaterialTextureData(final Map<ResourceLocation, Block> texturedComponents) {
        this.texturedComponents = texturedComponents;
    }

    public Map<ResourceLocation, Block> getTexturedComponents()
    {
        return texturedComponents;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final MaterialTextureData that))
        {
            return false;
        }
        return Objects.equals(getTexturedComponents(), that.getTexturedComponents());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getTexturedComponents());
    }

    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = new CompoundTag();

        if (this == EMPTY)
            return nbt;

        this.getTexturedComponents().forEach((key, value) -> nbt.putString(key.toString(), Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(value)).toString()));

        return nbt;
    }

    public void deserializeNBT(final CompoundTag nbt)
    {
        this.texturedComponents.clear();

        nbt.getAllKeys().forEach(key -> {
            final ResourceLocation name = new ResourceLocation(nbt.getString(key));

            if (BuiltInRegistries.BLOCK.get(name) != Blocks.AIR)
            {
                this.texturedComponents.put(new ResourceLocation(key), BuiltInRegistries.BLOCK.get(name));
            }
        });
    }

    public static MaterialTextureData deserializeFromNBT(final CompoundTag nbt) {
        if (nbt.getAllKeys().isEmpty())
            return EMPTY;

        final MaterialTextureData newData = new MaterialTextureData();
        newData.deserializeNBT(nbt);
        return newData;
    }

    public boolean isEmpty()
    {
        return this.equals(EMPTY);
    }

    @Override
    public String toString()
    {
        return "MaterialTextureData{" +
                 "texturedComponents=" + texturedComponents +
                 '}';
    }
}
