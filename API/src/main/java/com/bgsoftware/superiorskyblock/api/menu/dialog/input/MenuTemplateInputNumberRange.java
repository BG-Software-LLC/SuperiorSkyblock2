package com.bgsoftware.superiorskyblock.api.menu.dialog.input;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

public interface MenuTemplateInputNumberRange<V extends MenuView<V, ?>> extends MenuTemplateInput<V> {

    float getInitialValue();

    float getStepValue();

    float getStart();

    float getEnd();

    int getWidth();

    String getLabelFormat();

    static <V extends MenuView<V, ?>> MenuTemplateInputNumberRange.Builder<V> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createInputNumberRangeBuilder();
    }

    interface Builder<V extends MenuView<V, ?>> extends MenuTemplateInput.Builder<V> {

        Builder<V> setInitialValue(float value);

        Builder<V> setStepValue(float value);

        Builder<V> setStart(float value);

        Builder<V> setEnd(float value);

        Builder<V> setWidth(int width);

        Builder<V> setLabelFormat(String labelFormat);

        MenuTemplateInputNumberRange<V> build();

    }

}
