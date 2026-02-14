package com.colorchallenge.registry;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.entity.StaffEntity;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ColorChallenge.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<StaffEntity>> MERCHANT =
            ENTITY_TYPES.register("staff",
                    () -> EntityType.Builder.<StaffEntity>of(StaffEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    ResourceLocation.fromNamespaceAndPath(ColorChallenge.MOD_ID, "staff"))));

    public static void register() {
        ENTITY_TYPES.register();
        EntityAttributeRegistry.register(MERCHANT, Mob::createMobAttributes);
    }
}
