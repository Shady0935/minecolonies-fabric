package com.ldtteam.multipiston.client;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

/** Shared button dispatch for the Multi-Piston BlockUI window. */
public abstract class AbstractWindowSkeleton extends BOWindow implements ButtonHandler
{
    @NotNull
    private final HashMap<String, Consumer<Button>> buttons = new HashMap<>();

    protected AbstractWindowSkeleton(final String resource)
    {
        super(new ResourceLocation(resource));
    }

    public final void registerButton(final String id, final Runnable action)
    {
        registerButton(id, ignored -> action.run());
    }

    public final void registerButton(final String id, final Consumer<Button> action)
    {
        buttons.put(id, action);
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        final Consumer<Button> action = buttons.get(button.getID());
        if (action == null)
        {
            Log.getLogger().warn(getClass().getName() + ": Unhandled Button ID:" + button.getID());
            return;
        }
        action.accept(button);
    }

    public final void doNothing(final Button ignored)
    {
    }
}
