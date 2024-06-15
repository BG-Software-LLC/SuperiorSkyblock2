package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuConfigEditor;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CmdAdminSettings extends BaseCommand implements ISuperiorCommand {

    @Override
    protected List<String> aliases() {
        return Lists.singleton("settings");
    }

    @Override
    protected String permission() {
        return "superior.admin.settings";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "admin settings";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SETTINGS.getMessage(locale);
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
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Menus.MENU_CONFIG_EDITOR.createView(plugin.getPlayers().getSuperiorPlayer(sender), MenuConfigEditor.Args.ROOT);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Lists.emptyList();
    }

}
