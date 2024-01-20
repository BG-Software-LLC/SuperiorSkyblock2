package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.MenuCommands;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParser;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.layout.PagedMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.layout.RegularMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MenusManagerImpl extends Manager implements MenusManager {

    private final Map<String, Menu<?, ?>> registeredMenus = new HashMap<>();

    public MenusManagerImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() {
        plugin.getProviders().getMenusProvider().initializeMenus();
    }

    @Override
    public void openBankLogs(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openBankLogs(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshBankLogs(Island island) {
        plugin.getProviders().getMenusProvider().refreshBankLogs(island);
    }

    @Override
    public void openBiomes(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openBiomes(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandBiomesMenu(SuperiorPlayer superiorPlayer) {
        openBiomes(superiorPlayer, null, superiorPlayer.getIsland());
    }

    @Override
    public void openBorderColor(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        plugin.getProviders().getMenusProvider().openBorderColor(targetPlayer, previousMenu);
    }

    @Override
    public void openBorderColorMenu(SuperiorPlayer superiorPlayer) {
        openBorderColor(superiorPlayer, null);
    }

    @Override
    public void openConfirmBan(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer bannedPlayer) {
        plugin.getProviders().getMenusProvider().openConfirmBan(targetPlayer, previousMenu, targetIsland, bannedPlayer);
    }

    @Override
    public void openConfirmDisband(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openConfirmDisband(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openConfirmDisbandMenu(SuperiorPlayer superiorPlayer) {
        openConfirmDisband(superiorPlayer, null, superiorPlayer.getIsland());
    }

    @Override
    public void openConfirmKick(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer kickedPlayer) {
        plugin.getProviders().getMenusProvider().openConfirmKick(targetPlayer, previousMenu, targetIsland, kickedPlayer);
    }

    @Override
    public void openConfirmLeave(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        plugin.getProviders().getMenusProvider().openConfirmLeave(targetPlayer, previousMenu);
    }

    @Override
    public void openControlPanel(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openControlPanel(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandPanelMenu(SuperiorPlayer superiorPlayer) {
        openControlPanel(superiorPlayer, null, superiorPlayer.getIsland());
    }

    @Override
    public void openCoops(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openCoops(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshCoops(Island island) {
        plugin.getProviders().getMenusProvider().refreshCoops(island);
    }

    @Override
    public void openCounts(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openCounts(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandCountsMenu(SuperiorPlayer superiorPlayer, Island island) {
        openCounts(superiorPlayer, null, island);
    }

    @Override
    public void refreshCounts(Island island) {
        plugin.getProviders().getMenusProvider().refreshCounts(island);
    }

    @Override
    public void openGlobalWarps(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        plugin.getProviders().getMenusProvider().openGlobalWarps(targetPlayer, previousMenu);
    }

    @Override
    public void openGlobalWarpsMenu(SuperiorPlayer superiorPlayer) {
        openGlobalWarps(superiorPlayer, null);
    }

    @Override
    public void refreshGlobalWarps() {
        plugin.getProviders().getMenusProvider().refreshGlobalWarps();
    }

    @Override
    public void openIslandBank(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openIslandBank(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshIslandBank(Island island) {
        plugin.getProviders().getMenusProvider().refreshIslandBank(island);
    }

    @Override
    public void openIslandBannedPlayers(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openIslandBannedPlayers(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshIslandBannedPlayers(Island island) {
        plugin.getProviders().getMenusProvider().refreshIslandBannedPlayers(island);
    }

    @Override
    public void openIslandChest(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openIslandChest(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshIslandChest(Island island) {
        plugin.getProviders().getMenusProvider().refreshIslandChest(island);
    }

    @Override
    public void openIslandCreation(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, String islandName) {
        plugin.getProviders().getMenusProvider().openIslandCreation(targetPlayer, previousMenu, islandName);
    }

    @Override
    public void openIslandCreationMenu(SuperiorPlayer superiorPlayer, String islandName) {
        openIslandCreation(superiorPlayer, null, islandName);
    }

    @Override
    public void openIslandRate(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openIslandRate(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandRateMenu(SuperiorPlayer superiorPlayer, Island island) {
        openIslandRate(superiorPlayer, null, island);
    }

    @Override
    public void openIslandRatings(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openIslandRatings(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandRatingsMenu(SuperiorPlayer superiorPlayer, Island island) {
        openIslandRatings(superiorPlayer, null, island);
    }

    @Override
    public void refreshIslandRatings(Island island) {
        plugin.getProviders().getMenusProvider().refreshIslandRatings(island);
    }

    @Override
    public void openMemberManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SuperiorPlayer islandMember) {
        plugin.getProviders().getMenusProvider().openMemberManage(targetPlayer, previousMenu, islandMember);
    }

    @Override
    public void openMemberManageMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        openMemberManage(superiorPlayer, null, targetPlayer);
    }

    @Override
    public void destroyMemberManage(SuperiorPlayer islandMember) {
        plugin.getProviders().getMenusProvider().destroyMemberManage(islandMember);
    }

    @Override
    public void openMemberRole(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SuperiorPlayer islandMember) {
        plugin.getProviders().getMenusProvider().openMemberRole(targetPlayer, previousMenu, islandMember);
    }

    @Override
    public void openMemberRoleMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        openMemberRole(superiorPlayer, null, targetPlayer);
    }

    @Override
    public void destroyMemberRole(SuperiorPlayer islandMember) {
        plugin.getProviders().getMenusProvider().destroyMemberRole(islandMember);
    }

    @Override
    public void openMembers(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openMembers(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandMembersMenu(SuperiorPlayer superiorPlayer, Island island) {
        openMembers(superiorPlayer, null, island);
    }

    @Override
    public void refreshMembers(Island island) {
        plugin.getProviders().getMenusProvider().refreshMembers(island);
    }

    @Override
    public void openMissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        plugin.getProviders().getMenusProvider().openMissions(targetPlayer, previousMenu);
    }

    @Override
    public void openIslandMainMissionsMenu(SuperiorPlayer superiorPlayer) {
        openMissions(superiorPlayer, null);
    }

    @Override
    public void openMissionsCategory(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, MissionCategory missionCategory) {
        plugin.getProviders().getMenusProvider().openMissionsCategory(targetPlayer, previousMenu, missionCategory);
    }

    @Override
    public void openIslandMissionsMenu(SuperiorPlayer superiorPlayer, boolean islandMissions) {
        // Menu doesn't exist anymore.
    }

    @Override
    public void refreshMissionsCategory(MissionCategory missionCategory) {
        plugin.getProviders().getMenusProvider().refreshMissionsCategory(missionCategory);
    }

    @Override
    public void openPermissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer permissiblePlayer) {
        plugin.getProviders().getMenusProvider().openPermissions(targetPlayer, previousMenu, targetIsland, permissiblePlayer);
    }

    @Override
    public void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer) {
        openPermissions(superiorPlayer, null, island, targetPlayer);
    }

    @Override
    public void openPermissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, PlayerRole permissibleRole) {
        plugin.getProviders().getMenusProvider().openPermissions(targetPlayer, previousMenu, targetIsland, permissibleRole);
    }

    @Override
    public void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, PlayerRole playerRole) {
        openPermissions(superiorPlayer, null, island, playerRole);
    }

    @Override
    public void refreshPermissions(Island island) {
        plugin.getProviders().getMenusProvider().refreshPermissions(island);
    }

    @Override
    public void refreshPermissions(Island island, SuperiorPlayer permissiblePlayer) {
        plugin.getProviders().getMenusProvider().refreshPermissions(island, permissiblePlayer);
    }

    @Override
    public void refreshPermissions(Island island, PlayerRole permissibleRole) {
        plugin.getProviders().getMenusProvider().refreshPermissions(island, permissibleRole);
    }

    @Override
    public void updatePermission(IslandPrivilege islandPrivilege) {
        plugin.getProviders().getMenusProvider().updatePermission(islandPrivilege);
    }

    @Override
    public void openPlayerLanguage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        plugin.getProviders().getMenusProvider().openPlayerLanguage(targetPlayer, previousMenu);
    }

    @Override
    public void openPlayerLanguageMenu(SuperiorPlayer superiorPlayer) {
        openPlayerLanguage(superiorPlayer, null);
    }

    @Override
    public void openSettings(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openSettings(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandSettingsMenu(SuperiorPlayer superiorPlayer, Island island) {
        openSettings(superiorPlayer, null, island);
    }

    @Override
    public void refreshSettings(Island island) {
        plugin.getProviders().getMenusProvider().refreshSettings(island);
    }

    @Override
    public void updateSettings(IslandFlag islandFlag) {
        plugin.getProviders().getMenusProvider().updateSettings(islandFlag);
    }

    @Override
    public void openTopIslands(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SortingType sortingType) {
        plugin.getProviders().getMenusProvider().openTopIslands(targetPlayer, previousMenu, sortingType);
    }

    @Override
    public void openIslandsTopMenu(SuperiorPlayer superiorPlayer, SortingType sortingType) {
        openTopIslands(superiorPlayer, null, sortingType);
    }

    @Override
    public void refreshTopIslands(SortingType sortingType) {
        plugin.getProviders().getMenusProvider().refreshTopIslands(sortingType);
    }

    @Override
    public void openUniqueVisitors(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openUniqueVisitors(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openUniqueVisitorsMenu(SuperiorPlayer superiorPlayer, Island island) {
        openUniqueVisitors(superiorPlayer, null, island);
    }

    @Override
    public void refreshUniqueVisitors(Island island) {
        plugin.getProviders().getMenusProvider().refreshUniqueVisitors(island);
    }

    @Override
    public void openUpgrades(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openUpgrades(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandUpgradeMenu(SuperiorPlayer superiorPlayer, Island island) {
        openUpgrades(superiorPlayer, null, island);
    }

    @Override
    public void refreshUpgrades(Island island) {
        plugin.getProviders().getMenusProvider().refreshUpgrades(island);
    }

    @Override
    public void openValues(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openValues(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandValuesMenu(SuperiorPlayer superiorPlayer, Island island) {
        openValues(superiorPlayer, null, island);
    }

    @Override
    public void refreshValues(Island island) {
        plugin.getProviders().getMenusProvider().refreshValues(island);
    }

    @Override
    public void openVisitors(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openVisitors(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshVisitors(Island island) {
        plugin.getProviders().getMenusProvider().refreshVisitors(island);
    }

    @Override
    public void openIslandVisitorsMenu(SuperiorPlayer superiorPlayer, Island island) {
        openVisitors(superiorPlayer, null, island);
    }

    @Override
    public void openWarpCategories(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        plugin.getProviders().getMenusProvider().openWarpCategories(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshWarpCategories(Island island) {
        plugin.getProviders().getMenusProvider().refreshWarpCategories(island);
    }

    @Override
    public void destroyWarpCategories(Island island) {
        plugin.getProviders().getMenusProvider().destroyWarpCategories(island);
    }

    @Override
    public void openWarpCategoryIconEdit(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        plugin.getProviders().getMenusProvider().openWarpCategoryIconEdit(targetPlayer, previousMenu, targetCategory);
    }

    @Override
    public void openWarpCategoryManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        plugin.getProviders().getMenusProvider().openWarpCategoryManage(targetPlayer, previousMenu, targetCategory);
    }

    @Override
    public void refreshWarpCategoryManage(WarpCategory warpCategory) {
        plugin.getProviders().getMenusProvider().refreshWarpCategoryManage(warpCategory);
    }

    @Override
    public void openWarpIconEdit(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, IslandWarp targetWarp) {
        plugin.getProviders().getMenusProvider().openWarpIconEdit(targetPlayer, previousMenu, targetWarp);
    }

    @Override
    public void openWarpManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, IslandWarp targetWarp) {
        plugin.getProviders().getMenusProvider().openWarpManage(targetPlayer, previousMenu, targetWarp);
    }

    @Override
    public void refreshWarpManage(IslandWarp islandWarp) {
        plugin.getProviders().getMenusProvider().refreshWarpManage(islandWarp);
    }

    @Override
    public void openWarps(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        plugin.getProviders().getMenusProvider().openWarps(targetPlayer, previousMenu, targetCategory);
    }

    @Override
    public void openIslandWarpsMenu(SuperiorPlayer superiorPlayer, Island island) {
        openWarps(superiorPlayer, null, island.getWarpCategories().values()
                .stream().findFirst().orElseGet(() -> island.createWarpCategory("Default Category")));
    }

    @Override
    public void refreshWarps(WarpCategory warpCategory) {
        plugin.getProviders().getMenusProvider().refreshWarps(warpCategory);
    }

    @Override
    public void destroyWarps(WarpCategory warpCategory) {
        plugin.getProviders().getMenusProvider().destroyWarps(warpCategory);
    }

    @Override
    public void registerMenu(Menu<?, ?> menu) {
        Preconditions.checkNotNull(menu, "menu parameter cannot be null");
        Preconditions.checkState(getMenu(menu.getIdentifier()) == null, "There is already a menu with a similar identifier: " + menu.getIdentifier());
        this.registeredMenus.put(menu.getIdentifier().toLowerCase(Locale.ENGLISH), menu);
    }

    public void unregisterMenus() {
        this.registeredMenus.clear();
    }

    @Nullable
    @Override
    public <V extends MenuView<V, A>, A extends ViewArgs> Menu<V, A> getMenu(String identifier) {
        Preconditions.checkNotNull(identifier, "identifier parameter cannot be null");
        return (Menu<V, A>) this.registeredMenus.get(identifier.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Map<String, Menu<?, ?>> getMenus() {
        return Collections.unmodifiableMap(this.registeredMenus);
    }

    @Override
    public <V extends MenuView<V, ?>> MenuLayout.Builder<V> createPatternBuilder() {
        return RegularMenuLayoutImpl.newBuilder();
    }

    @Override
    public <V extends PagedMenuView<V, ?, E>, E> PagedMenuLayout.Builder<V, E> createPagedPatternBuilder() {
        return PagedMenuLayoutImpl.newBuilder();
    }

    @Override
    public <V extends MenuView<V, ?>> MenuTemplateButton.Builder<V> createButtonBuilder(
            Class<?> viewButtonType, MenuTemplateButton.MenuViewButtonCreator<V> viewButtonCreator) {
        return AbstractMenuTemplateButton.newBuilder(viewButtonType, viewButtonCreator);
    }

    @Override
    public <V extends MenuView<V, ?>, E> PagedMenuTemplateButton.Builder<V, E> createPagedButtonBuilder(
            Class<?> viewButtonType, PagedMenuTemplateButton.PagedMenuViewButtonCreator<V, E> viewButtonCreator) {
        return PagedMenuTemplateButtonImpl.newBuilder(viewButtonType, viewButtonCreator);
    }

    @Override
    public MenuParser getParser() {
        return MenuParserImpl.getInstance();
    }

    @Override
    public MenuCommands getMenuCommands() {
        return MenuCommandsImpl.getInstance();
    }

    @Override
    @Deprecated
    public ISuperiorMenu getOldMenuFromView(MenuView<?, ?> menuView) {
        return MenuViewWrapper.fromView(menuView);
    }

}
