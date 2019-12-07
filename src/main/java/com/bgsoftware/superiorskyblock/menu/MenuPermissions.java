package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuPermissions extends SuperiorMenu {

    private static List<Integer> slots;
    private static int previousSlot, currentSlot, nextSlot;
    private static List<IslandPermission> islandPermissions = new ArrayList<>();

    private final Island island;
    private final Object permissionHolder;

    private int currentPage = 1;

    private MenuPermissions(SuperiorPlayer superiorPlayer, Island island, Object permissionHolder){
        super("menuPermissions", superiorPlayer);
        this.island = island;
        this.permissionHolder = permissionHolder;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(e.getRawSlot() == previousSlot || e.getRawSlot() == nextSlot || e.getRawSlot() == currentSlot){
            if(e.getRawSlot() == currentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < islandPermissions.size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            currentPage = e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1;

            previousMove = false;
            open(previousMenu);
        }
        else {
            if(e.getCurrentItem() == null)
                return;

            int permissionsAmount = islandPermissions.size();

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf >= permissionsAmount || indexOf == -1)
                return;

            IslandPermission permission = islandPermissions.get(indexOf + (slots.size() * (currentPage - 1)));
            String permissionName = permission.name().toLowerCase();
            String permissionHolderName = "";

            boolean success = false, sendFailMessage = true;

            if(permissionHolder instanceof PlayerRole){
                PlayerRole currentRole = island.getRequiredPlayerRole(permission);

                //Left Click
                if(e.getAction() == InventoryAction.PICKUP_ALL){
                    if(!superiorPlayer.getPlayerRole().isLessThan(currentRole)) {
                        PlayerRole previousRole = SPlayerRole.of(currentRole.getWeight() - 1);
                        success = true;

                        if (previousRole == null) {
                            sendFailMessage = false;
                            success = false;
                        }
                        else {
                            island.setPermission(previousRole, permission, true);
                            island.setPermission(currentRole, permission, false);
                        }
                    }
                }

                //Right Click
                else if(e.getAction() == InventoryAction.PICKUP_HALF){
                    if(superiorPlayer.getPlayerRole().isHigherThan(currentRole)) {
                        PlayerRole nextRole = SPlayerRole.of(currentRole.getWeight() + 1);
                        success = true;

                        if (nextRole == null) {
                            sendFailMessage = false;
                            success = false;
                        }
                        else {
                            island.setPermission(nextRole, permission, true);
                        }
                    }
                }

                else return;

                permissionHolderName = StringUtils.format(permissionName);
            }

            else{
                if(!containsData(permissionName + "-permission-enabled"))
                    return;

                if(island.hasPermission(superiorPlayer, permission)){
                    success = true;
                    PermissionNode permissionNode = island.getPermissionNode((SuperiorPlayer) permissionHolder);

                    permissionHolderName = ((SuperiorPlayer) permissionHolder).getName();

                    boolean currentValue = permissionNode.hasPermission(permission);

                    island.setPermission((SuperiorPlayer) permissionHolder, permission, !currentValue);
                }
            }

            if(success){
                Locale.UPDATED_PERMISSION.send(superiorPlayer, permissionHolderName);

                SoundWrapper soundWrapper = (SoundWrapper) getData(permissionName + "-has-access-sound");
                if (soundWrapper != null)
                    soundWrapper.playSound(superiorPlayer.asPlayer());
                //noinspection unchecked
                List<String> commands = (List<String>) getData(permissionName + "-has-access-commands");
                if (commands != null)
                    commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

                previousMove = false;
                open(previousMenu);
            }
            else{
                if(sendFailMessage)
                    Locale.LACK_CHANGE_PERMISSION.send(superiorPlayer);

                SoundWrapper soundWrapper = (SoundWrapper) getData(permissionName + "-no-access-sound");
                if (soundWrapper != null)
                    soundWrapper.playSound(superiorPlayer.asPlayer());
                //noinspection unchecked
                List<String> commands = (List<String>) getData(permissionName + "-no-access-commands");
                if (commands != null)
                    commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));
            }

        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        int permissionsAmount = islandPermissions.size();

        for(int i = 0; i < slots.size() && (i + (slots.size() * (currentPage - 1))) < permissionsAmount; i++){
            IslandPermission permission = islandPermissions.get(i + (slots.size() * (currentPage - 1)));
            inventory.setItem(slots.get(i), getItem(permission).build(superiorPlayer));
        }

        inventory.setItem(previousSlot, new ItemBuilder(inventory.getItem(previousSlot))
                .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));

        inventory.setItem(currentSlot, new ItemBuilder(inventory.getItem(currentSlot))
                .replaceAll("{0}", currentPage + "").build(superiorPlayer));

        inventory.setItem(nextSlot, new ItemBuilder(inventory.getItem(nextSlot))
                .replaceAll("{0}", (permissionsAmount > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    private ItemBuilder getItem(IslandPermission islandPermission){
        ItemBuilder permissionItem = new ItemBuilder(Material.AIR);
        String permissionName = islandPermission.name().toLowerCase();

        if(permissionHolder instanceof PlayerRole){
            if (containsData(permissionName + "-role-permission")) {
                PlayerRole requiredRole = island.getRequiredPlayerRole(islandPermission);
                permissionItem = ((ItemBuilder) getData(permissionName + "-role-permission")).clone()
                        .replaceAll("{}", requiredRole.toString());
            }
        }
        else{
            if (containsData(permissionName + "-permission-enabled")) {
                boolean hasPermission = island.getPermissionNode((SuperiorPlayer) permissionHolder).hasPermission(islandPermission);
                permissionItem = ((ItemBuilder) getData(permissionName + "-permission-" + (hasPermission ? "enabled" : "disabled"))).clone();
            }
        }

        return permissionItem;
    }

    public static void init(){
        MenuPermissions menuPermissions = new MenuPermissions(null, null, null);

        File file = new File(plugin.getDataFolder(), "menus/permissions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/permissions.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuPermissions, "permissions.yml", cfg);

        previousSlot = charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0);
        currentSlot = charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0);
        nextSlot = charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0);

        slots = charSlots.getOrDefault(cfg.getString("slots", "@").charAt(0), Collections.singletonList(-1));
        slots.sort(Integer::compareTo);

        ConfigurationSection permissionsSection = cfg.getConfigurationSection("permissions");

        islandPermissions = new ArrayList<>();

        for(IslandPermission islandPermission : IslandPermission.values()){
            String permission = islandPermission.name().toLowerCase();
            if(permissionsSection.contains(permission)){
                ConfigurationSection permissionSection = permissionsSection.getConfigurationSection(permission);
                menuPermissions.addData(permission + "-has-access-sound", FileUtils.getSound(permissionSection.getConfigurationSection("access.sound")));
                menuPermissions.addData(permission + "-has-access-commands", cfg.getStringList("access.commands"));
                menuPermissions.addData(permission + "-no-access-sound", FileUtils.getSound(permissionSection.getConfigurationSection("no-access.sound")));
                menuPermissions.addData(permission + "-no-access-commands", cfg.getStringList("no-access.commands"));
                menuPermissions.addData(permission + "-permission-enabled", FileUtils.getItemStack("permissions.yml", permissionSection.getConfigurationSection("permission-enabled")));
                menuPermissions.addData(permission + "-permission-disabled", FileUtils.getItemStack("permissions.yml", permissionSection.getConfigurationSection("permission-disabled")));
                if(permissionSection.contains("role-permission")) {
                    menuPermissions.addData(permission + "-role-permission", FileUtils.getItemStack("permissions.yml", permissionSection.getConfigurationSection("role-permission")));
                }
                islandPermissions.add(islandPermission);
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island, Object permissionHolder){
        new MenuPermissions(superiorPlayer, island, permissionHolder).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuPermissions.class);
    }

}
