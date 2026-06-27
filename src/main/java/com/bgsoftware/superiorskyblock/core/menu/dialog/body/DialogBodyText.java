package com.bgsoftware.superiorskyblock.core.menu.dialog.body;

import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;

public class DialogBodyText implements DialogBodyElement {

    private final String text;

    public DialogBodyText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
