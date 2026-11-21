# Messages and languages

Exemplars: `git show 42fa8f1ae` (messages), `git show c59bf2e11` (a new language).

## New message
1. Add a constant to `core/messages/Message.java`, alphabetical, before `CUSTOM`.
   - Command arguments: `COMMAND_ARGUMENT_X("default-text")`.
   - Command descriptions: `COMMAND_DESCRIPTION_X` / `COMMAND_DESCRIPTION_ADMIN_X`.
2. Add the key, alphabetical, to **all 13** `src/main/resources/lang/*.yml` files: de-DE, en-US, es-ES, fr-FR, hu-HU, it-IT, iw-IL, pl-PL, pt-BR, ru-RU, tr-TR, vi-VN, zh-CN. Put the English text in any file you can't translate.
   - This is required. The loader fills a server's language file only from the bundled file of the **same** language, so a key missing from `de-DE.yml` means the message silently never sends to German players. There is no fallback to en-US.
3. Format: info `'&e&l<Category> | &7Text {0}.'` (the category is usually `Island`), errors `'&c&lError | &7Text.'`, plain text for `COMMAND_DESCRIPTION_*`. Placeholders are positional `{0}`, `{1}`. Escape `'` as `''`.
4. Ordering: insert next to the key's alphabetical neighbours **in each file**. `Message.java` and the lang files don't always sort identically (e.g. `TELEPORTED_*` vs `TELEPORT_*`), so follow the local order rather than re-sorting.
5. `iw-IL.yml` stores Hebrew text **reversed** (visual order, for Minecraft's left-to-right rendering). When adding a key there, use English, or reversed Hebrew like the existing entries.

## Rules
- **Admin commands that target `<player/island/*>`** get no success messages of their own (no `X_ALL`/`X_NAME`/`X`/`X_OTHER` trio). `BulkChange.sendSummary` reports for every command (`commands.md`):
  - one `ISLANDS_CHANGE_SUMMARY` / `PLAYERS_CHANGE_SUMMARY` line;
  - one `ISLANDS_/PLAYERS_CHANGE_SKIPPED` line per skip reason (`_CONSOLE` for non-player senders, which can't hover).
  - A new skip reason is a `CHANGE_SKIP_REASON_X` key plus a `ChangeResult` constant. Its text is lower-case with no final period, because it is inserted mid-sentence.
  - Messages to the targets themselves (`DISBAND_SET` to the player, the text msgall sends) keep their own keys.
- Existing servers keep their lang files, so editing an existing message's text reaches new installs only. If the old text would now be wrong (e.g. it lacks a new argument), add a new key instead.
- To add an argument to an existing message, append the next index. Never renumber or repurpose `{0}`, `{1}`…, because custom lang files break silently. Update every lang file.
- Don't overwrite existing translations with English.
- `if (!Message.X.isEmpty(locale))` before building expensive arguments. An empty value in a lang file means "don't send".
- Don't hard-code chat text, and don't use `Message.CUSTOM` for translatable text. Never join several messages into one `CUSTOM` string: each has its own component type (action bar, MiniMessage, complex), and joining loses it. Send them one by one.
- A change to a key's format: migrate it in `Message.convertData(cfg)`, and call `cfg.save(...)` before `syncWithConfig`.

## New language
1. Add `src/main/resources/lang/xx-XX.yml`, a full copy with every key.
2. Add `plugin.saveResource("lang/xx-XX.yml", false)`, alphabetical, in the `Message` loader.
3. Add an item and a pattern slot in every `menus/player-language*.yml` variant: legacy `SKULL_ITEM` + `data: 3`, modern `PLAYER_HEAD`.
