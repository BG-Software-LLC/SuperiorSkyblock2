# Hologram menus — session handoff

Branch: `feature/hologram-menus`. Nothing is committed. This file is untracked and is only a handoff note; delete it before committing.

## Goal

A third menu backing next to inventory and dialog menus, selected with `type: HOLOGRAM` in a menu yml. It is based on kangarko's "Ep70. Hologram GUI (Mouse Menus)" video (https://www.youtube.com/watch?v=ehlYco0E73w): the player's view is frozen, display entities draw a panel in front of them, head rotation drives a cursor, clicking presses the hovered button, sneak closes.

- Step 1: title, body text lines and labelled buttons, everything else constant.
- Step 2 (this session): everything is configurable from the menu file — background, text sizes, spacing, cursor, per-button appearance, buttons with items, rows of buttons, tooltips, right clicks. An inventory menu file (`pattern` + `items`) works when its `type` is changed to `HOLOGRAM`.

- Paged hologram menus: every paged menu of the plugin can use `type: HOLOGRAM`; changing pages slides the objects.

Decisions the user made:
- **Packet-only**: every entity is sent only to the viewer, nothing is added to the world.
- **Versions**: all modern NMS modules, 1.20.3 and newer (12 modules).

Plans: step 1 `/home/omer/.claude/plans/write-a-menu-implementation-keen-goose.md`, step 2 `/home/omer/.claude/plans/read-hologram-menus-handoff-md-breezy-pony.md`.

## Status

- Code is written for API, main module, NMS template and the 12 `properties` files.
- **Not built with Gradle by me and not tested in game** (neither step). Gradle cannot configure in the WSL sandbox. The user builds on Windows.
- Step 1: the user's first build reported one error (`EntityType.TEXT_DISPLAY` missing in 26.2+), fixed with `${ENTITY_TYPE_VALUES_CLASS}`. The rebuild after that fix was never reported back.
- Step 2 checks with direct `javac`:
  - API + main module changed files: compile with JDK 17 `-source 8 -target 8` against the previous build output (stub classes for `com.bgsoftware.common.annotations`, which is not available locally).
  - NMS template, 1.20.3–1.21.10 (9 modules) against each `mappedServerJar.jar`: compiles, apart from `TextDisplay.TextAlignment`, which is spelled `TextAligment` only in the old 1.19.4 API jar used for the check. `CraftTextDisplay` in the 1.20.3 and 26.3 server jars uses `TextAlignment`.
  - 26.1–26.3: not compiled (needs JDK 25). `Display.ItemDisplay(EntityType, Level)` and `ITEM_DISPLAY` / `TEXT_DISPLAY` in `${ENTITY_TYPE_VALUES_CLASS}` checked with `javap`.
- Default menu: `src/main/resources/menus/confirm-disband1_21.yml` is a hologram menu (it was a dialog menu; that version is in git history). `Resources.saveResource` picks the newest `<name><version>.yml` that is not newer than the server, so it is the default from 1.21 up to 26.3. Existing files on servers are never replaced.
- Test server menu file uses the new options: `/mnt/d/Servers/Testing/plugins/SuperiorSkyblock2/menus/confirm-disband.yml`. No backup of the old hopper layout was kept; the jar's default file is the way back.

## Next steps

1. User rebuilds (`gradle build`) and pastes any compile errors; fix them with property placeholders.
2. In-game test on the Testing server with `/is disband` (checklist at the bottom).
3. Tune the geometry constants in the template if things are misaligned.

## Menu file format

Everything under `hologram` and every per-button key is optional. Sizes are in blocks. Colours are `#AARRGGBB` or `#RRGGBB` and must be quoted (an unquoted `#` starts a yaml comment).

```yaml
title: '&c&lConfirm Disband'
type: HOLOGRAM

hologram:
  distance: 3.0
  hide-crosshair: true        # client-only spectator mode, see "Spectator view" below
  background:
    color: '#C0101010'
    width: 3.2                # minimum; grows to fit the widest row
    padding: 0.25
    item: { type: PAPER }     # optional, drawn instead of the colour
  title:   { scale: 1.2, height: 0.45, shadow: true }
  body:    { scale: 0.8, line-height: 0.28, shadow: true }
  spacing: { sections: 0.15, rows: 0.15, buttons: 0.15 }
  cursor:
    type: ARROW              # ARROW (U+1FBB0, the mouse pointer), CROSSHAIR ('+'), TEXT or ITEM
    color: '&7'              # ARROW / CROSSHAIR
    hovered-color: '&f'
    #text / hovered-text     # TEXT (a menu that only sets `text` is a TEXT cursor)
    #item / hovered-item     # ITEM; scale is the item's size in blocks (default 0.25)
    scale: 0.75
    hovered-scale: 0.75      # resized over one tick when entering or leaving a button
    shadow: true
    #offset-x / offset-y     # drawn this far from the clicking point, times the scale; ARROW defaults to 0.025 / -0.1, others 0
    menu-margin: 0.2         # area: MENU, how far past the menu edges
    screen-margin: 0.1       # area: SCREEN, how far from the screen edges, times the scale
    click-delay: 250         # milliseconds between clicks
    speed: 1.5               # cursor movement per head turn, 1 = where the player looks
    area: SCREEN             # or MENU
    screen-fov: 70           # assumed for every player, the server cannot know it
    screen-aspect-ratio: 1.778
  tooltip: { enabled: true, scale: 0.6, color: '#E0100010', offset: 0.15 }   # offset: gap between the cursor and the tooltip
  buttons:                    # defaults for every button
    width: 2.6
    height: 0.4
    color: '#FF555555'
    hovered-color: '#FF3B6FD9'
    label-scale: 1.0
    item-scale: 0.3
    shadow: true

body:
  - '&7Are you sure you want to delete your island?'

pattern:                      # optional: each line is one row of buttons
  - '@ ^'

buttons:
  '@':
    label: '&aConfirm'
    item: { type: LIME_WOOL, name: '&aConfirm', lore: ['&7Deletes the island'] }
    width: 1.2                # any key of hologram.buttons can be overridden
    tooltip: ['&7Custom tooltip']   # list, string, or false for none
  '^':
    label: '&4Cancel'

# commands / sounds / permissions sections work per button char, as in other menus
```

Rules:
- No `pattern`: each entry of `buttons` is its own row.
- With `pattern`: rows are centred. A character with no button and no item is an empty square of the default button height; a row made only of those is skipped.
- The item is `buttons.<ch>.item`, else `items.<ch>` (inventory format). A character that only has `items.<ch>` is an item button with the default appearance.
- Item only: square button (width = height unless `width` is set). Item and label: the item sits in a square at the left, the label is centred in the rest. Label only: the label is centred.
- Tooltip: `tooltip` of the button, else the name and lore of its item; none if both are empty.
- `body` accepts a string, a list of strings, or list entries that are maps with `text`.
- Text width is not known on the server, so long labels or body lines overflow; widen `background.width` or the button.

Paged menus use the same keys as paged inventory menus, as characters of the pattern: `slots` (the objects), `previous-page`, `next-page`, `current-page` and `back`. `{0}` in the items, labels and tooltips of page buttons is replaced like in inventories (`&a`/`&c` for previous and next, the page number for current). Objects are placed in the order of the pattern; `custom-layout` and `custom-order` are not supported. Slots without an object show the menu's null item if it has one, otherwise they are empty space with the size of their button, so the layout is the same on every page.

```yaml
hologram:
  page-animation:
    duration: 6      # ticks; 0 changes pages without an animation
    distance: 0.6    # blocks the objects slide by; they shrink while sliding
```

## Architecture

The main module owns layout, geometry, hover/click logic and lifecycle (Java 8, version independent). NMS is a dumb per-viewer renderer plus input capture.

### API (`API/src/main/java/com/bgsoftware/superiorskyblock/api/`)
- `menu/hologram/HologramButton` — label, width, height, colour, hovered colour, label scale, item scale, label shadow, tooltip. `newBuilder()`, `newBuilder(HologramButton other)`.
- `menu/hologram/HologramMenuStyle` — every `hologram.*` value plus `getDefaultButton()`. `newBuilder()`.
- `menu/layout/HologramMenuLayout` — `getBodyLines()`, `getStyle()`, `getRows()`; `Builder#addBodyLine`, `setStyle`, `addRow(List<Integer> slots)`.
- `menu/button/MenuTemplateButton` — `getButtonHologram()` and `Builder#setButtonHologram(...)`. It can be set together with an item, unlike `setButtonDialog`.
- `handlers/MenusManager` — `createHologramLayoutBuilder()`, `createHologramButtonBuilder()`, `createHologramStyleBuilder()`.
- No existing signature changed.

- `menu/layout/PagedHologramMenuLayout` (+ `Builder`, `MenusManager#createHologramPagedLayoutBuilder()`), and `HologramMenuStyle#getPageAnimationDuration()` / `getPageAnimationDistance()`.

### Main module (`src/main/java/com/bgsoftware/superiorskyblock/`)
- `core/menu/hologram/HologramButtonImpl`, `HologramMenuStyleImpl` — value classes; their builders hold the default values.
- `core/menu/button/AbstractMenuTemplateButton` — `hologramButton` field next to `buttonData`, carried over in `applyToBuilder`.
- `core/menu/parser/HologramMenuParser` — `hologram` section → style, `pattern` → rows, per-button label / item / overrides / tooltip, `parseColor`. `DialogMenuParser.parseButtonActions` (commands, sounds, permissions) is shared by both parsers.
- `core/menu/layout/RegularHologramMenuLayoutImpl` — stores style and rows; without rows, one row per slot.
- `core/menu/layout/PagedHologramMenuLayoutImpl` — objects per page = paged buttons; page buttons are mapped like in `PagedInventoryMenuLayoutImpl`. `MenuParserImpl`'s paged `parseMenu` builds it for `HOLOGRAM`, using `HologramMenuParser.parsePagedMenuPatternInternal` (back buttons, the only-back-button setting).
- Paged objects and page changes, in `HologramMenuWrapper`: `buildLayout` (main thread) gives each paged button its object (`getButtonIndex() + objectsPerPage * (page - 1)`, `updateObject`, `createViewItem`) and replaces `{0}` (`PagedObjects`). A page change calls `refreshView()`, which builds a new wrapper for the same view; when it takes over the renderer and only the page changed (same panel size), it does not clear it (`isPageChange`). `draw` adds the new page with its object elements shrunk at the side they come from; `slideToPage` removes the old non-object elements, animates the old objects out (moving by `distance` while shrinking, over `duration` ticks), animates the new objects in one tick later, removes the old objects afterwards, and blocks clicks until then.
- Elements are drawn on layers (background, button rectangles, content, tooltip, cursor), so a new background stays behind old objects that are still sliding.
- `core/menu/hologram/HologramMenuWrapper` — analogue of `DialogWrapper`. Static registry by player UUID (main thread only), `tickAll()`, `closeAll()`, `getByPlayer()`.
  - `draw()`: builds `ButtonsRow`s (`createButtonsRow`), sizes the panel, then draws background, title, body, buttons (`drawButton`), the tooltip element and the cursor. Items come from `templateButton.createViewButton(view).createViewItem()`, like inventory menus.
  - `tick()`: closes if the player is offline, dead, changed world or moved more than 1 block; otherwise projects yaw/pitch to the cursor (`tan(angle) * distance * cursor speed`), hit-tests the button rectangles, and on hover change recolours the button, changes the cursor text and shows or hides the tooltip. The tooltip follows the cursor.
  - `onClick(rightClick)`: 250 ms delay, then `menu.onClick(ctx)` with `ClickType.LEFT` or `RIGHT`.
  - `open()` takes over the renderer of an already-open hologram when both use the same distance; otherwise the old one is closed first.
  - Cursor bounds: with `area: SCREEN`, `distance * tan(screen-fov / 2)` vertically and that times the aspect ratio horizontally, minus `SCREEN_EDGE_MARGIN * cursor scale`, and never less than the panel. The block fitting ray traces that whole area (at most 24 rays per axis).
  - Constants left in code: `EDGE_MARGIN`, `SCREEN_EDGE_MARGIN`, `TOOLTIP_OFFSET`, `CLICK_DELAY`, `MAX_BASE_PITCH`, `MAX_MOVE_DISTANCE_SQUARED`.
- `core/menu/view/AbstractMenuView`, `core/menu/AbstractMenu`, `core/menu/MenuCommandsImpl`, `core/menu/parser/MenuParserImpl`, `SuperiorSkyblockPlugin`, `listener/PlayersListener` — step 1 wiring. Hologram menus can be opened anywhere: the old on-ground / not-in-vehicle check and its message `OPEN_HOLOGRAM_MENU_WHILE_NOT_ON_GROUND` were removed (server lang files may still contain the key; it is unused).
- `core/menu/button/click/ButtonClickContextImpl` — `obtain(menuView, player, slot, clickType)`.

### Threads
Hologram menus run on one static daemon thread, "SuperiorSkyblock Hologram Menus" (`HologramMenuWrapper.MENUS_THREAD`, a single-thread `ScheduledExecutorService`):
- Main thread: `open()` builds the contents (`buildLayout`: title, body, `createViewItem()`, tooltips, sizes; these can use placeholders and other plugins), creates the renderer candidate (it captures the viewer's rotation, eye height, shared flags, held slot and network channel), and `captureBlocks()` collects references to the loaded chunks around the eyes. Button logic (`menu.onClick`), `menu.onClose`, and the work `Renderer.destroy()` returns (game mode, abilities, real flags, inventory resync, the rotation-restoring teleport) also run on the main thread. The hotbar slot change for hidden hand items is handled on the main thread too.
- Menus thread: everything else. `openInternal` (take-over, registry), `draw` (block fitting, creating the packet-only entities, every packet), the 50 ms tick (cursor, hover, tooltip), click debouncing, `closeInternal`. `MENUS_BY_PLAYERS` is a `ConcurrentHashMap` changed only here.
- Network threads: the packet handler records the viewer's rotation from `ServerboundMovePlayerPacket` (so the cursor does not depend on the main thread) and reports clicks and sneaking; `InputDelegate` moves them to the menus thread.
- The block fitting walks the blocks along each ray itself (`traceBlocks`, voxel traversal, `getShape(EmptyBlockGetter.INSTANCE, pos)` + `VoxelShape.clip`) over the captured `LevelChunk`s, the same way island calculations read chunks outside the main thread. Unloaded chunks count as empty.
- The tick reads `player.isDead()` and `player.getLocation()` from the menus thread; these are plain reads of the player's state.
- `runOnMainThread` uses the Bukkit scheduler directly, because `BukkitExecutor.sync` runs tasks on the calling thread while the plugin is being disabled. `closeAll()` (plugin disable) stops the menus thread (5 s timeout), closes the remaining menus from the main thread and runs the queued main-thread work.

### NMS
`nms/NMSHologramMenus` and `NMS/src/main/templates/NMSHologramMenusImpl.java.template` (first line `//<generate-if:HOLOGRAM_MENUS_SUPPORT>`).

- `Renderer`: `addText(text, x, y, scale, shadowed)`, `addRectangle`, `addItem(item, x, y, width, height)`, `addTooltip(text, x, y, scale, argb)`, `show`, `moveElement`, `setColor`, `setText`, `clear`, `destroy`. `InputListener`: `onClick(rightClick)`, `onExit`.
- All entities are NMS display entities that are never added to a level; they are configured through `getBukkitEntity()` and the Bukkit display API, then sent with packets.
- Every element spawns at the viewer's eye position with yaw + 180 and is placed on the panel only by its transformation (translation `(x, y, -distance + 0.01 * index)` + scale).
- Rectangles are text displays with `" "` as text and a background colour, scaled by `width / 0.125` and `height / 0.25`, with a `-0.0125 * scaleX` x offset and `-height / 2` y offset. Text is offset by `-(0.25 * scale) / 2` to centre it vertically.
- Items are `Display.ItemDisplay` with the `GUI` transform, scaled to `(width, height, 0.001)` and rotated 180° around the y axis (`ITEM_ROTATION`), because the client's item display renderer turns items away from the direction the entity faces.
- Tooltips are left-aligned text displays with their own background, anchored at the bottom centre. A hidden tooltip has empty text and a transparent background.
- Blocks between the eyes and the panel hide it, as the panel is a real object 3 blocks away. Before drawing, the wrapper calls `getMaxScale(width, height)`, which ray traces a grid of sight lines (at most 0.2 blocks apart on the panel, up to 32 per axis, `FluidCollisionMode.NEVER`, passable blocks such as grass count) and returns the largest scale up to 1 that keeps the panel `FIT_MARGIN` in front of every hit (minimum distance 0.1). `setScale(scale)` then multiplies every element's translation, size and depth, so the panel moves towards the eyes and looks the same, only closer. The ray tracing uses the Bukkit API in the template because the main module compiles against the 1.8 API. It is computed once per draw; blocks placed while the menu is open are not accounted for.
- Element identifiers come from a counter and are never reused (elements are kept in a `LinkedHashMap`), so delayed animation steps cannot touch newer elements. Depth = `-distance + 0.01 * layer + 0.0003 * (id % 32)`. `animateElement` sets the interpolation duration and resizes around the element's centre (`Element.sizeScale`); `removeElements` removes chosen elements and their mirrors.
- Moving an element re-sends its transformation plus a hand-built "interpolation delay = 0" `DataValue`, so the client interpolates over 1 tick.
- View lock: the camera (`ClientboundSetCameraPacket`) is locked on an invisible packet-only armor stand (`createCameraAnchor()`), and a text display at the feet is a client-side vehicle (`ClientboundSetPassengersPacket` built from a buffer). The camera anchor is a living entity because of two client behaviours (26.3 `Camera`):
  - `Camera.tick` moves the camera height halfway towards the camera entity's eye height every tick. A camera entity with a different eye height makes the view slide on open and on close. The armor stand is placed at the viewer's feet, and its `scale` attribute is set so its eye height equals the viewer's (1.20.5+; on 1.20.3/1.20.4 there is no scale, so a 0.16 block slide remains).
  - Third person (F5) moves the camera back by `scale * camera_distance` of a living camera entity (4 blocks for other entities). `camera_distance` is set to 0 (1.21.6+ clients). Older clients still move it back about 3.6 blocks, so the menu looks smaller there.
  - The mirrored third person view (second F5) turns the camera around. Every element therefore has a mirror (`Element.mirrorId`/`mirrorUUID`, identifiers taken from a dummy display): the same entity data, spawned with the yaw turned by 180°, so it sits behind the viewer facing him with the same layout. `sendElementData()` sends every data update to both; removal removes both. Text pipelines cull back faces, so the copies are invisible from behind; flat item icons are drawn on both sides, which only shows on clients whose third person view still moves the camera back. The block fitting casts its rays behind the viewer as well (at most 16 per axis per side).
  - Attributes are sent with `ClientboundUpdateAttributesPacket`. Properties: `ATTRIBUTE_TYPE` (`Attribute` in v1_20_3, `Holder<Attribute>` elsewhere), `SCALE_ATTRIBUTE` (`null` in v1_20_3), `CAMERA_DISTANCE_ATTRIBUTE` (`Attributes.CAMERA_DISTANCE` in v1_21_7 and newer, `null` elsewhere). Starting to ride makes the client show "Press LEFT SHIFT to dismount" (`mount.onboard`, through `Hud.setOverlayMessage`); an empty `ClientboundSetActionBarTextPacket` replaces it. Both are sent in one `ClientboundBundlePacket` (`sendBundle`): Paper sends action bars immediately (`Connection.InnerUtil.canSendImmediate`) but queues other packets sent outside the main thread, so sent separately from the menus thread the empty action bar arrived first. The client handles a bundle's packets together and in order. The narrator, if enabled, still reads the message.
- Spectator view (`hide-crosshair: true`, default): the crosshair can only be hidden from the server by putting the client in spectator mode (`Hud.extractCrosshair` skips it only for spectators; `Hud.isHidden` is F1 only). `lockView()` sends `ClientboundGameEventPacket(CHANGE_GAME_MODE, SPECTATOR)` to the viewer only; `showViewer()` sends his real `gameMode.getGameModeForPlayer()` and `onUpdateAbilities()`. Spectators do not see their hands either, so held items are not faked as empty, which is what played the lower-and-raise animation (`FirstPersonHandsAndItems.shouldInstantlyReplaceVisibleItem`: only the `damage` component skips it, and no vanilla item model sets `hand_animation_on_swap: false`). Costs: the hotbar and health are hidden; number keys open the spectator teleport menu (its `ServerboundTeleportToEntityPacket` is swallowed); spectators see invisible players, so the viewer sees himself see-through in his inventory screen. 26.2+ clients report spectator left clicks with `ServerboundSpectatorActionPacket` (new property `SPECTATOR_ACTION_PACKET_CLASS`; the swing class on older modules). With `hide-crosshair: false`, the held items are faked as empty as described next, and the animation plays.
- The viewer is hidden from himself (like menus on other servers): no first-person arm, no model in his inventory screen. `hideViewer()` sends him his own shared flags (entity data id 0) with the invisible bit (`1 << Entity.FLAG_INVISIBLE`), plus fake empty slots for the offhand (`InventoryMenu.SHIELD_SLOT`) and the selected hotbar slot, because held items are still drawn for invisible players. While the menu is open, the packet handler's `write()` forces the invisible bit into every entity data packet about him (also inside bundles), and blanks server slot updates for the hidden slots. Selecting another hotbar slot moves the hidden slot (`hideHeldItem`). `showViewer()` in `destroy()` sends the real flags and `inventoryMenu.sendAllDataToRemote()`. Other players never receive these packets. Full inventory syncs (`ClientboundContainerSetContentPacket`) are not rewritten, because the packet changed in 1.21.5; one would show the held item until the hotbar slot changes or the menu closes.
- Opening anywhere: while the menu is shown, the viewer only sends his rotation and stays where he opened it, so in the air the server counts him as floating and kicks him after about 4 seconds ("Flying is not enabled"), unless flight is allowed. `Renderer.tick()` (called by every menus-thread tick) resets the counters: `ServerGamePacketListenerImpl.resetFlyingTicks()` from 1.21.10 (`RESET_FLYING_TICKS`), and on older versions the private `aboveGroundTickCount` / `aboveGroundVehicleTickCount` ints through `ReflectField` by type and position (`ABOVE_GROUND_TICK_COUNT_FIELD_INDEX`: the 5th private int field before 1.21.2, the 4th after; positions match on Spigot and Paper because those fields are vanilla and come first). Fall distance is untouched, so falling continues normally after closing. The packet handler drops move packets that carry a position while the menu is open (after reading their rotation), so positions sent before the view was locked (several blocks per tick while falling) cannot move the viewer on the server. For the first 5 ticks (`MOVE_SETTLE_TICKS`), the start location of the movement check follows the server position instead of closing the menu, for move packets the server had already received; a world change still closes it. In a real vehicle, riding the menu's vehicle makes the viewer leave his real one on his side; the main-thread part of `destroy()` re-sends `ClientboundSetPassengersPacket(realVehicle)` after the teleport. A moving vehicle still closes the menu once the viewer moved more than 1 block.
- Click targets: a client only reports a right click on nothing when it holds an item (`Minecraft.startUseItem` skips `useItem` for an empty hand), and spectators never do (`MultiPlayerGameMode.useItem` returns early). Clicks on entities are always reported, even by spectators (`interact` sends the packet before its spectator check). `lockView()` therefore spawns two packet-only `Interaction` entities (1×1 block, 1 block in front of the camera and 1 behind it for the mirrored view). Entity clicks arrive as `ServerboundInteractPacket`: a right click, or a left click before 26.x (`INTERACT_PACKET_IS_ATTACK`: `interactPacket.isAttack()` before 26.x, `false` on 26.x, where attacks have their own packet). Without spectator mode, the viewer's own body (around the camera) is usually the entity that is hit, which works the same way.
- Input: a Netty duplex handler (`PacketHandler`) named `superiorskyblock_hologram_menu` before `packet_handler`. Swing/punch, attack and start-destroy-block packets are left clicks; use-item and use-item-on packets are right clicks; a player-input packet with shift is `onExit`; interact packets are swallowed. Dropping items, swapping with the offhand and creative-mode slot changes are swallowed too, as the viewer cannot see the items he would change. Block changes are acknowledged so client predictions revert.
- `destroy()` resets the camera, clears passengers, removes the entities and calls `connection.teleport(...)` with the rotation the viewer had when the renderer was created, because turning the head to move the cursor changes his real rotation.

Property keys in `NMS/<version>/properties`: `HOLOGRAM_MENUS_SUPPORT`, `SET_PASSENGERS_PACKET_FROM_BUFFER`, `PLAYER_INPUT_PACKET_SHIFT_METHOD`, `SWING_PACKET_CLASS`, `ATTACK_PACKET_CLASS`, `SPECTATOR_ACTION_PACKET_CLASS` (`ServerboundSpectatorActionPacket` in v26_2 and v26_3, the swing class elsewhere), `INTERACT_PACKET_IS_ATTACK` (see click targets), `RESET_FLYING_TICKS`, `ABOVE_GROUND_TICK_COUNT_FIELD_INDEX` (see opening anywhere), `ATTRIBUTE_TYPE`, `SCALE_ATTRIBUTE`, `CAMERA_DISTANCE_ATTRIBUTE` (see the view lock), `USE_ITEM_PACKET_SEQUENCE_METHOD`, plus the pre-existing `ENTITY_TYPE_VALUES_CLASS`, `NMS_VERSION`, `CRAFTBUKKIT_PACKAGE`.

## Client facts checked in the 26.3 client jar

The 26.x client jars are unobfuscated (`PrismLauncher/libraries/com/mojang/minecraft/26.3/minecraft-26.3-client.jar`), so `javap -c` shows the real code:
- `DisplayRenderer.calculateOrientation`, `FIXED`: `rotationYXZ(-yaw, pitch, 0)`; `CENTER` uses `cameraYaw - 180`, so yaw + 180 makes texts face the viewer.
- `TextDisplayRenderer.submitInner`: rotate π around y, scale -0.025, line height 10 px (0.25 blocks).
- `Display.updateCulling`: culling is off when the display width or height is 0 (the default).
- `LevelExtractor`: the camera entity itself is not drawn, and the `LocalPlayer` is only drawn when it is the camera entity, so the viewer's own model does not block the view.
- `GameRenderer.renderItemInHand` does not check the camera entity. `FirstPersonHandsAndItemsRenderer.submitArmWithItem` draws the empty arm only when the player is not invisible, but draws held items either way.
- `LivingEntityRenderer.getRenderType` returns nothing for a player who is invisible to the local player, which is himself, so the inventory screen draws only his layers (armor, held items).
- `ClientPacketListener.handleContainerSetSlot` applies container 0 updates to `player.inventoryMenu` directly.

## Open risks (need in-game confirmation)

- A packet-only display entity as the vehicle, and an armor stand as the camera anchor.
- Rectangle, text and item geometry constants at the top of the template, including the item rotation (icons mirrored or showing their back means `ITEM_ROTATION` is wrong) and how flattened blocks are shaded.
- No vanilla kick ("flying", "moved wrongly") while the menu is open, including when opened in the air or in a vehicle (see opening anywhere).
- Sneak detection on 1.20.4–1.21.1 relies on the per-tick player-input packet.
- Right click: whether the client sends use-item packets while its camera is on the anchor. A right click on a block can also send a swing; the 250 ms click delay keeps only the first one.
- Tooltip position near the top edge of the panel.
- Hiding the viewer from himself: on a scoreboard team that can see invisible teammates (the default for teams, common with TAB or nametag plugins) he is drawn see-through in his inventory screen instead of hidden. Armor is still drawn there. While the menu is open, his hotbar shows the selected slot as empty.
- A superseded hologram view is not reported to its menu with `onClose`, mirroring how dialogs behave.

## Environment notes

- The whole working tree shows as modified because of CRLF line endings (pre-existing). New and edited source files are kept in CRLF. Use `git diff --ignore-cr-at-eol` to see real changes.
- Do not use `sed -i` on this drive from WSL: it replaces the file with a read-only copy that `chmod` cannot fix. Edit with python (`open(..., newline='')`) or the editor tools.
- `./gradlew` has CRLF and does not run under WSL; the cached Gradle 9.4.1 distribution starts but cannot configure the project there.
- Each NMS module has `NMS/<version>/.gradle/caches/paperweight/taskCache/mappedServerJar.jar` (classes and `.java` sources), useful for `javap` and for reading server source. Library jars for ad-hoc `javac` checks are under `/home/omer/server/libraries` and `~/.gradle/caches/modules-2`. JDKs 8, 11, 17 and 21 are in `/usr/lib/jvm`; there is no JDK 25.

## In-game checklist

1. `/is disband` on the Testing server: panel, title, two body lines and two buttons side by side, each with a wool icon at the left and a label; the view is frozen; the cursor follows the mouse and stops at the panel edges.
2. Hover recolours the button (green / red) and turns the cursor aqua; a tooltip appears above the cursor ("Confirm" + lore, "Keep your island."). Left and right click both press the button; click sounds play.
3. Change values in the `hologram` section (distance, background colour, scales, button sizes), reload, and confirm each takes effect. Try `background.item`.
4. Sneak closes the menu and restores camera and position.
5. A second player sees no entities and sees the viewer standing normally.
6. Quit, death, teleport, world change, plugin disable: no stuck camera or leftover entities.
7. Set a non-paged inventory menu (for example `control-panel.yml`) to `type: HOLOGRAM` with no other change: the pattern renders as a grid of item buttons with tooltips, and clicks work.
8. Bad values (`color: red`, unknown item type) log a warning and fall back; a paged menu set to `HOLOGRAM` logs the parse error; inventory and dialog menus still work.
9. Repeat on 1.20.4, 1.21.1 and a Spigot build.
