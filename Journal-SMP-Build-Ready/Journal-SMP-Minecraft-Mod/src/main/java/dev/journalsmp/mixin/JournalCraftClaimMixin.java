package dev.journalsmp.mixin;

import dev.journalsmp.JournalItems;
import dev.journalsmp.JournalKind;
import dev.journalsmp.ability.AbilityManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResultSlot.class)
public abstract class JournalCraftClaimMixin {
    @Shadow
    @Final
    private Player player;

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void journalsmp$claimJournalOnTake(int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ResultSlot slot = (ResultSlot) (Object) this;
        JournalKind kind = JournalItems.kindOf(slot.getItem());
        if (kind == null) {
            return;
        }

        if (!AbilityManager.claimJournal(serverPlayer, kind)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        serverPlayer.displayClientMessage(
                Component.literal("You claimed " + kind.itemId() + " for this server."), false);
    }
}
