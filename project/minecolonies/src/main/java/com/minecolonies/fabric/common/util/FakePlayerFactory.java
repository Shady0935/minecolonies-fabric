package com.minecolonies.fabric.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;

public final class FakePlayerFactory
{
    private FakePlayerFactory()
    {
    }

    public static FakePlayer get(final ServerLevel level, final GameProfile profile)
    {
        return new FakePlayer(level, profile);
    }

    public static FakePlayer getMinecraft(final ServerLevel level)
    {
        return get(level, new GameProfile(java.util.UUID.nameUUIDFromBytes("minecolonies-fake-player".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "MineColonies"));
    }
}
