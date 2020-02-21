package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuPermissions extends PagedSuperiorMenu<IslandPermission> {

    private static List<IslandPermission> islandPermissions = new ArrayList<>();

    private final Island island;
    private final Object permissionHolder;

    private MenuPermissions(SuperiorPlayer superiorPlayer, Island island, Object permissionHolder){
        super("menuPermissions", superiorPlayer);
        this.island = island;
        this.permissionHolder = permissionHolder;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, IslandPermission permission) {
        String permissionName = permission.name().toLowerCase();
        String permissionHolderName = "";

        boolean success = false, sendFailMessage = true;

        if(permissionHolder instanceof PlayerRole){
            PlayerRole currentRole = island.getRequiredPlayerRole(permission);

            //Left Click
            if(event.getAction() == InventoryAction.PICKUP_ALL){
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
            else if(event.getAction() == InventoryAction.PICKUP_HALF){
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

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, IslandPermission islandPermission) {
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

        return permissionItem.build(superiorPlayer);
    }

    @Override
    protected List<IslandPermission> requestObjects() {
        return islandPermissions;
    }

    public static void init(){
        MenuPermissions menuPermissions = new MenuPermissions(null, null, null);

        File file = new File(plugin.getDataFolder(), "menus/permissions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/permissions.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuPermissions, "permissions.yml", cfg);

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

        menuPermissions.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuPermissions.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuPermissions.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuPermissions.setSlots(charSlots.getOrDefault(cfg.getString("slots", " ").charAt(0), Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island, Object permissionHolder){
        new MenuPermissions(superiorPlayer, island, permissionHolder).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuPermissions.class);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/permissions-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("permissions-gui.title"));

        int size = cfg.getInt("permissions-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("permissions-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("permissions-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("permissions-gui"), newMenu,
                patternChars, slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("permissions", cfg.getConfigurationSection("permissions-gui.permissions"));
        newMenu.set("sounds", null);
        newMenu.set("commands", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}