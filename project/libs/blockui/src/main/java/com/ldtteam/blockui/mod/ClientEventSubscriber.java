package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonVanilla;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.mod.BlockStateTestGui;
import com.ldtteam.blockui.mod.container.ContainerHook;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.BOWindow;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.function.Consumer;

/** Fabric lifecycle bridge for BlockUI's client-side services. */
public final class ClientEventSubscriber
{
    private ClientEventSubscriber()
    {
    }

    public static void register()
    {
        ClientTickEvents.START_CLIENT_TICK.register(ClientEventSubscriber::onClientTickStart);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEventSubscriber::onClientTickEnd);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ContainerHook.init());
        WorldRenderEvents.LAST.register(context -> {
            if (context.world() != null)
            {
                context.world().getProfiler().push("blockui_hook_manager_render");
                com.ldtteam.blockui.hooks.HookRegistries.render(context.matrixStack(), context.tickDelta());
                context.world().getProfiler().pop();
            }
        });
    }

    private static void onClientTickStart(final Minecraft client)
    {
        if (Screen.hasAltDown() && Screen.hasControlDown() && Screen.hasShiftDown()
            && InputConstants.isKeyDown(client.getWindow().getWindow(), GLFW.GLFW_KEY_X))
        {
            final BOWindow window = new BOWindow();
            window.addChild(createTestGuiButton(0, "General All-in-one", new ResourceLocation(BlockUI.MOD_ID, "gui/test.xml"), parent -> {
                parent.findPaneOfTypeByID("missing_out_of_jar", Image.class)
                    .setImage(OutOfJarResourceLocation.ofMinecraftFolder(BlockUI.MOD_ID, "missing_out_of_jar.png"), false);
                parent.findPaneOfTypeByID("working_out_of_jar", Image.class)
                    .setImage(OutOfJarResourceLocation.of(BlockUI.MOD_ID, Path.of("../../src/test/resources/button.png")), false);
                final ResourceLocation resourceLocation = OutOfJarResourceLocation.ofMinecraftSkin(client, client.getUser().getGameProfile(), null);
                if (resourceLocation != null)
                {
                    parent.findPaneOfTypeByID("player_skin", Image.class).setImage(resourceLocation, false);
                }
            }));
            window.addChild(createTestGuiButton(1, "Tooltip Positioning", new ResourceLocation(BlockUI.MOD_ID, "gui/test2.xml")));
            window.addChild(createTestGuiButton(2, "ItemIcon To BlockState", new ResourceLocation(BlockUI.MOD_ID, "gui/test3.xml"), BlockStateTestGui::setup));
            window.addChild(createTestGuiButton(3, "Scrolling Lists", new ResourceLocation(BlockUI.MOD_ID, "gui/test4.xml"), ScrollingListsGui::setup));
            window.open();
        }
    }

    private static void onClientTickEnd(final Minecraft client)
    {
        if (client.level != null)
        {
            client.getProfiler().push("blockui_hook_manager_tick");
            com.ldtteam.blockui.hooks.HookRegistries.tick(client.level.getGameTime());
            client.getProfiler().pop();
        }
    }

    @SafeVarargs
    private static Button createTestGuiButton(final int order,
        final String name,
        final ResourceLocation testGuiResLoc,
        final Consumer<BOWindow>... setups)
    {
        final Button button = new ButtonVanilla();
        button.setPosition((order % 2) * (button.getWidth() + 20), (order / 2) * (button.getHeight() + 10));
        button.setText(Component.literal(name));
        button.setHandler(b -> {
            new BOWindow(testGuiResLoc)
            {
                @Override
                public void onOpened()
                {
                    super.onOpened();
                    for (final Consumer<BOWindow> setup : setups)
                    {
                        setup.accept(this);
                    }
                }
            }.openAsLayer();
        });
        return button;
    }
}
