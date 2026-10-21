package dev.journalsmp;

import dev.journalsmp.ability.AbilityManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public final class JournalItem extends Item {
    private final JournalKind kind;

    public JournalItem(JournalKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public JournalKind kind() {
        return kind;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            AbilityManager.activate(serverPlayer, kind);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Passive I — " + AbilityManager.passiveOne(kind))
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Passive II — " + AbilityManager.passiveTwo(kind))
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Ultimate — " + kind.ultimateName())
                .withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.literal("Cooldown: " + AbilityManager.cooldownText(kind))
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("Must be held in the off-hand.")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
