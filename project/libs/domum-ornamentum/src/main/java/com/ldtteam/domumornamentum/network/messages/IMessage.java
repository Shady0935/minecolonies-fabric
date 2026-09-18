package com.ldtteam.domumornamentum.network.messages;

import net.minecraft.network.FriendlyByteBuf;

/** Small common contract for the 1.20.1 Fabric packet encoders. */
public interface IMessage
{
    void toBytes(FriendlyByteBuf buf);
}
