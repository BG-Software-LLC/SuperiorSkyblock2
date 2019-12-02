package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.utils.reflections.Fields;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class SuperiorMenu implements InventoryHolder {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<String, MenuData> dataMap = new HashMap<>();

    private final String identifier;
    protected final SuperiorPlayer superiorPlayer;

    protected SuperiorMenu previousMenu;
    protected boolean previousMove = true;
    private boolean refreshing = false;

    public SuperiorMenu(String identifier, SuperiorPlayer superiorPlayer){
        this.identifier = identifier;
        this.superiorPlayer = superiorPlayer;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void addSound(int slot, SoundWrapper sound) {
        if(sound != null)
            getData().sounds.put(slot, sound);
    }

    public void addCommands(int slot, List<String> commands) {
        if(commands != null && !commands.isEmpty())
            getData().commands.put(slot, commands);
    }

    public void addFillItem(int slot, ItemStack itemStack){
        if(itemStack != null)
            getData().fillItems.put(slot, itemStack);
    }

    public void resetData(){
        dataMap.put(identifier, new MenuData());
    }

    public void setTitle(String title){
        getData().title = title;
    }

    public void setRowsSize(int rowsSize){
        getData().rowsSize = rowsSize;
    }

    public void setInventoryType(InventoryType inventoryType){
        getData().inventoryType = inventoryType;
    }

    public void addData(String key, Object value){
        getData().data.put(key, value);
    }

    public Object getData(String key){
        return getData().data.get(key);
    }

    public boolean containsData(String key){
        return getData().data.containsKey(key);
    }

    @Override
    public Inventory getInventory(){
        return buildInventory(null);
    }

    public final void onClick(InventoryClickEvent e){
        if(refreshing)
            return;

        Player player = superiorPlayer.asPlayer();

        if(e.getCurrentItem() != null) {
            SoundWrapper sound = getSound(e.getRawSlot());
            if (sound != null)
                sound.playSound(player);

            List<String> commands = getCommands(e.getRawSlot());
            if (commands != null)
                commands.forEach(command ->
                        Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? player : Bukkit.getConsoleSender(),
                                command.replace("PLAYER:", "").replace("%player%", player.getName())));
        }

        onPlayerClick(e);
    }

    protected abstract void onPlayerClick(InventoryClickEvent e);

    public void open(SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(previousMenu));
            return;
        }

        Inventory inventory = getInventory();

        Executor.sync(() -> {
            Player player = superiorPlayer.asPlayer();

            if(player == null)
                return;

            SuperiorMenu currentMenu = null;
            InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();
            if(inventoryHolder instanceof SuperiorMenu)
                currentMenu = (SuperiorMenu) inventoryHolder;

            if(Arrays.equals( player.getOpenInventory().getTopInventory().getContents(), inventory.getContents()))
                return;

            player.openInventory(inventory);

            refreshing = false;

            this.previousMenu = previousMenu != null ? previousMenu : previousMove ? currentMenu : null;
        });
    }

    public void closeInventory(SuperiorPlayer superiorPlayer){
        if(previousMenu != null) {
            Executor.sync(() -> {
                if(previousMove)
                    previousMenu.open(previousMenu.previousMenu);
                else
                    previousMove = true;
            });
        }
    }

    protected Inventory buildInventory(Function<String, String> titleReplacer){
        MenuData menuData = getData();
        Inventory inventory;

        String title = PlaceholderHook.parse(superiorPlayer, menuData.title);
        if(titleReplacer != null)
            title = titleReplacer.apply(title);

        if(menuData.inventoryType != InventoryType.CHEST){
            inventory = Bukkit.createInventory(this, menuData.inventoryType, title);
        }

        else{
            inventory = Bukkit.createInventory(this, menuData.rowsSize * 9, title);
        }

        if(inventory.getHolder() == null)
            Fields.CRAFT_INVENTORY_INVENTORY.set(inventory, plugin.getNMSAdapter().getCustomHolder(menuData.inventoryType,this, title));

        for(Map.Entry<Integer, ItemStack> itemStackEntry : menuData.fillItems.entrySet())
            inventory.setItem(itemStackEntry.getKey(), new ItemBuilder(itemStackEntry.getValue()).build(superiorPlayer));

        return inventory;
    }

    private SoundWrapper getSound(int slot){
        return getData().sounds.get(slot);
    }

    private List<String> getCommands(int slot){
        return getData().commands.get(slot);
    }

    private MenuData getData(){
        if(!dataMap.containsKey(identifier)){
            dataMap.put(identifier, new MenuData());
        }

        return dataMap.get(identifier);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuperiorMenu && ((SuperiorMenu) obj).getIdentifier().equals(getIdentifier());
    }

    protected static <T extends SuperiorMenu> void refreshMenus(Class<T> menuClazz){
        runActionOnMenus(menuClazz, superiorMenu -> true, ((player, superiorMenu) -> {
            superiorMenu.previousMove = false;
            superiorMenu.open(superiorMenu.previousMenu);
        }));
    }

    protected static <T extends SuperiorMenu> void destroyMenus(Class<T> menuClazz){
        destroyMenus(menuClazz, superiorMenu -> true);
    }

    protected static <T extends SuperiorMenu> void destroyMenus(Class<T> menuClazz, Predicate<T> predicate){
        runActionOnMenus(menuClazz, predicate, ((player, superiorMenu) -> player.closeInventory()));
    }

    private static <T extends SuperiorMenu> void runActionOnMenus(Class<T> menuClazz, Predicate<T> predicate, MenuCallback callback){
        for(Player player : Bukkit.getOnlinePlayers()){
            InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();
            //noinspection unchecked
            if(menuClazz.isInstance(inventoryHolder) && predicate.test((T) inventoryHolder)){
                SuperiorMenu superiorMenu = (SuperiorMenu) inventoryHolder;
                callback.run(player, superiorMenu);
            }
        }
    }

    private interface MenuCallback{

        void run(Player player, SuperiorMenu superiorMenu);

    }

    protected static class MenuData{

        private Map<Integer, SoundWrapper> sounds = new HashMap<>();
        private Map<Integer, List<String>> commands = new HashMap<>();
        private Map<Integer, ItemStack> fillItems = new HashMap<>();
        private Map<String, Object> data = new HashMap<>();
        private String title = "";
        private InventoryType inventoryType = InventoryType.CHEST;
        private int rowsSize = 6;

    }

}
