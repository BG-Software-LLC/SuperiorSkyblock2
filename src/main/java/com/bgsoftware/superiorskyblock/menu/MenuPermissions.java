package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
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
import java.util.List;

public final class MenuPermissions extends PagedSuperiorMenu<IslandPrivilege> {

    private static final List<IslandPrivilege> islandPermissions = new ArrayList<>();
    private static String noRolePermission = "", exactRolePermission = "", higherRolePermission = "";

    private final Island island;
    private final Object permissionHolder;

    private MenuPermissions(SuperiorPlayer superiorPlayer, Island island, Object permissionHolder){
        super("menuPermissions", superiorPlayer);
        this.island = island;
        this.permissionHolder = permissionHolder;
        if(permissionHolder instanceof SuperiorPlayer)
            updateTargetPlayer((SuperiorPlayer) permissionHolder);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, IslandPrivilege permission) {
        String permissionName = permission.getName().toLowerCase();
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
                soundWrapper.playSound(event.getWhoClicked());
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
                soundWrapper.playSound(event.getWhoClicked());
            //noinspection unchecked
            List<String> commands = (List<String>) getData(permissionName + "-no-access-commands");
            if (commands != null)
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island, permissionHolder);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, IslandPrivilege islandPermission) {
        try {
            ItemBuilder permissionItem = new ItemBuilder(Material.AIR);
            String permissionName = islandPermission.getName().toLowerCase();

            if (permissionHolder instanceof PlayerRole) {
                if (containsData(permissionName + "-role-permission")) {
                    PlayerRole requiredRole = island.getRequiredPlayerRole(islandPermission);
                    permissionItem = ((ItemBuilder) getData(permissionName + "-role-permission")).clone()
                            .replaceAll("{}", requiredRole.toString());

                    if(!noRolePermission.isEmpty() && !exactRolePermission.isEmpty() && !higherRolePermission.isEmpty()) {
                        List<String> roleString = new ArrayList<>();

                        int roleWeight = requiredRole.getWeight();
                        PlayerRole currentRole;

                        for (int i = -2; (currentRole = SPlayerRole.of(i)) != null; i++) {
                            if (i < roleWeight) {
                                roleString.add(noRolePermission.replace("{}", currentRole + ""));
                            } else if (i == roleWeight) {
                                roleString.add(exactRolePermission.replace("{}", currentRole + ""));
                            } else {
                                roleString.add(higherRolePermission.replace("{}", currentRole + ""));
                            }
                        }

                        List<String> lore = permissionItem.getItemMeta().getLore();

                        for (int i = 0; i < lore.size(); i++) {
                            String line = lore.get(i);
                            if (line.equals("{0}")) {
                                lore.set(i, roleString.get(0));
                                for (int j = 1; j < roleString.size(); j++) {
                                    lore.add(i + j, roleString.get(j));
                                }
                                i += roleString.size();
                            }
                        }

                        permissionItem.withLore(lore);
                    }
                }
            } else {
                if (containsData(permissionName + "-permission-enabled")) {
                    boolean hasPermission = island.getPermissionNode((SuperiorPlayer) permissionHolder).hasPermission(islandPermission);
                    permissionItem = ((ItemBuilder) getData(permissionName + "-permission-" + (hasPermission ? "enabled" : "disabled"))).clone();
                }
            }

            return permissionItem.build(superiorPlayer);
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of permission: " + islandPermission.getName());
            throw ex;
        }
    }

    @Override
    protected List<IslandPrivilege> requestObjects() {
        return islandPermissions;
    }

    public static void init(){
        MenuPermissions menuPermissions = new MenuPermissions(null, null, null);

        File file = new File(plugin.getDataFolder(), "menus/permissions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/permissions.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try {
            cfg.syncWithConfig(file, FileUtils.getResource("menus/permissions.yml"), additionalMenuSections("permissions"));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        noRolePermission = cfg.getString("messages.no-role-permission", "");
        exactRolePermission = cfg.getString("messages.exact-role-permission", "");
        higherRolePermission = cfg.getString("messages.higher-role-permission", "");

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuPermissions, "permissions.yml", cfg);

        ConfigurationSection permissionsSection = cfg.getConfigurationSection("permissions");

        islandPermissions.clear();
        int position = 0;

        for(String key : permissionsSection.getKeys(false)){
            if(permissionsSection.getBoolean(key + ".display-menu", false)) {
                try {
                    String permission = key.toLowerCase();
                    updatePermission(IslandPrivilege.getByName(permission), cfg, position++);
                }catch (Exception ignored){}
            }
        }

        menuPermissions.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuPermissions.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuPermissions.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuPermissions.setSlots(getSlots(cfg, "slots", charSlots));

        charSlots.delete();

        menuPermissions.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island, Object permissionHolder){
        new MenuPermissions(superiorPlayer, island, permissionHolder).open(previousMenu);
    }

    public static void refreshMenus(Island island){
        SuperiorMenu.refreshMenus(MenuPermissions.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static void refreshMenus(Island island, Object permissionHolder){
        SuperiorMenu.refreshMenus(MenuPermissions.class, superiorMenu -> superiorMenu.island.equals(island) &&
                superiorMenu.permissionHolder.equals(permissionHolder));
    }

    public static void updatePermission(IslandPrivilege islandPrivilege){
        File file = new File(plugin.getDataFolder(), "menus/permissions.yml");
        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        int position = 0;

        for(String key : cfg.getConfigurationSection("permissions").getKeys(false)){
            if(islandPrivilege.getName().equalsIgnoreCase(key))
                break;

            position++;
        }

        updatePermission(islandPrivilege, cfg, position);
    }

    public static void updatePermission(IslandPrivilege islandPrivilege, YamlConfiguration cfg, int position){
        if(!islandPermissions.contains(islandPrivilege)) {
            MenuPermissions menuPermissions = new MenuPermissions(null, null, null);
            String permission = islandPrivilege.getName().toLowerCase();
            if (cfg.contains("permissions." + permission)) {
                ConfigurationSection permissionSection = cfg.getConfigurationSection("permissions." + permission);
                menuPermissions.addData(permission + "-has-access-sound", FileUtils.getSound(permissionSection.getConfigurationSection("has-access.sound")));
                menuPermissions.addData(permission + "-has-access-commands", cfg.getStringList("has-access.commands"));
                menuPermissions.addData(permission + "-no-access-sound", FileUtils.getSound(permissionSection.getConfigurationSection("no-access.sound")));
                menuPermissions.addData(permission + "-no-access-commands", cfg.getStringList("no-access.commands"));
                menuPermissions.addData(permission + "-permission-enabled", FileUtils.getItemStack("permissions.yml", permissionSection.getConfigurationSection("permission-enabled")));
                menuPermissions.addData(permission + "-permission-disabled", FileUtils.getItemStack("permissions.yml", permissionSection.getConfigurationSection("permission-disabled")));
                if (permissionSection.contains("role-permission")) {
                    menuPermissions.addData(permission + "-role-permission", FileUtils.getItemStack("permissions.yml", permissionSection.getConfigurationSection("role-permission")));
                }
                if(position >= 0 && position < islandPermissions.size())
                    islandPermissions.add(position, islandPrivilege);
                else
                    islandPermissions.add(islandPrivilege);
            }
        }
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