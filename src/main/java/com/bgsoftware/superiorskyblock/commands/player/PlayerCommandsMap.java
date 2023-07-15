package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.CommandsMap;

public class PlayerCommandsMap extends CommandsMap {

    public PlayerCommandsMap(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadDefaultCommands() {
        registerCommand(new CmdAccept(), false);
        registerCommand(new CmdAdmin(), false);
        registerCommand(new CmdBan(), false);
        registerCommand(new CmdBiome(), false);
        registerCommand(new CmdBorder(), false);
        registerCommand(new CmdChest(), false);
        registerCommand(new CmdClose(), false);
        if (plugin.getSettings().isCoopMembers()) {
            registerCommand(new CmdCoop(), false);
            registerCommand(new CmdCoops(), false);
        }
        registerCommand(new CmdCounts(), false);
        registerCommand(new CmdCreate(), false);
        registerCommand(new CmdDelWarp(), false);
        registerCommand(new CmdDemote(), false);
        registerCommand(new CmdDisband(), false);
        registerCommand(new CmdExpel(), false);
        registerCommand(new CmdFly(), false);
        registerCommand(new CmdHelp(), false);
        registerCommand(new CmdInvite(), false);
        registerCommand(new CmdKick(), false);
        registerCommand(new CmdLang(), false);
        registerCommand(new CmdLeave(), false);
        registerCommand(new CmdMembers(), false);
        registerCommand(new CmdName(), false);
        registerCommand(new CmdOpen(), false);
        registerCommand(new CmdPanel(), false);
        registerCommand(new CmdPardon(), false);
        registerCommand(new CmdPermissions(), false);
        registerCommand(new CmdPromote(), false);
        registerCommand(new CmdRate(), false);
        registerCommand(new CmdRatings(), false);
        registerCommand(new CmdRecalc(), false);
        registerCommand(new CmdSetDiscord(), false);
        registerCommand(new CmdSetPaypal(), false);
        registerCommand(new CmdSetRole(), false);
        registerCommand(new CmdSetTeleport(), false);
        registerCommand(new CmdSettings(), false);
        registerCommand(new CmdSetWarp(), false);
        registerCommand(new CmdShow(), false);
        registerCommand(new CmdTeam(), false);
        registerCommand(new CmdTeamChat(), false);
        registerCommand(new CmdTeleport(), false);
        registerCommand(new CmdToggle(), false);
        registerCommand(new CmdTop(), false);
        registerCommand(new CmdTransfer(), false);
        if (plugin.getSettings().isCoopMembers())
            registerCommand(new CmdUncoop(), false);
        registerCommand(new CmdValue(), false);
        registerCommand(new CmdValues(), false);
        registerCommand(new CmdVisit(), false);
        registerCommand(new CmdVisitors(), false);
        registerCommand(new CmdWarp(), false);
        registerCommand(new CmdWarps(), false);
    }

}
