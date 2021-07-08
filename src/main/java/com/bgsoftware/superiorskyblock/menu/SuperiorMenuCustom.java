package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.google.common.collect.Sets;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SuperiorMenuCustom extends SuperiorMenu {

    private static final Set<String> customMenus = Sets.newHashSet();

    private final String fileName;

    private SuperiorMenuCustom(SuperiorPlayer superiorPlayer, String fileName){
        super("menu-" + (fileName = fileName.split("\\.")[0].toLowerCase()), superiorPlayer);
        this.fileName = fileName;
        if(superiorPlayer == null)
            customMenus.add(fileName);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, fileName, previousMenu);
    }

    public static void createMenu(File file){
        String fileName = file.getName();

        SuperiorMenuCustom superiorMenuCustom = new SuperiorMenuCustom(null, fileName);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(cfg.contains("command")){
            ConfigurationSection commandsSection = cfg.getConfigurationSection("command");
            List<String> aliases = Arrays.asList(commandsSection.getString("aliases", "").split(", "));
            String permission = commandsSection.getString("permission", "");
            Map<Locale, String> descriptions = new HashMap<>();
            if(commandsSection.contains("description")){
                for(String locale : commandsSection.getConfigurationSection("description").getKeys(false))
                    descriptions.put(LocaleUtils.getLocale(locale), commandsSection.getString("description." + locale));
            }
            boolean displayCommand = commandsSection.getBoolean("display-command", false);

            plugin.getCommands().registerCommand(new CustomMenuCommand(fileName, aliases, permission, descriptions, displayCommand));
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(superiorMenuCustom, fileName, cfg);

        charSlots.delete();

        superiorMenuCustom.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, String fileName, SuperiorMenu previousMenu){
        new SuperiorMenuCustom(superiorPlayer, fileName).open(previousMenu);
    }

    public static boolean isValidMenu(String menuName){
        return customMenus.contains(menuName.toLowerCase());
    }

    public static Set<String> getCustomMenus(){
        return Collections.unmodifiableSet(customMenus);
    }

    public static void resetMenus(){
        customMenus.clear();
    }

    private static final class CustomMenuCommand implements ISuperiorCommand{

        private final String fileName;
        private final List<String> aliases;
        private final String permission;
        private final Map<Locale, String> descriptions;
        private final boolean displayCommand;

        public CustomMenuCommand(String fileName, List<String> aliases, String permission, Map<Locale, String> descriptions, boolean displayCommand){
            this.fileName = fileName;
            this.aliases = aliases;
            this.permission = permission;
            this.descriptions = descriptions;
            this.displayCommand = displayCommand;
        }

        @Override
        public List<String> getAliases() {
            return aliases;
        }

        @Override
        public String getPermission() {
            return permission;
        }

        @Override
        public String getUsage(Locale locale) {
            return aliases.get(0);
        }

        @Override
        public String getDescription(Locale locale) {
            return descriptions.getOrDefault(locale, "");
        }

        @Override
        public int getMinArgs() {
            return 1;
        }

        @Override
        public int getMaxArgs() {
            return 1;
        }

        @Override
        public boolean canBeExecutedByConsole() {
            return false;
        }

        @Override
        public boolean displayCommand() {
            return displayCommand;
        }

        @Override
        public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
            SuperiorMenuCustom.openInventory(plugin.getPlayers().getSuperiorPlayer(sender), fileName, null);
        }

        @Override
        public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
            return new ArrayList<>();
        }

    }

}
