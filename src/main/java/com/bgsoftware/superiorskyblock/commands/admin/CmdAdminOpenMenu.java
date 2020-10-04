package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuCustom;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminOpenMenu implements IAdminPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("openmenu", "menu");
    }

    @Override
    public String getPermission() {
        return "superior.admin.openmenu";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin openmenu <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_MENU.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_OPEN_MENU.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultiplePlayers() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        if(!SuperiorMenuCustom.isValidMenu(args[3])){
            return;
        }

        SuperiorMenuCustom.openInventory(targetPlayer, args[3], null);
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getMenus(args[3]) : new ArrayList<>();
    }

}
