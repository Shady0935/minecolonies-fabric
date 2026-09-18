package com.minecolonies.fabric.common;

import java.util.Objects;

public final class ToolAction
{
    private final String name;

    public ToolAction(final String name)
    {
        this.name = name;
    }

    public String name()
    {
        return name;
    }

    @Override
    public boolean equals(final Object other)
    {
        return other instanceof ToolAction action && Objects.equals(name, action.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
