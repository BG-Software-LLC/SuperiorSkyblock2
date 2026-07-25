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
import java.util.function.Consumer;

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
        ItemMeta itemMeta = itemStack.getItemMeta();
        this.itemMeta = itemMeta == null ? null : itemMeta.clone();

        if (this.itemMeta != null) {
            this.displayName = this.itemMeta.getDisplayName();
            this.lore = this.itemMeta.getLore();
        }
    }

    public ItemBuilder(Material type) {
        this(type, 0);
    }

    public ItemBuilder(Material type, int damage) {
        this.itemStack = new ItemStack(type, 1, (short) damage);
        this.itemMeta = this.itemStack.getItemMeta();

        if (this.itemMeta != null) {
            this.displayName = this.itemMeta.getDisplayName();
            this.lore = this.itemMeta.getLore();
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
        if (this.itemMeta != null && hasDisplayName()) {
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
        if (this.itemMeta == null || !hasLore()) {
            return withLore(lore);
        }

        if (lore != null && !lore.isEmpty()) {
            this.lore.addAll(new SequentialListBuilder<String>().build(lore, Formatters.COLOR_FORMATTER::format));
        }

        return this;
    }

    public ItemBuilder withLore(String... lore) {
        return withLore(Arrays.asList(lore));
    }

    public ItemBuilder withLore(String firstLine, List<String> listLine) {
        if (this.itemMeta == null) {
            return this;
        }

        List<String> newLore = new LinkedList<>();

        firstLine = Formatters.COLOR_FORMATTER.format(firstLine);
        newLore.add(firstLine);

        for (String line : listLine) {
            newLore.add(ChatColor.getLastColors(firstLine) + Formatters.COLOR_FORMATTER.format(line));
        }

        if (newLore.size() > 10) {
            for (int i = 10; i < newLore.size(); i++) {
                newLore.remove(newLore.get(i));
            }

            newLore.add(ChatColor.getLastColors(firstLine) + "...");
        }

        this.lore = newLore;
        return this;
    }

    public ItemBuilder replaceLore(String regex, String replace) {
        if (this.itemMeta == null || !hasLore()) {
            return this;
        }

        List<String> newLore = new ArrayList<>(this.lore.size());

        for (String line : this.lore) {
            newLore.add(line.replace(regex, replace));
        }

        withLore(newLore);
        return this;
    }

    public ItemBuilder replaceLoreWithLines(String regex, String... lines) {
        return replaceLoreWithLines(regex, Arrays.asList(lines));
    }

    public ItemBuilder replaceLoreWithLines(String regex, List<String> lines) {
        if (this.itemMeta == null || !hasLore()) {
            return this;
        }

        List<String> newLore = new LinkedList<>();
        boolean isEmpty = lines.isEmpty() || lines.stream().allMatch(String::isEmpty);

        for (String line : this.lore) {
            if (line.contains(regex)) {
                if (!isEmpty) {
                    newLore.addAll(lines);
                }
            } else {
                newLore.add(line);
            }
        }

        withLore(newLore);
        return this;
    }

    public ItemBuilder replaceAll(String regex, String replace) {
        replaceName(regex, replace);
        replaceLore(regex, replace);
        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchant, int level) {
        withMeta(meta -> meta.addEnchant(enchant, level, true));
        return this;
    }

    public ItemBuilder withFlags(ItemFlag... itemFlags) {
        if (this.itemMeta != null) {
            this.itemMeta.addItemFlags(itemFlags);

            for (ItemFlag itemFlag : itemFlags) {
                if (itemFlag == ItemFlag.HIDE_ATTRIBUTES) {
                    plugin.getNMSAlgorithms().hideAttributes(this.itemMeta);
                    break;
                }
            }
        }

        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder withEntityType(EntityType entityType) {
        if (this.itemMeta == null) {
            return this;
        }

        if (BukkitItems.isValidAndSpawnEgg(this.itemStack)) {
            if (ServerVersion.isLegacy()) {
                try {
                    ((SpawnEggMeta) this.itemMeta).setSpawnedType(entityType);
                } catch (NoClassDefFoundError error) {
                    this.itemStack.setDurability(entityType.getTypeId());
                }
            } else {
                this.itemStack.setType(Material.valueOf(entityType.name() + "_SPAWN_EGG"));
            }
        }

        return this;
    }

    public ItemBuilder setUnbreakable() {
        withMeta(meta -> meta.spigot().setUnbreakable(true));
        return this;
    }

    public ItemBuilder setHideTooltip() {
        withMeta(meta -> plugin.getNMSAlgorithms().setHideTooltip(meta));
        return this;
    }

    public ItemBuilder makeItemGlow() {
        withMeta(meta -> plugin.getNMSAlgorithms().makeItemGlow(meta));
        return this;
    }

    public ItemBuilder withCustomModel(int customModel) {
        withMeta(meta -> plugin.getNMSAlgorithms().setCustomModel(meta, customModel));
        return this;
    }

    public ItemBuilder withItemModel(String itemModel) {
        withMeta(meta -> plugin.getNMSAlgorithms().setItemModel(meta, itemModel));
        return this;
    }

    public ItemBuilder withRarity(String rarity) {
        withMeta(meta -> plugin.getNMSAlgorithms().setRarity(meta, rarity.toUpperCase(Locale.ENGLISH)));
        return this;
    }

    public ItemBuilder withTrim(String trimMaterial, String trimPattern) {
        plugin.getNMSAlgorithms().setTrim(this.itemMeta,
                trimMaterial.toLowerCase(Locale.ENGLISH), trimPattern.toLowerCase(Locale.ENGLISH));
        return this;
    }

    public ItemBuilder withBannerMeta(DyeColor dyeColor, PatternType patternType) {
        if (this.itemMeta instanceof BannerMeta) {
            BannerMeta bannerMeta = (BannerMeta) this.itemMeta;
            bannerMeta.addPattern(new Pattern(dyeColor, patternType));
        }

        return this;
    }

    public ItemBuilder withLeatherColor(int leatherColor) {
        if (this.itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) this.itemMeta;
            leatherArmorMeta.setColor(Color.fromRGB(leatherColor));
        }

        return this;
    }

    public ItemBuilder withPotionEffect(PotionEffect potionEffect) {
        if (this.itemMeta instanceof PotionMeta) {
            plugin.getNMSAlgorithms().addPotion((PotionMeta) this.itemMeta, potionEffect);
        }

        return this;
    }

    private void withMeta(Consumer<ItemMeta> consumer) {
        if (this.itemMeta != null) {
            consumer.accept(this.itemMeta);
        }
    }

    @Nullable
    public String getDisplayName() {
        return this.displayName;
    }

    public boolean hasDisplayName() {
        return this.displayName != null && !this.displayName.isEmpty();
    }

    @Nullable
    public List<String> getLore() {
        return this.lore;
    }

    public boolean hasLore() {
        return this.lore != null && !this.lore.isEmpty();
    }

    public ItemStack build(SuperiorPlayer superiorPlayer) {
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();

        if (this.itemMeta != null) {
            if (hasDisplayName()) {
                withName(placeholdersService.get().parsePlaceholders(offlinePlayer, this.displayName));
            }
            if (hasLore()) {
                withLore(new SequentialListBuilder<String>().build(this.lore, line
                        -> placeholdersService.get().parsePlaceholders(offlinePlayer, line)));
            }
        }

        if (this.textureValue.equals("%superior_player_texture%")) {
            this.textureValue = superiorPlayer.getTextureValue();
        }

        return build();
    }

    public ItemStack build() {
        if (this.itemMeta != null) {
            if (hasDisplayName()) {
                plugin.getProviders().getUIProvider().setItemMetaDisplayName(this.itemMeta, this.displayName);
            }
            if (hasLore()) {
                plugin.getProviders().getUIProvider().setItemMetaLore(this.itemMeta, this.lore);
            }
        }

        this.itemStack.setItemMeta(this.itemMeta);
        return this.textureValue.isEmpty() ? this.itemStack : ItemSkulls.getPlayerHead(this.itemStack, this.textureValue);
    }

    public ItemBuilder copy() {
        ItemBuilder itemBuilder = new ItemBuilder(Material.AIR);
        itemBuilder.itemStack = this.itemStack.clone();

        if (this.itemMeta != null) {
            itemBuilder.itemMeta = this.itemMeta.clone();
            itemBuilder.displayName = this.displayName;
            itemBuilder.lore = this.lore == null ? null : new ArrayList<>(this.lore);
        }

        itemBuilder.textureValue = this.textureValue;
        return itemBuilder;
    }

}
