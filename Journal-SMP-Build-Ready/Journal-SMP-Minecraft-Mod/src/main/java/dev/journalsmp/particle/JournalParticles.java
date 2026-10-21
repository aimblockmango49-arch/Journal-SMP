package dev.journalsmp.particle;

import dev.journalsmp.JournalSmpMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;

public final class JournalParticles {
    public static final SimpleParticleType EINO = create("eino");
    public static final SimpleParticleType ARBY = create("arby");
    public static final SimpleParticleType JOURNAL = create("journal");
    public static final SimpleParticleType DEXZYN = create("dexzyn");
    public static final SimpleParticleType ACE = create("ace");

    private JournalParticles() {
    }

    public static void register() {
        // Static initializers perform the registrations.
    }

    private static SimpleParticleType create(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(JournalSmpMod.MOD_ID, name);
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, FabricParticleTypes.simple());
    }
}
