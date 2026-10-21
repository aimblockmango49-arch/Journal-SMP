package dev.journalsmp.event;

import dev.journalsmp.JournalItems;
import dev.journalsmp.JournalKind;
import java.util.ArrayList;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerPlayer;

public final class JournalLootEvents {
    private JournalLootEvents() {
    }

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
            if (!context.hasParameter(LootContextParams.THIS_ENTITY)
                    || !context.hasParameter(LootContextParams.DAMAGE_SOURCE)) {
                return;
            }

            Entity victim = context.getParameter(LootContextParams.THIS_ENTITY);
            if (!(victim instanceof LivingEntity living)
                    || living.getLootTable().isEmpty()
                    || entry.unwrapKey().filter(living.getLootTable().get()::equals).isEmpty()) {
                return;
            }

            DamageSource source = context.getParameter(LootContextParams.DAMAGE_SOURCE);
            if (!(source.getEntity() instanceof ServerPlayer player)
                    || !source.getWeaponItem().is(ItemTags.SWORDS)
                    || JournalItems.kindOf(player.getOffhandItem()) != JournalKind.DEXZYN) {
                return;
            }

            Holder<Enchantment> looting = player.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.LOOTING);
            if (EnchantmentHelper.getItemEnchantmentLevel(looting, source.getWeaponItem()) > 0) {
                return;
            }

            for (ItemStack stack : new ArrayList<>(drops)) {
                if (!stack.isEmpty() && context.getRandom().nextFloat() < 0.5F) {
                    ItemStack bonus = stack.copy();
                    bonus.setCount(1);
                    drops.add(bonus);
                }
            }
        });
    }
}
