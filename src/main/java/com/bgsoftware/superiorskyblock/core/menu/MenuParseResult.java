package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.common.config.CommentedConfiguration;

public class MenuParseResult {

    private final MenuPatternSlots patternSlots;
    private final CommentedConfiguration config;

    public MenuParseResult(MenuPatternSlots patternSlots, CommentedConfiguration config) {
        this.patternSlots = patternSlots;
        this.config = config;
    }

    public MenuPatternSlots getPatternSlots() {
        return patternSlots;
    }

    public CommentedConfiguration getConfig() {
        return config;
    }

}
