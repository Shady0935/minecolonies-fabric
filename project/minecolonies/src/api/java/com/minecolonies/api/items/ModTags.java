package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.TagConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModTags
{
    private static TagKey<Block> blockTag(final ResourceLocation id)
    {
        return TagKey.create(Registries.BLOCK, id);
    }

    private static TagKey<Item> itemTag(final ResourceLocation id)
    {
        return TagKey.create(Registries.ITEM, id);
    }

    /**
     * Flag to check if tags are already loaded.
     */
    public static boolean tagsLoaded = false;

    public static final TagKey<Block> decorationItems = blockTag(TagConstants.DECORATION_ITEMS);
    public static final TagKey<Item>  concretePowder  = itemTag(TagConstants.CONCRETE_POWDER);
    public static final TagKey<Block> concreteBlock   = blockTag(TagConstants.CONCRETE_BLOCK);
    public static final TagKey<Block> pathingBlocks   = blockTag(TagConstants.PATHING_BLOCKS);
    public static final TagKey<Block> tier1blocks     = blockTag(TagConstants.TIER1_BLOCKS);
    public static final TagKey<Block> tier2blocks     = blockTag(TagConstants.TIER2_BLOCKS);
    public static final TagKey<Block> tier3blocks     = blockTag(TagConstants.TIER3_BLOCKS);
    public static final TagKey<Block> tier4blocks     = blockTag(TagConstants.TIER4_BLOCKS);
    public static final TagKey<Block> tier5blocks     = blockTag(TagConstants.TIER5_BLOCKS);
    public static final TagKey<Block> tier6blocks     = blockTag(TagConstants.TIER6_BLOCKS);
    public static final TagKey<Block> mangroveTree    = blockTag(TagConstants.MANGROVE_TREE_BLOCKS);
    public static final TagKey<Block> tree            = blockTag(TagConstants.TREE_BLOCKS);

    public static final TagKey<Block> colonyProtectionException = blockTag(TagConstants.COLONYPROTECTIONEXCEPTION);
    public static final TagKey<Block> indestructible            = blockTag(TagConstants.INDESTRUCTIBLE);

    public static final TagKey<Block> oreChanceBlocks = blockTag(TagConstants.ORECHANCEBLOCKS);

    public static final TagKey<Block> validSpawn = blockTag(TagConstants.VALIDSPAWNBLOCKS);

    public static final TagKey<Item> fungi             = itemTag(TagConstants.FUNGI);
    public static final TagKey<Item> compostables      = itemTag(TagConstants.COMPOSTABLES);
    public static final TagKey<Item> compostables_poor = itemTag(TagConstants.COMPOSTABLES_POOR);
    public static final TagKey<Item> compostables_rich = itemTag(TagConstants.COMPOSTABLES_RICH);

    public static final TagKey<Item> meshes = itemTag(TagConstants.MESHES);

    public static final TagKey<Item> floristFlowers = itemTag(TagConstants.FLORIST_FLOWERS);
    public static final TagKey<Item> excludedFood = itemTag(TagConstants.EXCLUDED_FOOD);

    public static final TagKey<Item> breakable_ore = itemTag(TagConstants.BREAKABLE_ORE);
    public static final TagKey<Item> raw_ore = itemTag(TagConstants.RAW_ORE);

    public static final TagKey<EntityType<?>> hostile = TagKey.create(Registries.ENTITY_TYPE, TagConstants.HOSTILE);
    public static final TagKey<EntityType<?>> mobAttackBlacklist = TagKey.create(Registries.ENTITY_TYPE, TagConstants.MOB_ATTACK_BLACKLIST);

    public static final TagKey<EntityType<?>> raiders = TagKey.create(Registries.ENTITY_TYPE, TagConstants.RAIDERS);

    public static final TagKey<Item> ignoreNBT = itemTag(TagConstants.IGNORE_NBT);

    public static final Map<String, TagKey<Item>> crafterProduct              = new HashMap<>();
    public static final Map<String, TagKey<Item>> crafterProductExclusions    = new HashMap<>();
    public static final Map<String, TagKey<Item>> crafterIngredient           = new HashMap<>();
    public static final Map<String, TagKey<Item>> crafterIngredientExclusions = new HashMap<>();
    public static final Map<String, TagKey<Item>> crafterDoIngredient         = new HashMap<>();


    /**
     * Tag specifier for Products to Include
     */
    private static final String PRODUCT = "_product";

    /**
     * Tag specifier for Products to Exclude
     */
    private static final String PRODUCT_EXCLUDED = "_product_excluded";

    /**
     * Tag specifier for Ingredients to include
     */
    private static final String INGREDIENT = "_ingredient";

    /**
     * Tag specifier for Ingredients to exclude
     */
    private static final String INGREDIENT_EXCLUDED = "_ingredient_excluded";

    /**
     * Tag specifier for Ingredients to include
     */
    private static final String DO_INGREDIENT = "_do_ingredient";

    public static void init()
    {
        initCrafterRules(TagConstants.CRAFTING_BAKER);  // both crafting and smelting
        initCrafterRules(TagConstants.CRAFTING_BLACKSMITH);
        initCrafterRules(TagConstants.CRAFTING_COOK);   // both crafting and smelting
        initCrafterRules(TagConstants.CRAFTING_DYER);
        initCrafterRules(TagConstants.CRAFTING_DYER_SMELTING);
        initCrafterRules(TagConstants.CRAFTING_FARMER);
        initCrafterRules(TagConstants.CRAFTING_FLETCHER);
        initCrafterRules(TagConstants.CRAFTING_GLASSBLOWER);
        initCrafterRules(TagConstants.CRAFTING_GLASSBLOWER_SMELTING);
        initCrafterRules(TagConstants.CRAFTING_MECHANIC);
        initCrafterRules(TagConstants.CRAFTING_PLANTATION);
        initCrafterRules(TagConstants.CRAFTING_SAWMILL);
        initCrafterRules(TagConstants.CRAFTING_STONEMASON);
        initCrafterRules(TagConstants.CRAFTING_STONE_SMELTERY);

        initCrafterRules(TagConstants.CRAFTING_REDUCEABLE);
    }

    /**
     * Initialize the four tags for a particular crafter
     * @param crafterName the string name of the crafter to initialize
     */
    private static void initCrafterRules(@NotNull final String crafterName)
    {
        final ResourceLocation products = new ResourceLocation(MOD_ID, crafterName.concat(PRODUCT));
        final ResourceLocation ingredients = new ResourceLocation(MOD_ID, crafterName.concat(INGREDIENT));
        final ResourceLocation productsExcluded = new ResourceLocation(MOD_ID, crafterName.concat(PRODUCT_EXCLUDED));
        final ResourceLocation ingredientsExcluded = new ResourceLocation(MOD_ID, crafterName.concat(INGREDIENT_EXCLUDED));
        final ResourceLocation doIngredients = new ResourceLocation(MOD_ID, crafterName.concat(DO_INGREDIENT));

        crafterProduct.put(crafterName, itemTag(products));
        crafterProductExclusions.put(crafterName, itemTag(productsExcluded));
        crafterIngredient.put(crafterName, itemTag(ingredients));
        crafterIngredientExclusions.put(crafterName, itemTag(ingredientsExcluded));
        crafterDoIngredient.put(crafterName, itemTag(doIngredients));
    }

    private ModTags()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModTags. This is a utility class");
    }
}
