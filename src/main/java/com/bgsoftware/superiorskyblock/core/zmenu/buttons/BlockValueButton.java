package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BlockValueButton extends SuperiorButton {

    private final BigInteger maxStack = BigInteger.valueOf(64);
    private final Key block;

    public BlockValueButton(SuperiorSkyblockPlugin plugin, Key block) {
        super(plugin);
        this.block = block;
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {
        super.onInventoryOpen(player, inventory, placeholders);
        placeholders.register("player-name", player.getName());

        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);
        Island island = superiorPlayer.getIsland();
        placeholders.register("worth", Formatters.NUMBER_FORMATTER.format(island.getWorth()));
        placeholders.register("worth-formatted", Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), superiorPlayer.getUserLocale()));
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {

        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);
        Island island = superiorPlayer.getIsland();

        BigDecimal amount = new BigDecimal(block.getGlobalKey().contains("SPAWNER") ? island.getExactBlockCountAsBigInteger(block) : island.getBlockCountAsBigInteger(block));

        BlockValue blockValue = plugin.getBlockValues().getBlockValue(block);
        BigDecimal blockWorth = blockValue.getWorth();
        BigDecimal blockLevel = blockValue.getLevel();

        Placeholders placeholders = new Placeholders();
        placeholders.register("quantity", String.valueOf(amount));
        placeholders.register("worth", Formatters.NUMBER_FORMATTER.format(blockWorth.multiply(amount)));
        placeholders.register("level", Formatters.NUMBER_FORMATTER.format(blockLevel.multiply(amount)));
        placeholders.register("worth-formatted", Formatters.FANCY_NUMBER_FORMATTER.format(blockWorth.multiply(amount), superiorPlayer.getUserLocale()));
        placeholders.register("level-formatted", Formatters.FANCY_NUMBER_FORMATTER.format(blockLevel.multiply(amount), superiorPlayer.getUserLocale()));

        ItemStack itemStack = getItemStack().build(player, false, placeholders);
        itemStack.setAmount(BigInteger.ONE.max(maxStack.min(amount.toBigInteger())).intValue());
        return itemStack;
    }
}
