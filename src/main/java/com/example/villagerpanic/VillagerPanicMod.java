package com.example.villagerpanic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.passive.VillagerEntity;

public class VillagerPanicMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (amount > 0.0F && entity instanceof VillagerEntity villager && !villager.getWorld().isClient) {
                PanicManager.spreadPanic(villager, source);
            }
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> PanicManager.tick());
    }
}
