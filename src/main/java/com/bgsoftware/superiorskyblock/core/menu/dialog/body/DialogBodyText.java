package com.bgsoftware.superiorskyblock.core.menu.dialog.body;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;

import java.util.function.Function;

public class DialogBodyText implements DialogBodyElement {

    private static final TextConfig DEFAULT_CONFIG = new TextConfig();

    private final String text;
    private final int width;

    private Object nmsHandle;

    public DialogBodyText(String text, @Nullable TextConfig textConfig) {
        this.text = text;
        if (textConfig == null)
            textConfig = DEFAULT_CONFIG;
        this.width = textConfig.getWidth();
    }

    public String getText() {
        return text;
    }

    public int getWidth() {
        return width;
    }

    public Object getNMSHandle(Function<DialogBodyText, Object> nmsHandleCreator) {
        if (this.nmsHandle == null)
            this.nmsHandle = nmsHandleCreator.apply(this);

        return this.nmsHandle;
    }

}
