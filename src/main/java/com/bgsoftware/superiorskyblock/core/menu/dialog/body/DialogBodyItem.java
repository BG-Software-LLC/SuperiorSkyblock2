package com.bgsoftware.superiorskyblock.core.menu.dialog.body;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;

import java.util.function.Function;

public class DialogBodyItem implements DialogBodyElement {

    private static final ItemConfig DEFAULT_CONFIG = new ItemConfig();

    private final TemplateItem item;
    @Nullable
    private final DialogBodyElement description;
    private final boolean showDecorations;
    private final boolean showTooltip;
    private final int width;
    private final int height;

    private Object nmsHandle;

    public DialogBodyItem(TemplateItem item, @Nullable ItemConfig itemConfig) {
        this.item = item;
        if (itemConfig == null)
            itemConfig = DEFAULT_CONFIG;
        this.description = itemConfig.getDescription();
        this.showDecorations = itemConfig.isShowDecorations();
        this.showTooltip = itemConfig.isShowTooltip();
        this.width = itemConfig.getWidth();
        this.height = itemConfig.getHeight();
    }

    public TemplateItem getItem() {
        return item;
    }

    @Nullable
    public DialogBodyElement getDescription() {
        return description;
    }

    public boolean isShowDecorations() {
        return showDecorations;
    }

    public boolean isShowTooltip() {
        return showTooltip;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Object getNMSHandle(Function<DialogBodyItem, Object> nmsHandleCreator) {
        if (this.nmsHandle == null)
            this.nmsHandle = nmsHandleCreator.apply(this);

        return this.nmsHandle;
    }

}
