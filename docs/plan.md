# Color Challenge Implementation Plan

This document outlines the implementation plan for transforming the existing MinersMarket-based codebase into the Color Challenge mod, as described in [draft.md](draft.md).

**Target**: Get Fabric 1.21.1 working first.

## Current State

Based on the MinersMarket codebase (mine ores, sell to NPC, race to 10,000 gold). Package names and Mod ID already renamed to `colorchallenge`. The following transformation is needed:

| Old (MinersMarket) | New (Color Challenge) |
|---|---|
| Sell ores for gold | Collect and deliver all 16 dyes |
| Goal: 10,000 gold | Goal: Deliver all 16 dye colors |
| PriceList (item → gold conversion) | Not needed (remove) |
| Price fluctuation events | Not needed (remove) |
| Miner's Pickaxe | Instructions (map item for checking delivery status) |
| HUD: Gold display | HUD: Delivery count (X / 16) |
| — | Add rare items to village chests |
| — | Add trades to Wandering Trader after 40 minutes |

## Package Structure

- Base package: `com.colorchallenge`
- Sub-packages:
  - `registry` - Item, Block, Entity registration
  - `state` - Game state management (delivery tracking)
  - `entity` - Staff entity (dye delivery NPC)
  - `block` - Game Start Block, Game Reset Block
  - `item` - Instructions item
  - `hud` - HUD rendering (client-only)
  - `network` - Network packet synchronization
  - `event` - Event listeners (player spawn, tick, etc.)
  - `structure` - Delivery Area structure placement
  - `loot` - Loot table modifications (village chests)

## Phases

### Phase 1: Code Cleanup & Remove Old Systems

Remove MinersMarket-specific systems and prepare the foundation for Color Challenge.

- [x] 1-1. Delete `PriceList.java`
  - `common-shared/src/main/java/com/colorchallenge/trade/PriceList.java`
  - Delete the entire `trade` package
- [x] 1-2. Remove price fluctuation event system from `GameStateManager`
  - Remove all price event related fields and methods
  - Remove price event debug command
- [x] 1-3. Remove price event data from `GameStateSyncPacket`
  - Remove price event active/remaining/multiplier data transmission
- [x] 1-4. Remove price event data from `ClientGameState`
  - Remove price event related fields and methods
- [x] 1-5. Remove price event display from `GameHudOverlay`
  - Remove price event rendering code
- [x] 1-6. Rename `MINERS_MARKET_TAB` to `COLOR_CHALLENGE_TAB` in `ModCreativeTab`
- [x] 1-7. Remove Miner's Pickaxe
  - Remove `MINERS_PICKAXE` registration from `ModItems`
  - Delete related model/texture files
  - Remove grant logic from `PlayerSpawnHandler` (will be replaced with Instructions in Phase 7)
- [x] 1-8. Remove `salesAmounts` save/load from `GameStateSavedData`
- [x] 1-9. Delete old coin texture `textures/gui/coin.png`
- [x] 1-10. Verify build passes on Fabric 1.21.1
  - `./gradlew :fabric:build -Ptarget_mc_version=1.21.1`

### Phase 2: Dye Delivery Data Model

Replace gold/sales tracking with per-player dye delivery status tracking.

- [x] 2-1. Change `GameStateManager` data model
  - `Map<UUID, Long> salesAmounts` → `Map<UUID, Set<DyeColor>> deliveredDyes`
  - `TARGET_SALES = 10000` → `TARGET_DYE_COUNT = 16` (all 16 colors)
  - Add methods:
    - `deliverDye(UUID player, DyeColor color)` — mark a dye as delivered
    - `getDeliveredDyes(UUID player)` — get player's delivered dye set
    - `getDeliveredCount(UUID player)` — get delivered count
    - `hasDeliveredAll(UUID player)` — check if all colors delivered
  - Rename `canSell()` → `canDeliver()` (done in Phase 1)
  - Remove old methods: `addSalesAmount()`, `getSalesAmount()`, `hasReachedTarget()`, etc. (done in Phase 1)
- [x] 2-2. Update `GameStateSavedData` for dye delivery persistence
  - NBT save: player UUID → int array of delivered `DyeColor` ordinals
  - NBT load: reverse conversion with bounds checking
- [x] 2-3. Reuse `FinishedPlayer` as-is (UUID, name, finish time in ticks)
- [x] 2-4. Reuse `GameState` enum as-is (`NOT_STARTED`, `IN_PROGRESS`, `ENDED`)
- [x] 2-5. Verify build

### Phase 3: Network Sync Update

Sync dye delivery status to clients.

- [x] 3-1. Update `GameStateSyncPacket`
  - Change `salesAmount` (long) → `deliveredDyes` (Set\<DyeColor\>)
  - Transmit DyeColor as ordinal() int values
  - Price event data already removed in Phase 1
- [x] 3-2. Update `ClientGameState`
  - `salesAmount` → `deliveredDyes` (Set\<DyeColor\>)
  - `getSalesAmount()` → `getDeliveredDyes()`, `getDeliveredCount()`
- [x] 3-3. Verify build

### Phase 4: Staff Entity — Dye Delivery Interaction

Change StaffEntity right-click interaction from ore selling to dye delivery.

- [x] 4-1. Rewrite `StaffEntity.mobInteract()`
  - Check if held item is a vanilla dye item (one of 16 DyeColors)
  - If dye:
    - Verify game is `IN_PROGRESS`
    - Verify this color is not yet delivered (show message if already delivered)
    - Consume 1 dye item
    - Call `GameStateManager.deliverDye()`
    - Show delivery success message (action bar)
    - Check win condition if all 16 colors delivered
  - If not a dye item or empty hand:
    - Show "please hold a dye" message
- [x] 4-2. Win condition logic
  - Process player as winner when `hasDeliveredAll()` returns true
  - Reuse existing win effects (title display, message broadcast)
  - Transition state to `ENDED` (first player to complete only)
- [x] 4-3. Verify build and test

### Phase 5: HUD Update

Change gold display to dye delivery count display.

- [x] 5-1. Change `GameHudOverlay` sales display to delivery count
  - Format: `2 / 16` (top-right, right-aligned)
  - No coin icon needed (text only or dye icon)
- [x] 5-2. Reuse play time display as-is
  - Position: top-right, below delivery count
  - Format: `MM:SS`
- [x] 5-3. Change floating text (sales notification) to delivery notification
  - `"+X gold"` → `"+Color Name"` style display (removed — not needed for dye delivery)
- [x] 5-4. Reuse ranking/finisher list display as-is
- [x] 5-5. Verify build

### Phase 6: Instructions Item

Implement the Instructions item to replace Miner's Pickaxe, allowing players to check delivery status.

- [x] 6-1. Register `INSTRUCTIONS` in `ModItems`
  - ID: `colorchallenge:instructions`
  - Appearance: vanilla filled_map item look
- [x] 6-2. Implement Instructions item use action
  - Right-click opens delivery status screen
  - Display 16 dye colors in a grid layout
  - Delivered dyes shown with green background
- [x] 6-3. Implement delivery status screen (Screen)
  - Client-side GUI Screen
  - Get delivery status from `ClientGameState`
- [ ] 6-4. Instructions item is not dropped on death
  - Control via item settings or event handler (deferred to Phase 7)
- [x] 6-5. Set up item model/texture
- [x] 6-6. Verify build

### Phase 7: Player Spawn & Initial Equipment Update

Change initial equipment to Color Challenge specification.

- [x] 7-1. Change `PlayerSpawnHandler` initial equipment
  - Grant Instructions item
  - Grant 1 stack of Bread (keep existing)
  - Grant permanent Night Vision (keep existing)
- [x] 7-2. Instructions item death persistence
  - Re-grant on respawn if not in inventory (via grantEquipment)
  - Duplicate check prevents multiple copies
- [x] 7-3. Verify build

### Phase 8: Delivery Area Structure Update

Update the existing market structure as the Delivery Area.

- [x] 8-1. Review and update structure template
  - `delivery_area.nbt` already in place, no changes needed
- [x] 8-2. Clean up structure names
  - Renamed worldgen JSON files from `market` → `delivery_area`
  - Updated `start_pool` and `location` references
- [x] 8-3. Update chest contents
  - Updated `DeliveryAreaGenerator.fillChestWithEquipment()` to include Instructions item
  - Keep bread
- [x] 8-4. Verify build

### Phase 9: Village Chests & Wandering Trader Extension

Add systems to ease dye acquisition.

- [x] 9-1. Add the following to village chest loot tables
  - 1 Jungle Sapling (for Cocoa Beans → Brown Dye ingredient)
  - 1 Cactus (Green Dye ingredient)
  - 1 Sea Pickle (Lime Dye ingredient)
  - Used Architectury `LootEvent.MODIFY_LOOT_TABLE`
- [x] 9-2. Add items to Wandering Trader trades after 40 minutes
  - Same 3 items as above (1 Emerald each)
  - `isTraderItemsUnlocked()` checks `playTime >= 48,000 ticks`
  - Used `InteractionEvent.INTERACT_ENTITY` + scoreboard tag to inject trades
- [x] 9-3. Verify build and test

### Phase 10: Language Files & Text Update

Update all game messages to Color Challenge specification.

- [x] 10-1. Update `en_us.json`
  - Change all gold/sales messages to dye delivery messages
  - Update Staff entity descriptions
  - Add new messages (delivery success, already delivered, please hold a dye, etc.)
  - Add Instructions item name
- [x] 10-2. Update `ja_jp.json`
  - Japanese translations of the above
- [x] 10-3. Verify build

### Phase 11: Integration Testing & Polish

Integrate and test all features.

- [ ] 11-1. Full build and launch test on Fabric 1.21.1
- [ ] 11-2. End-to-end game flow verification
  - New world creation → Delivery Area generation
  - Initial equipment (Instructions, Bread) granted
  - Night Vision applied
  - Game Start Block starts the game
  - Deliver dyes to Staff → HUD update
  - Check delivery status via Instructions
  - All 16 dyes delivered → win condition & message
  - Game Reset Block resets
- [ ] 11-3. Multiplayer verification
  - Per-player delivery status tracking
  - Winner detection accuracy
- [ ] 11-4. Village chest & Wandering Trader verification
- [ ] 11-5. Final cleanup of unused files

## MVP Scope

**MVP = Phases 1–8, 10** (core gameplay + UI + text)

Phase 9 (Village Chests & Wandering Trader) will be implemented after core gameplay is complete.

MVP simplifications:
- Game Start/Reset Block executes immediately without confirmation prompt (reuse existing double-click confirmation)
- Instructions delivery status screen starts with minimal layout

## Implementation Order

Order enabling incremental testing:

1. **Phase 1** — Cleanup (remove old systems)
2. **Phase 2** — Data model change (dye delivery tracking)
3. **Phase 3** — Network sync update
4. **Phase 10** — Language file update (needed for testing Phase 4+)
5. **Phase 4** — Staff Entity dye delivery
6. **Phase 5** — HUD update
7. **Phase 6** — Instructions item
8. **Phase 7** — Player spawn & equipment
9. **Phase 8** — Structure update
10. **Phase 9** — Village chests & Wandering Trader (post-MVP)
11. **Phase 11** — Integration testing

## File Change Summary

### Files to Delete
- `common-shared/src/main/java/com/colorchallenge/trade/PriceList.java`
- `common-1.21.1/src/main/resources/assets/colorchallenge/textures/gui/coin.png`
- Miner's Pickaxe related files (model, texture) if they exist

### Files with Major Changes
- `common-1.21.1/src/main/java/com/colorchallenge/state/GameStateManager.java` — sales → dyes
- `common-1.21.1/src/main/java/com/colorchallenge/state/GameStateSavedData.java` — persistence
- `common-shared/src/main/java/com/colorchallenge/state/ClientGameState.java` — client cache
- `common-1.21.1/src/main/java/com/colorchallenge/network/GameStateSyncPacket.java` — sync data
- `common-1.21.1/src/main/java/com/colorchallenge/entity/StaffEntity.java` — interaction
- `common-1.21.1/src/main/java/com/colorchallenge/hud/GameHudOverlay.java` — display
- `common-1.21.1/src/main/java/com/colorchallenge/event/PlayerSpawnHandler.java` — equipment
- `common-1.21.1/src/main/java/com/colorchallenge/registry/ModItems.java` — items
- `common-1.21.1/src/main/java/com/colorchallenge/structure/DeliveryAreaGenerator.java` — chest
- `common-1.21.1/src/main/resources/assets/colorchallenge/lang/en_us.json`
- `common-1.21.1/src/main/resources/assets/colorchallenge/lang/ja_jp.json`

### New Files to Create
- Instructions item class (`item/InstructionsItem.java` etc.)
- Delivery status screen (`hud/DeliveryStatusScreen.java` etc.)
- Village chest loot table modification (Phase 9)
- Wandering Trader trade extension handler (Phase 9)

### Minor Changes
- `common-shared/src/main/java/com/colorchallenge/registry/ModCreativeTab.java` — rename
- `common-shared/src/main/java/com/colorchallenge/event/GameTickHandler.java` — remove price event references
- `common-1.21.1/src/main/java/com/colorchallenge/ColorChallenge.java` — remove price event initialization
- `common-1.21.1/src/main/java/com/colorchallenge/block/GameStartBlock.java` — no change or minor
- `common-1.21.1/src/main/java/com/colorchallenge/block/GameResetBlock.java` — no change or minor
