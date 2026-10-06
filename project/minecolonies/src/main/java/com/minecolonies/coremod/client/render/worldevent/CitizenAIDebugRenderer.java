package com.minecolonies.coremod.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Client-side visualization of the opt-in server citizen AI snapshot.
 */
public final class CitizenAIDebugRenderer
{
    private static final double RENDER_RANGE = 48.0D;
    private static final long ENTITY_REFRESH_INTERVAL_TICKS = 5L;
    private static ClientLevel cachedLevel;
    private static long lastEntityRefreshTick = Long.MIN_VALUE;
    private static List<EntityCitizen> nearbyCitizens = List.of();

    private CitizenAIDebugRenderer()
    {
    }

    static void render(final WorldEventContext ctx)
    {
        if (ctx.clientLevel == null || ctx.clientPlayer == null)
        {
            return;
        }

        refreshNearbyCitizens(ctx);
        for (final EntityCitizen citizen : nearbyCitizens)
        {
            if (citizen.isRemoved() || citizen.getAIDebugSummary().isEmpty()
                  || citizen.distanceToSqr(ctx.clientPlayer) > RENDER_RANGE * RENDER_RANGE)
            {
                continue;
            }
            final BlockPos labelPosition = BlockPos.containing(citizen.getX(),
              citizen.getY() + citizen.getBbHeight() + 0.75D, citizen.getZ());
            WorldRenderMacros.renderDebugText(labelPosition, Arrays.asList(citizen.getAIDebugSummary().split("\n")),
              ctx.poseStack, true, 1, ctx.bufferSource);
            renderNavigation(citizen, ctx);
        }
    }

    private static void refreshNearbyCitizens(final WorldEventContext ctx)
    {
        final long gameTime = ctx.clientLevel.getGameTime();
        if (cachedLevel == ctx.clientLevel && gameTime >= lastEntityRefreshTick
              && gameTime - lastEntityRefreshTick < ENTITY_REFRESH_INTERVAL_TICKS)
        {
            return;
        }

        cachedLevel = ctx.clientLevel;
        lastEntityRefreshTick = gameTime;
        final AABB searchBounds = ctx.clientPlayer.getBoundingBox().inflate(RENDER_RANGE);
        nearbyCitizens = new ArrayList<>(ctx.clientLevel.getEntitiesOfClass(EntityCitizen.class, searchBounds,
          citizen -> !citizen.getAIDebugSummary().isEmpty()));
    }

    private static void renderNavigation(final EntityCitizen citizen, final WorldEventContext ctx)
    {
        final List<BlockPos> path = citizen.getAIDebugPath();
        final boolean hasTarget = citizen.hasAIDebugTarget();
        if (path.isEmpty() && !hasTarget)
        {
            return;
        }

        final Matrix4f matrix = ctx.poseStack.last().pose();
        final VertexConsumer lines = ctx.bufferSource.getBuffer(WorldRenderMacros.LINES);
        Vec3 previous = new Vec3(citizen.getX(), citizen.getY() + 0.2D, citizen.getZ());
        for (int index = 0; index < path.size(); index++)
        {
            final BlockPos node = path.get(index);
            final Vec3 next = Vec3.atCenterOf(node);
            lines.vertex(matrix, (float) previous.x, (float) previous.y, (float) previous.z)
              .color(0.1F, 0.9F, 1.0F, 1.0F).endVertex();
            lines.vertex(matrix, (float) next.x, (float) next.y, (float) next.z)
              .color(0.1F, 0.9F, 1.0F, 1.0F).endVertex();

            final int color = index == 0 ? 0xffffb52e : 0xff27d9e6;
            ColonyWorldRenderMacros.renderLineBox(ctx.poseStack, ctx.bufferSource,
              new AABB(node).inflate(-0.35D), 0.04F, color, true);
            previous = next;
        }

        if (hasTarget)
        {
            final BlockPos target = citizen.getAIDebugTarget();
            ColonyWorldRenderMacros.renderLineBox(ctx.poseStack, ctx.bufferSource,
              new AABB(target).inflate(0.08D), 0.08F, 0xffff4038, true);
        }

        ColonyWorldRenderMacros.endRenderLineBox(ctx.bufferSource);
    }
}
