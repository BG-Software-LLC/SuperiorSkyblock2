package com.bgsoftware.superiorskyblock.core.menu.dialog.body;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;

import java.util.Objects;

public class DialogBodyText implements DialogBodyElement {

    private static final Config DEFAULT_CONFIG = new Config();

    private final MessageContent text;
    private final Config config;

    public DialogBodyText(String text, @Nullable TextConfig textConfig) {
        this.text = MessageContent.parse(text);
        this.config = textConfig == null || DEFAULT_CONFIG.equals(textConfig) ? DEFAULT_CONFIG : (Config) textConfig;
    }

    public String getText(OfflinePlayer offlinePlayer) {
        return text.getContent(offlinePlayer).orElse("");
    }

    public int getWidth() {
        return this.config.width;
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
