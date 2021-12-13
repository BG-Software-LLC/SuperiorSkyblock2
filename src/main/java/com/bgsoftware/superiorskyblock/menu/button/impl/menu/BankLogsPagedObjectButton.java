package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBankLogs;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public final class BankLogsPagedObjectButton extends PagedObjectButton<MenuBankLogs, BankTransaction> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private BankLogsPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                      String requiredPermission, SoundWrapper lackPermissionSound,
                                      ItemBuilder nullItem) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem);
    }


    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuBankLogs superiorMenu, InventoryClickEvent clickEvent) {
        superiorMenu.setFilteredPlayer(pagedObject.getPlayer());
        superiorMenu.refreshPage();
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, SuperiorPlayer inventoryViewer,
                                      SuperiorPlayer targetPlayer, BankTransaction transaction) {
        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", transaction.getPosition() + "")
                .replaceAll("{1}", getFilteredPlayerName(transaction.getPlayer() == null ? CONSOLE_UUID : transaction.getPlayer()))
                .replaceAll("{2}", (transaction.getAction() == BankAction.WITHDRAW_COMPLETED ?
                        Locale.BANK_WITHDRAW_COMPLETED : Locale.BANK_DEPOSIT_COMPLETED).getMessage(inventoryViewer.getUserLocale()))
                .replaceAll("{3}", transaction.getDate())
                .replaceAll("{4}", transaction.getAmount() + "")
                .replaceAll("{5}", StringUtils.format(transaction.getAmount()))
                .replaceAll("{6}", StringUtils.fancyFormat(transaction.getAmount(), inventoryViewer.getUserLocale()))
                .asSkullOf(inventoryViewer)
                .build(inventoryViewer);
    }

    private static String getFilteredPlayerName(UUID filteredPlayer) {
        if (filteredPlayer == null) {
            return "";
        } else if (filteredPlayer.equals(CONSOLE_UUID)) {
            return "Console";
        } else {
            return plugin.getPlayers().getSuperiorPlayer(filteredPlayer).getName();
        }
    }

    public static class Builder extends PagedObjectBuilder<Builder, BankLogsPagedObjectButton, MenuBankLogs> {

        @Override
        public BankLogsPagedObjectButton build() {
            return new BankLogsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem);
        }

    }

}
