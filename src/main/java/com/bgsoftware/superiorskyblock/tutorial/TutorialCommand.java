package com.bgsoftware.superiorskyblock.tutorial;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public final class TutorialCommand extends BukkitCommand {

    private static final char DOUBLE_RIGHT_POINTING_ANGEL = Character.toChars(0xBB)[0];
    private static final char DOUBLE_LEFT_POINTING_ANGEL = Character.toChars(0xAB)[0];
    private static final char BULLET = Character.toChars(0x2022)[0];

    private final SuperiorSkyblockPlugin plugin;

    public TutorialCommand(SuperiorSkyblockPlugin plugin){
        super("sbt");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(args.length != 1){
            return false;
        }

        if(args[0].equalsIgnoreCase("confirm")){
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdirs();
            Bukkit.shutdown();
            return false;
        }

        int stageNumber;

        try{
            stageNumber = Integer.parseInt(args[0]);
        }catch (Exception ex){
            return false;
        }

        BaseComponent stageMessage;

        switch (stageNumber){
            case 1:
                stageMessage = handleStage1();
                break;
            case 2:
                stageMessage = handleStage2();
                break;
            case 3:
                stageMessage = handleStage3();
                break;
            case 4:
                stageMessage = handleStage4();
                break;
            case 5:
                stageMessage = handleStage5();
                break;
            case 6:
                stageMessage = handleStage6();
                break;
            case 7:
                stageMessage = handleStage7();
                break;
            default:
                return false;
        }

        sender.sendMessage(stageMessage);

        return false;
    }

    private BaseComponent handleStage1(){
        return new CompoundBuilder().addText("&8&m----------------------------------------------------\n&f" +
                "  &9&lSuperiorSkyblock2\n\n&f" +
                " SuperiorSkyblock2 is a new, modern and optimized Skyblock core. It's packed with all the features you " +
                "need to run your server smoothly without any issues. In this quick tutorial, you'll be guided through " +
                "the important parts of the plugin:\n")
                .addText("  &81. &fIsland schematics\n")
                .addText("  &82. &fWorth and levelling systems\n")
                .addText("  &83. &fNether and end worlds\n")
                .addText("  &84. &fIsland permissions and settings\n")
                .addText("  &85. &fAdmin tools\n\n")
                .addText("&8&m----------------------")
                .addText("&9 " + DOUBLE_LEFT_POINTING_ANGEL + " ").addTooltip("&7There's no previous page")
                .addText("&f1/7")
                .addText("&9 " + DOUBLE_RIGHT_POINTING_ANGEL + " ").addCommand("sbt 2").addTooltip("&7Continue to page #2")
                .addText("&8&m----------------------").build();
    }

    private BaseComponent handleStage2(){
        return new CompoundBuilder().addText("&8&m----------------------------------------------------\n&f" +
                "  &9&lSchematics\n\n&f" +
                " Similar to WorldEdit, the plugin contains a built-in schematics plugin for your islands. " +
                "You can enable the schematics mode by executing ")
                .addText("&9/is admin schematic").addCommand("is admin schematic").addTooltip("&7Click to enable schematics mode")
                .addText(", and then select two points that cover your schematic using a golden axe. After that, " +
                        "you should stand at the location you want" +
                        "the players to teleport at, and execute /is admin schematic <schematic-name>.\n" +
                        " You can find a short video tutorial showing this process ")
                .addText("&9here").addLink("https://wiki.bg-software.com/#/superiorskyblock/schematics/").addText("&f.\n")
                .addText(" After you created your schematic, make sure you add it to the island creation menu so it " +
                        "will be displayed when players run /is create.\n\n")
                .addText("&8&m----------------------")
                .addText("&9 " + DOUBLE_LEFT_POINTING_ANGEL + " ").addCommand("sbt 1").addTooltip("&7Go to page #1")
                .addText("&f2/7")
                .addText("&9 " + DOUBLE_RIGHT_POINTING_ANGEL + " ").addCommand("sbt 3").addTooltip("&7Continue to page #3")
                .addText("&8&m----------------------").build();
    }

    private BaseComponent handleStage3(){
        return new CompoundBuilder().addText("&8&m----------------------------------------------------\n&f" +
                "  &9&lWorth and levelling system\n\n&f" +
                " The core contains a fast and reliable system to calculate the worth of blocks on your island. " +
                "The system tracks all block actions and calculates the worth and level of your island based on these " +
                "actions live without running any command. The worth system is meant to give worth value for blocks " +
                "based on your shop prices, while the levelling system is for showing how well you progressed. By default, " +
                "the blocks of the schematics are calculated towards the worth and level values, however this can be changed " +
                "by adding \"offset: true\" to the schematic in the island creation menu.\n" +
                " Besides calculating worth and levels live, you can force the plugin to re-calculate your island worth " +
                "and levels by executing &9/is recalc&f, or by enabling the automatic calculation task in the ocnfig. " +
                "As an admin, you can force other islands to recalculate their worth and levels with &9/is admin recalc&f.\n\n")
                .addText("&8&m----------------------")
                .addText("&9 " + DOUBLE_LEFT_POINTING_ANGEL + " ").addCommand("sbt 2").addTooltip("&7Go to page #2")
                .addText("&f3/7")
                .addText("&9 " + DOUBLE_RIGHT_POINTING_ANGEL + " ").addCommand("sbt 4").addTooltip("&7Continue to page #4")
                .addText("&8&m----------------------").build();
    }

    private BaseComponent handleStage4(){
        return new CompoundBuilder().addText("&8&m----------------------------------------------------\n&f" +
                "  &9&lNether and end worlds\n\n&f" +
                " You can enable two additional worlds for your players: nether and end worlds. Similar to vanilla, " +
                "they will need to build portals to join these worlds (nether and end portals). Therefore, if you " +
                "enable the end world, you might want to pre-build an end portal so they can access it." +
                " Even when enabling the worlds, they aren't accessible by default for islands, as the worlds must be " +
                "unlocked before being able to go through the portals (You can make them unlocked by default in the " +
                "config if you wish).\n" +
                " In order to unlock the worlds, execute the command &9/is admin unlockworld&f. This command can be " +
                "executed by console, which means you can make it as a mission or upgrade reward.\n\n")
                .addText("&8&m----------------------")
                .addText("&9 " + DOUBLE_LEFT_POINTING_ANGEL + " ").addCommand("sbt 3").addTooltip("&7Go to page #3")
                .addText("&f4/7")
                .addText("&9 " + DOUBLE_RIGHT_POINTING_ANGEL + " ").addCommand("sbt 5").addTooltip("&7Continue to page #5")
                .addText("&8&m----------------------").build();
    }

    private BaseComponent handleStage5(){
        return new CompoundBuilder().addText("&8&m----------------------------------------------------\n&f" +
                "  &9&lIsland permissions and settings\n\n&f" +
                " The core is packed with a permissions system for players and roles. Island leaders and admins can " +
                "change permissions of other players and roles to do certain things, such as interacting, placing and " +
                "breaking blocks and more. The permissions menu can be accessed by using &9/is admin permissions&f. " +
                "By using this system, they can give permanent access to other players to be part of their island. " +
                "However, if you want to give temporary access to other players to build in your islands, you can use " +
                "&9/is coop&f command. This command gives access to players until they leave the server.\n\n" +
                " Alongside of the permissions system, the core is packed with an island settings system. Island admins " +
                "and leaders can change settings on their islands. For example, make their island have day time all the " +
                "time, restrict mobs spawning, pvp and more. When players teleport to an island with pvp enabled, they " +
                "will be immune to damage for a few seconds and will get a warning, so they can leave before they die. " +
                "Players can view the settings menu by running &9/is settings&f.\n\n")
                .addText("&8&m----------------------")
                .addText("&9 " + DOUBLE_LEFT_POINTING_ANGEL + " ").addCommand("sbt 4").addTooltip("&7Go to page #4")
                .addText("&f5/7")
                .addText("&9 " + DOUBLE_RIGHT_POINTING_ANGEL + " ").addCommand("sbt 6").addTooltip("&7Continue to page #6")
                .addText("&8&m----------------------").build();
    }

    private BaseComponent handleStage6(){
        return new CompoundBuilder().addText("&8&m----------------------------------------------------\n&f" +
                "  &9&lAdmin tools\n\n&f" +
                " As an admin, you get access to many powerful commands to help you with controlling islands on " +
                "the server. You get access to join islands without an invite, delete islands, set roles of players, " +
                "bypass island restrictions and more. In order to bypass the plugin restrictions, simply run &9/is " +
                "admin bypass&f. This command will enable for you the bypass-mode, which will bypass protections, " +
                "teleport warmups, free-roams, locked islands and more. Besides that, you should give yourself the " +
                "&9superior.admin.ban.bypass&f permission to bypass island bans.\n" +
                "If you want to still play as an admin but not have your islands listed in the island top list, simply " +
                "run the &9/is admin ignore&f command. You may read more about the admin tools on ")
                .addText("&9our wiki").addLink("https://wiki.bg-software.com/#/superiorskyblock/?id=commands")
                .addTooltip("&7Click to open the wiki page")
                .addText("&f.\n\n")
                .addText("&8&m----------------------")
                .addText("&9 " + DOUBLE_LEFT_POINTING_ANGEL + " ").addCommand("sbt 5").addTooltip("&7Go to page #5")
                .addText("&f6/7")
                .addText("&9 " + DOUBLE_RIGHT_POINTING_ANGEL + " ").addCommand("sbt 7").addTooltip("&7Continue to page #7")
                .addText("&8&m----------------------").build();
    }

    private BaseComponent handleStage7(){
        return new CompoundBuilder().addText("&8&m----------------------------------------------------\n&f" +
                "               Thanks for using &9SuperiorSkyblock2&f!\n\n")
                .addText("&9  " + BULLET + " ")
                .addText("&9Wiki\n").addLink("https://wiki.bg-software.com/#/superiorskyblock/").addTooltip("&7Click to open the wiki page")
                .addText("&9  " + BULLET + " ")
                .addText("&9Discord\n").addLink("https://bg-software.com/discord/").addTooltip("&7Click to open the discord invite")
                .addText("&9  " + BULLET + " ")
                .addText("&9Issues Tracker\n").addLink("https://github.com/OmerBenGera/SuperiorSkyblock2/issues")
                .addTooltip("&7Click to open the issues tracker")
                .addText("&9  " + BULLET + " ")
                .addText("&9Source Code\n").addLink("https://github.com/OmerBenGera/SuperiorSkyblock2/")
                .addTooltip("&7Click to view the source code")
                .addText("&9  " + BULLET + " ")
                .addText("&9Plugin's page\n\n").addLink("https://bg-software.com/superiorskyblock/")
                .addTooltip("&7Click to open the plugin's page")
                .addText("&f  Click here to complete the tutorial.\n").addCommand("sbt confirm").addTooltip("&7Click to complete the tutorial")
                .addText("&7&o  This requires a server restart!\n\n")
                .addText("  You can complete the tutorial later by running &9/sbt confirm&f.\n")
                .addText("&8&m----------------------")
                .addText("&9 " + DOUBLE_LEFT_POINTING_ANGEL + " ").addCommand("sbt 6").addTooltip("&7Go to page #6")
                .addText("&f7/7")
                .addText("&9 " + DOUBLE_RIGHT_POINTING_ANGEL + " ").addTooltip("&7There's no next page")
                .addText("&8&m----------------------").build();
    }

}
