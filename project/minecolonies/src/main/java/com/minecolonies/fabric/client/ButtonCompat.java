package com.minecolonies.fabric.client;

import net.minecraft.client.gui.components.Button;

/** Public equivalent of Forge-era code's protected Button narration constant. */
public final class ButtonCompat
{
    public static final Button.CreateNarration DEFAULT_NARRATION = supplier -> supplier.get();

    private ButtonCompat() { }
}
