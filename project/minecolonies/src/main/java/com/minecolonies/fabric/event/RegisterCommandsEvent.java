package com.minecolonies.fabric.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class RegisterCommandsEvent extends Event
{
    private final CommandDispatcher<CommandSourceStack> dispatcher;

    public RegisterCommandsEvent(final CommandDispatcher<CommandSourceStack> dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    public CommandDispatcher<CommandSourceStack> getDispatcher()
    {
        return dispatcher;
    }
}
