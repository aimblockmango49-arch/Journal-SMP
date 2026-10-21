package dev.journalsmp;

public enum JournalKind {
    EINO("einos_playbook", "Eino's Playbook", "Bankruptcy", 300_000L),
    ARBY("arbys_menu", "Arby's Menu", "All You Can Eat", 180_000L),
    JOURNAL("journals_journal", "Journal's Journal", "World Record", 180_000L),
    DEXZYN("dexzyns_afklog", "Dexzyn's AFK Log", "AFK to Impact", 180_000L),
    ACE("aces_blueprint", "Ace's Blueprint", "Masterpiece", 180_000L);

    private final String itemId;
    private final String displayName;
    private final String ultimateName;
    private final long cooldownMillis;

    JournalKind(String itemId, String displayName, String ultimateName, long cooldownMillis) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.ultimateName = ultimateName;
        this.cooldownMillis = cooldownMillis;
    }

    public String itemId() {
        return itemId;
    }

    public String displayName() {
        return displayName;
    }

    public String ultimateName() {
        return ultimateName;
    }

    public long cooldownMillis() {
        return cooldownMillis;
    }
}
