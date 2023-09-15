package com.bgsoftware.superiorskyblock.api.menu.button;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The template button class is used to store data about the button.
 * For example, the item to display, the sounds and other data of the button, etc.
 */
public interface MenuTemplateButton<V extends MenuView<V, ?>> {

    /**
     * Get the item to display in the menu.
     * The returned item has no changes to it (placeholders are unparsed, for example).
     * This item is later used in {@link MenuViewButton#createViewItem()}
     */
    @Nullable
    ItemStack getButtonItem();

    /**
     * Get the sound to play when clicking the button.
     */
    @Nullable
    GameSound getClickSound();

    /**
     * Get the commands to run when clicking the button.
     */
    List<String> getClickCommands();

    /**
     * Get the required permission for clicking the button.
     */
    @Nullable
    String getRequiredPermission();

    /**
     * Get the sound to play when clicking the button without having {@link #getRequiredPermission()} permission.
     */
    @Nullable
    GameSound getLackPermissionSound();

    /**
     * Get the class type of view buttons created using {@link #createViewButton(MenuView)}
     */
    Class<?> getViewButtonType();

    /**
     * Create a view-button object to be used by the provided menu view.
     * Unlike the template button, the view button handles the logic for buttons within the view.
     *
     * @param menuView The view to create the button for.
     */
    MenuViewButton<V> createViewButton(V menuView);

    /**
     * Create a new {@link Builder} object for a new {@link MenuTemplateButton}.
     *
     * @param viewButtonCreator The creator used to create a view-button by the builder.
     *                          You will probably want to implement your own {@link MenuViewButton} which will be
     *                          returned by this creator.
     */
    static <V extends MenuView<V, ?>> Builder<V> newBuilder(Class<?> viewButtonType, MenuViewButtonCreator<V> viewButtonCreator) {
        return SuperiorSkyblockAPI.getMenus().createButtonBuilder(viewButtonType, viewButtonCreator);
    }

    interface Builder<V extends MenuView<V, ?>> {

        /**
         * Set the item to display in the menu.
         *
         * @param buttonItem The item.
         */
        Builder<V> setButtonItem(@Nullable ItemStack buttonItem);

        /**
         * Set the sound to play when clicking the button.
         *
         * @param clickSound The sound to play.
         */
        Builder<V> setClickSound(@Nullable GameSound clickSound);

        /**
         * Set the commands to run when clicking the button.
         *
         * @param commands The commands to run.
         */
        Builder<V> setClickCommands(@Nullable List<String> commands);

        /**
         * Set the required permission for clicking the button.
         *
         * @param requiredPermission The required permission for using the button.
         */
        Builder<V> setRequiredPermission(@Nullable String requiredPermission);

        /**
         * Set the sound to play when clicking the button without having {@link #getRequiredPermission()} permission.
         *
         * @param lackPermissionSound The sound to play.
         */
        Builder<V> setLackPermissionsSound(@Nullable GameSound lackPermissionSound);

        /**
         * Get the {@link MenuTemplateButton} from this builder.
         */
        MenuTemplateButton<V> build();

    }

    interface MenuViewButtonCreator<V extends MenuView<V, ?>> {

        /**
         * Create a new {@link MenuViewButton}.
         * You will probably want to implement your own {@link MenuViewButton} which will be
         * returned by this creator.
         */
        MenuViewButton<V> create(MenuTemplateButton<V> templateButton, V menuView);

    }

}
