package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.PluginReloadReason;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;

public class CmdAdminReload implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("reload");
    }

    @Override
    public String getPermission() {
        return "superior.admin.reload";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin reload";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RELOAD.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (plugin.getTaskScheduler().isFolia() && !plugin.getTaskScheduler().isGlobalThread()) {
            plugin.getTaskScheduler().global(() -> execute(plugin, sender, args), 0L, 0L);
            return;
        }
        Message.RELOAD_PROCCESS_REQUEST.send(sender);

        if (plugin.getTaskScheduler().isFolia()) {
            plugin.reloadPluginAsync(PluginReloadReason.COMMAND).whenComplete((ignored, error) ->
                    BukkitExecutor.ensureMain(() -> {
                        if (error != null) {
                            Throwable cause = error;
                            while (cause instanceof CompletionException && cause.getCause() != null)
                                cause = cause.getCause();
                            Log.error(cause, "An unexpected error occurred while reloading the plugin:");
                            if (!(cause instanceof ManagerLoadException) ||
                                    !ManagerLoadException.handle((ManagerLoadException) cause))
                                return;
                        }
                        Message.RELOAD_COMPLETED.send(sender);
                    }));
            return;
        }

        try {
            plugin.reloadPlugin(PluginReloadReason.COMMAND);
        } catch (ManagerLoadException error) {
            Log.error(error, "An unexpected error occurred while reloading the plugin:");
            if (!ManagerLoadException.handle(error))
                return;
        }

        Message.RELOAD_COMPLETED.send(sender);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
