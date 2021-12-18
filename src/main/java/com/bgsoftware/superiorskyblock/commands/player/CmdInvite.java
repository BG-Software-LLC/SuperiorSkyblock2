package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdInvite implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("invite", "add");
    }

    @Override
    public String getPermission() {
        return "superior.island.invite";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "invite <" + Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_INVITE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.INVITE_MEMBER;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_INVITE_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if (targetPlayer == null)
            return;

        if (island.isMember(targetPlayer)) {
            Message.ALREADY_IN_ISLAND_OTHER.send(superiorPlayer);
            return;
        }

        if (island.isBanned(targetPlayer)) {
            Message.INVITE_BANNED_PLAYER.send(superiorPlayer);
            return;
        }

        java.util.Locale locale = superiorPlayer.getUserLocale();
        String message;

        if (island.isInvited(targetPlayer)) {
            island.revokeInvite(targetPlayer);
            message = Message.REVOKE_INVITE_ANNOUNCEMENT.getMessage(locale, superiorPlayer.getName(), targetPlayer.getName());
            if (targetPlayer.isOnline())
                Message.GOT_REVOKED.send(targetPlayer, superiorPlayer.getName());
        } else {
            if (island.getTeamLimit() >= 0 && island.getIslandMembers(true).size() >= island.getTeamLimit()) {
                Message.INVITE_TO_FULL_ISLAND.send(superiorPlayer);
                return;
            }

            if (!EventsCaller.callIslandInviteEvent(superiorPlayer, targetPlayer, island))
                return;

            island.inviteMember(targetPlayer);
            message = Message.INVITE_ANNOUNCEMENT.getMessage(locale, superiorPlayer.getName(), targetPlayer.getName());

            java.util.Locale targetLocal = targetPlayer.getUserLocale();
            Player target = targetPlayer.asPlayer();

            if (target != null && !Message.GOT_INVITE.isEmpty(targetLocal)) {
                TextComponent textComponent = new TextComponent(Message.GOT_INVITE.getMessage(targetLocal, superiorPlayer.getName()));
                if (!Message.GOT_INVITE_TOOLTIP.isEmpty(targetLocal))
                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(Message.GOT_INVITE_TOOLTIP.getMessage(targetLocal))}));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.getCommands().getLabel() + " accept " + superiorPlayer.getName()));
                target.spigot().sendMessage(textComponent);
            }
        }

        if (message != null)
            island.sendMessage(message);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayers(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), onlinePlayer -> !island.isMember(onlinePlayer)) :
                new ArrayList<>();
    }

}
