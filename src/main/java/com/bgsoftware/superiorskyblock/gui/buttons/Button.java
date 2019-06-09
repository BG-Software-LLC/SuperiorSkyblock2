package com.bgsoftware.superiorskyblock.gui.buttons;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Button {

    protected ItemStack item;

    protected BiConsumer<? super Player, ? super ClickType> action;

    protected List<String> commands;
    protected boolean console;

    public Button(ItemStack item, BiConsumer<? super Player, ? super ClickType> action) {
        this.item = item;
        this.action = action;

        commands = new ArrayList<>();
        console = false;
    }

    public void setItem(ItemStack item) {
        this.item.setType(item.getType());
        this.item.setData(item.getData());
        this.item.setAmount(item.getAmount());
        this.item.setItemMeta(item.getItemMeta());
    }

    public ItemStack getItem() {
        return item;
    }

    public BiConsumer<? super Player, ? super ClickType> getAction() {
        return action;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void setConsole(boolean console) {
        this.console = console;
    }

    public void sendCommands(CommandSender sender, String... placeholders) {
        if (console)
            sender = Bukkit.getConsoleSender();

        for (String command : commands) {
            String replaced = command + "";
            for (int i = 0; i < placeholders.length; i++)
                replaced = replaced.replace("{" + i + "}", placeholders[i]);

            Bukkit.dispatchCommand(sender, replaced);
        }
    }
}
