package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.GUIIdentifier;
import com.bgsoftware.superiorskyblock.gui.GUIInventory;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;

import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class PanelHandler {

    private SuperiorSkyblockPlugin plugin;
    public GUIInventory mainPage, membersPage, visitorsPage, playerPage, rolePage, islandCreationPage;

    public Map<UUID, UUID> islands = new HashMap<>();

    public PanelHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        loadMenus(plugin);
    }

    private void loadMenus(SuperiorSkyblockPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "guis/panel-gui-old.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/panel-gui-old.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        initMainPage(cfg);
        initMembersPage(cfg);
        initVisitorsPage(cfg);
        initPlayerPage(cfg);
        initRolePage(cfg);

        file = new File(plugin.getDataFolder(), "guis/creation-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/creation-gui.yml");

        cfg = YamlConfiguration.loadConfiguration(file);

        initIslandCreationPage(cfg);
    }

    private Sound getSound(String name){
        try{
            return Sound.valueOf(name);
        }catch(Exception ex){
            return null;
        }
    }

    public void openPanel(SuperiorPlayer superiorPlayer){
        mainPage.openInventory(superiorPlayer, true);
    }

    public void openMembersPanel(SuperiorPlayer superiorPlayer, int page){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openMembersPanel(superiorPlayer, page)).start();
            return;
        }

        Inventory inventory = membersPage.clonedInventory();
        List<UUID> members = new ArrayList<>();

        if(superiorPlayer.getIsland() != null)
            members.addAll(superiorPlayer.getIsland().getAllMembers());

        members.sort(Comparator.comparing(o -> SSuperiorPlayer.of(o).getName()));

        //noinspection unchecked
        List<Integer> slots = membersPage.get("slots", List.class);

        ItemStack memberItem = membersPage.get("memberItem", ItemStack.class);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < members.size(); i++){
            SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(members.get(i + (slots.size() * (page - 1))));
            inventory.setItem(slots.get(i), new ItemBuilder(memberItem)
                    .replaceAll("{0}", _superiorPlayer.getName())
                    .asSkullOf(_superiorPlayer).build());
        }

        int previousSlot = membersPage.get("previousSlot", Integer.class);
        ItemStack previousButton = membersPage.get("previousButton", ItemStack.class);
        inventory.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        int currentSlot = membersPage.get("currentSlot", Integer.class);
        ItemStack currentButton = membersPage.get("currentButton", ItemStack.class);
        inventory.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        int nextSlot = membersPage.get("nextSlot", Integer.class);
        ItemStack nextButton = membersPage.get("nextButton", ItemStack.class);
        inventory.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (members.size() > page * slots.size() ? "&a" : "&c")).build());

        membersPage.openInventory(superiorPlayer, inventory);
    }

    public void openVisitorsPanel(SuperiorPlayer superiorPlayer, int page){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openVisitorsPanel(superiorPlayer, page)).start();
            return;
        }

        Inventory inventory = visitorsPage.clonedInventory();
        List<UUID> visitors = new ArrayList<>();

        if(superiorPlayer.getIsland() != null)
            visitors.addAll(superiorPlayer.getIsland().getVisitors());

        visitors.sort(Comparator.comparing(o -> SSuperiorPlayer.of(o).getName()));

        //noinspection unchecked
        List<Integer> slots = visitorsPage.get("slots", List.class);

        ItemStack visitorItem = visitorsPage.get("visitorItem", ItemStack.class);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < visitors.size(); i++){
            SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(visitors.get(i + (slots.size() * (page - 1))));
            String islandOwner = "None";
            if(_superiorPlayer.getIsland() != null)
                islandOwner = _superiorPlayer.getIsland().getOwner().getName();
            inventory.setItem(slots.get(i), new ItemBuilder(visitorItem)
                    .replaceAll("{0}", _superiorPlayer.getName())
                    .replaceAll("{1}", islandOwner)
                    .asSkullOf(_superiorPlayer).build());
        }

        int previousSlot = visitorsPage.get("previousSlot", Integer.class);
        ItemStack previousButton = visitorsPage.get("previousButton", ItemStack.class);
        inventory.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        int currentSlot = visitorsPage.get("currentSlot", Integer.class);
        ItemStack currentButton = visitorsPage.get("currentButton", ItemStack.class);
        inventory.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        int nextSlot = visitorsPage.get("nextSlot", Integer.class);
        ItemStack nextButton = visitorsPage.get("nextButton", ItemStack.class);
        inventory.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (visitors.size() > page * slots.size() ? "&a" : "&c")).build());

        visitorsPage.openInventory(superiorPlayer, inventory);
    }

    public void openPlayerPanel(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        Inventory inventory = Bukkit.createInventory(new GUIIdentifier(GUIInventory.PLAYER_PAGE_IDENTIFIER), playerPage.getSize(), ChatColor.BOLD + targetPlayer.getName());
        inventory.setContents(playerPage.getContents());
        playerPage.openInventory(superiorPlayer, inventory);
    }

    public void openRolePanel(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        Inventory inventory = Bukkit.createInventory(new GUIIdentifier(GUIInventory.ROLE_PAGE_IDENTIFIER), rolePage.getSize(), ChatColor.BOLD + targetPlayer.getName());
        inventory.setContents(rolePage.getContents());
        rolePage.openInventory(superiorPlayer, inventory);
    }

    public void openIslandCreationPanel(SuperiorPlayer superiorPlayer){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openIslandCreationPanel(superiorPlayer)).start();
            return;
        }

        Inventory inventory = islandCreationPage.clonedInventory();

        for(String schematic : plugin.getSchematics().getSchematics()){
            if(islandCreationPage.contains(schematic + "-has-access-item")) {
                ItemStack schematicItem = islandCreationPage.get(schematic + "-has-access-item", ItemStack.class);
                String permission = islandCreationPage.get(schematic + "-permission", String.class);
                int slot = islandCreationPage.get(schematic + "-slot", Integer.class);

                if(!superiorPlayer.hasPermission(permission))
                    schematicItem = islandCreationPage.get(schematic + "-no-access-item", ItemStack.class);

                inventory.setItem(slot, schematicItem);
            }
        }

        islandCreationPage.openInventory(superiorPlayer, inventory);
    }

    public Island getIsland(SuperiorPlayer superiorPlayer){
        return plugin.getGrid().getIsland(SSuperiorPlayer.of(islands.get(superiorPlayer.getUniqueId())));
    }

    private void initMainPage(YamlConfiguration cfg){
        mainPage = FileUtil.getGUI(GUIInventory.MAIN_PAGE_IDENTIFIER, cfg.getConfigurationSection("main-panel"), 5, "&lIsland Panel");

        ItemStack membersButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.members"));
        ItemStack settingsButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.settings"));
        ItemStack visitorsButton = FileUtil.getItemStack(cfg.getConfigurationSection("main-panel.visitors"));
        int membersSlot = cfg.getInt("main-panel.members.slot");
        int settingsSlot = cfg.getInt("main-panel.settings.slot");
        int visitorsSlot = cfg.getInt("main-panel.visitors.slot");
        Sound membersSound = getSound(cfg.getString("main-panel.members.sound", ""));
        Sound settingsSound = getSound(cfg.getString("main-panel.settings.sound", ""));
        Sound visitorsSound = getSound(cfg.getString("main-panel.visitors.sound", ""));

        mainPage.setItem(membersSlot, membersButton);
        mainPage.setItem(settingsSlot, settingsButton);
        mainPage.setItem(visitorsSlot, visitorsButton);

        mainPage.put("membersSound", membersSound);
        mainPage.put("settingsSound", settingsSound);
        mainPage.put("visitorsSound", visitorsSound);
        mainPage.put("membersSlot", membersSlot);
        mainPage.put("settingsSlot", settingsSlot);
        mainPage.put("visitorsSlot", visitorsSlot);
    }

    private void initMembersPage(YamlConfiguration cfg){
        membersPage = FileUtil.getGUI(GUIInventory.MEMBERS_PAGE_IDENTIFIER, cfg.getConfigurationSection("members-panel"), 6, "&lIsland Members");

        ItemStack previousButton = FileUtil.getItemStack(cfg.getConfigurationSection("members-panel.previous-page"));
        ItemStack currentButton = FileUtil.getItemStack(cfg.getConfigurationSection("members-panel.current-page"));
        ItemStack nextButton = FileUtil.getItemStack(cfg.getConfigurationSection("members-panel.next-page"));
        ItemStack memberItem = FileUtil.getItemStack(cfg.getConfigurationSection("members-panel.member-item"));
        int previousSlot = cfg.getInt("members-panel.previous-page.slot");
        int currentSlot = cfg.getInt("members-panel.current-page.slot");
        int nextSlot = cfg.getInt("members-panel.next-page.slot");
        Sound previousSound = getSound(cfg.getString("members-panel.previous-page.sound", ""));
        Sound currentSound = getSound(cfg.getString("members-panel.current-page.sound", ""));
        Sound nextSound = getSound(cfg.getString("members-panel.next-page.sound", ""));
        Sound memberSound = getSound(cfg.getString("members-panel.member-item.sound", ""));

        List<Integer> slots = new ArrayList<>();
        Arrays.stream(cfg.getString("members-panel.member-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
        slots.sort(Integer::compareTo);

        membersPage.put("previousButton", previousButton);
        membersPage.put("currentButton", currentButton);
        membersPage.put("nextButton", nextButton);
        membersPage.put("memberItem", memberItem);
        membersPage.put("previousSound", previousSound);
        membersPage.put("currentSound", currentSound);
        membersPage.put("nextSound", nextSound);
        membersPage.put("previousSlot", previousSlot);
        membersPage.put("currentSlot", currentSlot);
        membersPage.put("nextSlot", nextSlot);
        membersPage.put("memberSound", memberSound);
        membersPage.put("slots", slots);
    }

    private void initVisitorsPage(YamlConfiguration cfg){
        visitorsPage = FileUtil.getGUI(GUIInventory.VISITORS_PAGE_IDENTIFIER, cfg.getConfigurationSection("visitors-panel"), 6, "&lIsland Visitors");

        ItemStack previousButton = FileUtil.getItemStack(cfg.getConfigurationSection("visitors-panel.previous-page"));
        ItemStack currentButton = FileUtil.getItemStack(cfg.getConfigurationSection("visitors-panel.current-page"));
        ItemStack nextButton = FileUtil.getItemStack(cfg.getConfigurationSection("visitors-panel.next-page"));
        ItemStack visitorItem = FileUtil.getItemStack(cfg.getConfigurationSection("visitors-panel.visitor-item"));
        int previousSlot = cfg.getInt("visitors-panel.previous-page.slot");
        int currentSlot = cfg.getInt("visitors-panel.current-page.slot");
        int nextSlot = cfg.getInt("visitors-panel.next-page.slot");
        Sound previousSound = getSound(cfg.getString("visitors-panel.previous-page.sound", ""));
        Sound currentSound = getSound(cfg.getString("visitors-panel.current-page.sound", ""));
        Sound nextSound = getSound(cfg.getString("visitors-panel.next-page.sound", ""));
        Sound visitorSound = getSound(cfg.getString("visitors-panel.visitor-item.sound", ""));

        List<Integer> slots = new ArrayList<>();
        Arrays.stream(cfg.getString("visitors-panel.visitor-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
        slots.sort(Integer::compareTo);

        visitorsPage.put("previousButton", previousButton);
        visitorsPage.put("currentButton", currentButton);
        visitorsPage.put("nextButton", nextButton);
        visitorsPage.put("visitorItem", visitorItem);
        visitorsPage.put("previousSound", previousSound);
        visitorsPage.put("currentSound", currentSound);
        visitorsPage.put("nextSound", nextSound);
        visitorsPage.put("previousSlot", previousSlot);
        visitorsPage.put("currentSlot", currentSlot);
        visitorsPage.put("nextSlot", nextSlot);
        visitorsPage.put("visitorSound", visitorSound);
        visitorsPage.put("slots", slots);
    }

    private void initPlayerPage(YamlConfiguration cfg){
        playerPage = FileUtil.getGUI(GUIInventory.PLAYER_PAGE_IDENTIFIER, cfg.getConfigurationSection("players-panel"), 6, "");

        ItemStack rolesButton = FileUtil.getItemStack(cfg.getConfigurationSection("players-panel.roles"));
        ItemStack banButton = FileUtil.getItemStack(cfg.getConfigurationSection("players-panel.ban"));
        ItemStack kickButton = FileUtil.getItemStack(cfg.getConfigurationSection("players-panel.kick"));
        int rolesSlot = cfg.getInt("players-panel.roles.slot");
        int banSlot = cfg.getInt("players-panel.ban.slot");
        int kickSlot = cfg.getInt("players-panel.kick.slot");
        Sound rolesSound = getSound(cfg.getString("players-panel.roles.sound", ""));
        Sound banSound = getSound(cfg.getString("players-panel.ban.sound", ""));
        Sound kickSound = getSound(cfg.getString("players-panel.kick.sound", ""));

        playerPage.setItem(rolesSlot, rolesButton);
        playerPage.setItem(banSlot, banButton);
        playerPage.setItem(kickSlot, kickButton);

        playerPage.put("rolesSound", rolesSound);
        playerPage.put("banSound", banSound);
        playerPage.put("kickSound", kickSound);
        playerPage.put("rolesSlot", rolesSlot);
        playerPage.put("banSlot", banSlot);
        playerPage.put("kickSlot", kickSlot);
    }

    private void initRolePage(YamlConfiguration cfg){
        rolePage = FileUtil.getGUI(GUIInventory.ROLE_PAGE_IDENTIFIER, cfg.getConfigurationSection("roles-panel"), 5, "");

        ItemStack memberButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.member-role"));
        ItemStack modButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.mod-role"));
        ItemStack adminButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.admin-role"));
        ItemStack leaderButton = FileUtil.getItemStack(cfg.getConfigurationSection("roles-panel.leader-role"));
        int memberSlot = cfg.getInt("roles-panel.member-role.slot");
        int modSlot = cfg.getInt("roles-panel.mod-role.slot");
        int adminSlot = cfg.getInt("roles-panel.admin-role.slot");
        int leaderSlot = cfg.getInt("roles-panel.leader-role.slot");
        Sound memberSound = getSound(cfg.getString("roles-panel.member-role.sound", ""));
        Sound modSound = getSound(cfg.getString("roles-panel.mod-role.sound", ""));
        Sound adminSound = getSound(cfg.getString("roles-panel.admin-role.sound", ""));
        Sound leaderSound = getSound(cfg.getString("roles-panel.leader-role.sound", ""));

        rolePage.setItem(memberSlot, memberButton);
        rolePage.setItem(modSlot, modButton);
        rolePage.setItem(adminSlot, adminButton);
        rolePage.setItem(leaderSlot, leaderButton);

        rolePage.put("memberSound", memberSound);
        rolePage.put("modSound", modSound);
        rolePage.put("adminSound", adminSound);
        rolePage.put("leaderSound", leaderSound);
        rolePage.put("memberSlot", memberSlot);
        rolePage.put("modSlot", modSlot);
        rolePage.put("adminSlot", adminSlot);
        rolePage.put("leaderSlot", leaderSlot);
    }

    private void initIslandCreationPage(YamlConfiguration cfg){
        islandCreationPage = FileUtil.getGUI(GUIInventory.ISLAND_CREATION_PAGE_IDENTIFIER, cfg.getConfigurationSection("creation-gui"), 1, "&lCreate a new island...");

        ConfigurationSection section = cfg.getConfigurationSection("creation-gui.schematics");

        for(String schematic : section.getKeys(false)){
            islandCreationPage.put(schematic + "-permission", section.getString(schematic + ".required-permission"));
            islandCreationPage.put(schematic + "-bonus", section.getLong(schematic + ".bonus-worth", 0));
            islandCreationPage.put(schematic + "-biome", section.getString(schematic + ".biome", "PLAINS"));
            islandCreationPage.put(schematic + "-slot", section.getInt(schematic + ".slot"));
            islandCreationPage.put(schematic + "-has-access-item",
                    FileUtil.getItemStack(section.getConfigurationSection(schematic + ".has-access-item")));
            islandCreationPage.put(schematic + "-no-access-item",
                    FileUtil.getItemStack(section.getConfigurationSection(schematic + ".no-access-item")));
        }
    }

}
