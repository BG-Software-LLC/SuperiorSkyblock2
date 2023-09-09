package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandValues;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
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
    public void onButtonClick(InventoryClickEvent clickEvent) {
        // Dummy Button
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

        BigDecimal blockWorth = plugin.getBlockValues().getBlockWorth(block);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(block);

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
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, block);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuIslandValues.View> {

        private final Key block;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, Key block) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    ValuesButton.class, ValuesButton::new);
            this.block = Objects.requireNonNull(block, "block cannot be null");
        }

    }

}
