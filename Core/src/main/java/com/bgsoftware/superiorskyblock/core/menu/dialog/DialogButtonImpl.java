package com.bgsoftware.superiorskyblock.core.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButtonAction;
import com.bgsoftware.superiorskyblock.core.Text;

import java.util.EnumMap;

public class DialogButtonImpl implements DialogButton {

    private final String label;
    private final int width;
    @Nullable
    private final DialogButtonAction action;

    public DialogButtonImpl(String label, int width, DialogButtonAction action) {
        this.label = label;
        this.width = width;
        this.action = action;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Nullable
    @Override
    public DialogButtonAction getAction() {
        return this.action;
    }

    private static class DialogButtonActionImpl implements DialogButtonAction {

        private static final EnumMap<Type, DialogButtonActionImpl> DEFAULTS = new EnumMap<>(Type.class);

        static {
            for (Type type : Type.values())
                DEFAULTS.put(type, new DialogButtonActionImpl(type, null));
        }

        private final Type type;
        @Nullable
        private final String data;

        DialogButtonActionImpl(Type type, @Nullable String data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public Type getType() {
            return this.type;
        }

        @Nullable
        @Override
        public String getActionData() {
            return this.data;
        }
    }

    public static class Builder implements DialogButton.Builder {

        private String label;
        private int width = 150;
        private DialogButtonAction action;

        @Override
        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        @Override
        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        @Override
        public Builder setAction(DialogButtonAction.Type type, @Nullable String data) {
            this.action = Text.isBlank(data) ? DialogButtonActionImpl.DEFAULTS.get(type) :
                    new DialogButtonActionImpl(type, data);
            return this;
        }

        @Override
        public DialogButton build() {
            return new DialogButtonImpl(this.label, this.width, this.action);
        }
    }

}
