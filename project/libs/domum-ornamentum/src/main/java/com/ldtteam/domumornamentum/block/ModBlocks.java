package com.ldtteam.domumornamentum.block;

import com.google.common.collect.Maps;
import com.ldtteam.domumornamentum.block.decorative.*;
import com.ldtteam.domumornamentum.block.types.BrickType;
import com.ldtteam.domumornamentum.block.types.ExtraBlockType;
import com.ldtteam.domumornamentum.block.types.FramedLightType;
import com.ldtteam.domumornamentum.block.types.TimberFrameType;
import com.ldtteam.domumornamentum.block.vanilla.*;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.item.decoration.*;
import com.ldtteam.domumornamentum.item.interfaces.IDoItem;
import com.ldtteam.domumornamentum.item.vanilla.*;
import com.ldtteam.domumornamentum.shingles.ShingleHeightType;
import com.ldtteam.domumornamentum.util.Constants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Eager Fabric registry for Domum Ornamentum blocks and their block items.
 *
 * <p>The Forge source used {@code DeferredRegister} and {@code RegistryObject}. Fabric 1.20.1
 * exposes the vanilla registries directly, so registration is performed once during the common
 * mod initializer and the public accessors return the registered instances.</p>
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444", "squid:S1820"})
public final class ModBlocks implements IModBlocks
{
    private static final List<Block> BLOCKS = new ArrayList<>();
    private static final List<Item> ITEMS = new ArrayList<>();

    private static final List<Supplier<TimberFrameBlock>> TIMBER_FRAMES = new ArrayList<>();
    private static final List<Supplier<FramedLightBlock>> FRAMED_LIGHT = new ArrayList<>();
    private static final List<Supplier<FloatingCarpetBlock>> FLOATING_CARPETS = new ArrayList<>();
    private static final List<Supplier<ExtraBlock>> EXTRA_TOP_BLOCKS = new ArrayList<>();
    private static final List<Supplier<BrickBlock>> BRICK = new ArrayList<>();
    private static final List<Supplier<PillarBlock>> PILLARS = new ArrayList<>();
    private static final List<Supplier<AllBrickBlock>> ALL_BRICK = new ArrayList<>();
    private static final List<Supplier<AllBrickStairBlock>> ALL_BRICK_STAIR = new ArrayList<>();

    private static final ModBlocks INSTANCE = new ModBlocks();
    private static final Object ITEM_GROUP_LOCK = new Object();

    private static final Supplier<ArchitectsCutterBlock> ARCHITECTS_CUTTER;
    private static final Supplier<ShingleBlock> SHINGLE;
    private static final Supplier<ShingleBlock> SHINGLE_FLAT;
    private static final Supplier<ShingleBlock> SHINGLE_FLAT_LOWER;
    private static final Supplier<ShingleSlabBlock> SHINGLE_SLAB;
    private static final Supplier<PaperWallBlock> PAPER_WALL;
    private static final Supplier<BarrelBlock> STANDING_BARREL;
    private static final Supplier<BarrelBlock> LAYING_BARREL;
    private static final Supplier<FenceBlock> FENCE;
    private static final Supplier<FenceGateBlock> FENCE_GATE;
    private static final Supplier<SlabBlock> SLAB;
    private static final Supplier<WallBlock> WALL;
    private static final Supplier<StairBlock> STAIR;
    private static final Supplier<TrapdoorBlock> TRAPDOOR;
    private static final Supplier<DoorBlock> DOOR;
    private static final Supplier<PostBlock> POST;
    private static final Supplier<PanelBlock> PANEL;
    private static final Supplier<FancyDoorBlock> FANCY_DOOR;
    private static final Supplier<FancyTrapdoorBlock> FANCY_TRAPDOOR;

    static
    {
        ARCHITECTS_CUTTER = registerSimpleBlockItem("architectscutter", ArchitectsCutterBlock::new);

        for (final TimberFrameType blockType : TimberFrameType.values())
        {
            TIMBER_FRAMES.add(registerCustomBlockItem(blockType.getName(), () -> new TimberFrameBlock(blockType), TimberFrameBlockItem::new));
        }

        SHINGLE = registerCustomBlockItem("shingle", ShingleBlock::new, ShingleBlockItem::new);
        SHINGLE_FLAT = registerCustomBlockItem("shingle_flat", ShingleBlock::new, ShingleBlockItem::new);
        SHINGLE_FLAT_LOWER = registerCustomBlockItem("shingle_flat_lower", ShingleBlock::new, ShingleBlockItem::new);
        SHINGLE_SLAB = registerCustomBlockItem("shingle_slab", ShingleSlabBlock::new, ShingleSlabBlockItem::new);
        PAPER_WALL = registerCustomBlockItem("blockpaperwall", PaperWallBlock::new, PaperwallBlockItem::new);

        PILLARS.add(registerCustomBlockItem("blockpillar", PillarBlock::new, PillarBlockItem::new));
        PILLARS.add(registerCustomBlockItem("blockypillar", PillarBlock::new, PillarBlockItem::new));
        PILLARS.add(registerCustomBlockItem("squarepillar", PillarBlock::new, PillarBlockItem::new));

        for (final ExtraBlockType blockType : ExtraBlockType.values())
        {
            EXTRA_TOP_BLOCKS.add(registerCustomBlockItem(blockType.getSerializedName(), () -> new ExtraBlock(blockType), ExtraBlockItem::new));
        }

        for (final FramedLightType blockType : FramedLightType.values())
        {
            FRAMED_LIGHT.add(registerCustomBlockItem(blockType.getName(), () -> new FramedLightBlock(blockType), FramedLightBlockItem::new));
        }

        for (final DyeColor color : DyeColor.values())
        {
            FLOATING_CARPETS.add(registerSimpleBlockItem(color.getName().toLowerCase(Locale.ROOT) + "_floating_carpet", () -> new FloatingCarpetBlock(color)));
        }

        for (final BrickType type : BrickType.values())
        {
            BRICK.add(registerSimpleBlockItem(type.getSerializedName(), () -> new BrickBlock(type)));
        }

        STANDING_BARREL = registerSimpleBlockItem("blockbarreldeco_standing", BarrelBlock::new);
        LAYING_BARREL = registerSimpleBlockItem("blockbarreldeco_onside", BarrelBlock::new);
        FENCE = registerCustomBlockItem("vanilla_fence_compat", FenceBlock::new, FenceBlockItem::new);
        FENCE_GATE = registerCustomBlockItem("vanilla_fence_gate_compat", FenceGateBlock::new, FenceGateBlockItem::new);
        SLAB = registerCustomBlockItem("vanilla_slab_compat", SlabBlock::new, SlabBlockItem::new);
        WALL = registerCustomBlockItem("vanilla_wall_compat", WallBlock::new, WallBlockItem::new);
        STAIR = registerCustomBlockItem("vanilla_stairs_compat", StairBlock::new, StairsBlockItem::new);
        TRAPDOOR = registerCustomBlockItem("vanilla_trapdoors_compat", TrapdoorBlock::new, TrapdoorBlockItem::new);
        DOOR = registerCustomBlockItem("vanilla_doors_compat", DoorBlock::new, DoorBlockItem::new);
        PANEL = registerCustomBlockItem("panel", PanelBlock::new, PanelBlockItem::new);
        ALL_BRICK.add(registerCustomBlockItem("light_brick", AllBrickBlock::new, AllBrickBlockItem::new));
        ALL_BRICK.add(registerCustomBlockItem("dark_brick", AllBrickBlock::new, AllBrickBlockItem::new));
        ALL_BRICK_STAIR.add(registerCustomBlockItem("light_brick_stair", AllBrickStairBlock::new, AllBrickStairBlockItem::new));
        ALL_BRICK_STAIR.add(registerCustomBlockItem("dark_brick_stair", AllBrickStairBlock::new, AllBrickStairBlockItem::new));
        POST = registerCustomBlockItem("post", PostBlock::new, PostBlockItem::new);
        FANCY_DOOR = registerCustomBlockItem("fancy_door", FancyDoorBlock::new, FancyDoorBlockItem::new);
        FANCY_TRAPDOOR = registerCustomBlockItem("fancy_trapdoors", FancyTrapdoorBlock::new, FancyTrapdoorBlockItem::new);
    }

    /** Published atomically after the first item-group computation. */
    private volatile Map<ResourceLocation, List<ItemStack>> itemGroups = Map.of();

    private ModBlocks()
    {
    }

    public static ModBlocks getInstance()
    {
        return INSTANCE;
    }

    /** Touches this class from the Fabric initializer after its static registration has completed. */
    public static void init()
    {
    }

    public static ResourceKey<Block> blockKey(final String name)
    {
        return ResourceKey.create(Registries.BLOCK, new ResourceLocation(Constants.MOD_ID, name.toLowerCase(Locale.ROOT)));
    }

    public static ResourceKey<Item> itemKey(final String name)
    {
        return ResourceKey.create(Registries.ITEM, new ResourceLocation(Constants.MOD_ID, name.toLowerCase(Locale.ROOT)));
    }

    public static <B extends Block> Supplier<B> registerSimpleBlockItem(final String name, final Supplier<B> block)
    {
        final B registered = registerBlock(name, block);
        registerItem(name, new BlockItem(registered, new Item.Properties()));
        return () -> registered;
    }

    public static <B extends Block> Supplier<B> registerCustomBlockItem(final String name,
                                                                           final Supplier<B> block,
                                                                           final BiFunction<B, Item.Properties, ? extends BlockItem> item)
    {
        final B registered = registerBlock(name, block);
        registerItem(name, item.apply(registered, new Item.Properties()));
        return () -> registered;
    }

    private static <B extends Block> B registerBlock(final String name, final Supplier<B> factory)
    {
        final ResourceLocation id = new ResourceLocation(Constants.MOD_ID, name.toLowerCase(Locale.ROOT));
        final B block = factory.get();
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        BLOCKS.add(block);
        return block;
    }

    private static <I extends Item> I registerItem(final String name, final I item)
    {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, name.toLowerCase(Locale.ROOT)), item);
        ITEMS.add(item);
        return item;
    }

    @Override public ArchitectsCutterBlock getArchitectsCutter() { return ARCHITECTS_CUTTER.get(); }

    @Override public ShingleBlock getShingle(final ShingleHeightType heightType)
    {
        return switch (heightType)
        {
            case DEFAULT -> SHINGLE.get();
            case FLAT -> SHINGLE_FLAT.get();
            case FLAT_LOWER -> SHINGLE_FLAT_LOWER.get();
        };
    }

    @Override public List<TimberFrameBlock> getTimberFrames() { return TIMBER_FRAMES.stream().map(Supplier::get).collect(Collectors.toList()); }
    @Override public List<FramedLightBlock> getFramedLights() { return FRAMED_LIGHT.stream().map(Supplier::get).collect(Collectors.toList()); }
    @Override public List<PillarBlock> getPillars() { return PILLARS.stream().map(Supplier::get).collect(Collectors.toList()); }
    @Override public ShingleSlabBlock getShingleSlab() { return SHINGLE_SLAB.get(); }
    @Override public PaperWallBlock getPaperWall() { return PAPER_WALL.get(); }
    @Override public List<ExtraBlock> getExtraTopBlocks() { return EXTRA_TOP_BLOCKS.stream().map(Supplier::get).toList(); }
    @Override public List<FloatingCarpetBlock> getFloatingCarpets() { return FLOATING_CARPETS.stream().map(Supplier::get).toList(); }
    @Override public BarrelBlock getStandingBarrel() { return STANDING_BARREL.get(); }
    @Override public BarrelBlock getLayingBarrel() { return LAYING_BARREL.get(); }
    @Override public FenceBlock getFence() { return FENCE.get(); }
    @Override public FenceGateBlock getFenceGate() { return FENCE_GATE.get(); }
    @Override public SlabBlock getSlab() { return SLAB.get(); }
    @Override public List<BrickBlock> getBricks() { return BRICK.stream().map(Supplier::get).toList(); }
    @Override public WallBlock getWall() { return WALL.get(); }
    @Override public StairBlock getStair() { return STAIR.get(); }
    @Override public TrapdoorBlock getTrapdoor() { return TRAPDOOR.get(); }
    @Override public PanelBlock getPanel() { return PANEL.get(); }
    @Override public PostBlock getPost() { return POST.get(); }
    @Override public DoorBlock getDoor() { return DOOR.get(); }
    @Override public FancyDoorBlock getFancyDoor() { return FANCY_DOOR.get(); }
    @Override public FancyTrapdoorBlock getFancyTrapdoor() { return FANCY_TRAPDOOR.get(); }
    @Override public List<AllBrickBlock> getAllBrickBlocks() { return ALL_BRICK.stream().map(Supplier::get).toList(); }
    @Override public List<AllBrickStairBlock> getAllBrickStairBlocks() { return ALL_BRICK_STAIR.stream().map(Supplier::get).toList(); }

    public Map<ResourceLocation, List<ItemStack>> getOrComputeItemGroups()
    {
        Map<ResourceLocation, List<ItemStack>> result = itemGroups;
        if (!result.isEmpty()) return result;

        synchronized (ITEM_GROUP_LOCK)
        {
            result = itemGroups;
            if (!result.isEmpty()) return result;

            final Map<ResourceLocation, List<ItemStack>> computed = new TreeMap<>();
            BuiltInRegistries.ITEM.forEach(item -> {
                if (item instanceof IDoItem doItem)
                {
                    final List<ItemStack> group = computed.computeIfAbsent(doItem.getGroup(), ignored -> new ArrayList<>());
                    if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof IMateriallyTexturedBlock texturedBlock)
                    {
                        if (blockItem.getBlock() instanceof ICachedItemGroupBlock cached)
                        {
                            final NonNullList<ItemStack> stacks = NonNullList.create();
                            cached.fillItemCategory(stacks);
                            stacks.forEach(stack -> group.add(process(stack.copy(), texturedBlock)));
                        }
                        else
                        {
                            group.add(process(new ItemStack(item), texturedBlock));
                        }
                    }
                }
            });

            itemGroups = Collections.unmodifiableMap(computed);
            return itemGroups;
        }
    }

    private ItemStack process(final ItemStack stack, final IMateriallyTexturedBlock block)
    {
        final Map<ResourceLocation, Block> textureData = Maps.newHashMap();
        block.getComponents().forEach(component -> textureData.put(component.getId(), component.getDefault()));
        stack.getOrCreateTag().put("textureData", new MaterialTextureData(textureData).serializeNBT());
        return stack;
    }

    public static Block[] getMateriallyTexturableBlocks()
    {
        return BLOCKS.stream().filter(IMateriallyTexturedBlock.class::isInstance).toArray(Block[]::new);
    }

    public static Item[] getMateriallyTexturableItems()
    {
        return Arrays.stream(getMateriallyTexturableBlocks())
            .map(BuiltInRegistries.BLOCK::getKey)
            .map(BuiltInRegistries.ITEM::get)
            .filter(Objects::nonNull)
            .toArray(Item[]::new);
    }
}
