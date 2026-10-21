package dev.journalsmp.mixin;

import dev.journalsmp.JournalItems;
import dev.journalsmp.JournalKind;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class AcePlacementReachClientMixin {
    private static final ThreadLocal<Boolean> JOURNAL_SMP_REPLAYING_PLACEMENT =
            ThreadLocal.withInitial(() -> false);

    @Shadow
    public abstract InteractionResult useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult);

    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void journalsmp$extendPlacementRay(LocalPlayer player, InteractionHand hand, BlockHitResult original,
                                               CallbackInfoReturnable<InteractionResult> cir) {
        if (JOURNAL_SMP_REPLAYING_PLACEMENT.get()
                || hand != InteractionHand.MAIN_HAND
                || JournalItems.kindOf(player.getOffhandItem()) != JournalKind.ACE
                || !(player.getMainHandItem().getItem() instanceof BlockItem)) {
            return;
        }

        double normalReach = player.blockInteractionRange();
        Vec3 eye = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0F);
        BlockHitResult extended = player.level().clip(new ClipContext(
                eye,
                eye.add(direction.scale(normalReach + 1.0)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (extended.getType() != HitResult.Type.BLOCK) {
            return;
        }
        double extendedDistance = extended.getLocation().distanceTo(eye);
        if (extendedDistance <= normalReach + 0.001 || extendedDistance > normalReach + 1.001) {
            return;
        }
        if (original.getType() == HitResult.Type.BLOCK
                && original.getLocation().distanceTo(eye) <= normalReach + 0.001) {
            return;
        }

        JOURNAL_SMP_REPLAYING_PLACEMENT.set(true);
        try {
            cir.setReturnValue(useItemOn(player, hand, extended));
        } finally {
            JOURNAL_SMP_REPLAYING_PLACEMENT.set(false);
        }
    }
}
