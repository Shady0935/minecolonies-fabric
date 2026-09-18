package net.minecraftforge.entity;

import net.minecraft.network.FriendlyByteBuf;

/** Spawn payload contract used by the upstream projectile entity. */
public interface IEntityAdditionalSpawnData
{
    void writeSpawnData(FriendlyByteBuf buffer);

    void readSpawnData(FriendlyByteBuf buffer);
}
