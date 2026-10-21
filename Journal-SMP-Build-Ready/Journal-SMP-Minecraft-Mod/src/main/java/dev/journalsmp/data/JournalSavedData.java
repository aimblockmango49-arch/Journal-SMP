package dev.journalsmp.data;

import dev.journalsmp.JournalKind;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.server.MinecraftServer;

public final class JournalSavedData extends SavedData {
    private static final String DATA_ID = "journalsmp_server_state";
    private static final Codec<JournalSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("cooldowns").forGetter(data -> data.cooldowns),
            Codec.STRING.listOf().fieldOf("claimed").forGetter(data -> new ArrayList<>(data.claimed)),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("claimants").forGetter(data -> data.claimants)
    ).apply(instance, JournalSavedData::new));
    private static final SavedDataType<JournalSavedData> TYPE =
            new SavedDataType<>(DATA_ID, JournalSavedData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<String> claimed = new HashSet<>();
    private final Map<String, String> claimants = new HashMap<>();

    public JournalSavedData() {
    }

    private JournalSavedData(Map<String, Long> cooldowns, java.util.List<String> claimed,
                             Map<String, String> claimants) {
        this.cooldowns.putAll(cooldowns);
        this.claimed.addAll(claimed);
        this.claimants.putAll(claimants);
    }

    public static JournalSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public long cooldownUntil(UUID player, JournalKind kind) {
        return cooldowns.getOrDefault(player + ":" + kind.itemId(), 0L);
    }

    public void setCooldown(UUID player, JournalKind kind, long untilMillis) {
        cooldowns.put(player + ":" + kind.itemId(), untilMillis);
        setDirty();
    }

    public boolean isClaimed(JournalKind kind) {
        return claimed.contains(kind.itemId());
    }

    public boolean claim(JournalKind kind, UUID playerId, String playerName) {
        if (!claimed.add(kind.itemId())) {
            return false;
        }
        claimants.put(kind.itemId(), playerId + " (" + playerName + ")");
        setDirty();
        return true;
    }

    public boolean claimAutomated(JournalKind kind, String source) {
        if (!claimed.add(kind.itemId())) {
            return false;
        }
        claimants.put(kind.itemId(), source);
        setDirty();
        return true;
    }

    public static void flush(MinecraftServer server) {
        JournalSavedData data = get(server);
        data.setDirty();
    }
}
