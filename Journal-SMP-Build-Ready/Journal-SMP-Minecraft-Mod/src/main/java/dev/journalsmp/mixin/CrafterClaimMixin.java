package dev.journalsmp.mixin;

import dev.journalsmp.JournalItems;
import dev.journalsmp.JournalKind;
import dev.journalsmp.data.JournalSavedData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterBlock.class)
public abstract class CrafterClaimMixin {
    @Inject(method = "dispenseFrom", at = @At("HEAD"), cancellable = true)
    private void journalsmp$preventClaimedAutoCraft(BlockState state, ServerLevel level, BlockPos pos,
                                                    CallbackInfo ci) {
        if (!(level.getBlockEntity(pos) instanceof CrafterBlockEntity crafter)) {
            return;
        }
        var input = crafter.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> recipe = CrafterBlock.getPotentialResults(level, input);
        if (recipe.isEmpty()) {
            return;
        }

        ItemStack result = recipe.get().value().assemble(input, level.registryAccess());
        JournalKind kind = JournalItems.kindOf(result);
        if (kind != null && JournalSavedData.get(level.getServer()).isClaimed(kind)) {
            ci.cancel();
        }
    }

    @Inject(method = "dispenseItem", at = @At("HEAD"), cancellable = true)
    private void journalsmp$claimAutoCraft(ServerLevel level, BlockPos pos, CrafterBlockEntity crafter,
                                           ItemStack result, BlockState state, RecipeHolder<?> recipe,
                                           CallbackInfo ci) {
        JournalKind kind = JournalItems.kindOf(result);
        if (kind == null) {
            return;
        }

        String source = "Automated crafter at " + level.dimension() + " " + pos.toShortString();
        if (!JournalSavedData.get(level.getServer()).claimAutomated(kind, source)) {
            ci.cancel();
        }
    }
}
