package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.serialization.impl.PersistentDataSerializer;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.google.common.collect.ImmutableMap;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CmdAdminData implements ISuperiorCommand {

    private static final Map<String, SubCommand> subCommands = new ImmutableMap.Builder<String, SubCommand>()
            .put("set", new SetSubCommand())
            .put("get", new GetSubCommand())
            .put("remove", new RemoveSubCommand())
            .build();

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("data");
    }

    @Override
    public String getPermission() {
        return "superior.admin.data";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin data <get/set/remove> <player/island> <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_DATA.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SubCommand subCommand = subCommands.get(args[2].toLowerCase(Locale.ENGLISH));

        if (subCommand == null) {
            Message.COMMAND_USAGE.send(sender, plugin.getCommands().getLabel() + " " + getUsage(PlayerLocales.getLocale(sender)));
            return;
        }

        if (args.length < subCommand.getMinArgs() || args.length > subCommand.getMaxArgs()) {
            Locale senderLocale = PlayerLocales.getLocale(sender);
            Message.COMMAND_USAGE.send(sender, plugin.getCommands().getLabel() + " " + getUsage(senderLocale) + subCommand.getUsage(senderLocale));
            return;
        }

        IPersistentDataHolder persistentDataHolder;

        if (args[3].equalsIgnoreCase("island")) {
            IslandArgument arguments = CommandArguments.getIsland(plugin, sender, args[4]);
            persistentDataHolder = arguments.getIsland();
        } else if (args[3].equalsIgnoreCase("player")) {
            persistentDataHolder = CommandArguments.getPlayer(plugin, sender, args[4]);
        } else {
            Locale senderLocale = PlayerLocales.getLocale(sender);
            Message.COMMAND_USAGE.send(sender, plugin.getCommands().getLabel() + " " + getUsage(senderLocale) + subCommand.getUsage(senderLocale));
            return;
        }

        if (persistentDataHolder != null)
            subCommand.execute(plugin, sender, persistentDataHolder, args);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        switch (args.length) {
            case 3:
                return CommandTabCompletes.getCustomComplete(args[2], "get", "set", "remove");
            case 4: {
                SubCommand subCommand = subCommands.get(args[2].toLowerCase(Locale.ENGLISH));
                return subCommand == null ? Collections.emptyList() :
                        CommandTabCompletes.getCustomComplete(args[3], "island", "player");
            }
            case 5: {
                SubCommand subCommand = subCommands.get(args[2].toLowerCase(Locale.ENGLISH));
                return subCommand == null ? Collections.emptyList() : args[3].equalsIgnoreCase("island") ?
                        CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[4], false, null) :
                        CommandTabCompletes.getOnlinePlayers(plugin, args[4], false);
            }
            default: {
                return Collections.emptyList();
            }
        }
    }

    interface SubCommand {

        String getUsage(Locale locale);

        int getMinArgs();

        int getMaxArgs();

        void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, IPersistentDataHolder persistentDataHolder,
                     String[] args);

    }

    private static class GetSubCommand implements SubCommand {

        @Override
        public String getUsage(Locale locale) {
            return " [" + Message.COMMAND_ARGUMENT_PATH.getMessage(locale) + "]";
        }

        @Override
        public int getMinArgs() {
            return 5;
        }

        @Override
        public int getMaxArgs() {
            return 6;
        }

        @Override
        public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, IPersistentDataHolder persistentDataHolder, String[] args) {
            if (persistentDataHolder.isPersistentDataContainerEmpty()) {
                Message.PERSISTENT_DATA_EMPTY.send(sender);
                return;
            }

            Locale locale = PlayerLocales.getLocale(sender);

            PersistentDataSerializer serializer = new PersistentDataSerializer(locale);

            PersistentDataContainer dataContainer = persistentDataHolder.getPersistentDataContainer();

            String serializedValue;

            if (args.length == 6) {
                if (!dataContainer.has(args[5])) {
                    Message.PERSISTENT_DATA_EMPTY.send(sender);
                    return;
                }

                serializedValue = serializer.serialize(dataContainer.get(args[5]));
            } else {
                if (dataContainer.isEmpty()) {
                    Message.PERSISTENT_DATA_EMPTY.send(sender);
                    return;
                }

                StringBuilder dataMessageBuilder = new StringBuilder();

                dataContainer.forEach((key, value) -> {
                    String valueSerialized = serializer.serialize(value);

                    if (valueSerialized.isEmpty())
                        return;

                    if (dataMessageBuilder.length() != 0)
                        dataMessageBuilder.append(Message.PERSISTENT_DATA_SHOW_SPACER.getMessage(locale));

                    dataMessageBuilder.append(Message.PERSISTENT_DATA_SHOW_PATH.getMessage(locale, key)).append(valueSerialized);
                });

                if (dataMessageBuilder.length() < 2) {
                    Message.PERSISTENT_DATA_EMPTY.send(sender);
                    return;
                }

                serializedValue = dataMessageBuilder.toString();
            }

            Message.PERSISTENT_DATA_SHOW.send(sender, args[4], serializedValue);
        }

    }

    private static class SetSubCommand implements SubCommand {

        @Override
        public String getUsage(Locale locale) {
            return " <path> <value...>";
        }

        @Override
        public int getMinArgs() {
            return 7;
        }

        @Override
        public int getMaxArgs() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, IPersistentDataHolder persistentDataHolder, String[] args) {
            PersistentDataContainer dataContainer = persistentDataHolder.getPersistentDataContainer();

            PersistentDataSerializer serializer = new PersistentDataSerializer(PlayerLocales.getLocale(sender));

            String path = args[5];

            // noinspection unchecked
            Pair<Object, PersistentDataType<Object>> value = (Pair<Object, PersistentDataType<Object>>)
                    serializer.deserialize(String.join(" ", Arrays.copyOfRange(args, 6, args.length)));

            assert value != null;

            dataContainer.remove(path);
            Object oldValue = dataContainer.put(path, value.getValue(), value.getKey());

            if (!value.getKey().equals(oldValue))
                persistentDataHolder.savePersistentDataContainer();

            Message.PERSISTENT_DATA_MODIFY.send(sender, args[4], path,
                    serializer.serialize(value.getKey()));
        }

    }

    private static class RemoveSubCommand implements SubCommand {

        @Override
        public String getUsage(Locale locale) {
            return " <path>";
        }

        @Override
        public int getMinArgs() {
            return 6;
        }

        @Override
        public int getMaxArgs() {
            return 6;
        }

        @Override
        public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, IPersistentDataHolder persistentDataHolder, String[] args) {
            if (persistentDataHolder.isPersistentDataContainerEmpty()) {
                Message.PERSISTENT_DATA_EMPTY.send(sender);
                return;
            }

            PersistentDataContainer dataContainer = persistentDataHolder.getPersistentDataContainer();

            if (!dataContainer.has(args[5])) {
                Message.PERSISTENT_DATA_EMPTY.send(sender);
                return;
            }

            Object oldValue = dataContainer.remove(args[5]);

            if (oldValue != null)
                persistentDataHolder.savePersistentDataContainer();

            Message.PERSISTENT_DATA_REMOVED.send(sender, args[4], args[5]);
        }
    }

}
