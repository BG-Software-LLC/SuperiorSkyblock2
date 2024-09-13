package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;

public class ChatFormatter implements IFormatter<ChatFormatter.ChatFormatArgs> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ChatFormatter INSTANCE = new ChatFormatter();

    public static ChatFormatter getInstance() {
        return INSTANCE;
    }

    private ChatFormatter() {

    }

    @Override
    public String format(ChatFormatter.ChatFormatArgs args) {
        String islandNameFormat = Message.NAME_CHAT_FORMAT.getMessage(PlayerLocales.getDefaultLocale(),
                args.island == null ? "" : plugin.getSettings().getIslandNames().isColorSupport() ?
                        Formatters.COLOR_FORMATTER.format(args.island.getName()) : args.island.getName());

        return args.format
                .replace("{island-level}", String.valueOf(args.island == null ? 0 : args.island.getIslandLevel()))
                .replace("{island-level-format}", String.valueOf(args.island == null ? 0 :
                        Formatters.FANCY_NUMBER_FORMATTER.format(args.island.getIslandLevel(), args.superiorPlayer.getUserLocale())))
                .replace("{island-worth}", String.valueOf(args.island == null ? 0 : args.island.getWorth()))
                .replace("{island-worth-format}", String.valueOf(args.island == null ? 0 :
                        Formatters.FANCY_NUMBER_FORMATTER.format(args.island.getWorth(), args.superiorPlayer.getUserLocale())))
                .replace("{island-name}", islandNameFormat == null ? "" : islandNameFormat)
                .replace("{island-role}", args.superiorPlayer.getPlayerRole().getDisplayName())
                .replace("{island-position-worth}", args.island == null ? "" : (plugin.getGrid().getIslandPosition(args.island, SortingTypes.BY_WORTH) + 1) + "")
                .replace("{island-position-level}", args.island == null ? "" : (plugin.getGrid().getIslandPosition(args.island, SortingTypes.BY_LEVEL) + 1) + "")
                .replace("{island-position-rating}", args.island == null ? "" : (plugin.getGrid().getIslandPosition(args.island, SortingTypes.BY_RATING) + 1) + "")
                .replace("{island-position-players}", args.island == null ? "" : (plugin.getGrid().getIslandPosition(args.island, SortingTypes.BY_PLAYERS) + 1) + "");
    }

    public static class ChatFormatArgs {

        private final String format;
        private final SuperiorPlayer superiorPlayer;
        @Nullable
        private final Island island;

        public ChatFormatArgs(String format, SuperiorPlayer superiorPlayer, @Nullable Island island) {
            this.format = format;
            this.superiorPlayer = superiorPlayer;
            this.island = island;
        }

    }

}
