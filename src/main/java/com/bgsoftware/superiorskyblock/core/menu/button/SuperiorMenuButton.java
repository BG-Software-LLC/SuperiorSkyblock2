package com.bgsoftware.superiorskyblock.core.menu.button;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class SuperiorMenuButton<M extends ISuperiorMenu> {

    protected final TemplateItem buttonItem;
    protected final GameSound clickSound;
    protected final List<String> commands;
    protected final String requiredPermission;
    protected final GameSound lackPermissionSound;

    protected SuperiorMenuButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                 String requiredPermission, GameSound lackPermissionSound) {
        this.buttonItem = buttonItem;
        this.clickSound = clickSound;
        this.commands = commands == null ? Collections.emptyList() : Collections.unmodifiableList(commands);
        this.requiredPermission = requiredPermission;
        this.lackPermissionSound = lackPermissionSound;
    }

    @Nullable
    public ItemStack getButtonItem(M superiorMenu) {
        return buttonItem == null ? null : buttonItem.getBuilder().build(superiorMenu.getInventoryViewer());
    }

    @Nullable
    public ItemStack getRawButtonItem() {
        return buttonItem == null ? null : buttonItem.getBuilder().build();
    }

    @Nullable
    public GameSound getClickSound() {
        return clickSound;
    }

    public List<String> getCommands() {
        return commands;
    }

    @Nullable
    public String getRequiredPermission() {
        return requiredPermission;
    }

    @Nullable
    public GameSound getLackPermissionSound() {
        return lackPermissionSound;
    }

    public boolean ignorePagedButton() {
        return false;
    }

    public <T extends AbstractBuilder<T, ?, M>> T applyToBuilder(AbstractBuilder<T, ?, M> buttonBuilder) {
        if (buttonBuilder.buttonItem == null)
            buttonBuilder.setButtonItem(this.buttonItem);

        return buttonBuilder
                .setClickSound(this.clickSound)
                .setCommands(this.commands)
                .setRequiredPermission(this.requiredPermission)
                .setLackPermissionsSound(this.lackPermissionSound);
    }

    public abstract void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent);

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<B extends AbstractBuilder<B, T, M>,
            T extends SuperiorMenuButton<M>, M extends ISuperiorMenu> {

        protected TemplateItem buttonItem = null;
        protected GameSound clickSound = null;
        protected List<String> commands = null;
        protected String requiredPermission = null;
        protected GameSound lackPermissionSound = null;

        protected AbstractBuilder() {

        }

        public B setButtonItem(TemplateItem buttonItem) {
            this.buttonItem = buttonItem;
            return (B) this;
        }

        public B setClickSound(GameSound clickSound) {
            this.clickSound = clickSound;
            return (B) this;
        }

        public B setCommands(List<String> commands) {
            this.commands = commands;
            return (B) this;
        }

        public B setRequiredPermission(String requiredPermission) {
            this.requiredPermission = requiredPermission;
            return (B) this;
        }

        public B setLackPermissionsSound(GameSound lackPermissionSound) {
            this.lackPermissionSound = lackPermissionSound;
            return (B) this;
        }

        public abstract T build();

    }

}
