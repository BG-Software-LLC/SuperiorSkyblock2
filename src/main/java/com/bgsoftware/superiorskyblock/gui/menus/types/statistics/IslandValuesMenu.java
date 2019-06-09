package com.bgsoftware.superiorskyblock.gui.menus.types.statistics;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.gui.menus.YamlScroll;
import com.bgsoftware.superiorskyblock.utils.HeadUtil;
import com.bgsoftware.superiorskyblock.utils.ItemSerializer;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IslandValuesMenu extends YamlScroll {

    private Island island;

    private List<String> materials;
    private ItemStack template;

    public IslandValuesMenu(Player player, Island island) {
        super(player, MenuTemplate.ISLAND_VALUES.getFile());
        create(title.replace("{0}", island.getOwner().getName()).replace("{1}", island.getWorthAsBigDecimal().toString()), rows);

        this.island = island;
        materials = file.getStringList("list");
        template = ItemSerializer.getItem("STONE", file.getConfigurationSection("template"));

        setList(createButtons());

        setPage(0);
        open();
    }

    private List<Button> createButtons() {
        List<Button> buttons = new ArrayList<>();

        for (String material : materials) {
            buttons.add(new Button(getItem(material), (clicker, type) -> {}));
        }

        return buttons;
    }

    private ItemStack getItem(String serialized) {
        ItemStack item;

        String[] split = serialized.split(":");

        Material material;
        try {
            material = Material.valueOf(split[0]);
        } catch (Exception e) {
            material = Material.STONE;
        }

        int data = 0;
        if (!material.toString().contains("SPAWNER"))
            try {
                data = Integer.valueOf(split[1]);
            } catch (Exception ignored) {}
        else {
            if (split.length > 1) {
                item = HeadUtil.getEntityHead(EntityType.valueOf(split[1]));
                applyTemplate(item);
                item = ItemSerializer.replace(item, StringUtil.format(split[1]) + " Spawner", island.getBlockCount(SKey.of(serialized)) + "");
                return item;
            }
        }

        item = new ItemStack(material, 1, (short) data);
        applyTemplate(item);
        item = ItemSerializer.replace(item, StringUtil.format(material.toString()), island.getBlockCount(SKey.of(item)) + "");

        return item;
    }

    private ItemStack applyTemplate(ItemStack item) {
        ItemMeta template = this.template.getItemMeta();
        ItemMeta meta = item.getItemMeta();

        if (template.hasDisplayName())
            meta.setDisplayName(template.getDisplayName());

        if (template.hasLore())
            meta.setLore(template.getLore());

        item.setItemMeta(meta);
        return item;
    }

}
