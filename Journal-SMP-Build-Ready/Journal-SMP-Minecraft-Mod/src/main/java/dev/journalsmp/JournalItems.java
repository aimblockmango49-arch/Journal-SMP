package dev.journalsmp;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class JournalItems {
    private static final Map<JournalKind, JournalItem> ITEMS = new EnumMap<>(JournalKind.class);

    public static Item EINOS_PLAYBOOK;
    public static Item ARBYS_MENU;
    public static Item JOURNALS_JOURNAL;
    public static Item DEXZYNS_AFKLOG;
    public static Item ACES_BLUEPRINT;

    private JournalItems() {
    }

    public static void register() {
        for (JournalKind kind : JournalKind.values()) {
            Identifier id = Identifier.fromNamespaceAndPath(JournalSmpMod.MOD_ID, kind.itemId());
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
            JournalItem item = new JournalItem(kind, new Item.Properties().setId(key));
            ITEMS.put(kind, Registry.register(BuiltInRegistries.ITEM, id, item));
        }

        EINOS_PLAYBOOK = ITEMS.get(JournalKind.EINO);
        ARBYS_MENU = ITEMS.get(JournalKind.ARBY);
        JOURNALS_JOURNAL = ITEMS.get(JournalKind.JOURNAL);
        DEXZYNS_AFKLOG = ITEMS.get(JournalKind.DEXZYN);
        ACES_BLUEPRINT = ITEMS.get(JournalKind.ACE);
    }

    public static JournalKind kindOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (Map.Entry<JournalKind, JournalItem> entry : ITEMS.entrySet()) {
            if (stack.is(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
