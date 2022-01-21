package com.bgsoftware.superiorskyblock.utils.items;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public final class ItemBuilder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private ItemStack itemStack;
    @Nullable
    private ItemMeta itemMeta;
    private String textureValue = "";

    public ItemBuilder(ItemStack itemStack) {
        this(itemStack.getType(), itemStack.getDurability());
        this.itemMeta = itemStack.getItemMeta().clone();
    }

    public ItemBuilder(Material type) {
        this(type, 0);
    }

    public ItemBuilder(Material type, int damage) {
        itemStack = new ItemStack(type, 1, (short) damage);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder withType(Material type) {
        this.itemStack.setType(type);
        return this;
    }

    public ItemBuilder withDurablity(short durability) {
        if (durability >= 0)
            this.itemStack.setDurability(durability);
        return this;
    }

    public ItemBuilder withAmount(int amount) {
        if (amount >= 1 && amount <= itemStack.getMaxStackSize())
            itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder asSkullOf(SuperiorPlayer superiorPlayer) {
        if (itemStack.getType() == Materials.PLAYER_HEAD.toBukkitType())
            textureValue = superiorPlayer == null ? HeadUtils.getNullPlayerTexture() : superiorPlayer.getTextureValue();
        return this;
    }

    public ItemBuilder asSkullOf(String textureValue) {
        if (itemStack.getType() == Materials.PLAYER_HEAD.toBukkitType())
            this.textureValue = textureValue;
        return this;
    }

    public ItemBuilder withName(String name) {
        if (itemMeta != null && name != null)
            itemMeta.setDisplayName(StringUtils.translateColors(name));
        return this;
    }

    public ItemBuilder replaceName(String regex, String replace) {
        if (itemMeta != null && itemMeta.hasDisplayName())
            withName(itemMeta.getDisplayName().replace(regex, replace));
        return this;
    }

    public ItemBuilder withLore(List<String> lore) {
        if (itemMeta != null && lore != null)
            itemMeta.setLore(StringUtils.translateColors(lore));
        return this;
    }

    public ItemBuilder appendLore(List<String> lore) {
        List<String> currentLore = itemMeta == null || itemMeta.getLore() == null ? new ArrayList<>() : itemMeta.getLore();
        currentLore.addAll(lore);
        return withLore(currentLore);
    }

    public ItemBuilder withLore(String... lore) {
        return withLore(Arrays.asList(lore));
    }

    public ItemBuilder withLore(String firstLine, List<String> listLine) {
        if (itemMeta == null)
            return this;

        List<String> loreList = new ArrayList<>();

        firstLine = StringUtils.translateColors(firstLine);
        loreList.add(firstLine);

        for (String line : listLine)
            loreList.add(ChatColor.getLastColors(firstLine) + StringUtils.translateColors(line));

        if (loreList.size() > 10) {
            for (int i = 10; i < loreList.size(); i++) {
                loreList.remove(loreList.get(i));
            }
            loreList.add(ChatColor.getLastColors(firstLine) + "...");
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder withLore(String firstLine, ConfigurationSection configurationSection) {
        if (itemMeta == null)
            return this;

        List<String> loreList = new ArrayList<>();

        firstLine = StringUtils.translateColors(firstLine);
        loreList.add(firstLine);

        for (String section : configurationSection.getKeys(false)) {
            section = section + ": " + configurationSection.get(section).toString();
            loreList.add(ChatColor.getLastColors(firstLine) + StringUtils.translateColors(section));
        }

        if (loreList.size() > 16) {
            loreList = loreList.subList(0, 16);
            loreList.add(ChatColor.getLastColors(firstLine) + "...");
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder replaceLore(String regex, String replace) {
        if (itemMeta == null || !itemMeta.hasLore())
            return this;

        List<String> loreList = new ArrayList<>();

        for (String line : itemMeta.getLore()) {
            loreList.add(line.replace(regex, replace));
        }

        withLore(loreList);
        return this;
    }

    public ItemBuilder replaceLoreWithLines(String regex, String... lines) {
        if (itemMeta == null || !itemMeta.hasLore())
            return this;

        List<String> loreList = new ArrayList<>();
        List<String> linesToAdd = Arrays.asList(lines);
        boolean isEmpty = linesToAdd.isEmpty() || linesToAdd.stream().allMatch(String::isEmpty);

        for (String line : itemMeta.getLore()) {
            if (line.contains(regex)) {
                if (!isEmpty)
                    loreList.addAll(linesToAdd);
            } else {
                loreList.add(line);
            }
        }

        withLore(loreList);
        return this;
    }

    public ItemBuilder replaceAll(String regex, String replace) {
        replaceName(regex, replace);
        replaceLore(regex, replace);
        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchant, int level) {
        if (itemMeta != null)
            itemMeta.addEnchant(enchant, level, true);
        return this;
    }

    public ItemBuilder withFlags(ItemFlag... itemFlags) {
        if (itemMeta != null)
            itemMeta.addItemFlags(itemFlags);
        return this;
    }

    public ItemBuilder setUnbreakable() {
        if (itemMeta != null)
            itemMeta.spigot().setUnbreakable(true);
        return this;
    }

    public ItemBuilder withPotionEffect(PotionEffect potionEffect) {
        if (itemMeta instanceof PotionMeta)
            plugin.getNMSAlgorithms().addPotion((PotionMeta) itemMeta, potionEffect);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder withEntityType(EntityType entityType) {
        if (itemMeta == null)
            return this;

        if (ItemUtils.isValidAndSpawnEgg(itemStack)) {
            if (ServerVersion.isLegacy()) {
                try {
                    ((SpawnEggMeta) itemMeta).setSpawnedType(entityType);
                } catch (NoClassDefFoundError error) {
                    itemStack.setDurability(entityType.getTypeId());
                }
            } else {
                itemStack.setType(Material.valueOf(entityType.name() + "_SPAWN_EGG"));
            }
        }

        return this;
    }

    public ItemBuilder withCustomModel(int customModel) {
        plugin.getNMSAlgorithms().setCustomModel(itemMeta, customModel);
        return this;
    }

    @Nullable
    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    public ItemStack build(SuperiorPlayer superiorPlayer) {
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();

        if (itemMeta != null) {
            if (itemMeta.hasDisplayName()) {
                withName(PlaceholderHook.parse(offlinePlayer, itemMeta.getDisplayName()));
            }

            if (itemMeta.hasLore()) {
                withLore(itemMeta.getLore().stream().map(line -> PlaceholderHook.parse(offlinePlayer, line)).collect(Collectors.toList()));
            }
        }

        if (textureValue.equals("%superior_player_texture%"))
            textureValue = superiorPlayer.getTextureValue();

        return build();
    }

    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return textureValue.isEmpty() ? itemStack : HeadUtils.getPlayerHead(itemStack, textureValue);
    }

    public ItemBuilder copy() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.AIR);
        itemBuilder.itemStack = itemStack.clone();
        if(itemMeta != null)
            itemBuilder.itemMeta = itemMeta.clone();
        itemBuilder.textureValue = textureValue;
        return itemBuilder;
    }

}
