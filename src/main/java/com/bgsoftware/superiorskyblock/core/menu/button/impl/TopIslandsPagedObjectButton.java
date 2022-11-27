package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class TopIslandsPagedObjectButton extends AbstractPagedMenuButton<MenuTopIslands.View, Island> {

    private TopIslandsPagedObjectButton(MenuTemplateButton<MenuTopIslands.View> templateButton, MenuTopIslands.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        TopIslandsSelfIslandButton.onButtonClick(clickEvent, menuView, pagedObject, getTemplate().islandSound, getTemplate().islandCommands,
                getTemplate().noIslandSound, getTemplate().noIslandCommands);
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        if (pagedObject == null) {
            return getTemplate().getNullTemplateItem().build();
        } else {
            return TopIslandsSelfIslandButton.modifyViewItem(menuView, pagedObject, getTemplate().islandItem);
        }
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuTopIslands.View, Island> {

        private TemplateItem noIslandItem;
        private GameSound noIslandSound;
        private List<String> noIslandCommands;

        public void setIslandItem(TemplateItem islandItem) {
            this.buttonItem = islandItem;
        }

        public void setNoIslandItem(TemplateItem noIslandItem) {
            this.noIslandItem = noIslandItem;
        }

        public void setIslandSound(GameSound islandSound) {
            this.clickSound = islandSound;
        }

        public void setNoIslandSound(GameSound noIslandSound) {
            this.noIslandSound = noIslandSound;
        }

        public void setIslandCommands(List<String> islandCommands) {
            this.commands = islandCommands;
        }

        public void setNoIslandCommands(List<String> noIslandCommands) {
            this.noIslandCommands = noIslandCommands;
        }

        @Override
        public PagedMenuTemplateButton<MenuTopIslands.View, Island> build() {
            return new Template(requiredPermission, lackPermissionSound, buttonItem,
                    clickSound, commands, noIslandItem, noIslandSound, noIslandCommands, getButtonIndex());
        }

    }

    public static class Template extends PagedMenuTemplateButtonImpl<MenuTopIslands.View, Island> {

        private final TemplateItem islandItem;
        private final GameSound islandSound;
        private final GameSound noIslandSound;
        private final List<String> islandCommands;
        private final List<String> noIslandCommands;

        Template(String requiredPermission, GameSound lackPermissionSound,
                 TemplateItem islandItem, GameSound islandSound, List<String> islandCommands,
                 TemplateItem noIslandItem, GameSound noIslandSound, List<String> noIslandCommands, int buttonIndex) {
            super(null, null, null, requiredPermission, lackPermissionSound, noIslandItem,
                    buttonIndex, TopIslandsPagedObjectButton.class, TopIslandsPagedObjectButton::new);
            this.islandItem = islandItem;
            this.islandSound = islandSound;
            this.islandCommands = islandCommands == null ? Collections.emptyList() : islandCommands;
            this.noIslandSound = noIslandSound;
            this.noIslandCommands = noIslandCommands == null ? Collections.emptyList() : noIslandCommands;
            if (this.getNullTemplateItem() != null)
                this.getNullTemplateItem().getEditableBuilder().asSkullOf((SuperiorPlayer) null);
        }

    }

}
