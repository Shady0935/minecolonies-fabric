package com.minecolonies.api;

import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.colony.guardtype.GuardType;
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
import com.minecolonies.fabric.registry.FabricRegistry;

public interface IMinecoloniesAPI
{

    static IMinecoloniesAPI getInstance()
    {
        return MinecoloniesAPIProxy.getInstance();
    }

    IColonyManager getColonyManager();

    ICitizenDataManager getCitizenDataManager();

    IMobAIRegistry getMobAIRegistry();

    IPathNavigateRegistry getPathNavigateRegistry();

    IBuildingDataManager getBuildingDataManager();

    FabricRegistry<BuildingEntry> getBuildingRegistry();

    FabricRegistry<FieldRegistries.FieldEntry> getFieldRegistry();

    IJobDataManager getJobDataManager();

    FabricRegistry<JobEntry> getJobRegistry();

    FabricRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry();

    IGuardTypeDataManager getGuardTypeDataManager();

    FabricRegistry<GuardType> getGuardTypeRegistry();

    IModelTypeRegistry getModelTypeRegistry();

    Configuration getConfig();

    IFurnaceRecipes getFurnaceRecipes();

    IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager();

    IGlobalResearchTree getGlobalResearchTree();

    FabricRegistry<ResearchRequirementEntry> getResearchRequirementRegistry();

    FabricRegistry<ResearchEffectEntry> getResearchEffectRegistry();

    FabricRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry();

    FabricRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry();

    FabricRegistry<RecipeTypeEntry> getRecipeTypeRegistry();

    FabricRegistry<CraftingType> getCraftingTypeRegistry();

    FabricRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry();

    FabricRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry();

    FabricRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry();

    FabricRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry();

    FabricRegistry<HappinessRegistry.HappinessFactorTypeEntry> getHappinessTypeRegistry();

    FabricRegistry<HappinessRegistry.HappinessFunctionEntry> getHappinessFunctionRegistry();
}
