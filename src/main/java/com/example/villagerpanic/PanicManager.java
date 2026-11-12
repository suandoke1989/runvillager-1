package com.example.villagerpanic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public final class PanicManager {

    private static final int PANIC_DURATION_TICKS = 160;
    private static final double FLEE_SPEED = 1.5D;
    private static final double SPREAD_RADIUS = 16.0D;
    private static final double FLEE_DISTANCE = 16.0D;
    private static final long BRAIN_MEMORY_TICKS = 200L;

    private static final Map<UUID, Integer> PANIC_TICKS = new HashMap<>();
    private static final Map<UUID, VillagerEntity> PANICKING_VILLAGERS = new HashMap<>();

    private PanicManager() {
    }

    public static void spreadPanic(VillagerEntity hurtVillager, DamageSource source) {
        Entity attackerEntity = source.getAttacker();
        LivingEntity attacker = attackerEntity instanceof LivingEntity living ? living : null;

        panic(hurtVillager, attacker);
        playAlarmSound(hurtVillager);

        for (VillagerEntity nearby : hurtVillager.getWorld().getEntitiesByClass(
                VillagerEntity.class,
                hurtVillager.getBoundingBox().expand(SPREAD_RADIUS),
                v -> v != hurtVillager && v.isAlive())) {
            panic(nearby, attacker);
        }
    }

    private static void panic(VillagerEntity villager, LivingEntity attacker) {
        if (attacker != null && attacker != villager) {
            villager.setAttacker(attacker);
            villager.getBrain().remember(MemoryModuleType.HURT_BY_ENTITY, attacker, BRAIN_MEMORY_TICKS);
        }
        PANIC_TICKS.put(villager.getUuid(), PANIC_DURATION_TICKS);
        PANICKING_VILLAGERS.put(villager.getUuid(), villager);
        navigateAway(villager, attacker);
    }

    private static void navigateAway(VillagerEntity villager, LivingEntity attacker) {
        Vec3d delta;
        if (attacker != null && attacker.isAlive()) {
            delta = villager.getPos().subtract(attacker.getPos());
        } else {
            double angle = villager.getRandom().nextDouble() * Math.PI * 2.0D;
            delta = new Vec3d(Math.cos(angle), 0.0D, Math.sin(angle));
        }

        Vec3d flat = new Vec3d(delta.x, 0.0D, delta.z);
        if (flat.lengthSquared() < 1.0E-4D) {
            flat = new Vec3d(1.0D, 0.0D, 0.0D);
        }
        flat = flat.normalize();

        double targetX = villager.getX() + flat.x * FLEE_DISTANCE;
        double targetZ = villager.getZ() + flat.z * FLEE_DISTANCE;
        villager.getNavigation().startMovingTo(targetX, villager.getY(), targetZ, FLEE_SPEED);
    }

    public static void tick() {
        if (PANIC_TICKS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = PANIC_TICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            VillagerEntity villager = PANICKING_VILLAGERS.get(entry.getKey());

            if (villager == null || !villager.isAlive() || villager.isRemoved()) {
                iterator.remove();
                PANICKING_VILLAGERS.remove(entry.getKey());
                continue;
            }

            int ticksLeft = entry.getValue() - 1;
            if (ticksLeft <= 0) {
                iterator.remove();
                PANICKING_VILLAGERS.remove(entry.getKey());
                continue;
            }
            entry.setValue(ticksLeft);

            if (ticksLeft % 10 == 0) {
                navigateAway(villager, villager.getAttacker());
            }
        }
    }

    private static void playAlarmSound(VillagerEntity villager) {
        villager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
    }
}
