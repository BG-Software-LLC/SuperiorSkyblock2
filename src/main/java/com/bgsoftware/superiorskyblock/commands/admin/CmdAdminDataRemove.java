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
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminDataRemove implements InternalPersistentDataCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("remove");
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
                .add(CommandArguments.required("path", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PATH))
                .build();
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PersistentDataHolderCommandContext context) throws CommandSyntaxException {
        CommandSender dispatcher = context.getDispatcher();

        IPersistentDataHolder persistentDataHolder = context.getPersistentDataHolder();
        String path = context.getRequiredArgument("path", String.class);

        if (persistentDataHolder.isPersistentDataContainerEmpty()) {
            Message.PERSISTENT_DATA_EMPTY.send(dispatcher);
            return;
        }

        PersistentDataContainer dataContainer = persistentDataHolder.getPersistentDataContainer();

        if (!dataContainer.has(path)) {
            Message.PERSISTENT_DATA_EMPTY.send(dispatcher);
            return;
        }

        Object oldValue = dataContainer.remove(path);

        if (oldValue != null)
            persistentDataHolder.savePersistentDataContainer();

        Message.PERSISTENT_DATA_REMOVED.send(dispatcher, context.getInputArgument("holder"), path);
    }

}
