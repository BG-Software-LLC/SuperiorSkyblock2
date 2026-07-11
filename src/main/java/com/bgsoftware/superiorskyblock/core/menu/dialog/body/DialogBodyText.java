package com.bgsoftware.superiorskyblock.core.menu.dialog.body;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.function.Function;

public class DialogBodyText implements DialogBodyElement {

    private static final Config DEFAULT_CONFIG = new Config();

    private final String text;
    private final Config config;

    private Object nmsHandle;

    public DialogBodyText(String text, @Nullable TextConfig textConfig) {
        this.text = text;
        this.config = textConfig == null || DEFAULT_CONFIG.equals(textConfig) ? DEFAULT_CONFIG : (Config) textConfig;
    }

    public String getText() {
        return text;
    }

    public int getWidth() {
        return this.config.width;
    }

    public Object getNMSHandle(Function<DialogBodyText, Object> nmsHandleCreator) {
        if (this.nmsHandle == null)
            this.nmsHandle = nmsHandleCreator.apply(this);

        return this.nmsHandle;
    }

    public static class Config implements TextConfig {

        private int width = TextConfig.DEFAULT_WIDTH;

        @Override
        public Config setWidth(int width) {
            Preconditions.checkArgument(width > 0, "width must be a positive number");
            this.width = width;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Config config = (Config) o;
            return width == config.width;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(width);
        }

    }

}
