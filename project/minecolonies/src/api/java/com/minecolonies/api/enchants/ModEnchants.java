package com.minecolonies.api.enchants;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.item.enchantment.Enchantment;
import com.minecolonies.fabric.registry.FabricDeferredRegister;
import com.minecolonies.fabric.registry.FabricRegistries;
import com.minecolonies.fabric.registry.FabricRegistryObject;

/**
 * All our mods renchants
 */
public class ModEnchants
{
    public static final FabricDeferredRegister<Enchantment> ENCHANTMENTS = FabricDeferredRegister.create(FabricRegistries.ENCHANTMENTS, Constants.MOD_ID);

    private ModEnchants()
    {
        // Intentionally left empty
    }

    /**
     * Raider damage enchant, gives extra damage against raiders
     */
    public static FabricRegistryObject<? extends Enchantment> raiderDamage;
}
