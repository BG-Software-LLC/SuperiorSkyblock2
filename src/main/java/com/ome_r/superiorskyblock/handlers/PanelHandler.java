package com.ome_r.superiorskyblock.handlers;

import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.gui.GUIInventory;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.utils.FileUtil;
import com.ome_r.superiorskyblock.utils.HeadUtil;
import com.ome_r.superiorskyblock.utils.ItemBuilder;
import com.ome_r.superiorskyblock.utils.ItemUtil;
import com.ome_r.superiorskyblock.utils.StringUtil;
import com.ome_r.superiorskyblock.utils.key.Key;
import com.ome_r.superiorskyblock.utils.key.KeyMap;
import com.ome_r.superiorskyblock.utils.legacy.Materials;
import com.ome_r.superiorskyblock.wrappers.WrappedLocation;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
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
public class PanelHandler {

    private SuperiorSkyblock plugin;
    public GUIInventory mainPage, membersPage, visitorsPage, playerPage, rolePage,
            islandCreationPage, biomesPage, warpsPage, valuesPage;

    Map<UUID, PanelType> openedPanel = new HashMap<>();
    public Map<UUID, UUID> islands = new HashMap<>();

    public PanelHandler(SuperiorSkyblock plugin){
        this.plugin = plugin;
        loadMenus(plugin);
    }

    private void loadMenus(SuperiorSkyblock plugin) {
        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            plugin.saveResource("guis/panel-gui.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        initMainPage(cfg);
        initMembersPage(cfg);
        initVisitorsPage(cfg);
        initPlayerPage(cfg);
        initRolePage(cfg);

        file = new File(plugin.getDataFolder(), "guis/creation-gui.yml");

        if(!file.exists())
            plugin.saveResource("guis/creation-gui.yml", false);

        cfg = YamlConfiguration.loadConfiguration(file);

        initIslandCreationPage(cfg);

        file = new File(plugin.getDataFolder(), "guis/biomes-gui.yml");

        if(!file.exists())
            plugin.saveResource("guis/biomes-gui.yml", false);

        cfg = YamlConfiguration.loadConfiguration(file);

        initBiomesPage(cfg);

        file = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if(!file.exists())
            plugin.saveResource("guis/warps-gui.yml", false);

        cfg = YamlConfiguration.loadConfiguration(file);

        initWarpsPage(cfg);

        file = new File(plugin.getDataFolder(), "guis/values-gui.yml");

        if(!file.exists())
            plugin.saveResource("guis/values-gui.yml", false);

        cfg = YamlConfiguration.loadConfiguration(file);

        initValuesPage(cfg);
    }

    private Sound getSound(String name){
        try{
            return Sound.valueOf(name);
        }catch(Exception ex){
            return null;
        }
    }

    public void openPanel(WrappedPlayer wrappedPlayer){
        openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.GENERAL);
        mainPage.openInventory(wrappedPlayer);
    }

    public void openMembersPanel(WrappedPlayer wrappedPlayer, int page){
        if(Bukkit.isPrimaryThread()){
            new Thread(() -> openMembersPanel(wrappedPlayer, page)).start();
            return;
        }

        Inventory inventory = membersPage.getInventory();
        List<UUID> members = new ArrayList<>();

        if(wrappedPlayer.getIsland() != null)
            members.addAll(wrappedPlayer.getIsland().getAllMembers());

        members.sort(Comparator.comparing(o -> WrappedPlayer.of(o).getName()));

        //noinspection unchecked
        List<Integer> slots = membersPage.get("slots", List.class);

        ItemStack memberItem = membersPage.get("memberItem", ItemStack.class);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < members.size(); i++){
            WrappedPlayer _wrappedPlayer = WrappedPlayer.of(members.get(i + (slots.size() * (page - 1))));
            inventory.setItem(slots.get(i), new ItemBuilder(memberItem)
                    .replaceAll("{0}", _wrappedPlayer.getName())
                    .asSkullOf(_wrappedPlayer).build());
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

        openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.MEMBERS);
        openInventory(wrappedPlayer, inventory);
    }

    public void openVisitorsPanel(WrappedPlayer wrappedPlayer, int page){
        if(Bukkit.isPrimaryThread()){
            new Thread(() -> openVisitorsPanel(wrappedPlayer, page)).start();
            return;
        }

        Inventory inventory = visitorsPage.getInventory();
        List<UUID> visitors = new ArrayList<>();

        if(wrappedPlayer.getIsland() != null)
            visitors.addAll(wrappedPlayer.getIsland().getVisitors());

        visitors.sort(Comparator.comparing(o -> WrappedPlayer.of(o).getName()));

        //noinspection unchecked
        List<Integer> slots = visitorsPage.get("slots", List.class);

        ItemStack visitorItem = visitorsPage.get("visitorItem", ItemStack.class);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < visitors.size(); i++){
            WrappedPlayer _wrappedPlayer = WrappedPlayer.of(visitors.get(i + (slots.size() * (page - 1))));
            String islandOwner = "None";
            if(_wrappedPlayer.getIsland() != null)
                islandOwner = _wrappedPlayer.getIsland().getOwner().getName();
            inventory.setItem(slots.get(i), new ItemBuilder(visitorItem)
                    .replaceAll("{0}", _wrappedPlayer.getName())
                    .replaceAll("{1}", islandOwner)
                    .asSkullOf(_wrappedPlayer).build());
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

        openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.VISITORS);
        openInventory(wrappedPlayer, inventory);
    }

    public void openPlayerPanel(WrappedPlayer wrappedPlayer, WrappedPlayer targetPlayer){
        Inventory inventory = Bukkit.createInventory(null, playerPage.getSize(), ChatColor.BOLD + targetPlayer.getName());
        inventory.setContents(playerPage.getContents());
        openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.PLAYER);
        openInventory(wrappedPlayer, inventory);
    }

    public void openRolePanel(WrappedPlayer wrappedPlayer, WrappedPlayer targetPlayer){
        Inventory inventory = Bukkit.createInventory(null, rolePage.getSize(), ChatColor.BOLD + targetPlayer.getName());
        inventory.setContents(rolePage.getContents());
        openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.ROLE);
        openInventory(wrappedPlayer, inventory);
    }

    public void openIslandCreationPanel(WrappedPlayer wrappedPlayer){
        if(Bukkit.isPrimaryThread()){
            new Thread(() -> openIslandCreationPanel(wrappedPlayer)).start();
            return;
        }

        Inventory inventory = islandCreationPage.getInventory();

        for(String schematic : plugin.getSchematics().getSchematics()){
            if(islandCreationPage.contains(schematic + "-has-access-item")) {
                ItemStack schematicItem = islandCreationPage.get(schematic + "-has-access-item", ItemStack.class);
                String permission = islandCreationPage.get(schematic + "-permission", String.class);
                int slot = islandCreationPage.get(schematic + "-slot", Integer.class);

                if(!wrappedPlayer.hasPermission(permission))
                    schematicItem = islandCreationPage.get(schematic + "-no-access-item", ItemStack.class);

                inventory.setItem(slot, schematicItem);
            }
        }

        islandCreationPage.playOpenSound(wrappedPlayer);

        openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.SCHEMATICS);
        openInventory(wrappedPlayer, inventory);
    }

    public void openBiomesPanel(WrappedPlayer wrappedPlayer){
        if(Bukkit.isPrimaryThread()){
            new Thread(() -> openBiomesPanel(wrappedPlayer)).start();
            return;
        }

        Inventory inventory = biomesPage.getInventory();

        for(Biome biome : Biome.values()){
            String biomeName = biome.name().toLowerCase();
            if(biomesPage.contains(biomeName + "-has-access-item")) {
                ItemStack biomeItem = biomesPage.get(biomeName + "-has-access-item", ItemStack.class);
                String permission = biomesPage.get(biomeName + "-permission", String.class);
                int slot = biomesPage.get(biomeName + "-slot", Integer.class);

                if(!wrappedPlayer.hasPermission(permission))
                    biomeItem = biomesPage.get(biomeName + "-no-access-item", ItemStack.class);

                inventory.setItem(slot, biomeItem);
            }
        }

        biomesPage.playOpenSound(wrappedPlayer);

        openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.BIOMES);
        openInventory(wrappedPlayer, inventory);
    }

    public void openWarpsPanel(WrappedPlayer wrappedPlayer, int page) {
        openWarpsPanel(wrappedPlayer, getIsland(wrappedPlayer), page);
    }

    public Island getIsland(WrappedPlayer wrappedPlayer){
        return plugin.getGrid().getIsland(WrappedPlayer.of(islands.get(wrappedPlayer.getUniqueId())));
    }

    public void openWarpsPanel(WrappedPlayer wrappedPlayer, Island island, int page) {
        if (Bukkit.isPrimaryThread()) {
            new Thread(() -> openWarpsPanel(wrappedPlayer, island, page)).start();
            return;
        }

        Inventory inventory = warpsPage.getInventory();
        List<String> warps = new ArrayList<>(island.getAllWarps());

        warps.sort(String::compareTo);

        //noinspection unchecked
        List<Integer> slots = warpsPage.get("slots", List.class);

        ItemStack warpItem = warpsPage.get("warpItem", ItemStack.class);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < warps.size(); i++){
            String warpName = warps.get(i + (slots.size() * (page - 1)));
            inventory.setItem(slots.get(i), new ItemBuilder(warpItem)
                    .replaceAll("{0}", warpName)
                    .replaceAll("{1}", WrappedLocation.of(island.getWarpLocation(warpName)).toString()).build());
        }

        int previousSlot = warpsPage.get("previousSlot", Integer.class);
        ItemStack previousButton = warpsPage.get("previousButton", ItemStack.class);
        inventory.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        int currentSlot = warpsPage.get("currentSlot", Integer.class);
        ItemStack currentButton = warpsPage.get("currentButton", ItemStack.class);
        inventory.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        int nextSlot = warpsPage.get("nextSlot", Integer.class);
        ItemStack nextButton = warpsPage.get("nextButton", ItemStack.class);
        inventory.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (warps.size() > page * slots.size() ? "&a" : "&c")).build());

        openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.WARPS);
        islands.put(wrappedPlayer.getUniqueId(), island.getOwner().getUniqueId());
        openInventory(wrappedPlayer, inventory);
    }

    public void openValuesPanel(WrappedPlayer wrappedPlayer, Island island){
        Inventory valuesPageInventory = valuesPage.getInventory();
        Inventory inventory = Bukkit.createInventory(null, valuesPageInventory.getSize(),
                valuesPageInventory.getTitle().replace("{0}", island.getOwner().getName())
                        .replace("{1}", String.valueOf(island.getWorth())));
        inventory.setContents(valuesPageInventory.getContents());

        new Thread(() -> {
            //noinspection unchecked
            KeyMap<Integer> countedBlocks = (KeyMap<Integer>) valuesPage.get("countedBlocks", KeyMap.class);

            for(Key key : countedBlocks.keySet()){
                String[] sections = key.toString().split(":");
                ItemStack itemStack = new ItemStack(Material.valueOf(sections[0]));
                int slot = countedBlocks.get(key);

                String typeName = StringUtil.format(sections[0]);
                int amount = island.getBlockCount(Key.of(itemStack));

                if(sections.length == 2) {
                    if(itemStack.getType() == Materials.SPAWNER.toBukkitType()) {
                        EntityType entityType = EntityType.valueOf(sections[1]);
                        amount = island.getBlockCount(Key.of(Materials.SPAWNER.toBukkitType() + ":" + entityType));
                        itemStack = HeadUtil.getEntityHead(entityType);
                        typeName = StringUtil.format(sections[1]) + " Spawner";
                    }
                    else {
                        itemStack.setDurability(Short.valueOf(sections[1]));
                        amount = island.getBlockCount(Key.of(itemStack));
                    }
                }

                String blockName = valuesPage.get("blockName", String.class);
                //noinspection unchecked
                List<String> blockLore = (List<String>) valuesPage.get("blockLore", List.class);

                itemStack = new ItemBuilder(itemStack).withName(blockName).withLore(blockLore)
                        .replaceAll("{0}", typeName).replaceAll("{1}", String.valueOf(amount)).build();

                if(amount == 0)
                    amount = 1;
                else if(amount > 64)
                    amount = 64;

                itemStack.setAmount(amount);

                inventory.setItem(slot, itemStack);
            }

            openedPanel.put(wrappedPlayer.getUniqueId(), PanelType.VALUES);

            openInventory(wrappedPlayer, inventory);
        }).start();
    }

    public PanelType getOpenedPanelType(WrappedPlayer wrappedPlayer){
        return openedPanel.getOrDefault(wrappedPlayer.getUniqueId(), PanelType.NONE);
    }

    public void closeInventory(WrappedPlayer wrappedPlayer){
        openedPanel.remove(wrappedPlayer.getUniqueId());
        islands.remove(wrappedPlayer.getUniqueId());
    }

    private void initMainPage(YamlConfiguration cfg){
        mainPage = FileUtil.getGUI(cfg.getConfigurationSection("main-panel"), 5, "&lIsland Panel");

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
        membersPage = FileUtil.getGUI(cfg.getConfigurationSection("members-panel"), 6, "&lIsland Members");

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
        visitorsPage = FileUtil.getGUI(cfg.getConfigurationSection("visitors-panel"), 6, "&lIsland Visitors");

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
        playerPage = FileUtil.getGUI(cfg.getConfigurationSection("players-panel"), 6, "");

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
        rolePage = FileUtil.getGUI(cfg.getConfigurationSection("roles-panel"), 5, "");

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
        islandCreationPage = FileUtil.getGUI(cfg.getConfigurationSection("creation-gui"), 1, "&lCreate a new island...");

        ConfigurationSection section = cfg.getConfigurationSection("creation-gui.schematics");

        for(String schematic : section.getKeys(false)){
            islandCreationPage.put(schematic + "-permission", section.getString(schematic + ".required-permission"));
            islandCreationPage.put(schematic + "-slot", section.getInt(schematic + ".slot"));
            islandCreationPage.put(schematic + "-has-access-item",
                    FileUtil.getItemStack(section.getConfigurationSection(schematic + ".has-access-item")));
            islandCreationPage.put(schematic + "-no-access-item",
                    FileUtil.getItemStack(section.getConfigurationSection(schematic + ".no-access-item")));
        }
    }

    private void initBiomesPage(YamlConfiguration cfg){
        biomesPage = FileUtil.getGUI(cfg.getConfigurationSection("biomes-gui"), 1, "&lSelect a biome");

        ConfigurationSection section = cfg.getConfigurationSection("biomes-gui.biomes");

        for(String biome : section.getKeys(false)){
            biome = biome.toLowerCase();
            biomesPage.put(biome + "-permission", section.getString(biome + ".required-permission"));
            biomesPage.put(biome + "-slot", section.getInt(biome + ".slot"));
            biomesPage.put(biome + "-has-access-item",
                    FileUtil.getItemStack(section.getConfigurationSection(biome + ".has-access-item")));
            biomesPage.put(biome + "-no-access-item",
                    FileUtil.getItemStack(section.getConfigurationSection(biome + ".no-access-item")));
        }
    }

    private void initWarpsPage(YamlConfiguration cfg){
        warpsPage = FileUtil.getGUI(cfg.getConfigurationSection("warps-gui"), 6, "&lIsland Warps");

        ItemStack previousButton = FileUtil.getItemStack(cfg.getConfigurationSection("warps-gui.previous-page"));
        ItemStack currentButton = FileUtil.getItemStack(cfg.getConfigurationSection("warps-gui.current-page"));
        ItemStack nextButton = FileUtil.getItemStack(cfg.getConfigurationSection("warps-gui.next-page"));
        ItemStack warpItem = FileUtil.getItemStack(cfg.getConfigurationSection("warps-gui.warp-item"));
        int previousSlot = cfg.getInt("warps-gui.previous-page.slot");
        int currentSlot = cfg.getInt("warps-gui.current-page.slot");
        int nextSlot = cfg.getInt("warps-gui.next-page.slot");
        Sound previousSound = getSound(cfg.getString("warps-gui.previous-page.sound", ""));
        Sound currentSound = getSound(cfg.getString("warps-gui.current-page.sound", ""));
        Sound nextSound = getSound(cfg.getString("warps-gui.next-page.sound", ""));
        Sound warpSound = getSound(cfg.getString("warps-gui.warp-item.sound", ""));

        List<Integer> slots = new ArrayList<>();
        Arrays.stream(cfg.getString("warps-gui.warp-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
        slots.sort(Integer::compareTo);

        warpsPage.put("previousButton", previousButton);
        warpsPage.put("currentButton", currentButton);
        warpsPage.put("nextButton", nextButton);
        warpsPage.put("warpItem", warpItem);
        warpsPage.put("previousSound", previousSound);
        warpsPage.put("currentSound", currentSound);
        warpsPage.put("nextSound", nextSound);
        warpsPage.put("previousSlot", previousSlot);
        warpsPage.put("currentSlot", currentSlot);
        warpsPage.put("nextSlot", nextSlot);
        warpsPage.put("warpSound", warpSound);
        warpsPage.put("slots", slots);
    }

    private void initValuesPage(YamlConfiguration cfg){
        valuesPage = FileUtil.getGUI(cfg.getConfigurationSection("values-gui"), 6, "&lIsland Warps");

        Sound blockSound = getSound(cfg.getString("values-gui.block-item.sound", ""));
        String blockName = cfg.getString("values-gui.block-item.name", "&e&l[!] &7{0}");
        List<String> blockLore = cfg.getStringList("values-gui.block-item.lore");

        KeyMap<Integer> countedBlocks = new KeyMap<>();

        for(String materialName : cfg.getStringList("values-gui.materials")){
            String[] sections = materialName.split(":");
            if(sections.length == 2){
                countedBlocks.put(sections[0], Integer.valueOf(sections[1]));
            }else{
                countedBlocks.put(sections[0] + ":" + sections[1], Integer.valueOf(sections[2]));
            }
        }

        valuesPage.put("blockSound", blockSound);
        valuesPage.put("blockName", blockName);
        valuesPage.put("blockLore", blockLore);
        valuesPage.put("countedBlocks", countedBlocks);
    }

    private void openInventory(WrappedPlayer wrappedPlayer, Inventory inventory){
        if(!Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(plugin, () -> openInventory(wrappedPlayer, inventory));
            return;
        }
        wrappedPlayer.asPlayer().openInventory(inventory);
    }

    public enum PanelType{
        NONE, GENERAL, MEMBERS, VISITORS, PLAYER, ROLE, SCHEMATICS, BIOMES, WARPS, VALUES, TOP
    }

}
