package com.minecolonies.fabric.common.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import com.mojang.authlib.GameProfile;

/** Server player identity used for worker actions that require a player context. */
public class FakePlayer extends ServerPlayer
{
    public FakePlayer(final ServerLevel level, final GameProfile profile)
    {
        super(level.getServer(), level, profile);
    }
}
