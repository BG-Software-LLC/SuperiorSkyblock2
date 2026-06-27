package com.bgsoftware.superiorskyblock.api.menu.dialog.input;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

public interface MenuTemplateInputText<V extends MenuView<V, ?>> extends MenuTemplateInput<V> {

    @Nullable
    String getInitialValue();

    int getWidth();

    int getMaxLength();

    int getMultilineHeight();

    int getMultilineMaxLines();

    boolean isLabelVisible();

    static <V extends MenuView<V, ?>> Builder<V> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createInputTextBuilder();
    }

    interface Builder<V extends MenuView<V, ?>> extends MenuTemplateInput.Builder<V> {

        Builder<V> setInitialValue(@Nullable String text);

        Builder<V> setWidth(int width);

        Builder<V> setMaxLength(int maxLength);

        Builder<V> setMultilineHeight(int multilineHeight);

        Builder<V> setMultilineMaxLines(int multilineMaxLines);

        Builder<V> setLabelVisible(boolean labelVisible);

        MenuTemplateInputText<V> build();

    }

}
