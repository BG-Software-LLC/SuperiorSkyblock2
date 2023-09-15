package com.bgsoftware.superiorskyblock.core.menu.impl.internal;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.ConfigEditorPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.ConfigEditorSaveButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.PagedMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MenuConfigEditor extends AbstractPagedMenu<MenuConfigEditor.View, MenuConfigEditor.Args, ItemStack> {

    private final File configFile;
    private final CommentedConfiguration config = new CommentedConfiguration();
    private final String[] ignorePaths;

    private MenuConfigEditor(MenuParseResult<View> parseResult, File configFile, String[] ignorePaths) {
        super(MenuIdentifiers.MENU_CONFIG_EDITOR, parseResult, true);
        this.configFile = configFile;
        this.ignorePaths = ignorePaths;
        this.reloadConfig();
    }

    public CommentedConfiguration getConfig() {
        return config;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, MenuConfigEditor.Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void saveConfig(SaveCallback onSave) {
        try {
            config.save(this.configFile);
            onSave.accept(this.config);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while saving config file:");
        }
    }

    public void reloadConfig() {
        try {
            config.load(this.configFile);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while reloading config file:");
        }
    }

    public void updateConfig(Player player, String path, Object value) {
        config.set(path, value);
        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY +
                " Changed value of " + path + " to " + value);
    }

    public static MenuConfigEditor createInstance() {
        PagedMenuLayoutImpl.Builder<MenuConfigEditor.View, ItemStack> patternBuilder = PagedMenuLayoutImpl.newBuilder();

        patternBuilder.setTitle(ChatColor.BOLD + "Settings Editor");
        patternBuilder.setInventoryType(InventoryType.CHEST);
        patternBuilder.setRowsCount(6);

        patternBuilder.setButton(47, new DummyButton.Builder<View>()
                .setButtonItem(new TemplateItem(new ItemBuilder(Material.PAPER).withName("{0}Previous Page")))
                .build());
        patternBuilder.setPreviousPageSlots(Collections.singletonList(47));

        patternBuilder.setButton(49, new DummyButton.Builder<View>()
                .setButtonItem(new TemplateItem(new ItemBuilder(Materials.SUNFLOWER.toBukkitType())
                        .withName("&aCurrent Page").withLore("&7Page {0}")))
                .build());
        patternBuilder.setCurrentPageSlots(Collections.singletonList(49));

        patternBuilder.setButton(51, new DummyButton.Builder<View>()
                .setButtonItem(new TemplateItem(new ItemBuilder(Material.PAPER).withName("{0}Next Page")))
                .build());
        patternBuilder.setNextPageSlots(Collections.singletonList(51));

        patternBuilder.setPagedObjectSlots(IntStream.range(0, 36).boxed().collect(Collectors.toList()),
                new ConfigEditorPagedObjectButton.Builder());

        patternBuilder.setButtons(IntStream.range(36, 45).boxed().collect(Collectors.toList()),
                new DummyButton.Builder<View>().setButtonItem(new TemplateItem(
                                new ItemBuilder(Materials.BLACK_STAINED_GLASS_PANE.toBukkitItem()).withName(" ")))
                        .build());

        patternBuilder.setButton(40, new ConfigEditorSaveButton.Builder().build());

        return new MenuConfigEditor(new MenuParseResult<>(patternBuilder),
                new File(plugin.getDataFolder(), "config.yml"),
                new String[]{"database", "max-island-size", "island-roles", "worlds.normal-world", "commands-cooldown", "starter-chest", "event-commands"});
    }

    public interface SaveCallback {

        void accept(CommentedConfiguration config) throws Exception;

    }

    public static class Args implements ViewArgs {

        public static final Args ROOT = new Args("");

        private final String path;

        public Args(String path) {
            this.path = path;
        }

    }

    public class View extends AbstractPagedMenuView<View, Args, ItemStack> {

        private final List<String> pathSlots = new LinkedList<>();
        private final String path;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.path = args.path;
        }

        public List<String> getPathSlots() {
            return pathSlots;
        }

        public String getPath() {
            return path;
        }

        @Override
        public String replaceTitle(String title) {
            return path.isEmpty() ? title : ChatColor.BOLD + "Section: " + path;
        }

        @Override
        protected List<ItemStack> requestObjects() {
            LinkedList<ItemStack> itemStacks = new LinkedList<>();
            buildFromSection(itemStacks, config.getConfigurationSection(this.path));
            return Collections.unmodifiableList(itemStacks);
        }

        private void buildFromSection(List<ItemStack> itemStacks, ConfigurationSection section) {
            pathSlots.clear();

            for (String path : section.getKeys(false)) {
                String fullPath = section.getCurrentPath().isEmpty() ? path : section.getCurrentPath() + "." + path;

                if (Arrays.stream(ignorePaths).anyMatch(fullPath::contains))
                    continue;

                ItemBuilder itemBuilder = new ItemBuilder(Materials.CLOCK.toBukkitItem()).withName("&6" +
                        Formatters.CAPITALIZED_FORMATTER.format(path.replace("-", "_")
                                .replace(".", "_").replace(" ", "_"))
                );

                if (section.isBoolean(path))
                    itemBuilder.withLore("&7Value: " + section.getBoolean(path));
                else if (section.isInt(path))
                    itemBuilder.withLore("&7Value: " + section.getInt(path));
                else if (section.isDouble(path))
                    itemBuilder.withLore("&7Value: " + section.getDouble(path));
                else if (section.isString(path))
                    itemBuilder.withLore("&7Value: " + section.getString(path));
                else if (section.isList(path))
                    itemBuilder.withLore("&7Value:", section.getStringList(path));
                else if (section.isConfigurationSection(path))
                    itemBuilder.withLore("&7Click to edit section.");

                pathSlots.add(path);
                itemStacks.add(itemBuilder.build());
            }
        }

    }

}
