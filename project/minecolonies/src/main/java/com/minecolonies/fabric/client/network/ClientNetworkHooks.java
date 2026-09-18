package com.minecolonies.fabric.client.network;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.apiimp.initializer.ModParticleTypesInitializer;
import com.minecolonies.coremod.client.render.worldevent.PathfindingDebugRenderer;
import com.minecolonies.coremod.client.gui.WindowBuildDecoration;
import com.minecolonies.coremod.client.gui.map.WindowColonyMap;
import com.minecolonies.coremod.entity.pathfinding.MNode;
import com.minecolonies.coremod.network.messages.client.colony.ColonyListMessage;
import com.minecolonies.coremod.network.messages.server.DecorationBuildRequestMessage;
import com.minecolonies.coremod.network.messages.server.PlantationFieldBuildRequestMessage;
import com.minecolonies.coremod.util.ChunkCapData;
import com.minecolonies.coremod.util.ChunkClientDataHelper;
import com.minecolonies.coremod.util.FurnaceRecipes;
import com.minecolonies.coremod.datalistener.QuestJsonListener;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.minecolonies.fabric.network.simple.SimpleChannel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.ldtteam.structurize.api.util.constant.Constants.BLUEPRINT_FOLDER;
import static com.ldtteam.structurize.api.util.constant.Constants.SCANS_FOLDER;

/** Client-only Fabric boundary for the common MineColonies network facade. */
public final class ClientNetworkHooks
{
    private static final Set<ResourceLocation> REGISTERED = new HashSet<>();

    private ClientNetworkHooks()
    {
    }

    public static void register(final SimpleChannel channel)
    {
        if (!REGISTERED.add(channel.id()))
        {
            return;
        }

        final boolean registered = ClientPlayNetworking.registerGlobalReceiver(channel.id(),
          (client, handler, buffer, responseSender) -> channel.handleClient(buffer, client::execute));
        if (!registered && !ClientPlayNetworking.getGlobalReceivers().contains(channel.id()))
        {
            throw new IllegalStateException("Unable to register MineColonies client packet channel " + channel.id());
        }
        Log.getLogger().debug("Registered MineColonies client packet channel {}", channel.id());
    }

    /** Reflection target used by the common channel when a client sends C2S. */
    public static void send(final ResourceLocation channel, final FriendlyByteBuf buffer)
    {
        ClientPlayNetworking.send(channel, buffer);
    }

    /** Apply the client-side colony view without leaking client classes into common messages. */
    public static void handleColonyViewMessage(final int colonyId,
                                               final FriendlyByteBuf colonyBuffer,
                                               final boolean isNewSubscription,
                                               final ResourceKey<Level> dimension)
    {
        if (Minecraft.getInstance().level != null)
        {
            IColonyManager.getInstance().handleColonyViewMessage(colonyId, colonyBuffer,
              Minecraft.getInstance().level, isNewSubscription, dimension);
        }
    }

    /** Populate the client colony map without loading the map GUI on a server. */
    public static void handleColonyListMessage(final List<ColonyListMessage.ColonyInfo> colonies)
    {
        WindowColonyMap.setColonies(colonies);
    }

    /** Remove a citizen from a client colony view. */
    public static void handleColonyViewRemoveCitizenMessage(final int colonyId, final int citizenId)
    {
        if (Minecraft.getInstance().level != null)
        {
            IColonyManager.getInstance().handleColonyViewRemoveCitizenMessage(colonyId, citizenId,
              Minecraft.getInstance().level.dimension());
        }
    }

    /** Remove a building from a client colony view. */
    public static void handleColonyViewRemoveBuildingMessage(final int colonyId, final net.minecraft.core.BlockPos buildingId)
    {
        if (Minecraft.getInstance().level != null)
        {
            IColonyManager.getInstance().handleColonyViewRemoveBuildingMessage(colonyId, buildingId,
              Minecraft.getInstance().level.dimension());
        }
    }

    /** Remove a work order from a client colony view. */
    public static void handleColonyViewRemoveWorkOrderMessage(final int colonyId, final int workOrderId)
    {
        if (Minecraft.getInstance().level != null)
        {
            IColonyManager.getInstance().handleColonyViewRemoveWorkOrderMessage(colonyId, workOrderId,
              Minecraft.getInstance().level.dimension());
        }
    }

    /** Apply one server-sent chunk capability update on the client. */
    public static void handleUpdateChunkCapabilityMessage(final ChunkCapData chunkCapData)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        if (!WorldUtil.isChunkLoaded(world, new ChunkPos(chunkCapData.x, chunkCapData.z)))
        {
            ChunkClientDataHelper.addCapData(chunkCapData);
            return;
        }

        final LevelChunk chunk = world.getChunk(chunkCapData.x, chunkCapData.z);
        final IColonyTagCapability cap = com.minecolonies.fabric.capability.CapabilityHooks
          .getCapability(chunk, com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP, null).orElse(null);
        if (cap != null && cap.getOwningColony() != chunkCapData.owningColony)
        {
            ChunkClientDataHelper.applyCap(chunkCapData, chunk);
        }
    }

    /** Apply a server-sent range of chunk capability updates on the client. */
    public static void handleUpdateChunkRangeCapabilityMessage(final List<ChunkCapData> caps)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        for (final ChunkCapData data : caps)
        {
            if (!WorldUtil.isChunkLoaded(world, new ChunkPos(data.x, data.z)))
            {
                ChunkClientDataHelper.addCapData(data);
                continue;
            }

            final LevelChunk chunk = world.getChunk(data.x, data.z);
            ChunkClientDataHelper.applyCap(data, chunk);
        }
    }

    /** Apply the server compatibility snapshot on the client. */
    public static void handleUpdateClientWithCompatibilityMessage(final FriendlyByteBuf buffer)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        FurnaceRecipes.getInstance().loadUtilityPredicates();
        try
        {
            IColonyManager.getInstance().getCompatibilityManager().deserialize(buffer, world);
        }
        catch (final Exception exception)
        {
            Log.getLogger().error("Failed to load compatibility manager", exception);
        }
    }

    /** Apply the server quest snapshot on the client. */
    public static void handleGlobalQuestSyncMessage(final FriendlyByteBuf buffer)
    {
        if (Minecraft.getInstance().level != null)
        {
            QuestJsonListener.readGlobalQuestPackets(buffer);
        }
    }

    /** Open the decoration/plantation build selector on the client. */
    public static void handleOpenBuildWindowMessage(final BlockPos pos,
                                                    final String packName,
                                                    final String path,
                                                    final Rotation rotation,
                                                    final boolean mirror,
                                                    final boolean plantation)
    {
        final Minecraft client = Minecraft.getInstance();
        if (client.level == null)
        {
            return;
        }

        new WindowBuildDecoration(pos, packName, path, rotation, mirror, builder -> {
            if (plantation)
            {
                return new PlantationFieldBuildRequestMessage(WorkOrderType.BUILD, pos, packName, path,
                  client.level.dimension(), rotation, mirror, builder);
            }
            return new DecorationBuildRequestMessage(WorkOrderType.BUILD, pos, packName, path,
              client.level.dimension(), rotation, mirror, builder);
        }).open();
    }

    /** Save a server-provided scan on the client. */
    public static void handleSaveStructureNBTMessage(final CompoundTag compoundNBT, final String fileName)
    {
        final Minecraft client = Minecraft.getInstance();
        if (compoundNBT == null || client.player == null || client.getUser() == null)
        {
            return;
        }

        final String packName = client.getUser().getName().toLowerCase(Locale.US);
        RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprintFuture(
          StructurePacks.storeBlueprint(packName, compoundNBT, client.gameDirectory.toPath()
                                                                  .resolve(BLUEPRINT_FOLDER)
                                                                  .resolve(packName)
                                                                  .resolve(SCANS_FOLDER).resolve(fileName)));
        client.player.displayClientMessage(Component.translatable("Scan successfully saved as %s", fileName), false);
    }

    /** Render a block crack or break particle effect on the client. */
    public static void handleBlockParticleEffectMessage(final BlockPos pos, final BlockState block, final int side)
    {
        if (side == -1)
        {
            Minecraft.getInstance().particleEngine.destroy(pos, block);
        }
        else
        {
            Minecraft.getInstance().particleEngine.crack(pos, Direction.from3DDataValue(side));
        }
    }

    /** Render compost particles on the client. */
    public static void handleCompostParticleMessage(final BlockPos pos)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        final Random random = new Random();
        final int amount = random.nextInt(15) + 1;
        final BlockState state = world.getBlockState(pos);
        for (int i = 0; i < amount; ++i)
        {
            final double d0 = random.nextGaussian() * 0.02D;
            final double d1 = random.nextGaussian() * 0.02D;
            final double d2 = random.nextGaussian() * 0.02D;
            final double height = state.isAir() ? 1.0D : state.getShape(world, pos).bounds().maxY;
            world.addParticle(ParticleTypes.HAPPY_VILLAGER,
              (double) ((float) pos.getX() + random.nextFloat()),
              (double) pos.getY() + (double) random.nextFloat() * height,
              (double) ((float) pos.getZ() + random.nextFloat()), d0, d1, d2);
        }
    }

    /** Render eating particles on the client. */
    public static void handleItemParticleEffectMessage(final ItemStack stack,
                                                       final double posX,
                                                       final double posY,
                                                       final double posZ,
                                                       final double rotationPitch,
                                                       final double rotationYaw,
                                                       final double eyeHeight)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null || stack.getUseAnimation() != UseAnim.EAT)
        {
            return;
        }

        final Random random = new Random();
        for (int i = 0; i < 5; ++i)
        {
            Vec3 randomPos = new Vec3((random.nextDouble() - 0.5D) * 0.1D,
              Math.random() * 0.1D + 0.1D, 0.0D);
            randomPos = randomPos.xRot((float) (-rotationPitch * 0.017453292F));
            randomPos = randomPos.yRot((float) (-rotationYaw * 0.017453292F));
            final double d0 = -random.nextDouble() * 0.6D - 0.3D;
            Vec3 randomOffset = new Vec3((random.nextDouble() - 0.5D) * 0.3D, d0, 0.6D);
            randomOffset = randomOffset.xRot((float) (-rotationPitch * 0.017453292F));
            randomOffset = randomOffset.yRot((float) (-rotationYaw * 0.017453292F));
            randomOffset = randomOffset.add(posX, posY + eyeHeight, posZ);
            world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), randomOffset.x, randomOffset.y,
              randomOffset.z, randomPos.x, randomPos.y + 0.05D, randomPos.z);
        }
    }

    /** Render localized item particles on the client. */
    public static void handleLocalizedParticleEffectMessage(final ItemStack stack,
                                                            final double posX,
                                                            final double posY,
                                                            final double posZ)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        final Random random = new Random();
        for (int i = 0; i < 5; ++i)
        {
            final Vec3 randomPos = new Vec3((random.nextDouble() - 0.5D) * 0.1D,
              random.nextDouble() * 0.1D + 0.1D, 0.0D);
            final Vec3 randomOffset = new Vec3((random.nextDouble() - 0.5D) * 0.1D,
              random.nextDouble() - 0.5D * 0.1D, (random.nextDouble() - 0.5D) * 0.1D);
            world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), posX + randomOffset.x,
              posY + randomOffset.y, posZ + randomOffset.z, randomPos.x, randomPos.y + 0.05D, randomPos.z);
        }
    }

    /** Render one stage of a circular particle effect on the client. */
    public static void handleCircleParticleEffectMessage(final SimpleParticleType type,
                                                         final double posX,
                                                         final double posY,
                                                         final double posZ,
                                                         final int stage)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        final Random random = new Random();
        final double x = Math.cos(stage * 45.0) + posX;
        final double z = Math.sin(stage * 45.0) + posZ;
        for (int i = 0; i < 5; ++i)
        {
            final Vec3 randomPos = new Vec3(random.nextDouble() * 0.1D + 0.1D,
              random.nextDouble() * 0.1D + 0.1D, random.nextDouble() * 0.1D + 0.1D);
            final Vec3 randomOffset = new Vec3((random.nextDouble() - 0.5D) * 0.1D,
              (random.nextDouble() - 0.5D) * 0.1D, (random.nextDouble() - 0.5D) * 0.1D);
            world.addParticle(type, x + randomOffset.x, posY + randomOffset.y, z + randomOffset.z,
              randomPos.x, randomPos.y + 0.05D, randomPos.z);
        }
    }

    /** Render one stage of a streamed particle effect on the client. */
    public static void handleStreamParticleEffectMessage(final SimpleParticleType type,
                                                         final double sPosX,
                                                         final double sPosY,
                                                         final double sPosZ,
                                                         final double ePosX,
                                                         final double ePosY,
                                                         final double ePosZ,
                                                         final int stage,
                                                         final int maxStage)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null || maxStage == 0)
        {
            return;
        }

        final Random random = new Random();
        final Vec3 end = new Vec3(ePosX, ePosY, ePosZ);
        final double xDif = (sPosX - ePosX) / maxStage;
        final double yDif = (sPosY - ePosY) / maxStage;
        final double zDif = (sPosZ - ePosZ) / maxStage;
        final double curve = maxStage / 3.0;
        for (int step = Math.max(0, stage - 1); step <= Math.min(maxStage, stage + 1); step++)
        {
            final double minDif = Math.min(step, Math.abs(step - maxStage)) / curve;
            for (int i = 0; i < 10; ++i)
            {
                final Vec3 randomPos = new Vec3(random.nextDouble() * 0.1D + 0.1D,
                  random.nextDouble() * 0.1D + 0.1D, random.nextDouble() * 0.1D + 0.1D);
                final Vec3 randomOffset = new Vec3((random.nextDouble() - 0.5D) * 0.1D,
                  (random.nextDouble() - 0.5D) * 0.1D, (random.nextDouble() - 0.5D) * 0.1D);
                world.addParticle(type, end.x + randomOffset.x + xDif * step,
                  end.y + randomOffset.y + yDif * step + minDif, end.z + randomOffset.z + zDif * step,
                  randomPos.x, randomPos.y + 0.05D, randomPos.z);
            }
        }
    }

    /** Render the sleeping particle on the client. */
    public static void handleSleepingParticleMessage(final double x, final double y, final double z)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world != null)
        {
            world.addParticle(ModParticleTypesInitializer.SLEEPINGPARTICLE_TYPE, x, y, z, 1.0f, 1.0f, 1.0f);
        }
    }

    /** Render villager-like particles around a citizen. */
    public static void handleVanillaParticleMessage(final double x,
                                                    final double y,
                                                    final double z,
                                                    final SimpleParticleType type)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
        {
            return;
        }

        final Random random = new Random();
        for (int i = 0; i < 5; ++i)
        {
            world.addParticle(type,
              x + (random.nextFloat() * com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH * 2.0F)
                - com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH,
              y + 1.0D + random.nextFloat() * com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_HEIGHT,
              z + (random.nextFloat() * com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH * 2.0F)
                - com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH,
              random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D);
        }
    }

    /** Stop client music and sounds. */
    public static void handleStopMusicMessage()
    {
        Minecraft.getInstance().getSoundManager().stop();
        Minecraft.getInstance().getMusicManager().stopPlaying();
    }

    /** Play a positional audio event on the client. */
    public static void handlePlayAudioMessage(final SoundEvent soundEvent, final SoundSource category)
    {
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(soundEvent, category,
          1.0F, 1.0F, RandomSource.create(), 0.0, 0.0, 0.0));
    }

    /** Play a sound at a position when the client is in the packet dimension. */
    public static void handlePlayMusicAtPosMessage(final SoundEvent soundEvent,
                                                   final BlockPos pos,
                                                   final ResourceKey<Level> dimension,
                                                   final float volume,
                                                   final float pitch)
    {
        final Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.level.dimension() == dimension)
        {
            client.level.playSound(client.player, pos.getX(), pos.getY(), pos.getZ(), soundEvent,
              SoundSource.AMBIENT, volume, pitch);
        }
    }

    /** Queue a citizen sound on the client. */
    public static void handlePlaySoundForCitizenMessage(final int entityId,
                                                        final SoundEvent soundEvent,
                                                        final SoundSource soundSource,
                                                        final BlockPos pos,
                                                        final ResourceKey<Level> dimension,
                                                        final float volume,
                                                        final float pitch,
                                                        final int length,
                                                        final int repetitions)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null || world.dimension() != dimension)
        {
            return;
        }

        final Entity entity = world.getEntity(entityId);
        if (entity instanceof AbstractCivilianEntity civilian)
        {
            civilian.getSoundManager().addToQueue(soundEvent, soundSource, repetitions, length, pos, volume, pitch);
        }
    }

    /** Update the client pathfinding debug renderer. */
    public static void handleSyncPathMessage(final Set<MNode> visited,
                                             final Set<MNode> notVisited,
                                             final Set<MNode> path)
    {
        PathfindingDebugRenderer.lastDebugNodesVisited = visited;
        PathfindingDebugRenderer.lastDebugNodesNotVisited = notVisited;
        PathfindingDebugRenderer.lastDebugNodesPath = path;
    }

    /** Mark reached path nodes in the client pathfinding debug renderer. */
    public static void handleSyncPathReachedMessage(final Set<BlockPos> reached)
    {
        for (final MNode node : PathfindingDebugRenderer.lastDebugNodesPath)
        {
            if (reached.contains(node.pos))
            {
                node.setReachedByWorker(true);
            }
        }
    }
}
