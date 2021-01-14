package com.bgsoftware.superiorskyblock.tutorial;

import com.bgsoftware.superiorskyblock.utils.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public final class CompoundBuilder {

    private BaseComponent baseCompound;
    private BaseComponent lastCompound;

    public CompoundBuilder addText(String text){
        if(baseCompound == null) {
            baseCompound = lastCompound = new TextComponent(StringUtils.translateColors(text));
        }
        else{
            baseCompound.addExtra((lastCompound = new TextComponent(StringUtils.translateColors(text))));
        }

        return this;
    }

    public CompoundBuilder addCommand(String command){
        lastCompound.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        return this;
    }

    public CompoundBuilder addTooltip(String text){
        lastCompound.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new BaseComponent[]{ new TextComponent(StringUtils.translateColors(text)) }));
        return this;
    }

    public CompoundBuilder addLink(String link){
        lastCompound.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        return this;
    }

    public BaseComponent build(){
        return baseCompound;
    }

}
