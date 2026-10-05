package com.minecolonies.fabric.gametest;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.citizen.VisitorCitizen;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Opt-in vanilla right-click probes for packaged Fabric servers. */
public final class MineColoniesCitizenInteractionGameTests implements FabricGameTest
{
    @GameTestGenerator
    public Collection<TestFunction> citizenInteractionTests()
    {
        if (!Boolean.getBoolean("minecolonies.citizen-interaction-tests"))
        {
            return List.of();
        }
        return List.of(new TestFunction("minecolonies_citizen", "citizen_right_click",
          FabricGameTest.EMPTY_STRUCTURE, 100, 0, true, MineColoniesCitizenInteractionGameTests::rightClick));
    }

    private static void rightClick(final GameTestHelper helper)
    {
        final var level = helper.getLevel();
        final var player = new ServerPlayer(level.getServer(), level,
          new GameProfile(UUID.randomUUID(), "CitizenClickProbe"));
        final var citizen = (EntityCitizen) ModEntities.CITIZEN.create(level);
        final var visitor = (VisitorCitizen) ModEntities.VISITOR.create(level);
        helper.assertTrue(citizen != null && visitor != null, "Citizen entity types unavailable");
        for (final InteractionHand hand : InteractionHand.values())
        {
            helper.assertTrue(citizen.interact(player, hand) == InteractionResult.SUCCESS,
              "Vanilla right click did not reach the citizen interaction handler: " + hand);
            helper.assertTrue(visitor.interact(player, hand) == InteractionResult.SUCCESS,
              "Vanilla right click did not reach the visitor interaction handler: " + hand);
        }

        final ItemStack tag = new ItemStack(Items.NAME_TAG, 2);
        tag.setHoverName(Component.literal("Citizen rename probe"));
        player.setItemInHand(InteractionHand.MAIN_HAND, tag);
        helper.assertTrue(citizen.interact(player, InteractionHand.MAIN_HAND).consumesAction(),
          "Vanilla named name tag did not consume the interaction");
        // An unregistered citizen deliberately rejects renaming in setCustomName;
        // vanilla must still handle and consume the tag without opening a GUI.
        helper.assertTrue(tag.getCount() == 1, "Vanilla name-tag consumption was not preserved");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NAME_TAG));
        helper.assertTrue(citizen.interact(player, InteractionHand.MAIN_HAND) == InteractionResult.PASS,
          "Unnamed name tag must fall through without recursive interaction");
        helper.assertTrue(visitor.interact(player, InteractionHand.MAIN_HAND) == InteractionResult.PASS,
          "Visitor unnamed name tag must fall through without recursive interaction");
        LoggerFactory.getLogger(MineColoniesCitizenInteractionGameTests.class).info(
          "Citizen right-click dispatch passed all assertions in {}",
          FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace());
        helper.succeed();
    }
}
