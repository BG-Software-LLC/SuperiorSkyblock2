package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IslandPermissionsMenu extends SuperiorMenu {

    private static Map<String, Object> permissionsData = new HashMap<>();

    private static Inventory inventory = null;
    private static String title;
    private static List<Integer> slots;
    private static int previousSlot, currentSlot, nextSlot;
    private static ItemStack previousButton, currentButton, nextButton;
    private static List<IslandPermission> islandPermissions;

    private Island island;
    private Object permissionHolder;

    private int currentPage = 1;

    private IslandPermissionsMenu(Island island, Object permissionHolder){
        super("permissionsPage");
        this.island = island;
        this.permissionHolder = permissionHolder;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked().getUniqueId());

        if(e.getRawSlot() == previousSlot || e.getRawSlot() == nextSlot || e.getRawSlot() == currentSlot){
            if(e.getRawSlot() == currentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < islandPermissions.size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            open(superiorPlayer, e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1, null);
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
                if(!permissionsData.containsKey(permissionName + "-permission-enabled"))
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

                SoundWrapper soundWrapper = get(permissionName + "-has-access-sound", SoundWrapper.class);
                if (soundWrapper != null)
                    soundWrapper.playSound(superiorPlayer.asPlayer());
                //noinspection unchecked
                List<String> commands = get(permissionName + "-has-access-commands", List.class);
                if (commands != null)
                    commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

                open(superiorPlayer, currentPage, null);
            }
            else{
                if(sendFailMessage)
                    Locale.LACK_CHANGE_PERMISSION.send(superiorPlayer);

                SoundWrapper soundWrapper = get(permissionName + "-no-access-sound", SoundWrapper.class);
                if (soundWrapper != null)
                    soundWrapper.playSound(superiorPlayer.asPlayer());
                //noinspection unchecked
                List<String> commands = get(permissionName + "-no-access-commands", List.class);
                if (commands != null)
                    commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));
            }

        }
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        open(superiorPlayer, 1, previousMenu);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    private void open(SuperiorPlayer superiorPlayer, int page, SuperiorMenu previousMenu) {
        if (Bukkit.isPrimaryThread()) {
            Executor.async(() -> open(superiorPlayer, page, previousMenu));
            return;
        }

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        inv.setContents(inventory.getContents());

        int permissionsAmount = islandPermissions.size();

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < permissionsAmount; i++){
            IslandPermission permission = islandPermissions.get(i + (slots.size() * (page - 1)));
            inv.setItem(slots.get(i), getItem(permission));
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceAll("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceAll("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceAll("{0}", (permissionsAmount > page * slots.size() ? "&a" : "&c")).build());

        this.currentPage = page;

        this.previousMenu = null;

        Executor.sync(() -> {
            superiorPlayer.asPlayer().openInventory(inv);
            this.previousMenu = previousMenu;
        });
    }

    private ItemStack getItem(IslandPermission islandPermission){
        ItemStack permissionItem = new ItemStack(Material.AIR);
        String permissionName = islandPermission.name().toLowerCase();

        if(permissionHolder instanceof PlayerRole){
            if (permissionsData.containsKey(permissionName + "-role-permission")) {
                PlayerRole requiredRole = island.getRequiredPlayerRole(islandPermission);
                permissionItem = new ItemBuilder(get(permissionName + "-role-permission", ItemStack.class))
                        .replaceAll("{}", requiredRole.toString()).build();
            }
        }
        else{
            if (permissionsData.containsKey(permissionName + "-permission-enabled")) {
                boolean hasPermission = island.getPermissionNode((SuperiorPlayer) permissionHolder).hasPermission(islandPermission);
                permissionItem = get(permissionName + "-permission-" + (hasPermission ? "enabled" : "disabled"), ItemStack.class);
            }
        }

        return permissionItem;
    }

    private static <T> T get(String key, Class<T> type){
        return type.cast(permissionsData.get(key));
    }

    public static void init(){
        IslandPermissionsMenu islandPanelMenu = new IslandPermissionsMenu(null, null);

        File file = new File(plugin.getDataFolder(), "guis/permissions-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/permissions-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("permissions-gui.title", "&lPermissions Controller ({0})"));
        inventory = FileUtils.loadGUI(islandPanelMenu, cfg.getConfigurationSection("permissions-gui"), 1, "&lPermissions Controller ({0})");

        slots = new ArrayList<>();
        Arrays.stream(cfg.getString("permissions-gui.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));

        previousButton = FileUtils.getItemStack(cfg.getConfigurationSection("permissions-gui.previous-page"));
        currentButton = FileUtils.getItemStack(cfg.getConfigurationSection("permissions-gui.current-page"));
        nextButton = FileUtils.getItemStack(cfg.getConfigurationSection("permissions-gui.next-page"));
        previousSlot = cfg.getInt("permissions-gui.previous-page.slot");
        currentSlot = cfg.getInt("permissions-gui.current-page.slot");
        nextSlot = cfg.getInt("permissions-gui.next-page.slot");
        inventory.setItem(previousSlot, previousButton);
        inventory.setItem(currentSlot, currentButton);
        inventory.setItem(nextSlot, nextButton);

        ConfigurationSection section = cfg.getConfigurationSection("permissions-gui.permissions");

        islandPermissions = new ArrayList<>();
        for(IslandPermission islandPermission : IslandPermission.values()){
            String permission = islandPermission.name().toLowerCase();
            if(section.contains(permission)){
                permissionsData.put(permission + "-has-access-sound", FileUtils.getSound(section.getConfigurationSection(permission + ".has-access.sound")));
                permissionsData.put(permission + "-has-access-commands", FileUtils.getSound(section.getConfigurationSection(permission + ".has-access.commands")));
                permissionsData.put(permission + "-no-access-sound", FileUtils.getSound(section.getConfigurationSection(permission + ".no-access.sound")));
                permissionsData.put(permission + "-no-access-commands", FileUtils.getSound(section.getConfigurationSection(permission + ".no-access.commands")));
                permissionsData.put(permission + "-permission-enabled",
                        FileUtils.getItemStack(section.getConfigurationSection(permission + ".permission-enabled")));
                permissionsData.put(permission + "-permission-disabled",
                        FileUtils.getItemStack(section.getConfigurationSection(permission + ".permission-disabled")));
                if(section.contains(permission + ".role-permission")) {
                    permissionsData.put(permission + "-role-permission",
                            FileUtils.getItemStack(section.getConfigurationSection(permission + ".role-permission")));
                }
                islandPermissions.add(islandPermission);
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island, Object permissionHolder){
        new IslandPermissionsMenu(island, permissionHolder).open(superiorPlayer, previousMenu);
    }

}
