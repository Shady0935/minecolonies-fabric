package com.ldtteam.structurize.client;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.client.gui.AbstractBlueprintManipulationWindow;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class ModKeyMappings
{
    private static final String CATEGORY = "key.structurize.categories.general";

    /**
     * Teleport using active Scan Tool
     */
    public static final Supplier<StructurizeKeyMapping> TELEPORT = lazy("key.structurize.teleport", InputConstants.UNKNOWN.getValue());

    /**
     * Move build previews
     */
    public static final Supplier<StructurizeKeyMapping> MOVE_FORWARD = lazy("key.structurize.move_forward", GLFW.GLFW_KEY_UP);
    public static final Supplier<StructurizeKeyMapping> MOVE_BACK = lazy("key.structurize.move_back", GLFW.GLFW_KEY_DOWN);
    public static final Supplier<StructurizeKeyMapping> MOVE_LEFT = lazy("key.structurize.move_left", GLFW.GLFW_KEY_LEFT);
    public static final Supplier<StructurizeKeyMapping> MOVE_RIGHT = lazy("key.structurize.move_right", GLFW.GLFW_KEY_RIGHT);
    public static final Supplier<StructurizeKeyMapping> MOVE_UP = lazy("key.structurize.move_up", GLFW.GLFW_KEY_KP_ADD);
    public static final Supplier<StructurizeKeyMapping> MOVE_DOWN = lazy("key.structurize.move_down", GLFW.GLFW_KEY_KP_SUBTRACT);
    public static final Supplier<StructurizeKeyMapping> ROTATE_CW = lazy("key.structurize.rotate_cw", GLFW.GLFW_KEY_RIGHT);
    public static final Supplier<StructurizeKeyMapping> ROTATE_CCW = lazy("key.structurize.rotate_ccw", GLFW.GLFW_KEY_LEFT);
    public static final Supplier<StructurizeKeyMapping> MIRROR = lazy("key.structurize.mirror", GLFW.GLFW_KEY_M);
    public static final Supplier<StructurizeKeyMapping> PLACE = lazy("key.structurize.place", GLFW.GLFW_KEY_ENTER);

    /**
     * Register key mappings
     */
    public static void register()
    {
        KeyBindingHelper.registerKeyBinding(TELEPORT.get());
        KeyBindingHelper.registerKeyBinding(MOVE_FORWARD.get());
        KeyBindingHelper.registerKeyBinding(MOVE_BACK.get());
        KeyBindingHelper.registerKeyBinding(MOVE_LEFT.get());
        KeyBindingHelper.registerKeyBinding(MOVE_RIGHT.get());
        KeyBindingHelper.registerKeyBinding(MOVE_UP.get());
        KeyBindingHelper.registerKeyBinding(MOVE_DOWN.get());
        KeyBindingHelper.registerKeyBinding(ROTATE_CW.get());
        KeyBindingHelper.registerKeyBinding(ROTATE_CCW.get());
        KeyBindingHelper.registerKeyBinding(MIRROR.get());
        KeyBindingHelper.registerKeyBinding(PLACE.get());
    }

    private static Supplier<StructurizeKeyMapping> lazy(final String translationKey, final int key)
    {
        return new Supplier<>()
        {
            private StructurizeKeyMapping mapping;

            @Override
            public StructurizeKeyMapping get()
            {
                if (mapping == null)
                {
                    mapping = new StructurizeKeyMapping(translationKey, key, CATEGORY);
                }
                return mapping;
            }
        };
    }

    /** Forge-compatible helper used by the blueprint GUI. */
    public static final class StructurizeKeyMapping extends KeyMapping
    {
        private StructurizeKeyMapping(final String translationKey, final int key, final String category)
        {
            super(translationKey, InputConstants.Type.KEYSYM, key, category);
        }

        public boolean isActiveAndMatches(final InputConstants.Key key)
        {
            return matches(key.getValue(), key.getValue());
        }
    }

    /**
     * Private constructor to hide the implicit one.
     */
    private ModKeyMappings()
    {
        /*
         * Intentionally left empty.
         */
    }
}
