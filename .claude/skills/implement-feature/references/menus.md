# Menus

Exemplars:
- `git show e5e729868`: a new menu across all layers. But it added an abstract method to `MenusProvider`; make yours `default`.
- `git show 08473f315`: a new button.
- `git show e7992425f`: a new optional item and sound.

## Default menu files have version variants
- Files are `src/main/resources/menus/<name>.yml` plus `<name><ver>.yml`. The base file targets the oldest server (1.8 materials and sounds). `<ver>` is a `ServerVersion` constant without the `v`: `1_12`, `1_13`, `1_16`, `1_17`, `1_19`, `1_20`, `1_21`, `26_1`…
- `Resources.saveResource` picks the newest variant that isn't newer than the server and saves it under the plain name, **only when the server has no such file**. `Resources.getResource` always returns the base file (known bug).
- **Apply a menu change to every variant whose Minecraft version supports it** (`ls src/main/resources/menus/<name>*`). Version-gated materials or entities go only into the newer variants.
- A changed default reaches only new installs. To reach existing servers, write a converter (`core/menu/converter/MenuConverter`) or a code-side default.

## A new menu touches
- `core/menu/MenuIdentifiers.MENU_X` and `core/menu/Menus.MENU_X`.
- `core/menu/impl/MenuX`: a static `createInstance()` parser, the View and its `Args`.
- Buttons in `core/menu/button/impl/XButton`: `extends AbstractMenuViewButton<View>`, private constructor, a nested `Builder extends AbstractMenuTemplateButton.AbstractBuilder`, mapped in the menu with `patternBuilder.mapButtons(...)`.
- The yml variants.
- API `MenusManager` + `MenusManagerImpl` (delegating to `plugin.getProviders().getMenusProvider()`).
- API `MenusProvider` + `external/menus/MenusProvider_Default` (`Preconditions`, then `Menus.MENU_X.createView(...)`).
  - `MenusProvider` is a `hooks/*Provider` with no `Delegate*` class, so new methods are `default` and safe for old implementations.
- Commands open menus with `plugin.getMenus().openX(...)`, never through `Menus.MENU_X` directly.

## Rules
- **`build()` on a button builder runs once per slot.** Don't mutate builder fields in `build()` and don't write getters with side effects. Both made every button act like the first one.
- Player-facing text goes through `plugin.getProviders().getUIProvider()`: item names, lore, and inventory titles via `createInventory`. That is how MiniMessage support works. Don't call `Bukkit.createInventory`/`ItemMeta#setDisplayName` directly.
- Titles are `MessageContent` (placeholders and MiniMessage). The View supplies title arguments by overriding `updateTitleArgs()`.
- Item placeholders are positional `{0}`, `{1}`…, replaced with `ItemBuilder.replaceAll`/`replaceLoreWithLines`. The convention is `{0}` raw, `{1}` `NUMBER_FORMATTER`, `{2}` `FANCY_NUMBER_FORMATTER`.
- New per-item yml options are parsed in `core/menu/parser/MenuParserImpl` (camelCase keys). A bad value gets `Log.warnFromFile(file, ...)` and is skipped. Optional items: `display-menu: true/false`.
- Menu types come from `type:` in the yml (inventory types, `DIALOG`, `HOLOGRAM`), each with its own parser. A type the server can't support throws `MenuParseException("... not supported in your server version")`.
- Model changes refresh menus from the model (`plugin.getMenus().refreshX(island)` in `SIsland`), not per button.
- A view registered before an early return leaks. Register it only after opening succeeds.
- Clicks on empty slots must not trigger buttons.
- Cross-cutting menu code tests `IPlayerMenuView`/`IIslandMenuView`, not concrete view classes.
