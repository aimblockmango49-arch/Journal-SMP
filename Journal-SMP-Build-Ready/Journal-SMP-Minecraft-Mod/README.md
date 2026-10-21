# Journal SMP

A Fabric mod for Minecraft Java Edition 1.21.11 with five off-hand journals, custom abilities and particles, shaped recipes, persistent cooldowns, and once-per-world crafting claims.

## Build

Requirements: Java 21. From this directory run:

```sh
./gradlew build
```

The remapped mod is written to `build/libs/journalsmp-1.0.0.jar`. `build/libs/journalsmp-1.0.0-sources.jar` contains Java sources; this project directory also includes the Gradle wrapper and the complete source/resource tree.

## Install

1. Install a Fabric 1.21.11 dedicated server with Java 21 and Fabric Loader 0.18.1 or newer.
2. Add Fabric API `0.141.1+1.21.11` or newer to the server's `mods` directory.
3. Copy `journalsmp-1.0.0.jar` into that same `mods` directory.
4. Install the same mod and Fabric API version on every connecting client. The custom item textures and particles are supplied by the client-side mod entrypoint.
5. Start the server. The mod stores crafting claims and cooldown timestamps in the world save; back up the world before changing mod versions or removing the mod.

## Journals

Abilities work only while the matching journal is in the player's off-hand. Right-click the journal to activate its ultimate. `/ability` lists the journals; `/ability eino`, `/ability arby`, `/ability journal`, `/ability dexzyn`, and `/ability ace` show details. Aliases `einos`, `arbys`, and `dexzyns` are supported.

### Eino's Playbook

- **Master Trader:** Hero of the Village III while held.
- **Village Guardian:** Resistance II while a villager is within 5 blocks.
- **Bankruptcy (5-minute cooldown):** Arm the ultimate, then land an unblocked melee hit on another player within 10 seconds to disable their attacks, block breaking, item use, and block use for 10 seconds. A shield-blocked hit does not consume it. The cooldown starts only when the effect succeeds.

Recipe:

```text
Emerald | Gold Ingot | Emerald
Diamond | Book       | Totem of Undying
Emerald | Gold Ingot | Emerald
```

### Arby's Menu

- **Big Appetite:** Health Boost I while held.
- **Efficient Digestion:** 50% less food exhaustion while held.
- **All You Can Eat (3-minute cooldown):** Health Boost III for 60 seconds and Regeneration III for 10 seconds.

Recipe:

```text
Golden Apple | Golden Carrot | Golden Apple
Cooked Beef  | Book          | Cooked Beef
Golden Carrot| Honey Bottle  | Enchanted Golden Apple
```

### Journal's Journal

- **Speedrunner:** Speed I while held.
- **Blindside:** Successful hits on players with a vanilla hoe apply Blindness for 5 seconds.
- **World Record (3-minute cooldown):** Speed III for 60 seconds.

Recipe:

```text
Blaze Rod     | Ender Pearl    | Blaze Rod
Firework Rocket| Book          | Diamond
Ender Pearl   | Netherite Scrap| (empty)
```

### Dexzyn's AFK Log

- **Idle Protection:** After 5 seconds without moving, gain Resistance I for 3 seconds; it refreshes every 5 seconds while still.
- **Sword Looting:** With a sword and no Looting enchantment, each mob-drop stack has a 50% chance to grant one extra item on a sword kill.
- **AFK to Impact (3-minute cooldown):** Jump Boost III for 20 seconds. Landing after a fall of at least 3 blocks creates a 2-block-radius impact. Damage is `4 + 1.5 × max(0, fall distance − 1.5)`, capped at 24; the attack can be blocked by a properly raised shield.

Recipe:

```text
Iron Block | Phantom Membrane | Iron Block
Slime Block | Book            | Heavy Core
Phantom Membrane | Clock      | (empty)
```

### Ace's Blueprint

- **Master Builder:** Haste II while held.
- **Extended Reach:** With a block item in the main hand and this journal in the off-hand, place blocks up to one block farther away. Mining, entity reach, and container-block interaction range are not extended.
- **Masterpiece (3-minute cooldown):** Resistance V for 10 seconds. Normal void damage still applies.

Recipe:

```text
Iron Block | Diamond | Iron Block
Obsidian   | Book    | Diamond Pickaxe
Copper Block | Diamond | Netherite Ingot
```

## One-time crafting and persistence

Each journal can be claimed once per world. Taking a journal from a crafting result slot atomically records its journal type, the player's UUID, and name. Vanilla Crafter automation is also guarded; an automated first craft records the Crafter location instead. Claims are not restored if an item is lost or destroyed. Cooldowns are per player and journal, stored as timestamps in the world save, and survive death, disconnects, and restarts.

## Supplied textures

Item textures are in `src/main/resources/assets/journalsmp/textures/item/`:

| Supplied artwork | Resource path |
|---|---|
| `einos_journal` | `einos_playbook.png` |
| `arbys_journal` | `arbys_menu.png` |
| `journnals_journal` | `journals_journal.png` |
| `dezzynsjournal` | `dexzyns_afklog.png` |
| `acejournal` | `aces_blueprint.png` |

Particle artwork is in `src/main/resources/assets/journalsmp/textures/particle/`: the supplied Eino, Arby, Journal, Dexzyn, and Ace particle images are each registered to the corresponding particle effect.

## Verification and limitations

- The project compiles both common and client source sets and builds a remapped Fabric JAR.
- The five crafting recipes and all supplied item/particle textures are included.
- A Minecraft client/server play session was not available for end-to-end visual and multiplayer testing here. In particular, test the extended placement ray, shield-facing impact behavior, recipe-claim persistence after restart, and all ultimate timing in a disposable world before using the mod on a live SMP.
- The once-only crafting guard covers vanilla crafting result slots and the vanilla Crafter block. Third-party automated crafting systems are not covered.
- The server action lock covers attacks, block breaking, item use, and block use. It does not attempt to block movement or every action added by other mods.
- As with other Fabric mods, every client and the dedicated server must use a compatible Minecraft, Fabric Loader, and Fabric API version.
