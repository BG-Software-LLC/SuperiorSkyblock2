package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdInvite implements ISuperiorCommand {

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
        return "invite <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_INVITE.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.INVITE_MEMBER)){
            Locale.NO_INVITE_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.INVITE_MEMBER));
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if(island.isMember(targetPlayer)){
            Locale.ALREADY_IN_ISLAND_OTHER.send(superiorPlayer);
            return;
        }

        if(island.isBanned(targetPlayer)){
            Locale.INVITE_BANNED_PLAYER.send(superiorPlayer);
            return;
        }

        java.util.Locale locale = LocaleUtils.getLocale(sender);
        String message;

        if(island.isInvited(targetPlayer)){
            island.revokeInvite(targetPlayer);
            message = Locale.REVOKE_INVITE_ANNOUNCEMENT.getMessage(locale, superiorPlayer.getName(), targetPlayer.getName());
            if(targetPlayer.asOfflinePlayer().isOnline())
                Locale.GOT_REVOKED.send(targetPlayer, superiorPlayer.getName());
        }
        else {
            if(island.getTeamLimit() >= 0 && island.getIslandMembers(true).size() >= island.getTeamLimit()){
                Locale.INVITE_TO_FULL_ISLAND.send(superiorPlayer);
                return;
            }

            IslandInviteEvent islandInviteEvent = new IslandInviteEvent(superiorPlayer, targetPlayer, island);
            Bukkit.getPluginManager().callEvent(islandInviteEvent);

            if(islandInviteEvent.isCancelled())
                return;

            island.inviteMember(targetPlayer);
            message = Locale.INVITE_ANNOUNCEMENT.getMessage(locale, superiorPlayer.getName(), targetPlayer.getName());

            java.util.Locale targetLocal = LocaleUtils.getLocale(targetPlayer);

            if(targetPlayer.asOfflinePlayer().isOnline() && !Locale.GOT_INVITE.isEmpty(targetLocal)) {
                TextComponent textComponent = new TextComponent(Locale.GOT_INVITE.getMessage(targetLocal, superiorPlayer.getName()));
                if(!Locale.GOT_INVITE_TOOLTIP.isEmpty(targetLocal))
                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] {new TextComponent(Locale.GOT_INVITE_TOOLTIP.getMessage(targetLocal))}));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is accept " + superiorPlayer.getName()));
                targetPlayer.asPlayer().spigot().sendMessage(textComponent);
            }
        }

        if(message != null)
            island.sendMessage(message);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.INVITE_MEMBER)){
            List<String> list = new ArrayList<>();

            for(Player player : Bukkit.getOnlinePlayers()){
                if(!island.isMember(SSuperiorPlayer.of(player)) &&
                        player.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(player.getName());
                }
            }

            return list;
        }
        return new ArrayList<>();
    }
}
