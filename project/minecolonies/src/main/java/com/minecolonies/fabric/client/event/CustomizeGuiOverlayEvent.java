package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.event.Event;

import java.util.List;

/** Debug-overlay hook retained for the common event handlers. */
public final class CustomizeGuiOverlayEvent
{
    private CustomizeGuiOverlayEvent() { }

    public static final class DebugText extends Event
    {
        private final List<String> left;

        public DebugText(final List<String> left)
        {
            this.left = left;
        }

        public List<String> getLeft()
        {
            return left;
        }
    }
}
