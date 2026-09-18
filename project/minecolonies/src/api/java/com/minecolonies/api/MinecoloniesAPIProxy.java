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

public final class MinecoloniesAPIProxy implements IMinecoloniesAPI
{
    private static MinecoloniesAPIProxy ourInstance = new MinecoloniesAPIProxy();

    private IMinecoloniesAPI apiInstance;

    public static MinecoloniesAPIProxy getInstance()
    {
        return ourInstance;
    }

    private MinecoloniesAPIProxy()
    {
    }

    public void setApiInstance(final IMinecoloniesAPI apiInstance)
    {
        this.apiInstance = apiInstance;
    }

    @Override
    public IColonyManager getColonyManager()
    {
        return apiInstance.getColonyManager();
    }

    @Override
    public ICitizenDataManager getCitizenDataManager()
    {
        return apiInstance.getCitizenDataManager();
    }

    @Override
    public IMobAIRegistry getMobAIRegistry()
    {
        return apiInstance.getMobAIRegistry();
    }

    @Override
    public IPathNavigateRegistry getPathNavigateRegistry()
    {
        return apiInstance.getPathNavigateRegistry();
    }

    @Override
    public IBuildingDataManager getBuildingDataManager()
    {
        return apiInstance.getBuildingDataManager();
    }

    @Override
    public FabricRegistry<BuildingEntry> getBuildingRegistry()
    {
        return apiInstance.getBuildingRegistry();
    }

    @Override
    public FabricRegistry<FieldRegistries.FieldEntry> getFieldRegistry()
    {
        return apiInstance.getFieldRegistry();
    }

    @Override
    public IJobDataManager getJobDataManager()
    {
        return apiInstance.getJobDataManager();
    }

    @Override
    public FabricRegistry<JobEntry> getJobRegistry()
    {
        return apiInstance.getJobRegistry();
    }

    @Override
    public FabricRegistry<InteractionResponseHandlerEntry> getInteractionResponseHandlerRegistry()
    {
        return apiInstance.getInteractionResponseHandlerRegistry();
    }

    @Override
    public IGuardTypeDataManager getGuardTypeDataManager()
    {
        return apiInstance.getGuardTypeDataManager();
    }

    @Override
    public FabricRegistry<GuardType> getGuardTypeRegistry()
    {
        return apiInstance.getGuardTypeRegistry();
    }

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return apiInstance.getModelTypeRegistry();
    }

    @Override
    public Configuration getConfig()
    {
        return apiInstance.getConfig();
    }

    @Override
    public IFurnaceRecipes getFurnaceRecipes()
    {
        return apiInstance.getFurnaceRecipes();
    }

    @Override
    public IInteractionResponseHandlerDataManager getInteractionResponseHandlerDataManager()
    {
        return apiInstance.getInteractionResponseHandlerDataManager();
    }

    @Override
    public IGlobalResearchTree getGlobalResearchTree()
    {
        return apiInstance.getGlobalResearchTree();
    }

    @Override
    public FabricRegistry<ResearchRequirementEntry> getResearchRequirementRegistry() {return apiInstance.getResearchRequirementRegistry();}

    @Override
    public FabricRegistry<ResearchEffectEntry> getResearchEffectRegistry() {return apiInstance.getResearchEffectRegistry();}

    @Override
    public FabricRegistry<ColonyEventTypeRegistryEntry> getColonyEventRegistry()
    {
        return apiInstance.getColonyEventRegistry();
    }

    @Override
    public FabricRegistry<ColonyEventDescriptionTypeRegistryEntry> getColonyEventDescriptionRegistry()
    {
        return apiInstance.getColonyEventDescriptionRegistry();
    }

    @Override
    public FabricRegistry<RecipeTypeEntry> getRecipeTypeRegistry()
    {
        return apiInstance.getRecipeTypeRegistry();
    }

    @Override
    public FabricRegistry<CraftingType> getCraftingTypeRegistry()
    {
        return apiInstance.getCraftingTypeRegistry();
    }

    @Override
    public FabricRegistry<QuestRegistries.RewardEntry> getQuestRewardRegistry()
    {
        return apiInstance.getQuestRewardRegistry();
    }

    @Override
    public FabricRegistry<QuestRegistries.ObjectiveEntry> getQuestObjectiveRegistry()
    {
        return apiInstance.getQuestObjectiveRegistry();
    }

    @Override
    public FabricRegistry<QuestRegistries.TriggerEntry> getQuestTriggerRegistry()
    {
        return apiInstance.getQuestTriggerRegistry();
    }

    @Override
    public FabricRegistry<QuestRegistries.DialogueAnswerEntry> getQuestDialogueAnswerRegistry()
    {
        return apiInstance.getQuestDialogueAnswerRegistry();
    }

    @Override
    public FabricRegistry<HappinessRegistry.HappinessFactorTypeEntry> getHappinessTypeRegistry()
    {
        return apiInstance.getHappinessTypeRegistry();
    }

    @Override
    public FabricRegistry<HappinessRegistry.HappinessFunctionEntry> getHappinessFunctionRegistry()
    {
        return apiInstance.getHappinessFunctionRegistry();
    }
}
