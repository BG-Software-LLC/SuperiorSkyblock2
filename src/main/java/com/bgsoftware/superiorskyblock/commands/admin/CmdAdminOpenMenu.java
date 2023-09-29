package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPlayerCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PlayerCommandContext;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Arrays;
import java.util.List;

public class CmdAdminOpenMenu implements InternalPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("openmenu", "menu");
    }

    @Override
    public String getPermission() {
        return "superior.admin.openmenu";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_OPEN_MENU.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("player", PlayerArgumentType.ONLINE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .add(CommandArguments.required("menu-name", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_MENU))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean requireIslandFromPlayer() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PlayerCommandContext context) {
        SuperiorPlayer targetPlayer = context.getSuperiorPlayer();

        String menuName = context.getRequiredArgument("menu-name", String.class);

        Menu<BaseMenuView, EmptyViewArgs> menu = plugin.getMenus().getMenu(MenuIdentifiers.MENU_CUSTOM_PREFIX + menuName);
        if (menu != null)
            menu.createView(targetPlayer, EmptyViewArgs.INSTANCE);
    }

}
