package com.bgsoftware.superiorskyblock.api.data;

@Deprecated
public interface PlayerDataHandler {

    /**
     * Save the texture value to the database.
     */
    void saveTextureValue();

    /**
     * Save the player-name to the database.
     */
    void savePlayerName();

    /**
     * Save the user-locale to the database.
     */
    void saveUserLocale();

    /**
     * Save the island-leader to the database.
     */
    void saveIslandLeader();

    /**
     * Save the player-role to the database.
     */
    void savePlayerRole();

    /**
     * Save the toggled-border status to the database.
     */
    void saveToggledBorder();

    /**
     * Save the disbands amount to the database.
     */
    void saveDisbands();

    /**
     * Save the toggled-panel status to the database.
     */
    void saveToggledPanel();

    /**
     * Save the island-fly status to the database.
     */
    void saveIslandFly();

    /**
     * Save the border-color to the database.
     */
    void saveBorderColor();

    /**
     * Save the last-time-status to the database.
     */
    void saveLastTimeStatus();

    /**
     * Save the completed missions to the database.
     */
    void saveMissions();

}
