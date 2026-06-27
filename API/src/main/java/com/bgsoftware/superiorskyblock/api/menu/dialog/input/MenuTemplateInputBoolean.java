package com.bgsoftware.superiorskyblock.api.menu.dialog.input;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

public interface MenuTemplateInputBoolean<V extends MenuView<V, ?>> extends MenuTemplateInput<V> {

    boolean getInitialValue();

    String getOnTrue();

    String getOnFalse();

    static <V extends MenuView<V, ?>> MenuTemplateInputBoolean.Builder<V> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createInputBooleanBuilder();
    }

    interface Builder<V extends MenuView<V, ?>> extends MenuTemplateInput.Builder<V> {

        Builder<V> setInitialValue(boolean value);

        Builder<V> setOnTrue(String onTrue);

        Builder<V> setOnFalse(String onFalse);

        MenuTemplateInputBoolean<V> build();

    }

}
