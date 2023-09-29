package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuConfigEditor;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSettings implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("settings");
    }

    @Override
    public String getPermission() {
        return "superior.admin.settings";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SETTINGS.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return Collections.emptyList();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        CommandSender dispatcher = context.getDispatcher();
        Menus.MENU_CONFIG_EDITOR.createView(plugin.getPlayers().getSuperiorPlayer(dispatcher), MenuConfigEditor.Args.ROOT);

    }

}
