package com.bgsoftware.superiorskyblock.core.menu.dialog.body;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;

public class DialogBodyItem implements DialogBodyElement {

    private final TemplateItem item;
    @Nullable
    private final String description;
    private final boolean showDecorations;
    private final boolean showTooltip;


    public DialogBodyItem(TemplateItem item, ItemConfig itemConfig) {
        this.item = item;
        this.description = itemConfig.getDescription();
        this.showDecorations = itemConfig.isShowDecorations();
        this.showTooltip = itemConfig.isShowTooltip();
    }

    public TemplateItem getItem() {
        return item;
    }

    public String getDescription() {
        return description;
    }

    public boolean isShowDecorations() {
        return showDecorations;
    }

    public boolean isShowTooltip() {
        return showTooltip;
    }

}
