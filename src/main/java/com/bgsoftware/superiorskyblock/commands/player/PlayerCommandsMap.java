package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.CommandsMap;

public class PlayerCommandsMap extends CommandsMap {

    public PlayerCommandsMap(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadDefaultCommands() {
        clearCommands();

        registerCommand(new CmdAccept());
        registerCommand(new CmdAdmin());
        registerCommand(new CmdBan());
        registerCommand(new CmdBans());
        registerCommand(new CmdBiome());
        registerCommand(new CmdBorder());
        registerCommand(new CmdChest());
        registerCommand(new CmdClose());
        if (plugin.getSettings().isCoopMembers()) {
            registerCommand(new CmdCoop());
            registerCommand(new CmdCoops());
        }
        registerCommand(new CmdCounts());
        registerCommand(new CmdCreate());
        registerCommand(new CmdDelWarp());
        registerCommand(new CmdDemote());
        registerCommand(new CmdDisband());
        registerCommand(new CmdExpel());
        registerCommand(new CmdFly());
        registerCommand(new CmdHelp());
        registerCommand(new CmdInvite());
        registerCommand(new CmdKick());
        registerCommand(new CmdLang());
        registerCommand(new CmdLeave());
        registerCommand(new CmdMembers());
        registerCommand(new CmdName());
        registerCommand(new CmdOpen());
        registerCommand(new CmdPanel());
        registerCommand(new CmdPardon());
        registerCommand(new CmdPermissions());
        registerCommand(new CmdPromote());
        registerCommand(new CmdRate());
        registerCommand(new CmdRatings());
        registerCommand(new CmdRecalc());
        registerCommand(new CmdSetDiscord());
        registerCommand(new CmdSetPaypal());
        registerCommand(new CmdSetRole());
        registerCommand(new CmdSetTeleport());
        registerCommand(new CmdSettings());
        registerCommand(new CmdSetWarp());
        registerCommand(new CmdShow());
        registerCommand(new CmdTeam());
        registerCommand(new CmdTeamChat());
        registerCommand(new CmdTeleport());
        registerCommand(new CmdToggle());
        registerCommand(new CmdTop());
        registerCommand(new CmdTransfer());
        if (plugin.getSettings().isCoopMembers())
            registerCommand(new CmdUncoop());
        registerCommand(new CmdValue());
        registerCommand(new CmdValues());
        registerCommand(new CmdVisit());
        registerCommand(new CmdVisitors());
        registerCommand(new CmdWarp());
        registerCommand(new CmdWarps());
    }

}
