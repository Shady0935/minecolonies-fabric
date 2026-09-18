package com.ldtteam.domumornamentum.client.event.handlers;

import com.ldtteam.domumornamentum.client.render.ModelGhostRenderer;
import com.ldtteam.domumornamentum.util.ItemStackUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Fabric world-render hook for the materialized block placement preview. */
public final class MateriallyTexturedBlockPreviewRenderHandler
{
    private MateriallyTexturedBlockPreviewRenderHandler()
    {
    }

    public static void registerClient()
    {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            final Minecraft minecraft = Minecraft.getInstance();
            final HitResult hit = minecraft.hitResult;
            if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS) return;

            final Player player = minecraft.player;
            if (player == null || player.isSpectator()) return;

            final ItemStack held = ItemStackUtils.getMateriallyTexturedItemStackFromPlayer(player);
            if (held.isEmpty()) return;

            final Vec3 target = Vec3.atLowerCornerOf(blockHit.getBlockPos().relative(blockHit.getDirection()));
            ModelGhostRenderer.getInstance().renderGhost(context.matrixStack(), held, target, blockHit, false);
        });
    }
}
