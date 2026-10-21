package dev.journalsmp.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.journalsmp.JournalKind;
import dev.journalsmp.ability.AbilityManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class JournalCommands {
    private JournalCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ability")
                .executes(context -> showOverview(context.getSource()))
                .then(Commands.literal("eino").executes(context -> showJournal(context.getSource(), JournalKind.EINO)))
                .then(Commands.literal("einos").executes(context -> showJournal(context.getSource(), JournalKind.EINO)))
                .then(Commands.literal("arby").executes(context -> showJournal(context.getSource(), JournalKind.ARBY)))
                .then(Commands.literal("arbys").executes(context -> showJournal(context.getSource(), JournalKind.ARBY)))
                .then(Commands.literal("journal").executes(context -> showJournal(context.getSource(), JournalKind.JOURNAL)))
                .then(Commands.literal("dexzyn").executes(context -> showJournal(context.getSource(), JournalKind.DEXZYN)))
                .then(Commands.literal("dexzyns").executes(context -> showJournal(context.getSource(), JournalKind.DEXZYN)))
                .then(Commands.literal("ace").executes(context -> showJournal(context.getSource(), JournalKind.ACE))));
    }

    private static int showOverview(CommandSourceStack source) {
        line(source, "JOURNAL SMP — ABILITIES", ChatFormatting.GOLD);
        line(source, "Five unique journals grant powers only while held in the off-hand.", ChatFormatting.GRAY);
        line(source, "/ability eino — Eino's Playbook (trading and villagers)", ChatFormatting.GREEN);
        line(source, "/ability arby — Arby's Menu (food and survival)", ChatFormatting.GOLD);
        line(source, "/ability journal — Journal's Journal (speed and movement)", ChatFormatting.AQUA);
        line(source, "/ability dexzyn — Dexzyn's AFK Log (idleness and impact)", ChatFormatting.BLUE);
        line(source, "/ability ace — Ace's Blueprint (building and defense)", ChatFormatting.DARK_AQUA);
        return 1;
    }

    private static int showJournal(CommandSourceStack source, JournalKind kind) {
        switch (kind) {
            case EINO -> {
                line(source, "EINO'S PLAYBOOK", ChatFormatting.GREEN);
                line(source, "PASSIVE 1 — MASTER TRADER", ChatFormatting.GOLD);
                line(source, "Permanent Hero of the Village III.", ChatFormatting.GRAY);
                line(source, "PASSIVE 2 — VILLAGE GUARDIAN", ChatFormatting.GOLD);
                line(source, "Gain Resistance II while a villager is within 5 blocks.", ChatFormatting.GRAY);
                line(source, "ULTIMATE — BANKRUPTCY", ChatFormatting.RED);
                line(source, "Arm it, then land an unblocked melee hit on a player to disable their actions for 10 seconds.", ChatFormatting.GRAY);
                line(source, "A shield-blocked hit does not consume the ultimate. The arm expires after 10 seconds; cooldown begins on a successful hit.", ChatFormatting.GRAY);
                line(source, "COOLDOWN: 5 MINUTES. " + offhandText(), ChatFormatting.YELLOW);
            }
            case ARBY -> {
                line(source, "ARBY'S MENU", ChatFormatting.GOLD);
                line(source, "PASSIVE 1 — BIG APPETITE", ChatFormatting.GOLD);
                line(source, "Permanent Health Boost I while held in the off-hand.", ChatFormatting.GRAY);
                line(source, "PASSIVE 2 — EFFICIENT DIGESTION", ChatFormatting.GOLD);
                line(source, "Food exhaustion is reduced by 50% while held in the off-hand.", ChatFormatting.GRAY);
                line(source, "ULTIMATE — ALL YOU CAN EAT", ChatFormatting.RED);
                line(source, "Health Boost III for 60 seconds and Regeneration III for 10 seconds.", ChatFormatting.GRAY);
                line(source, "COOLDOWN: 3 MINUTES. " + offhandText(), ChatFormatting.YELLOW);
            }
            case JOURNAL -> {
                line(source, "JOURNAL'S JOURNAL", ChatFormatting.AQUA);
                line(source, "PASSIVE 1 — SPEEDRUNNER", ChatFormatting.GOLD);
                line(source, "Permanent Speed I while held in the off-hand.", ChatFormatting.GRAY);
                line(source, "PASSIVE 2 — BLINDSIDE", ChatFormatting.GOLD);
                line(source, "Successful hits on players with any vanilla hoe inflict Blindness for 5 seconds.", ChatFormatting.GRAY);
                line(source, "ULTIMATE — WORLD RECORD", ChatFormatting.RED);
                line(source, "Speed III for 60 seconds. Ends when the journal leaves your off-hand.", ChatFormatting.GRAY);
                line(source, "COOLDOWN: 3 MINUTES. " + offhandText(), ChatFormatting.YELLOW);
            }
            case DEXZYN -> {
                line(source, "DEXZYN'S AFK LOG", ChatFormatting.BLUE);
                line(source, "PASSIVE 1 — IDLE PROTECTION", ChatFormatting.GOLD);
                line(source, "After 5 seconds of stillness, gain Resistance I for 3 seconds; refreshes every 5 seconds while still.", ChatFormatting.GRAY);
                line(source, "PASSIVE 2 — SWORD LOOTING", ChatFormatting.GOLD);
                line(source, "On sword kills, each mob-drop stack has a 50% chance to grant one extra item. This bonus is disabled if the sword has Looting.", ChatFormatting.GRAY);
                line(source, "ULTIMATE — AFK TO IMPACT", ChatFormatting.RED);
                line(source, "Jump Boost III for 20 seconds. A fall of at least 3 blocks that ends in a landing creates a 2-block area impact.", ChatFormatting.GRAY);
                line(source, "Impact damage: 4 + 1.5 × max(0, fall distance − 1.5), capped at 24. Shield use is respected.", ChatFormatting.GRAY);
                line(source, "COOLDOWN: 3 MINUTES. " + offhandText(), ChatFormatting.YELLOW);
            }
            case ACE -> {
                line(source, "ACE'S BLUEPRINT", ChatFormatting.DARK_AQUA);
                line(source, "PASSIVE 1 — MASTER BUILDER", ChatFormatting.GOLD);
                line(source, "Permanent Haste II while held in the off-hand.", ChatFormatting.GRAY);
                line(source, "PASSIVE 2 — EXTENDED REACH", ChatFormatting.GOLD);
                line(source, "Place blocks up to one block farther away. Mining and entity range are unchanged; container blocks cannot be reached remotely.", ChatFormatting.GRAY);
                line(source, "ULTIMATE — MASTERPIECE", ChatFormatting.RED);
                line(source, "Resistance V for 10 seconds; normal void-damage rules still apply.", ChatFormatting.GRAY);
                line(source, "COOLDOWN: 3 MINUTES. " + offhandText(), ChatFormatting.YELLOW);
            }
        }
        return 1;
    }

    private static String offhandText() {
        return "Hold the journal in your off-hand.";
    }

    private static void line(CommandSourceStack source, String text, ChatFormatting color) {
        source.sendSuccess(() -> Component.literal(text).withStyle(color), false);
    }
}
