package com.minecolonies.apiimp;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import com.minecolonies.api.colony.interactionhandling.registry.IInteractionResponseHandlerDataManager;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.configuration.Configuration;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.entity.ai.registry.IMobAIRegistry;
import com.minecolonies.api.entity.citizen.happiness.HappinessRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.api.quests.registries.QuestRegistries;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenDataManager;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.registry.BuildingDataManager;
import com.minecolonies.coremod.colony.interactionhandling.registry.InteractionResponseHandlerManager;
import com.minecolonies.coremod.colony.jobs.registry.JobDataManager;
import com.minecolonies.coremod.entity.ai.registry.MobAIRegistry;
import com.minecolonies.coremod.entity.pathfinding.registry.PathNavigateRegistry;
import com.minecolonies.coremod.research.GlobalResearchTree;
import com.minecolonies.coremod.util.FurnaceRecipes;
import com.minecolonies.fabric.registry.FabricRegistry;
import com.minecolonies.fabric.registry.FabricRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.ModResearchRequirements.RESEARCH_RESEARCH_REQ_ID;
import static com.minecolonies.api.research.effects.ModResearchEffects.GLOBAL_EFFECT_ID;

/** Common API implementation backed by Fabric/vanilla registries. */
public class CommonMinecoloniesAPIImpl implements IMinecoloniesAPI
{
    private static ResourceLocation id(final String path)
    {
        return new ResourceLocation(Constants.MOD_ID, path);
    }

    private static final FabricRegistry<BuildingEntry> BUILDINGS = FabricRegistries.custom(id("buildings"), BuildingEntry.class, id("null"));
    private static final FabricRegistry<FieldRegistries.FieldEntry> FIELDS = FabricRegistries.custom(id("fields"), FieldRegistries.FieldEntry.class, id("null"));
    private static final FabricRegistry<JobEntry> JOBS = FabricRegistries.custom(id("jobs"), JobEntry.class, id("null"));
    private static final FabricRegistry<GuardType> GUARD_TYPES = FabricRegistries.custom(id("guardtypes"), GuardType.class, ModGuardTypes.KNIGHT_ID);
    private static final FabricRegistry<InteractionResponseHandlerEntry> INTERACTION_HANDLERS = FabricRegistries.custom(id("interactionresponsehandlers"), InteractionResponseHandlerEntry.class, id("null"));
    private static final FabricRegistry<ColonyEventTypeRegistryEntry> COLONY_EVENTS = FabricRegistries.custom(id("colonyeventtypes"), ColonyEventTypeRegistryEntry.class, id("null"));
    private static final FabricRegistry<ColonyEventDescriptionTypeRegistryEntry> COLONY_EVENT_DESCRIPTIONS = FabricRegistries.custom(id("colonyeventdesctypes"), ColonyEventDescriptionTypeRegistryEntry.class, id("null"));
    private static final FabricRegistry<CraftingType> CRAFTING_TYPES = FabricRegistries.custom(id("craftingtypes"), CraftingType.class, null);
    private static final FabricRegistry<RecipeTypeEntry> RECIPE_TYPES = FabricRegistries.custom(id("recipetypeentries"), RecipeTypeEntry.class, id("classic"));
    private static final FabricRegistry<ResearchRequirementEntry> RESEARCH_REQUIREMENTS = FabricRegistries.custom(id("researchrequirementtypes"), ResearchRequirementEntry.class, RESEARCH_RESEARCH_REQ_ID);
    private static final FabricRegistry<ResearchEffectEntry> RESEARCH_EFFECTS = FabricRegistries.custom(id("researcheffecttypes"), ResearchEffectEntry.class, GLOBAL_EFFECT_ID);
    private static final FabricRegistry<QuestRegistries.ObjectiveEntry> QUEST_OBJECTIVES = FabricRegistries.custom(id("questobjectives"), QuestRegistries.ObjectiveEntry.class, id("null"));
    private static final FabricRegistry<QuestRegistries.RewardEntry> QUEST_REWARDS = FabricRegistries.custom(id("questrewards"), QuestRegistries.RewardEntry.class, id("null"));
    private static final FabricRegistry<QuestRegistries.TriggerEntry> QUEST_TRIGGERS = FabricRegistries.custom(id("questtriggers"), QuestRegistries.TriggerEntry.class, id("null"));
    private static final FabricRegistry<QuestRegistries.DialogueAnswerEntry> QUEST_ANSWERS = FabricRegistries.custom(id("questanswerresults"), QuestRegistries.DialogueAnswerEntry.class, id("null"));
    private static final FabricRegistry<HappinessRegistry.HappinessFactorTypeEntry> HAPPINESS_FACTORS = FabricRegistries.custom(id("happinessfactortypes"), HappinessRegistry.HappinessFactorTypeEntry.class, id("null"));
    private static final FabricRegistry<HappinessRegistry.HappinessFunctionEntry> HAPPINESS_FUNCTIONS = FabricRegistries.custom(id("happinessfunction"), HappinessRegistry.HappinessFunctionEntry.class, id("null"));

    private final IColonyManager colonyManager = new ColonyManager();
    private final ICitizenDataManager citizenDataManager = new CitizenDataManager();
    private final IMobAIRegistry mobAIRegistry = new MobAIRegistry();
    private final IPathNavigateRegistry pathNavigateRegistry = new PathNavigateRegistry();
    private final IBuildingDataManager buildingDataManager = new BuildingDataManager();
    private final IJobDataManager jobDataManager = new JobDataManager();
    private final IGuardTypeDataManager guardTypeDataManager = new com.minecolonies.coremod.colony.buildings.registry.GuardTypeDataManager();
    private final IInteractionResponseHandlerDataManager interactionDataManager = new InteractionResponseHandlerManager();
    private static final IGlobalResearchTree GLOBAL_RESEARCH_TREE = new GlobalResearchTree();

    @Override
    @NotNull
    public IColonyManager getColonyManager() { return colonyManager; }

    @Override
    @NotNull
    public ICitizenDataManager getCitizenDataManager() { return citizenDataManager; }

    @Override
    @NotNull
    public IMobAIRegistry getMobAIRegistry() { return mobAIRegistry; }

    @Override
    @NotNull
    public IPathNavigateRegistry getPathNavigateRegistry() { return pathNavigateRegistry; }

    @Override
    @NotNull
    public IBuildingDataManager getBuildingDataManager() { return buildingDataManager; }

    @Override
    public FabricRegistry<BuildingEntry> getBuildingRegistry() { return BUILDINGS; }

    @Override
    public FabricRegistry<FieldRegistries.FieldEntry> getFieldRegistry() { return FIELDS; }

    @Override
    public IJobDataManager getJobDataManager() { return jobDataManager; }

    @Override
    public FabricRegistry<JobEntry> getJobRegistry() { return JOBS; }

    @Override
    public FabricRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry() { return INTERACTION_HANDLERS; }

    @Override
    public IGuardTypeDataManager getGuardTypeDataManager() { return guardTypeDataManager; }

    @Override
    public FabricRegistry<GuardType> getGuardTypeRegistry() { return GUARD_TYPES; }

    @Override
    public IModelTypeRegistry getModelTypeRegistry() { return null; }

    @Override
    public Configuration getConfig() { return MineColonies.getConfig(); }

    @Override
    public IFurnaceRecipes getFurnaceRecipes() { return FurnaceRecipes.getInstance(); }

    @Override
    public IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager() { return interactionDataManager; }

    @Override
    public IGlobalResearchTree getGlobalResearchTree() { return GLOBAL_RESEARCH_TREE; }

    @Override
    public FabricRegistry<ResearchRequirementEntry> getResearchRequirementRegistry() { return RESEARCH_REQUIREMENTS; }

    @Override
    public FabricRegistry<ResearchEffectEntry> getResearchEffectRegistry() { return RESEARCH_EFFECTS; }

    /** Compatibility entry point for code that still arrives through a Forge-style proxy. */
    public void onRegistryNewRegistry(final Object ignoredEvent)
    {
        // Fabric registries are created eagerly in the static initialization above.
    }

    @Override
    public FabricRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry() { return COLONY_EVENTS; }

    @Override
    public FabricRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry() { return COLONY_EVENT_DESCRIPTIONS; }

    @Override
    public FabricRegistry<RecipeTypeEntry> getRecipeTypeRegistry() { return RECIPE_TYPES; }

    @Override
    public FabricRegistry<CraftingType> getCraftingTypeRegistry() { return CRAFTING_TYPES; }

    @Override
    public FabricRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry() { return QUEST_REWARDS; }

    @Override
    public FabricRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry() { return QUEST_OBJECTIVES; }

    @Override
    public FabricRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry() { return QUEST_TRIGGERS; }

    @Override
    public FabricRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry() { return QUEST_ANSWERS; }

    @Override
    public FabricRegistry<HappinessRegistry.HappinessFactorTypeEntry> getHappinessTypeRegistry() { return HAPPINESS_FACTORS; }

    @Override
    public FabricRegistry<HappinessRegistry.HappinessFunctionEntry> getHappinessFunctionRegistry() { return HAPPINESS_FUNCTIONS; }
}
