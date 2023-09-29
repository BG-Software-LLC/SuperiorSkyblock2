package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.commands.InternalPersistentDataCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PersistentDataHolderArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PersistentDataHolderCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.serialization.impl.PersistentDataSerializer;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminDataSet implements InternalPersistentDataCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("set");
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
                .add(CommandArgument.required("holder-type", StringArgumentType.INSTANCE, "player/island"))
                .add(CommandArgument.required("holder", PersistentDataHolderArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArgument.required("path", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PATH))
                .add(CommandArgument.required("value", StringArgumentType.MULTIPLE, Message.COMMAND_ARGUMENT_VALUE))
                .build();
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PersistentDataHolderCommandContext context) throws CommandSyntaxException {
        CommandSender dispatcher = context.getDispatcher();

        IPersistentDataHolder persistentDataHolder = context.getPersistentDataHolder();
        String path = context.getRequiredArgument("path", String.class);
        String serializedValue = context.getRequiredArgument("value", String.class);

        PersistentDataContainer dataContainer = persistentDataHolder.getPersistentDataContainer();

        PersistentDataSerializer serializer = new PersistentDataSerializer(PlayerLocales.getLocale(dispatcher));

        // noinspection unchecked
        Pair<Object, PersistentDataType<Object>> value = (Pair<Object, PersistentDataType<Object>>) serializer.deserialize(serializedValue);

        assert value != null;

        dataContainer.remove(path);
        Object oldValue = dataContainer.put(path, value.getValue(), value.getKey());

        if (!value.getKey().equals(oldValue))
            persistentDataHolder.savePersistentDataContainer();

        Message.PERSISTENT_DATA_MODIFY.send(dispatcher, context.getInputArgument("holder"), path,
                serializer.serialize(value.getKey()));
    }

}
