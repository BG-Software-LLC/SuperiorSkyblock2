package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Applies a change to multiple targets (islands or players) and summarizes the result to the executor:
 * how many targets the change was applied to, and which targets were skipped and why.
 */
public class BulkChange<T> {

    private static final int MAX_LISTED_NAMES = 20;

    private final int targetsCount;
    private final List<String> appliedTargets = new LinkedList<>();
    private final Map<ChangeResult, List<String>> skippedTargets = new LinkedHashMap<>();

    private final Message summaryMessage;
    private final Message skippedMessage;
    private final Message skippedConsoleMessage;

    public static BulkChange<Island> ofIslands(Collection<Island> islands, Function<Island, ChangeResult> action) {
        return new BulkChange<>(islands, action, BulkChange::getIslandName, Message.ISLANDS_CHANGE_SUMMARY,
                Message.ISLANDS_CHANGE_SKIPPED, Message.ISLANDS_CHANGE_SKIPPED_CONSOLE);
    }

    public static BulkChange<SuperiorPlayer> ofPlayers(Collection<SuperiorPlayer> players, Function<SuperiorPlayer, ChangeResult> action) {
        return new BulkChange<>(players, action, SuperiorPlayer::getName, Message.PLAYERS_CHANGE_SUMMARY,
                Message.PLAYERS_CHANGE_SKIPPED, Message.PLAYERS_CHANGE_SKIPPED_CONSOLE);
    }

    private BulkChange(Collection<T> targets, Function<T, ChangeResult> action, Function<T, String> nameFunction,
                       Message summaryMessage, Message skippedMessage, Message skippedConsoleMessage) {
        this.targetsCount = targets.size();
        this.summaryMessage = summaryMessage;
        this.skippedMessage = skippedMessage;
        this.skippedConsoleMessage = skippedConsoleMessage;

        for (T target : targets) {
            ChangeResult result = action.apply(target);
            String targetName = nameFunction.apply(target);
            if (result.isApplied()) {
                this.appliedTargets.add(targetName);
            } else {
                this.skippedTargets.computeIfAbsent(result, r -> new LinkedList<>()).add(targetName);
            }
        }
    }

    public boolean hasApplied() {
        return !this.appliedTargets.isEmpty();
    }

    public int getAppliedCount() {
        return this.appliedTargets.size();
    }

    public void sendSummary(CommandSender sender, String commandLabel) {
        Locale locale = PlayerLocales.getLocale(sender);

        if (!this.summaryMessage.isEmpty(locale)) {
            int skippedCount = this.targetsCount - this.appliedTargets.size();
            this.summaryMessage.send(sender, locale, commandLabel, this.appliedTargets.size(), this.targetsCount,
                    skippedCount, joinNames(this.appliedTargets, ", ", locale),
                    joinNames(this.appliedTargets, "\n", locale));
        }

        if (this.skippedTargets.isEmpty())
            return;

        // Tooltips are not shown in the console, therefore it has its own message.
        Message skippedMessage = sender instanceof Player ? this.skippedMessage : this.skippedConsoleMessage;

        this.skippedTargets.forEach((result, targetNames) -> skippedMessage.send(sender, locale, targetNames.size(),
                result.getReason(locale), joinNames(targetNames, ", ", locale), joinNames(targetNames, "\n", locale)));
    }

    private static String joinNames(List<String> names, String separator, Locale locale) {
        if (names.size() <= MAX_LISTED_NAMES)
            return String.join(separator, names);

        String listedNames = String.join(separator, names.subList(0, MAX_LISTED_NAMES));
        String moreNames = Message.CHANGE_TARGETS_MORE.getMessage(locale, names.size() - MAX_LISTED_NAMES);
        return Text.isBlank(moreNames) ? listedNames : listedNames + separator + moreNames;
    }

    private static String getIslandName(Island island) {
        // Stripped names, so a colored island name doesn't color the names listed after it.
        return island.getStrippedName().isEmpty() ? island.getOwner().getName() : island.getStrippedName();
    }

}
