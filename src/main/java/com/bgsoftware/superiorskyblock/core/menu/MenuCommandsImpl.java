package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.MenuCommands;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.PlayerMenuView;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuCommandsImpl implements MenuCommands {

    private static final MenuCommandsImpl INSTANCE = new MenuCommandsImpl();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    private static final Pattern COMMAND_PATTERN_ARGS = Pattern.compile("\\[(.+)](.+)");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("\\[(.+)]");

    public static MenuCommandsImpl getInstance() {
        return INSTANCE;
    }

    private MenuCommandsImpl() {

    }

    @Override
    public void runCommand(MenuView<?, ?> menuView, String command, InventoryClickEvent clickEvent) {
        this.runCommand(menuView, command, clickEvent, Bukkit.getConsoleSender());
    }

    private void runCommand(MenuView<?, ?> menuView, String command, InventoryClickEvent clickEvent, CommandSender sender) {
        Matcher matcher = COMMAND_PATTERN_ARGS.matcher(command);

        if (matcher.matches()) {
            String subCommand = matcher.group(1);
            String args = matcher.group(2).trim();
            handleSubCommand(menuView, subCommand, args, clickEvent, sender);
        } else if ((matcher = COMMAND_PATTERN.matcher(command)).matches()) {
            String subCommand = matcher.group(1);
            handleSubCommand(menuView, subCommand, "", clickEvent, sender);
        } else if (command.equalsIgnoreCase("close")) {
            setClickedCloseButton(menuView);
            menuView.closeView();
        } else if (command.equalsIgnoreCase("back")) {
            setClickedCloseButton(menuView);
            clickEvent.getWhoClicked().closeInventory();
        } else {
            SuperiorPlayer targetPlayer = menuView instanceof PlayerMenuView ?
                    ((PlayerMenuView) menuView).getSuperiorPlayer() : null;

            if (targetPlayer != null)
                command = placeholdersService.get().parsePlaceholders(targetPlayer.asOfflinePlayer(), command);
            else if (sender instanceof Player)
                command = placeholdersService.get().parsePlaceholders((Player) sender, command);

            Bukkit.dispatchCommand(sender instanceof Player || command.startsWith("PLAYER:") ? clickEvent.getWhoClicked() : Bukkit.getConsoleSender(),
                    command.replace("PLAYER:", "").replace("%player%", clickEvent.getWhoClicked().getName()));
        }
    }

    private void handleSubCommand(MenuView<?, ?> menuView, String subCommand, String args,
                                  InventoryClickEvent clickEvent, CommandSender sender) {
        switch (subCommand.toLowerCase(Locale.ENGLISH)) {
            case "player":
                runCommand(menuView, args, clickEvent, clickEvent.getWhoClicked());
                break;
            case "admin":
                String commandLabel = plugin.getSettings().getIslandCommand().split(",")[0];
                runCommand(menuView, commandLabel + " admin " + args, clickEvent, sender);
                break;
            case "close":
                setClickedCloseButton(menuView);
                menuView.closeView();
                clickEvent.getWhoClicked().closeInventory();
                break;
            case "back":
                setClickedCloseButton(menuView);
                clickEvent.getWhoClicked().closeInventory();
                break;
            default:
                plugin.getCommands().dispatchSubCommand(sender, subCommand, args);
                break;
        }
    }

    private static void setClickedCloseButton(MenuView<?, ?> menuView) {
        if (menuView instanceof AbstractMenuView)
            ((AbstractMenuView<?, ?>) menuView).setClickedCloseButton();
    }

}
