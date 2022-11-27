package com.bgsoftware.superiorskyblock.core.menu.button;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;

import java.util.List;

public class MenuTemplateButtonImpl<V extends MenuView<V, ?>> extends AbstractMenuTemplateButton<V> implements MenuTemplateButton<V> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final ViewButtonCreator<V> viewButtonCreator;

    public MenuTemplateButtonImpl(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                  String requiredPermission, GameSound lackPermissionSound,
                                  Class<?> viewButtonType, ViewButtonCreator<V> viewButtonCreator) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, viewButtonType);
        this.viewButtonCreator = viewButtonCreator;
    }

    @Override
    public MenuViewButton<V> createViewButton(V menuView) {
        return ensureCorrectType(this.viewButtonCreator.create(this, menuView));
    }

    public interface ViewButtonCreator<V extends MenuView<V, ?>> {

        MenuViewButton<V> create(AbstractMenuTemplateButton<V> templateButton, V menuView);

    }

}
