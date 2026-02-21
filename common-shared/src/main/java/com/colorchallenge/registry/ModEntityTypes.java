package com.colorchallenge.registry;

import com.colorchallenge.entity.StaffEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static Supplier<EntityType<StaffEntity>> MERCHANT;

    public static EntityType<StaffEntity> createStaffEntityType() {
        return EntityType.Builder.<StaffEntity>of(StaffEntity::new, MobCategory.MISC)
                .sized(0.6F, 1.95F)
                .build("staff");
    }
}
