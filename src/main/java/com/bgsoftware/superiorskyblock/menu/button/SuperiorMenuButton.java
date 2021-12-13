package com.bgsoftware.superiorskyblock.menu.button;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class SuperiorMenuButton<M extends ISuperiorMenu> {

    protected final ItemBuilder buttonItem;
    protected final SoundWrapper clickSound;
    protected final List<String> commands;
    protected final String requiredPermission;
    protected final SoundWrapper lackPermissionSound;

    protected SuperiorMenuButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                 String requiredPermission, SoundWrapper lackPermissionSound) {
        this.buttonItem = buttonItem;
        this.clickSound = clickSound;
        this.commands = commands == null ? Collections.emptyList() : Collections.unmodifiableList(commands);
        this.requiredPermission = requiredPermission;
        this.lackPermissionSound = lackPermissionSound;
    }

    @Nullable
    public ItemStack getButtonItem(M superiorMenu) {
        return buttonItem == null ? null : buttonItem.build(superiorMenu.getInventoryViewer());
    }

    @Nullable
    public SoundWrapper getClickSound() {
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
    public SoundWrapper getLackPermissionSound() {
        return lackPermissionSound;
    }

    public <T extends AbstractBuilder<T, ?, M>> T applyToBuilder(AbstractBuilder<T, ?, M> buttonBuilder) {
        return buttonBuilder.setButtonItem(this.buttonItem)
                .setClickSound(this.clickSound)
                .setCommands(this.commands)
                .setRequiredPermission(this.requiredPermission)
                .setLackPermissionsSound(this.lackPermissionSound);
    }

    public abstract void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent);

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<B extends AbstractBuilder<B, T, M>,
            T extends SuperiorMenuButton<M>, M extends ISuperiorMenu> {

        protected boolean touched = false;

        protected ItemBuilder buttonItem = null;
        protected SoundWrapper clickSound = null;
        protected List<String> commands = null;
        protected String requiredPermission = null;
        protected SoundWrapper lackPermissionSound = null;

        protected AbstractBuilder() {

        }

        public B setButtonItem(ItemBuilder buttonItem) {
            this.touched = !Objects.equals(this.buttonItem, buttonItem);
            this.buttonItem = buttonItem;
            return (B) this;
        }

        public B setClickSound(SoundWrapper clickSound) {
            this.touched = !Objects.equals(this.clickSound, clickSound);
            this.clickSound = clickSound;
            return (B) this;
        }

        public B setCommands(List<String> commands) {
            this.touched = !Objects.equals(this.commands, commands) && !commands.isEmpty();
            this.commands = commands;
            return (B) this;
        }

        public B setRequiredPermission(String requiredPermission) {
            this.touched = !Objects.equals(this.requiredPermission, requiredPermission);
            this.requiredPermission = requiredPermission;
            return (B) this;
        }

        public B setLackPermissionsSound(SoundWrapper lackPermissionSound) {
            this.touched = !Objects.equals(this.lackPermissionSound, lackPermissionSound);
            this.lackPermissionSound = lackPermissionSound;
            return (B) this;
        }

        public abstract T build();

    }

}
