package com.bgsoftware.superiorskyblock.island.data;

import com.bgsoftware.superiorskyblock.api.data.PlayerDataHandler;

public final class SEmptyPlayerDataHandler implements PlayerDataHandler {

    private static final SEmptyPlayerDataHandler dataHandlerInstance = new SEmptyPlayerDataHandler();

    public static PlayerDataHandler getHandler(){
        return dataHandlerInstance;
    }

    @Override
    public void saveTextureValue() {

    }

    @Override
    public void savePlayerName() {

    }

    @Override
    public void saveUserLocale() {

    }

    @Override
    public void saveIslandLeader() {

    }

    @Override
    public void savePlayerRole() {

    }

    @Override
    public void saveToggledBorder() {

    }

    @Override
    public void saveDisbands() {

    }

    @Override
    public void saveToggledPanel() {

    }

    @Override
    public void saveIslandFly() {

    }

    @Override
    public void saveBorderColor() {

    }

    @Override
    public void saveLastTimeStatus() {

    }

    @Override
    public void saveMissions() {

    }

}
