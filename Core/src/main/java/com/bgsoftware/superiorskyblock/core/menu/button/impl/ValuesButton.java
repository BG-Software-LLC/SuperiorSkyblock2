package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandValues;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class ValuesButton extends AbstractMenuViewButton<MenuIslandValues.View> {

    private static final BigInteger MAX_STACK = BigInteger.valueOf(64);

    private ValuesButton(AbstractMenuTemplateButton<MenuIslandValues.View> templateButton, MenuIslandValues.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        TemplateItem buttonItem = getTemplate().getButtonTemplateItem();

        if (buttonItem == null)
            return null;

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island island = menuView.getIsland();

        Key block = getTemplate().block;

        BigDecimal amount = new BigDecimal(block.getGlobalKey().contains("SPAWNER") ?
                island.getExactBlockCountAsBigInteger(block) : island.getBlockCountAsBigInteger(block));

        BlockValue blockValue = plugin.getBlockValues().getBlockValue(block);
        BigDecimal blockWorth = blockValue.getWorth();
        BigDecimal blockLevel = blockValue.getLevel();

        ItemStack itemStack = buttonItem.getBuilder()
                .replaceAll("{0}", amount + "")
                .replaceAll("{1}", Formatters.NUMBER_FORMATTER.format(blockWorth.multiply(amount)))
                .replaceAll("{2}", Formatters.NUMBER_FORMATTER.format(blockLevel.multiply(amount)))
                .replaceAll("{3}", Formatters.FANCY_NUMBER_FORMATTER.format(blockWorth.multiply(amount), inventoryViewer.getUserLocale()))
                .replaceAll("{4}", Formatters.FANCY_NUMBER_FORMATTER.format(blockLevel.multiply(amount), inventoryViewer.getUserLocale()))
                .build(inventoryViewer);

        itemStack.setAmount(BigInteger.ONE.max(MAX_STACK.min(amount.toBigInteger())).intValue());

        return itemStack;
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuIslandValues.View> {

        private final Key block;

        public Builder(Key block) {
            this.block = block;
        }

        @Override
        public MenuTemplateButton<MenuIslandValues.View> build() {
            return new Template(this, block);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuIslandValues.View> {

        private final Key block;

        Template(AbstractBuilder<MenuIslandValues.View> builder, Key block) {
            super(builder, ValuesButton.class, ValuesButton::new);
            this.block = Objects.requireNonNull(block, "block cannot be null");
        }

    }

}
