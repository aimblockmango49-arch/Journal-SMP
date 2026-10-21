package dev.journalsmp.ability;

import dev.journalsmp.JournalItems;
import dev.journalsmp.JournalKind;
import dev.journalsmp.data.JournalSavedData;
import dev.journalsmp.particle.JournalParticles;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AbilityManager {
    private static final long SECOND = 1_000L;
    private static final Map<UUID, PlayerState> PLAYER_STATES = new HashMap<>();
    private static final Map<UUID, TimedUltimate> ACTIVE_ULTIMATES = new HashMap<>();
    private static final Map<UUID, Long> ACTION_LOCKS = new HashMap<>();
    private static long serverTick;

    private AbilityManager() {
    }

    public static void activate(ServerPlayer player, JournalKind kind) {
        if (player.getOffhandItem().isEmpty() || JournalItems.kindOf(player.getOffhandItem()) != kind) {
            tell(player, "That journal must be in your off-hand.", ChatFormatting.RED);
            return;
        }
        if (isActionLocked(player)) {
            tell(player, "You cannot act while Bankruptcy is affecting you.", ChatFormatting.RED);
            return;
        }
        if (!player.isAlive() || player.isSpectator()) {
            tell(player, "You cannot activate this ultimate right now.", ChatFormatting.RED);
            return;
        }
        if (kind != JournalKind.EINO) {
            long remaining = cooldownRemaining(player, kind);
            if (remaining > 0L) {
                tell(player, kind.ultimateName() + " is on cooldown for " + formatDuration(remaining) + ".", ChatFormatting.RED);
                return;
            }
        }

        if (kind == JournalKind.EINO) {
            PLAYER_STATES.computeIfAbsent(player.getUUID(), ignored -> new PlayerState())
                    .bankruptcyUntilMillis = System.currentTimeMillis() + 10L * SECOND;
            tell(player, "Bankruptcy armed: your next unblocked melee hit against a player disables their actions for 10 seconds.", ChatFormatting.GOLD);
            return;
        }

        long now = System.currentTimeMillis();
        JournalSavedData.get(player.level().getServer()).setCooldown(player.getUUID(), kind, now + kind.cooldownMillis());
        switch (kind) {
            case ARBY -> {
                addEffect(player, MobEffects.HEALTH_BOOST, 60 * 20, 2);
                addEffect(player, MobEffects.REGENERATION, 10 * 20, 2);
                ACTIVE_ULTIMATES.put(player.getUUID(), new TimedUltimate(kind, now + 60L * SECOND, now + 10L * SECOND));
                spawn(player, JournalParticles.ARBY, 22);
            }
            case JOURNAL -> {
                addEffect(player, MobEffects.SPEED, 60 * 20, 2);
                ACTIVE_ULTIMATES.put(player.getUUID(), new TimedUltimate(kind, now + 60L * SECOND, 0L));
                spawn(player, JournalParticles.JOURNAL, 18);
            }
            case DEXZYN -> {
                addEffect(player, MobEffects.JUMP_BOOST, 20 * 20, 2);
                ACTIVE_ULTIMATES.put(player.getUUID(), new TimedUltimate(kind, now + 20L * SECOND, 0L));
                spawn(player, JournalParticles.DEXZYN, 14);
            }
            case ACE -> {
                addEffect(player, MobEffects.RESISTANCE, 10 * 20, 4);
                ACTIVE_ULTIMATES.put(player.getUUID(), new TimedUltimate(kind, now + 10L * SECOND, 0L));
                spawn(player, JournalParticles.ACE, 18);
            }
            default -> throw new IllegalStateException("Unhandled journal: " + kind);
        }
        player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 0.8F, 1.15F);
        announce(player, kind);
        tell(player, kind.ultimateName() + " activated.", ChatFormatting.GREEN);
    }

    public static void tickServer(MinecraftServer server) {
        serverTick++;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        UUID id = player.getUUID();
        PlayerState state = PLAYER_STATES.computeIfAbsent(id, ignored -> new PlayerState());
        JournalKind held = JournalItems.kindOf(player.getOffhandItem());

        if (state.lastHeld != held) {
            if (state.lastHeld != null) {
                clearPassiveEffects(player, state.lastHeld);
            }
            if (held != null) {
                cancelUltimateIfJournalChanged(player, held);
            } else {
                cancelUltimateIfJournalChanged(player, null);
            }
            state.lastHeld = held;
            state.villageResistanceApplied = false;
            state.stillSinceTick = serverTick;
            state.lastIdlePulseTick = serverTick;
        }

        if (held == JournalKind.EINO) {
            ensureEffect(player, MobEffects.HERO_OF_THE_VILLAGE, 2, 45);
            if (serverTick % 20 == 0) {
                boolean nearbyVillager = !player.level().getEntitiesOfClass(
                        Villager.class,
                        player.getBoundingBox().inflate(5.0),
                        villager -> villager.distanceToSqr(player) <= 25.0
                ).isEmpty();
                if (nearbyVillager) {
                    ensureEffect(player, MobEffects.RESISTANCE, 1, 45);
                    state.villageResistanceApplied = true;
                } else if (state.villageResistanceApplied) {
                    removeIfAmplifier(player, MobEffects.RESISTANCE, 1);
                    state.villageResistanceApplied = false;
                }
            }
        } else if (held == JournalKind.ARBY) {
            ensureEffect(player, MobEffects.HEALTH_BOOST, 0, 45);
        } else if (held == JournalKind.JOURNAL) {
            ensureEffect(player, MobEffects.SPEED, 0, 45);
        } else if (held == JournalKind.ACE) {
            ensureEffect(player, MobEffects.HASTE, 1, 45);
        }

        tickIdleResistance(player, state, held);
        tickImpact(player, state, held);
        tickTimedUltimate(player);

        if (state.bankruptcyUntilMillis > 0L && System.currentTimeMillis() >= state.bankruptcyUntilMillis) {
            state.bankruptcyUntilMillis = 0L;
            tell(player, "Bankruptcy expired without a successful hit.", ChatFormatting.GRAY);
        }
        Long lockUntil = ACTION_LOCKS.get(id);
        if (lockUntil != null && System.currentTimeMillis() >= lockUntil) {
            ACTION_LOCKS.remove(id);
            tell(player, "Bankruptcy has ended. You can act again.", ChatFormatting.GREEN);
        }
    }

    private static void tickIdleResistance(ServerPlayer player, PlayerState state, JournalKind held) {
        Vec3 current = player.position();
        boolean dimensionChanged = state.lastDimension != player.level().dimension();
        if (dimensionChanged || state.lastPosition == null || current.distanceToSqr(state.lastPosition) > 0.0004) {
            state.lastPosition = current;
            state.stillSinceTick = serverTick;
            state.lastIdlePulseTick = serverTick;
            state.lastDimension = player.level().dimension();
            if (dimensionChanged) {
                cancelUltimateIfJournalChanged(player, held);
            }
            return;
        }

        if (held == JournalKind.DEXZYN && serverTick - state.stillSinceTick >= 100
                && serverTick - state.lastIdlePulseTick >= 100) {
            addEffect(player, MobEffects.RESISTANCE, 3 * 20, 0);
            state.lastIdlePulseTick = serverTick;
            spawn(player, JournalParticles.DEXZYN, 5);
        }
    }

    private static void tickImpact(ServerPlayer player, PlayerState state, JournalKind held) {
        boolean onGround = player.onGround();
        state.maxFallDistance = Math.max(state.maxFallDistance, (float) player.fallDistance);
        TimedUltimate active = ACTIVE_ULTIMATES.get(player.getUUID());
        if (held == JournalKind.DEXZYN && active != null && active.kind == JournalKind.DEXZYN
                && !state.wasOnGround && onGround && state.maxFallDistance >= 3.0F) {
            performImpact(player, state.maxFallDistance);
            state.maxFallDistance = 0.0F;
        }
        if (onGround) {
            state.maxFallDistance = 0.0F;
        }
        state.wasOnGround = onGround;
    }

    private static void performImpact(ServerPlayer attacker, float fallDistance) {
        double radius = 2.0;
        float damage = 4.0F + Math.min(Math.max(fallDistance - 1.5F, 0.0F) * 1.5F, 20.0F);
        AABB area = attacker.getBoundingBox().inflate(radius, 0.5, radius);
        DamageSource source = attacker.damageSources().playerAttack(attacker);

        for (LivingEntity target : attacker.level().getEntitiesOfClass(LivingEntity.class, area,
                living -> living != attacker && living.isAlive() && living.distanceToSqr(attacker) <= radius * radius
                        && (living instanceof Enemy || living instanceof ServerPlayer))) {
            if (target.hurtServer((ServerLevel) target.level(), source, damage)) {
                double dx = target.getX() - attacker.getX();
                double dz = target.getZ() - attacker.getZ();
                target.knockback(0.8, dx, dz);
            }
        }
        spawn(attacker, JournalParticles.DEXZYN, 24);
        attacker.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.8F, 1.35F);
    }

    private static void tickTimedUltimate(ServerPlayer player) {
        TimedUltimate active = ACTIVE_ULTIMATES.get(player.getUUID());
        if (active == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (JournalItems.kindOf(player.getOffhandItem()) != active.kind || now >= active.expiresAtMillis) {
            removeUltimateEffects(player, active.kind);
            ACTIVE_ULTIMATES.remove(player.getUUID());
            if (now < active.expiresAtMillis) {
                tell(player, active.kind.ultimateName() + " ended because the journal left your off-hand.", ChatFormatting.GRAY);
            }
            return;
        }
        if (active.kind == JournalKind.ARBY && active.secondaryExpiresAtMillis > 0L
                && now >= active.secondaryExpiresAtMillis) {
            removeIfAmplifier(player, MobEffects.REGENERATION, 2);
            active.secondaryExpiresAtMillis = 0L;
        }
    }

    private static void cancelUltimateIfJournalChanged(ServerPlayer player, JournalKind held) {
        TimedUltimate active = ACTIVE_ULTIMATES.get(player.getUUID());
        if (active != null && active.kind != held) {
            removeUltimateEffects(player, active.kind);
            ACTIVE_ULTIMATES.remove(player.getUUID());
        }
        PlayerState state = PLAYER_STATES.get(player.getUUID());
        if (state != null && state.bankruptcyUntilMillis > 0L && held != JournalKind.EINO) {
            state.bankruptcyUntilMillis = 0L;
            tell(player, "Bankruptcy was cancelled because Eino's Playbook left your off-hand.", ChatFormatting.GRAY);
        }
    }

    private static void removeUltimateEffects(ServerPlayer player, JournalKind kind) {
        switch (kind) {
            case ARBY -> {
                removeIfAmplifier(player, MobEffects.HEALTH_BOOST, 2);
                removeIfAmplifier(player, MobEffects.REGENERATION, 2);
            }
            case JOURNAL -> removeIfAmplifier(player, MobEffects.SPEED, 2);
            case DEXZYN -> removeIfAmplifier(player, MobEffects.JUMP_BOOST, 2);
            case ACE -> removeIfAmplifier(player, MobEffects.RESISTANCE, 4);
            default -> {
            }
        }
    }

    private static void clearPassiveEffects(ServerPlayer player, JournalKind kind) {
        switch (kind) {
            case EINO -> {
                removeIfAmplifier(player, MobEffects.HERO_OF_THE_VILLAGE, 2);
                removeIfAmplifier(player, MobEffects.RESISTANCE, 1);
            }
            case ARBY -> removeIfAmplifier(player, MobEffects.HEALTH_BOOST, 0);
            case JOURNAL -> removeIfAmplifier(player, MobEffects.SPEED, 0);
            case ACE -> removeIfAmplifier(player, MobEffects.HASTE, 1);
            case DEXZYN -> {
            }
        }
    }

    private static void ensureEffect(ServerPlayer player, Holder<MobEffect> effect, int amplifier, int duration) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getAmplifier() != amplifier || current.getDuration() < 25) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, true));
        }
    }

    private static void addEffect(ServerPlayer player, Holder<MobEffect> effect, int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true, true));
    }

    private static void removeIfAmplifier(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && current.getAmplifier() == amplifier) {
            player.removeEffect(effect);
        }
    }

    public static boolean isActionLocked(ServerPlayer player) {
        Long until = ACTION_LOCKS.get(player.getUUID());
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() >= until) {
            ACTION_LOCKS.remove(player.getUUID());
            return false;
        }
        return true;
    }

    public static void handleSuccessfulHit(ServerPlayer attacker, LivingEntity target, boolean blocked, float damageTaken) {
        if (blocked || damageTaken <= 0.0F) {
            return;
        }
        JournalKind held = JournalItems.kindOf(attacker.getOffhandItem());
        PlayerState state = PLAYER_STATES.get(attacker.getUUID());
        if (held == JournalKind.EINO && target instanceof ServerPlayer victim && state != null
                && state.bankruptcyUntilMillis > System.currentTimeMillis()) {
            state.bankruptcyUntilMillis = 0L;
            long until = System.currentTimeMillis() + 10L * SECOND;
            ACTION_LOCKS.put(victim.getUUID(), until);
            JournalSavedData.get(attacker.level().getServer()).setCooldown(attacker.getUUID(), JournalKind.EINO,
                    System.currentTimeMillis() + JournalKind.EINO.cooldownMillis());
            victim.displayClientMessage(Component.literal("Bankruptcy: your actions are disabled for 10 seconds.")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
            tell(attacker, "Bankruptcy disabled " + victim.getName().getString() + " for 10 seconds.", ChatFormatting.GREEN);
            announce(attacker, JournalKind.EINO);
            spawn(attacker, JournalParticles.EINO, 18);
        }

        if (held == JournalKind.JOURNAL && attacker.getMainHandItem().is(net.minecraft.tags.ItemTags.HOES)
                && target instanceof ServerPlayer victim) {
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20, 0, false, true, true));
            spawn(victim, JournalParticles.JOURNAL, 6);
        }
    }

    public static long cooldownRemaining(ServerPlayer player, JournalKind kind) {
        long until = JournalSavedData.get(player.level().getServer()).cooldownUntil(player.getUUID(), kind);
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public static void showCooldown(ServerPlayer player, JournalKind kind) {
        long remaining = cooldownRemaining(player, kind);
        if (remaining <= 0) {
            tell(player, kind.ultimateName() + " is ready.", ChatFormatting.GREEN);
        } else {
            tell(player, kind.ultimateName() + " cooldown: " + formatDuration(remaining) + ".", ChatFormatting.YELLOW);
        }
    }

    public static boolean claimJournal(ServerPlayer player, JournalKind kind) {
        JournalSavedData data = JournalSavedData.get(player.level().getServer());
        if (!data.claim(kind, player.getUUID(), player.getName().getString())) {
            player.displayClientMessage(Component.translatable("ability.journalsmp.claimed")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    public static String passiveOne(JournalKind kind) {
        return switch (kind) {
            case EINO -> "Master Trader — Hero of the Village III";
            case ARBY -> "Big Appetite — Health Boost I";
            case JOURNAL -> "Speedrunner — Speed I";
            case DEXZYN -> "Idle Protection — still for 5 seconds to gain Resistance I for 3 seconds";
            case ACE -> "Master Builder — Haste II";
        };
    }

    public static String passiveTwo(JournalKind kind) {
        return switch (kind) {
            case EINO -> "Village Guardian — Resistance II while a villager is within 5 blocks";
            case ARBY -> "Efficient Digestion — 50% less food exhaustion";
            case JOURNAL -> "Blindside — hoe hits on players inflict Blindness for 5 seconds";
            case DEXZYN -> "Sword Looting — 50% chance for one extra item per mob-drop stack on sword kills; disabled when the sword has Looting";
            case ACE -> "Extended Reach — place blocks up to one block farther away; containers, mining, and entity reach are unchanged";
        };
    }

    public static String cooldownText(JournalKind kind) {
        return formatDuration(kind.cooldownMillis());
    }

    private static String formatDuration(long milliseconds) {
        long totalSeconds = Math.max(0L, (milliseconds + 999L) / 1_000L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return minutes > 0 ? minutes + "m " + String.format("%02d", seconds) + "s" : seconds + "s";
    }

    private static void tell(ServerPlayer player, String message, ChatFormatting color) {
        player.displayClientMessage(Component.literal(message).withStyle(color), true);
    }

    private static void announce(ServerPlayer player, JournalKind kind) {
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(
                    Component.literal(player.getName().getString() + " activated " + kind.ultimateName() + "!")
                            .withStyle(ChatFormatting.GOLD),
                    false);
        }
    }

    private static void spawn(ServerPlayer player, SimpleParticleType particle, int count) {
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(particle, player.getX(), player.getY() + 1.0, player.getZ(),
                    count, 0.45, 0.55, 0.45, 0.025);
        }
    }

    public static void cleanup(ServerPlayer player) {
        PLAYER_STATES.remove(player.getUUID());
        ACTION_LOCKS.remove(player.getUUID());
        TimedUltimate active = ACTIVE_ULTIMATES.remove(player.getUUID());
        if (active != null) {
            removeUltimateEffects(player, active.kind);
        }
    }

    public static boolean isJournalItem(ItemStack stack, JournalKind kind) {
        return JournalItems.kindOf(stack) == kind;
    }

    private static final class PlayerState {
        private JournalKind lastHeld;
        private Vec3 lastPosition;
        private net.minecraft.resources.ResourceKey<Level> lastDimension;
        private long stillSinceTick;
        private long lastIdlePulseTick;
        private long bankruptcyUntilMillis;
        private boolean villageResistanceApplied;
        private boolean wasOnGround = true;
        private float maxFallDistance;
    }

    private static final class TimedUltimate {
        private final JournalKind kind;
        private final long expiresAtMillis;
        private long secondaryExpiresAtMillis;

        private TimedUltimate(JournalKind kind, long expiresAtMillis, long secondaryExpiresAtMillis) {
            this.kind = kind;
            this.expiresAtMillis = expiresAtMillis;
            this.secondaryExpiresAtMillis = secondaryExpiresAtMillis;
        }
    }
}
