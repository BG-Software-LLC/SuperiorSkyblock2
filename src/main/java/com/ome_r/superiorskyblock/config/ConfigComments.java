package com.ome_r.superiorskyblock.config;

public class ConfigComments {

    @Comment("######################################################")
    @Comment("##                                                  ##")
    @Comment("##                SuperiorSkyblock 2                ##")
    @Comment("##                Developed by Ome_R                ##")
    @Comment("##                                                  ##")
    @Comment("######################################################")
    public static String HEADER = "";

    @Comment("")
    @Comment("Here you can set the amount of time between auto saves.")
    @Comment("If you want to disable this feature, set interval to 0")
    @Comment("Disabling auto save is not recommended, as it's done async.")
    public static String SAVE_INTERVAL = "save-interval";

    @Comment("")
    @Comment("Here you can set the amount of time between auto calculations of all islands.")
    @Comment("If you want to disable this feature, set interval to 0")
    @Comment("It's recommended to set the task to a high interval, as it might cause lag.")
    public static String CALC_INTERVAL = "calc-interval";

    @Comment("")
    @Comment("Set the maximum island size. Island distances is 3 times bigger than the max size.")
    @Comment("Please, do not change it while you have a running islands world!")
    public static String MAX_ISLAND_SIZE = "max-island-size";

    @Comment("")
    @Comment("The default island size of all islands.")
    @Comment("This island size can be expanded by using the /is admin setsize command.")
    public static String DEFAULT_ISLAND_SIZE = "default-island-size";

    @Comment("")
    @Comment("Set the default hoppers limit of islands.")
    @Comment("This limit can be expanded by using the /is admin sethopperslimit command.")
    public static String DEFAULT_HOPPERS_LIMIT = "default-hoppers-limit";

    @Comment("")
    @Comment("Set the default team limit of islands.")
    @Comment("This limit can be expanded by using the /is admin setteamlimit command.")
    public static String DEFAULT_TEAM_LIMIT = "default-team-limit";

    @Comment("")
    @Comment("Set the default crop-growth multiplier of islands.")
    @Comment("This multiplier can be expanded by using the /is admin setcropgrowth command.")
    public static String DEFAULT_CROP_GROWTH = "default-crop-growth";

    @Comment("")
    @Comment("Set the default spawner-rates multiplier of islands.")
    @Comment("This multiplier can be expanded by using the /is admin setspawnerrates command.")
    public static String DEFAULT_SPAWNER_RATES = "default-spawner-rates";

    @Comment("")
    @Comment("Set the default mob-drops multiplier of islands.")
    @Comment("This multiplier can be expanded by using the /is admin setmobdrops command.")
    public static String DEFAULT_MOB_DROPS = "default-mob-drops";

    @Comment("")
    @Comment("Feature that adds per player world border in islands.")
    @Comment("If you want to globally disable it, set this section to false.")
    @Comment("World borders can be toggled by using the /is toggle border command.")
    public static String WORLD_BORDERS = "world-borders";

    @Comment("")
    @Comment("All settings related to stacked blocks.")
    public static String STACKED_BLOCKS = "stacked-blocks";

    @Comment("If you want to globally disable stacked blocks, set this section to false.")
    @Comment("Placement of stacked blocks can be toggled by using the /is toggle blocks command.")
    public static String STACKED_BLOCKS_ENABLED = "stacked-blocks.enabled";

    @Comment("")
    @Comment("Custom name for the blocks.")
    public static String STACKED_BLOCKS_CUSTOM_NAME = "stacked-blocks.custom-name";

    @Comment("")
    @Comment("A list of whitelisted blocks that will get stacked when players have stack mode toggled on.")
    public static String STACKED_BLOCKS_WHITELISTED = "stacked-blocks.whitelisted";

    @Comment("")
    @Comment("Set a formula to calculate the island level by it's worth.")
    @Comment("Use {} as a placeholder for worth. Make sure you only use digits and mathematical operations!")
    public static String ISLAND_LEVEL_FORMULA = "island-level-formula";

    @Comment("")
    @Comment("How should the island top be ordered by?")
    @Comment("Set 'WORTH' if you want it to be ordered by island worth, or 'LEVEL' if you want it to be ordered by island levels.")
    public static String ISLAND_TOP_ORDER = "island-top-order";



}
