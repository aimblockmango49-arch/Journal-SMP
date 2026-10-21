package dev.journalsmp;

import dev.journalsmp.command.JournalCommands;
import dev.journalsmp.data.JournalSavedData;
import dev.journalsmp.event.JournalEvents;
import dev.journalsmp.event.JournalLootEvents;
import dev.journalsmp.particle.JournalParticles;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class JournalSmpMod implements ModInitializer {
    public static final String MOD_ID = "journalsmp";

    @Override
    public void onInitialize() {
        JournalItems.register();
        JournalParticles.register();
        JournalEvents.register();
        JournalLootEvents.register();
        JournalCommands.register();
        ServerLifecycleEvents.SERVER_STOPPING.register(JournalSavedData::flush);
    }
}
