package dev.journalsmp.event;

import dev.journalsmp.ability.AbilityManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

public final class JournalEvents {
    private JournalEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(AbilityManager::tickServer);

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (source.getEntity() instanceof ServerPlayer attacker) {
                AbilityManager.handleSuccessfulHit(attacker, entity, blocked, damageTaken);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer && AbilityManager.isActionLocked(serverPlayer)) {
                serverPlayer.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Bankruptcy prevents you from attacking.")
                                .withStyle(net.minecraft.ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player instanceof ServerPlayer serverPlayer && AbilityManager.isActionLocked(serverPlayer)) {
                serverPlayer.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Bankruptcy prevents you from using items.")
                                .withStyle(net.minecraft.ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer
                    && AbilityManager.isActionLocked(serverPlayer)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                !(player instanceof ServerPlayer serverPlayer) || !AbilityManager.isActionLocked(serverPlayer));

        ServerPlayerEvents.LEAVE.register(AbilityManager::cleanup);
    }
}
