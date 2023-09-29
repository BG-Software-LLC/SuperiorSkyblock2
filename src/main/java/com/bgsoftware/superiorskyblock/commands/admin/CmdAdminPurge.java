package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminPurge implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("purge");
    }

    @Override
    public String getPermission() {
        return "superior.admin.purge";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_PURGE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("purge", StringArgumentType.INSTANCE, "cancel", Message.COMMAND_ARGUMENT_TIME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        String purge = context.getRequiredArgument("purge", String.class);

        if (purge.equalsIgnoreCase("cancel")) {
            plugin.getGrid().getIslandsToPurge().forEach(island -> plugin.getGrid().removeIslandFromPurge(island));
            Message.PURGE_CLEAR.send(dispatcher);
            return;
        }

        long timeToPurge = parseLongSafe(purge);
        long currentTime = System.currentTimeMillis() / 1000;

        List<Island> islands = new SequentialListBuilder<Island>().filter(island -> {
            long lastTimeUpdate = island.getLastTimeUpdate();
            return lastTimeUpdate != -1 && currentTime - lastTimeUpdate >= timeToPurge;
        }).build(plugin.getGrid().getIslands());

        if (islands.isEmpty()) {
            Message.NO_ISLANDS_TO_PURGE.send(dispatcher);
        } else {
            BukkitExecutor.async(() -> islands.forEach(island -> plugin.getGrid().addIslandToPurge(island)));
            Message.PURGED_ISLANDS.send(dispatcher, islands.size());
        }

    }

    private static long parseLongSafe(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception error) {
            return 0;
        }
    }

}
