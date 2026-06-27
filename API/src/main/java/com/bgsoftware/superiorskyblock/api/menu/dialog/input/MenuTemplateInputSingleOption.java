package com.bgsoftware.superiorskyblock.api.menu.dialog.input;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

import java.util.List;

public interface MenuTemplateInputSingleOption<V extends MenuView<V, ?>> extends MenuTemplateInput<V> {

    int getWidth();

    boolean isLabelVisible();

    List<OptionEntry> getOptionEntries();

    static <V extends MenuView<V, ?>> MenuTemplateInputSingleOption.Builder<V> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createInputSingleOptionBuilder();
    }

    interface OptionEntry {

        String getId();

        String getDisplay();

        boolean isInitial();

    }

    interface Builder<V extends MenuView<V, ?>> extends MenuTemplateInput.Builder<V> {

        Builder<V> setWidth(int width);

        Builder<V> setLabelVisible(boolean labelVisible);

        Builder<V> addOptionEntry(String id, String display, boolean initial);

        MenuTemplateInputSingleOption<V> build();

    }

}
