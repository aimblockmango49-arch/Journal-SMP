package dev.journalsmp.mixin;

import dev.journalsmp.JournalItems;
import dev.journalsmp.JournalKind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class PlayerFoodExhaustionMixin {
    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float journalsmp$reduceFoodExhaustion(float amount) {
        Player player = (Player) (Object) this;
        if (JournalItems.kindOf(player.getOffhandItem()) == JournalKind.ARBY) {
            return amount * 0.5F;
        }
        return amount;
    }
}
