package dev.journalsmp.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class JournalParticle extends SingleQuadParticle {
    private JournalParticle(ClientLevel level, double x, double y, double z,
                            double xd, double yd, double zd, SpriteSet sprites, RandomSource random) {
        super(level, x, y, z, xd, yd, zd, sprites.get(random));
        this.lifetime = 18;
        this.gravity = 0.02F;
        this.quadSize = 0.22F;
        this.hasPhysics = false;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd, RandomSource random) {
            return new JournalParticle(level, x, y, z, xd, yd, zd, sprites, random);
        }
    }
}
