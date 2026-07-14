package com.bgsoftware.superiorskyblock.core.itemstack;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ItemBuilder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    private ItemStack itemStack;
    @Nullable
    private ItemMeta itemMeta;
    @Nullable
    private String displayName;
    @Nullable
    private List<String> lore;
    private String textureValue = "";

    public ItemBuilder(ItemStack itemStack) {
        this(itemStack.getType(), itemStack.getDurability());
        this.itemMeta = itemStack.getItemMeta() == null ? null : itemStack.getItemMeta().clone();

        if (itemMeta != null) {
            displayName = itemMeta.getDisplayName();
            lore = itemMeta.hasLore() ? new ArrayList<>(itemMeta.getLore()) : null;
        }
    }

    public ItemBuilder(Material type) {
        this(type, 0);
    }

    public ItemBuilder(Material type, int damage) {
        itemStack = new ItemStack(type, 1, (short) damage);
        itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            displayName = itemMeta.getDisplayName();
            lore = itemMeta.getLore();
        }
    }

    public ItemBuilder withType(Material type) {
        this.itemStack.setType(type);

        return this;
    }

    public ItemBuilder withDurablity(short durability) {
        if (durability >= 0) {
            this.itemStack.setDurability(durability);
        }

        return this;
    }

    public ItemBuilder withAmount(int amount) {
        if (amount >= 1 && amount <= this.itemStack.getMaxStackSize()) {
            this.itemStack.setAmount(amount);
        }

        return this;
    }

    public ItemBuilder asSkullOf(SuperiorPlayer superiorPlayer) {
        if (this.itemStack.getType() == Materials.PLAYER_HEAD.toBukkitType()) {
            this.textureValue = superiorPlayer == null ? ItemSkulls.getNullPlayerTexture() : superiorPlayer.getTextureValue();
        }

        return this;
    }

    public ItemBuilder asSkullOf(String textureValue) {
        if (this.itemStack.getType() == Materials.PLAYER_HEAD.toBukkitType()) {
            this.textureValue = ItemSkulls.parseTexture(textureValue);
        }

        return this;
    }

    public ItemBuilder withName(String name) {
        if (this.itemMeta != null && name != null) {
            this.displayName = Formatters.COLOR_FORMATTER.format(name);
        }

        return this;
    }

    public ItemBuilder replaceName(String regex, String replace) {
        if (this.itemMeta != null && this.displayName != null) {
            withName(this.displayName.replace(regex, replace));
        }

        return this;
    }

    public ItemBuilder withLore(List<String> lore) {
        if (this.itemMeta != null && lore != null && !lore.isEmpty()) {
            this.lore = new SequentialListBuilder<String>().build(lore, Formatters.COLOR_FORMATTER::format);
        }

        return this;
    }

    public ItemBuilder appendLore(List<String> lore) {
        if (this.itemMeta != null && lore != null && !lore.isEmpty()) {
            this.lore.addAll(lore);
        }

        return this;
    }

    public ItemBuilder withLore(String... lore) {
        return withLore(Arrays.asList(lore));
    }

    public ItemBuilder withLore(String firstLine, List<String> listLine) {
        if (itemMeta == null)
            return this;

        List<String> loreList = new LinkedList<>();

        firstLine = Formatters.COLOR_FORMATTER.format(firstLine);
        loreList.add(firstLine);

        for (String line : listLine)
            loreList.add(ChatColor.getLastColors(firstLine) + Formatters.COLOR_FORMATTER.format(line));

        if (loreList.size() > 10) {
            for (int i = 10; i < loreList.size(); i++) {
                loreList.remove(loreList.get(i));
            }
            loreList.add(ChatColor.getLastColors(firstLine) + "...");
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder replaceLore(String regex, String replace) {
        if (itemMeta == null || lore == null || lore.isEmpty()) {
            return this;
        }

        List<String> loreList = new ArrayList<>(this.lore.size());

        for (String line : this.lore) {
            loreList.add(line.replace(regex, replace));
        }

        withLore(loreList);
        return this;
    }

    public ItemBuilder replaceLoreWithLines(String regex, String... lines) {
        return replaceLoreWithLines(regex, Arrays.asList(lines));
    }

    public ItemBuilder replaceLoreWithLines(String regex, List<String> lines) {
        if (itemMeta == null || lore == null || lore.isEmpty()) {
            return this;
        }

        List<String> currentLore = this.lore;

        List<String> loreList = new LinkedList<>();
        boolean isEmpty = lines.isEmpty() || lines.stream().allMatch(String::isEmpty);

        for (String line : currentLore) {
            if (line.contains(regex)) {
                if (!isEmpty)
                    loreList.addAll(lines);
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

    public ItemBuilder makeItemGlow() {
        plugin.getNMSAlgorithms().makeItemGlow(itemMeta);
        return this;
    }

    public ItemBuilder withFlags(ItemFlag... itemFlags) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(itemFlags);
            for (ItemFlag itemFlag : itemFlags) {
                if (itemFlag == ItemFlag.HIDE_ATTRIBUTES) {
                    plugin.getNMSAlgorithms().hideAttributes(itemMeta);
                    break;
                }
            }
        }
        return this;
    }

    public ItemBuilder setUnbreakable() {
        if (itemMeta != null)
            itemMeta.spigot().setUnbreakable(true);
        return this;
    }

    public ItemBuilder setHideTooltip() {
        if (itemMeta != null)
            plugin.getNMSAlgorithms().setHideTooltip(itemMeta);
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

        if (BukkitItems.isValidAndSpawnEgg(itemStack)) {
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

    public ItemBuilder withBannerMeta(DyeColor dyeColor, PatternType patternType) {
        if (itemMeta instanceof BannerMeta) {
            BannerMeta bannerMeta = (BannerMeta) itemMeta;
            bannerMeta.addPattern(new Pattern(dyeColor, patternType));
        }
        return this;
    }

    public ItemBuilder withCustomModel(int customModel) {
        plugin.getNMSAlgorithms().setCustomModel(itemMeta, customModel);
        return this;
    }

    public ItemBuilder withItemModel(String itemModel) {
        plugin.getNMSAlgorithms().setItemModel(itemMeta, itemModel);
        return this;
    }

    public ItemBuilder withRarity(String rarity) {
        plugin.getNMSAlgorithms().setRarity(itemMeta, rarity.toUpperCase(Locale.ENGLISH));
        return this;
    }

    public ItemBuilder withTrim(String trimMaterial, String trimPattern) {
        plugin.getNMSAlgorithms().setTrim(itemMeta, trimMaterial.toLowerCase(Locale.ENGLISH), trimPattern.toLowerCase(Locale.ENGLISH));
        return this;
    }

    public ItemBuilder withLeatherColor(int leatherColor) {
        if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
            leatherArmorMeta.setColor(Color.fromRGB(leatherColor));
        }
        return this;
    }

    @Nullable
    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    public ItemStack build(SuperiorPlayer superiorPlayer) {
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();

        if (itemMeta != null) {
            if (displayName != null) {
                withName(placeholdersService.get().parsePlaceholders(offlinePlayer, displayName));
            }

            if (lore != null && !lore.isEmpty()) {
                withLore(new SequentialListBuilder<String>()
                        .build(lore, line -> placeholdersService.get().parsePlaceholders(offlinePlayer, line)));
            }
        }

        if (textureValue.equals("%superior_player_texture%")) {
            textureValue = superiorPlayer.getTextureValue();
        }

        return build();
    }

    public ItemStack build() {
        if (itemMeta != null) {
            if (displayName != null) {
                plugin.getProviders().getInventoryProvider().setItemMetaDisplayName(itemMeta, displayName);
            }
            if (lore != null && !lore.isEmpty()) {
                plugin.getProviders().getInventoryProvider().setItemMetaLore(itemMeta, lore);
            }
        }

        itemStack.setItemMeta(itemMeta);
        return textureValue.isEmpty() ? itemStack : ItemSkulls.getPlayerHead(itemStack, textureValue);
    }

    public ItemBuilder copy() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.AIR);
        itemBuilder.itemStack = itemStack.clone();
        if (itemMeta != null) {
            itemBuilder.itemMeta = itemMeta.clone();
            itemBuilder.displayName = displayName;
            itemBuilder.lore = lore == null ? null : new ArrayList<>(lore);
        }
        itemBuilder.textureValue = textureValue;
        return itemBuilder;
    }

}
