package com.minecolonies.api.quests.registries;

import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.quests.*;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import com.minecolonies.fabric.registry.FabricRegistry;
import com.minecolonies.fabric.registry.FabricRegistryObject;
import java.util.function.Function;

/**
 * All quest registries related things.
 */
public class QuestRegistries
{
    /**
     * Get the reward registry.
     * @return the reward registry.
     */
    static FabricRegistry<RewardEntry> getQuestRewardsRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestRewardRegistry();
    }

    /**
     * Get the objective registry.
     * @return the reward registry.
     */
    static FabricRegistry<ObjectiveEntry> getQuestObjectiveRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestObjectiveRegistry();
    }

    /**
     * Get the trigger registry.
     * @return the reward registry.
     */
    static FabricRegistry<TriggerEntry> getQuestTriggerRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestTriggerRegistry();
    }

    /**
     * Get the dialogue answer result registry.
     * @return the reward registry.
     */
    static FabricRegistry<DialogueAnswerEntry> getDialogueAnswerResultRegistry()
    {
        return IMinecoloniesAPI.getInstance().getQuestDialogueAnswerRegistry();
    }

    /**
     * Quest reward entry type.
     */
    public static class RewardEntry
    {
        //todo create instance getters

        private final Function<JsonObject, IQuestRewardTemplate> producer;

        public RewardEntry(final Function<JsonObject, IQuestRewardTemplate> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the reward.
         */
        public IQuestRewardTemplate produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest objective entry type.
     */
    public static class ObjectiveEntry
    {
        private final Function<JsonObject, IQuestObjectiveTemplate> producer;

        public ObjectiveEntry(final Function<JsonObject, IQuestObjectiveTemplate> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the objective.
         */
        public IQuestObjectiveTemplate produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest trigger entry type.
     */
    public static class TriggerEntry
    {
        private final Function<JsonObject, IQuestTriggerTemplate> producer;

        public TriggerEntry(final Function<JsonObject, IQuestTriggerTemplate> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the trigger.
         */
        public IQuestTriggerTemplate produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    /**
     * Quest dialogue entry type.
     */
    public static class DialogueAnswerEntry
    {
        private final Function<JsonObject, IQuestDialogueAnswer> producer;

        public DialogueAnswerEntry(final Function<JsonObject, IQuestDialogueAnswer> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create one from json.
         * @param jsonObject the input.
         * @return the answer result.
         */
        public IQuestDialogueAnswer produce(final JsonObject jsonObject)
        {
            return producer.apply(jsonObject);
        }
    }

    public static ResourceLocation ITEM_REWARD_ID         = new ResourceLocation(Constants.MOD_ID, "item");
    public static ResourceLocation SKILL_REWARD_ID        = new ResourceLocation(Constants.MOD_ID, "skill");
    public static ResourceLocation RESEARCH_REWARD_ID     = new ResourceLocation(Constants.MOD_ID, "research");
    public static ResourceLocation RAID_REWARD_ID         = new ResourceLocation(Constants.MOD_ID, "raid");
    public static ResourceLocation RELATIONSHIP_REWARD_ID = new ResourceLocation(Constants.MOD_ID, "relationship");
    public static ResourceLocation HAPPINESS_REWARD_ID    = new ResourceLocation(Constants.MOD_ID, "happiness");
    public static ResourceLocation UNLOCK_QUEST_REWARD_ID     = new ResourceLocation(Constants.MOD_ID, "unlockquest");
    public static ResourceLocation QUEST_REPUTATION_REWARD_ID = new ResourceLocation(Constants.MOD_ID, "questreputation");

    public static ResourceLocation DIALOGUE_OBJECTIVE_ID   = new ResourceLocation(Constants.MOD_ID, "dialogue");
    public static ResourceLocation BREAKBLOCK_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "breakblock");
    public static ResourceLocation DELIVERY_OBJECTIVE_ID   = new ResourceLocation(Constants.MOD_ID, "delivery");
    public static ResourceLocation KILLENTITY_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "killentity");
    public static ResourceLocation PLACEBLOCK_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "placeblock");
    public static ResourceLocation BUILD_BUILDING_OBJECTIVE_ID = new ResourceLocation(Constants.MOD_ID, "buildbuilding");

    public static ResourceLocation STATE_TRIGGER_ID       = new ResourceLocation(Constants.MOD_ID, "state");
    public static ResourceLocation RANDOM_TRIGGER_ID      = new ResourceLocation(Constants.MOD_ID, "random");
    public static ResourceLocation CITIZEN_TRIGGER_ID     = new ResourceLocation(Constants.MOD_ID, "citizen");
    public static ResourceLocation UNLOCK_TRIGGER_ID      = new ResourceLocation(Constants.MOD_ID, "unlock");
    public static ResourceLocation QUEST_REPUTATION_TRIGGER_ID  = new ResourceLocation(Constants.MOD_ID, "questreputation");

    public static ResourceLocation DIALOGUE_ANSWER_ID = new ResourceLocation(Constants.MOD_ID, "dialogue");
    public static ResourceLocation RETURN_ANSWER_ID   = new ResourceLocation(Constants.MOD_ID, "return");
    public static ResourceLocation CANCEL_ANSWER_ID   = new ResourceLocation(Constants.MOD_ID, "cancel");
    public static ResourceLocation GOTO_ANSWER_ID     = new ResourceLocation(Constants.MOD_ID, "advanceobjective");


    public static FabricRegistryObject<RewardEntry>  itemReward;
    public static FabricRegistryObject <RewardEntry> skillReward;
    public static FabricRegistryObject <RewardEntry> researchReward;
    public static FabricRegistryObject <RewardEntry> raidReward;
    public static FabricRegistryObject <RewardEntry> relationshipReward;
    public static FabricRegistryObject <RewardEntry> happinessReward;
    public static FabricRegistryObject <RewardEntry> unlockQuestReward;
    public static FabricRegistryObject <RewardEntry> questReputationReward;

    public static FabricRegistryObject <ObjectiveEntry> dialogueObjective;
    public static FabricRegistryObject <ObjectiveEntry> breakBlockObjective;
    public static FabricRegistryObject <ObjectiveEntry> deliveryObjective;
    public static FabricRegistryObject <ObjectiveEntry> killEntityObjective;
    public static FabricRegistryObject <ObjectiveEntry> placeBlockObjective;
    public static FabricRegistryObject <ObjectiveEntry> buildBuildingObjective;

    public static FabricRegistryObject <TriggerEntry> stateTrigger;
    public static FabricRegistryObject <TriggerEntry> randomTrigger;
    public static FabricRegistryObject <TriggerEntry> citizenTrigger;
    public static FabricRegistryObject <TriggerEntry> unlockTrigger;
    public static FabricRegistryObject <TriggerEntry> questReputationTrigger;

    public static FabricRegistryObject <DialogueAnswerEntry> dialogueAnswerResult;
    public static FabricRegistryObject <DialogueAnswerEntry> returnAnswerResult;
    public static FabricRegistryObject <DialogueAnswerEntry> cancelAnswerResult;
    public static FabricRegistryObject <DialogueAnswerEntry> gotoAnswerResult;

}
