package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.CommandsMap;

public class AdminCommandsMap extends CommandsMap {

    public AdminCommandsMap(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadDefaultCommands() {
        registerCommand(new CmdAdminAdd(), false);
        registerCommand(new CmdAdminAddBlockLimit(), false);
        registerCommand(new CmdAdminAddBonus(), false);
        if (plugin.getSettings().isCoopMembers())
            registerCommand(new CmdAdminAddCoopLimit(), false);
        registerCommand(new CmdAdminAddDisbands(), false);
        registerCommand(new CmdAdminAddEntityLimit(), false);
        registerCommand(new CmdAdminAddSize(), false);
        registerCommand(new CmdAdminAddTeamLimit(), false);
        registerCommand(new CmdAdminAddWarpsLimit(), false);
        registerCommand(new CmdAdminBypass(), false);
        registerCommand(new CmdAdminChest(), false);
        registerCommand(new CmdAdminClose(), false);
        registerCommand(new CmdAdminCmdAll(), false);
        registerCommand(new CmdAdminCount(), false);
        registerCommand(new CmdAdminData(), false);
        registerCommand(new CmdAdminDebug(), false);
        registerCommand(new CmdAdminDelWarp(), false);
        registerCommand(new CmdAdminDemote(), false);
        registerCommand(new CmdAdminDisband(), false);
        registerCommand(new CmdAdminIgnore(), false);
        registerCommand(new CmdAdminJoin(), false);
        registerCommand(new CmdAdminKick(), false);
        registerCommand(new CmdAdminModules(), false);
        registerCommand(new CmdAdminMsg(), false);
        registerCommand(new CmdAdminMsgAll(), false);
        registerCommand(new CmdAdminName(), false);
        registerCommand(new CmdAdminOpen(), false);
        registerCommand(new CmdAdminOpenMenu(), false);
        registerCommand(new CmdAdminPromote(), false);
        registerCommand(new CmdAdminPurge(), false);
        registerCommand(new CmdAdminRecalc(), false);
        registerCommand(new CmdAdminReload(), false);
        registerCommand(new CmdAdminRemoveBlockLimit(), false);
        registerCommand(new CmdAdminRemoveRatings(), false);
        registerCommand(new CmdAdminResetWorld(), false);
        registerCommand(new CmdAdminSchematic(), false);
        registerCommand(new CmdAdminSetBankLimit(), false);
        registerCommand(new CmdAdminSetBiome(), false);
        registerCommand(new CmdAdminSetBlockAmount(), false);
        registerCommand(new CmdAdminSetBlockLimit(), false);
        registerCommand(new CmdAdminSetBonus(), false);
        registerCommand(new CmdAdminSetChestRow(), false);
        if (plugin.getSettings().isCoopMembers())
            registerCommand(new CmdAdminSetCoopLimit(), false);
        registerCommand(new CmdAdminSetDisbands(), false);
        registerCommand(new CmdAdminSetEntityLimit(), false);
        registerCommand(new CmdAdminSetLeader(), false);
        registerCommand(new CmdAdminSetPermission(), false);
        registerCommand(new CmdAdminSetRate(), false);
        registerCommand(new CmdAdminSetRoleLimit(), false);
        registerCommand(new CmdAdminSetSettings(), false);
        registerCommand(new CmdAdminSetSize(), false);
        registerCommand(new CmdAdminSetSpawn(), false);
        registerCommand(new CmdAdminSetTeamLimit(), false);
        registerCommand(new CmdAdminSettings(), false);
        registerCommand(new CmdAdminSetWarpsLimit(), false);
        registerCommand(new CmdAdminShow(), false);
        registerCommand(new CmdAdminSpawn(), false);
        registerCommand(new CmdAdminSpy(), false);
        registerCommand(new CmdAdminStats(), false);
        registerCommand(new CmdAdminSyncBonus(), false);
        registerCommand(new CmdAdminTeleport(), false);
        registerCommand(new CmdAdminTitle(), false);
        registerCommand(new CmdAdminTitleAll(), false);
        registerCommand(new CmdAdminUnignore(), false);
        registerCommand(new CmdAdminUnlockWorld(), false);
    }

}
