# Commands and tab-completion

Exemplars:
- `git show e5344b280`: a player command with the full stack (API, event, messages, permission).
- `git show 3a8abdc3a`: an admin island command.
- `git show e96a44078 168496c61`: an admin player command, plus the `plugin.yml` node it forgot.

The admin exemplars predate `BulkChange`, so take their reporting from "Admin command" below instead.

## Player command: `commands/player/CmdX.java`
It `implements ISuperiorCommand`, or `IPermissibleCommand` when it needs an island privilege or role (`getPrivilege()`).
- `getAliases()`: lowercase. The first alias is the canonical label, used by help and `disabled-commands`. Never drop an existing alias.
- `getPermission()` = `"superior.island.<name>"`.
- `getUsage(locale)`: built from `Message.COMMAND_ARGUMENT_*.getMessage(locale)`, with `<required>` and `[optional]` arguments.
- `getDescription(locale)` = `Message.COMMAND_DESCRIPTION_<NAME>.getMessage(locale)`.
- `getMinArgs()`/`getMaxArgs()` count `args[0]`, which is the sub-command itself.
- `canBeExecutedByConsole()` and `tabComplete(...)`.

## Admin command: `commands/admin/CmdAdminX.java`
Permission `"superior.admin.<alias>"`. Pick the interface by target:
- `IAdminIslandCommand`: `supportMultipleIslands()`, `execute(plugin, sender, @Nullable targetPlayer, List<Island> islands, args)`, completions in `adminTabComplete`;
- `IAdminPlayerCommand`: `execute(plugin, sender, targetPlayer, args)`, `supportMultiplePlayers()`;
- plain `ISuperiorCommand`.

Argument counts include `admin` and the sub-command; the target is `args[2]`. Parse the shared arguments first; the parsers send their own errors. Then build `BulkChange.ofIslands(islands, island -> ...)` (or `BulkChange.ofPlayers(targetPlayers, ...)`), which returns a `ChangeResult` per target, and finish with `change.sendSummary(sender, args[1])`. Don't add success messages of your own (`messages.md`). Per target:
- A value check that fails returns its skip reason before the event fires: `ChangeResult.UNLIMITED` (an add on a limit `<= IslandUpgradeConstants.NO_LIMIT_VALUE`), `BELOW_MINIMUM`, or `ABOVE_MAXIMUM`.
- Otherwise fire the cancellable event and `return ChangeResult.ifAllowed(event, eventArgs -> ...)`, applying the values from `eventArgs`. Events that return a `boolean` use `ChangeResult.ifAllowed(allowed, island, [value, ...,] Island::setX)`.
- A failure that carries a reason from another plugin returns `ChangeResult.cancelled(reason)`.
- Without an event, apply the change and `return ChangeResult.APPLIED`.
- Appliers take what they need from their parameters and capture nothing (see `ChangeResult`).
- Messages to the targets themselves (e.g. `DISBAND_SET` to the player) are sent inside the per-target lambda.
- A follow-up message after the summary uses `change.hasApplied()` / `change.getAppliedCount()` (`CmdAdminSetSize`, `CmdAdminSetGenerator`).

Current exemplars: `CmdAdminSetTeamLimit` (set), `CmdAdminAddTeamLimit` (add, with unlimited and minimum), `CmdAdminResetSettings` (boolean event), `CmdAdminSetDisbands` (players).

## Registration
1. `registerCommand(new CmdX())`, alphabetical, in `commands/player/PlayerCommandsMap` / `commands/admin/AdminCommandsMap`. A command that belongs to a built-in module goes in that module's `getSuperiorCommands`/`getSuperiorAdminCommands` (upgrade types: `getCommands()`).
2. `plugin.yml`: the node, alphabetical, with a `description`, under the `superior.island.*` / `superior.admin.*` children.
   - Without it only ops get the command by default, and wildcard holders don't inherit it.
   - This is the step most often forgotten.
3. `COMMAND_DESCRIPTION_*` and any new `COMMAND_ARGUMENT_*`, in `Message` and all 13 lang files.

## Arguments
`commands/arguments/CommandArguments.getX(plugin, sender, arg)` sends the error message itself. Depending on the helper, failure comes back as `null`, an `IslandArgument` with a null island, an empty `IslandsListArgument`, or a `NumberArgument` with `isSucceed() == false`. Check for it and return.

These parsers are shared: tightening one changes every command that uses it. Grep its callers first.

## Tab-completion (`commands/CommandTabCompletes`)
- `return args.length == N ? CommandTabCompletes.getX(plugin, args[N - 1]) : Collections.emptyList();`. The index is **N - 1**; most tab-complete bugs were off-by-one.
- For several positions, `switch (args.length)` with **every branch returning**.
- `IAdminIslandCommand`/`IAdminPlayerCommand` complete extra arguments in `adminTabComplete`, where the island or player may be null for `*`. Override `tabComplete` only to filter the target list itself.
- Player-facing name completions pass `plugin.getSettings().isTabCompleteHideVanished()`; admin ones pass `false`.
- `getOnlinePlayersAndIslands` (players + island names) is different from `getOnlinePlayersWithIsland` (players who have an island).
- An optional argument has to agree across `getUsage`, `getMaxArgs`, `execute` and `tabComplete`.

## Where logic lives
- Business rules (costs, warm-ups, bypass permissions, side effects) go in the domain method (`SIsland`, `SSuperiorPlayer`) or a shared helper (`IslandUtils`, `EntityTeleports`, `CommandArguments`). Never only in the command or the menu click: entry points that held their own copy of a rule drifted apart and became bugs.
- Fire API events from the shared method, so commands, menus and API calls all fire them.
- Player/admin twins (`CmdPromote`/`CmdDemote`/`CmdAdminPromote`/`CmdAdminDemote`, `CmdShow`/`CmdAdminShow`) change together.
- A menu button that mirrors a command calls `plugin.getCommands().dispatchSubCommand(sender, "name"[, args])`, so the command's permission checks apply.
