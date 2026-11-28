package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
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
    public void onButtonClick(ButtonClickContext<MenuTopIslands.View> context) {
        TopIslandsSelfIslandButton.onButtonClick(context, menuView, pagedObject, getTemplate().islandSound, getTemplate().islandCommands,
                getTemplate().noIslandSound, getTemplate().noIslandCommands);
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        if (pagedObject == null) {
            return getTemplate().getNullTemplateItem().build();
        } else {
            return TopIslandsSelfIslandButton.modifyViewItem(menuView, pagedObject, getTemplate().islandItem.getLeft());
        }
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuTopIslands.View, Island> {

        private Either<TemplateItem, DialogButton> noIslandItem;
        private GameSound noIslandSound;
        private List<String> noIslandCommands;

        public void setIslandItem(TemplateItem islandItem) {
            this.buttonData = Either.left(islandItem);
        }

        public void setIslandDialog(DialogButton islandDialog) {
            this.buttonData = Either.right(islandDialog);
        }

        public void setNoIslandItem(TemplateItem noIslandItem) {
            this.noIslandItem = Either.left(noIslandItem);
        }

        public void setNoIslandDialog(DialogButton noIslandDialog) {
            this.noIslandItem = Either.right(noIslandDialog);
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
            Either<TemplateItem, DialogButton> islandItem = buttonData;
            GameSound islandSound = clickSound;
            List<String> islandCommands = commands;
            this.buttonData = null;
            this.clickSound = null;
            this.commands = null;
            this.nullItem = this.noIslandItem == null ? Either.left(TemplateItem.AIR) : this.noIslandItem;
            try {
                return new Template(this, islandItem, islandSound, islandCommands, noIslandSound, noIslandCommands);
            } finally {
                this.buttonData = islandItem;
                this.clickSound = islandSound;
                this.commands = islandCommands;
            }
        }

    }

    public static class Template extends PagedMenuTemplateButtonImpl<MenuTopIslands.View, Island> {

        private final Either<TemplateItem, DialogButton> islandItem;
        private final GameSound islandSound;
        private final GameSound noIslandSound;
        private final List<String> islandCommands;
        private final List<String> noIslandCommands;

        Template(AbstractBuilder<MenuTopIslands.View, Island> builder, Either<TemplateItem, DialogButton> islandItem, GameSound islandSound,
                 List<String> islandCommands, GameSound noIslandSound, List<String> noIslandCommands) {
            super(builder, TopIslandsPagedObjectButton.class, TopIslandsPagedObjectButton::new);
            this.islandItem = islandItem == null ? Either.left(TemplateItem.AIR) : islandItem;
            this.islandSound = islandSound;
            this.islandCommands = islandCommands == null ? Collections.emptyList() : islandCommands;
            this.noIslandSound = noIslandSound;
            this.noIslandCommands = noIslandCommands == null ? Collections.emptyList() : noIslandCommands;
            if (this.getNullTemplateItem() != null)
                this.getNullTemplateItem().getEditableBuilder().asSkullOf((SuperiorPlayer) null);
        }

    }

}
