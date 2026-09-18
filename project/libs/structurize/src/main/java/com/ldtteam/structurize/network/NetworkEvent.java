package com.ldtteam.structurize.network;

import net.minecraft.server.level.ServerPlayer;

/** Minimal context passed to message handlers, backed by Fabric networking. */
public final class NetworkEvent
{
    private NetworkEvent() { }

    public static final class Context
    {
        private final ServerPlayer sender;
        private final LogicalSide originationSide;

        public Context(final ServerPlayer sender, final LogicalSide originationSide)
        {
            this.sender = sender;
            this.originationSide = originationSide;
        }

        public ServerPlayer getSender()
        {
            return sender;
        }

        public Direction getDirection()
        {
            return new Direction(originationSide);
        }

        public void enqueueWork(final Runnable action)
        {
            action.run();
        }

        public void setPacketHandled(final boolean handled)
        {
            // Fabric consumes the packet when the receiver returns.
        }
    }

    public record Direction(LogicalSide getOriginationSide) { }
}
