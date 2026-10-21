package dev.journalsmp.client;

import dev.journalsmp.particle.JournalParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public final class JournalSmpClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(JournalParticles.EINO, JournalParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(JournalParticles.ARBY, JournalParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(JournalParticles.JOURNAL, JournalParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(JournalParticles.DEXZYN, JournalParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(JournalParticles.ACE, JournalParticle.Provider::new);
    }
}
