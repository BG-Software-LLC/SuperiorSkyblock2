package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class IslandMembersMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";
    private static ItemStack previousButton, currentButton, nextButton, memberItem;
    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots = new ArrayList<>();

    private List<SuperiorPlayer> members;
    private int page;

    private IslandMembersMenu(Island island){
        super("membersPage");
        if(island != null) {
            this.members = island.getIslandMembers(true);
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
        int clickedSlot = e.getRawSlot();

        if(clickedSlot == previousSlot || clickedSlot == nextSlot || clickedSlot == currentSlot){
            int nextPage;

            if(clickedSlot == previousSlot){
                nextPage = page == 1 ? -1 : page - 1;
            }
            else if(clickedSlot == nextSlot){
                nextPage = members.size() > page * slots.size() ? page + 1 : -1;
            }
            else return;

            if(nextPage == -1)
                return;

            open(superiorPlayer, nextPage, previousMenu);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf < 0 || indexOf >= members.size())
                return;

            SuperiorPlayer targetPlayer = members.get(indexOf);

            if (targetPlayer != null) {
                SoundWrapper sound = getSound(-1);
                if(sound != null)
                    sound.playSound(e.getWhoClicked());
                List<String> commands = getCommands(-1);
                if(commands != null)
                    commands.forEach(command ->
                            Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                                    command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));
                previousMove = false;
                MemberManageMenu.openInventory(superiorPlayer, this, targetPlayer);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        open(superiorPlayer, 1, previousMenu);
    }

    private void open(SuperiorPlayer superiorPlayer, int page, SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(superiorPlayer, page, previousMenu));
            return;
        }

        this.page = page;

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        inv.setContents(inventory.getContents());

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < members.size(); i++){
            SuperiorPlayer _superiorPlayer = members.get(i + (slots.size() * (page - 1)));
            inv.setItem(slots.get(i), new ItemBuilder(memberItem)
                    .replaceAll("{0}", _superiorPlayer.getName())
                    .replaceAll("{1}", _superiorPlayer.getPlayerRole() + "")
                    .asSkullOf(_superiorPlayer).build());
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceAll("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceAll("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceAll("{0}", (members.size() > page * slots.size() ? "&a" : "&c")).build());


        Executor.sync(() -> {
            superiorPlayer.asPlayer().openInventory(inv);
            this.previousMenu = previousMenu;
        });
    }

    public static void init(){
        IslandMembersMenu islandMembersMenu = new IslandMembersMenu(null);

        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(islandMembersMenu, cfg.getConfigurationSection("members-panel"), 6, "&lIsland Members");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("members-panel.title"));

        previousButton = FileUtils.getItemStack(cfg.getConfigurationSection("members-panel.previous-page"));
        currentButton = FileUtils.getItemStack(cfg.getConfigurationSection("members-panel.current-page"));
        nextButton = FileUtils.getItemStack(cfg.getConfigurationSection("members-panel.next-page"));
        memberItem = FileUtils.getItemStack(cfg.getConfigurationSection("members-panel.member-item"));

        previousSlot = cfg.getInt("members-panel.previous-page.slot");
        currentSlot = cfg.getInt("members-panel.current-page.slot");
        nextSlot = cfg.getInt("members-panel.next-page.slot");

        islandMembersMenu.addSound(previousSlot, FileUtils.getSound(cfg.getConfigurationSection("members-panel.previous-page.sound")));
        islandMembersMenu.addSound(currentSlot, FileUtils.getSound(cfg.getConfigurationSection("members-panel.current-page.sound")));
        islandMembersMenu.addSound(nextSlot, FileUtils.getSound(cfg.getConfigurationSection("members-panel.next-page.sound")));
        islandMembersMenu.addSound(-1, FileUtils.getSound(cfg.getConfigurationSection("members-panel.member-item.sound")));
        islandMembersMenu.addCommands(previousSlot, cfg.getStringList("members-panel.previous-page.commands"));
        islandMembersMenu.addCommands(currentSlot, cfg.getStringList("members-panel.current-page.commands"));
        islandMembersMenu.addCommands(nextSlot, cfg.getStringList("members-panel.next-page.commands"));
        islandMembersMenu.addCommands(-1, cfg.getStringList("members-panel.member-item.commands"));

        inventory.setItem(previousSlot, previousButton);
        inventory.setItem(currentSlot, currentButton);
        inventory.setItem(nextSlot, nextButton);

        Arrays.stream(cfg.getString("members-panel.member-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
    }

    public static SuperiorMenu getMenu(Island island){
        return new IslandMembersMenu(island);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new IslandMembersMenu(island).open(superiorPlayer, previousMenu);
    }

}
