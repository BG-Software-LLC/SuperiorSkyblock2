package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.commands.InternalPersistentDataCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PersistentDataHolderArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PersistentDataHolderCommandContext;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.serialization.impl.PersistentDataSerializer;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminDataGet implements InternalPersistentDataCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("get");
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getDescription(Locale locale) {
        return "";
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("holder-type", StringArgumentType.INSTANCE, "player/island"))
                .add(CommandArguments.required("holder", PersistentDataHolderArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArguments.optional("path", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PATH))
                .build();
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PersistentDataHolderCommandContext context) throws CommandSyntaxException {
        CommandSender dispatcher = context.getDispatcher();

        IPersistentDataHolder persistentDataHolder = context.getPersistentDataHolder();
        String path = context.getOptionalArgument("path", String.class).orElse(null);

        if (persistentDataHolder.isPersistentDataContainerEmpty()) {
            Message.PERSISTENT_DATA_EMPTY.send(dispatcher);
            return;
        }

        Locale locale = PlayerLocales.getLocale(dispatcher);

        PersistentDataSerializer serializer = new PersistentDataSerializer(locale);

        PersistentDataContainer dataContainer = persistentDataHolder.getPersistentDataContainer();

        String serializedValue;

        if (!Text.isBlank(path)) {
            if (!dataContainer.has(path)) {
                Message.PERSISTENT_DATA_EMPTY.send(dispatcher);
                return;
            }

            serializedValue = serializer.serialize(dataContainer.get(path));
        } else {
            if (dataContainer.isEmpty()) {
                Message.PERSISTENT_DATA_EMPTY.send(dispatcher);
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
                Message.PERSISTENT_DATA_EMPTY.send(dispatcher);
                return;
            }

            serializedValue = dataMessageBuilder.toString();
        }

        Message.PERSISTENT_DATA_SHOW.send(dispatcher, context.getInputArgument("holder"), serializedValue);
    }

}
