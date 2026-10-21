package dev.journalsmp.mixin;

import dev.journalsmp.JournalItems;
import dev.journalsmp.JournalKind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class AcePlacementReachServerMixin {
    @Shadow
    public ServerPlayer player;

    @Redirect(
            method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isWithinBlockInteractionRange(Lnet/minecraft/core/BlockPos;D)Z"
            )
    )
    private boolean journalsmp$extendBlockPlacementRange(ServerPlayer targetPlayer, BlockPos pos, double margin) {
        if (targetPlayer.isWithinBlockInteractionRange(pos, margin)) {
            return true;
        }
        if (JournalItems.kindOf(player.getOffhandItem()) != JournalKind.ACE
                || !(player.getMainHandItem().getItem() instanceof BlockItem)) {
            return false;
        }

        BlockState target = player.level().getBlockState(pos);
        MenuProvider menuProvider = target.getMenuProvider(player.level(), pos);
        if (menuProvider != null) {
            return false;
        }

        return targetPlayer.isWithinBlockInteractionRange(pos, margin + 1.0);
    }
}
