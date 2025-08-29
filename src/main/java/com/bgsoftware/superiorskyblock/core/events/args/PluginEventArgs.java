package com.bgsoftware.superiorskyblock.core.events.args;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.player.container.PlayersContainer;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class PluginEventArgs {

    private PluginEventArgs() {

    }

    public static class Empty extends PluginEventArgs {

        public static final Empty INSTANCE = new Empty();

        private Empty() {

        }

    }

    public static class AttemptPlayerSendMessage extends PluginEventArgs {

        public SuperiorPlayer receiver;
        public String messageType;
        public Object[] args;

    }

    public static class BlockStack extends StackedBlockChangeCountArgs {

    }

    public static class BlockUnstack extends StackedBlockChangeCountArgs {

    }

    public static class IslandBan extends IslandDoActionArgs {

        public SuperiorPlayer targetPlayer;

    }

    public static class IslandBankDeposit extends IslandBankTransactionArgs {

    }

    public static class IslandBankWithdraw extends IslandBankTransactionArgs {

    }

    public static class IslandBiomeChange extends IslandDoActionArgs {

        public Biome biome;

    }

    public static class IslandChangeBankLimit extends IslandDoActionArgs {

        public BigDecimal bankLimit;

    }

    public static class IslandChangeBlockLimit extends IslandDoActionArgs {

        public Key block;
        public int blockLimit;

    }

    public static class IslandChangeBorderSize extends IslandDoActionArgs {

        public int borderSize;

    }

    public static class IslandChangeCoopLimit extends IslandDoActionArgs {

        public int coopLimit;

    }

    public static class IslandChangeCropGrowth extends IslandDoActionArgs {

        public double cropGrowth;

    }

    public static class IslandChangeDescription extends IslandDoActionArgs {

        public String description;

    }

    public static class IslandChangeDiscord extends IslandDoActionArgs {

        public String discord;

    }

    public static class IslandChangeEffectLevel extends IslandDoActionArgs {

        public PotionEffectType effectType;
        public int effectLevel;

    }

    public static class IslandChangeEntityLimit extends IslandDoActionArgs {

        public Key entity;
        public int entityLimit;

    }

    public static class IslandChangeGeneratorRate extends IslandDoActionArgs {

        public Key block;
        public Dimension dimension;
        public int generatorRate;

    }

    public static class IslandChangeLevelBonus extends IslandDoActionArgs {

        public IslandChangeLevelBonusEvent.Reason reason;
        public BigDecimal levelBonus;

    }

    public static class IslandChangeMembersLimit extends IslandDoActionArgs {

        public int membersLimit;

    }

    public static class IslandChangeMobDrops extends IslandDoActionArgs {

        public double mobDrops;

    }

    public static class IslandChangePaypal extends IslandDoActionArgs {

        public String paypal;

    }

    public static class IslandChangePlayerPrivilege extends IslandDoActionArgs {

        public SuperiorPlayer privilegedPlayer;
        public boolean privilegeEnabled;

    }

    public static class IslandChangeRoleLimit extends IslandDoActionArgs {

        public PlayerRole playerRole;
        public int roleLimit;

    }

    public static class IslandChangeSpawnerRates extends IslandDoActionArgs {

        public double spawnerRates;

    }

    public static class IslandChangeWarpCategoryIcon extends WarpCategoryDoActionArgs {

        public ItemStack icon;

    }

    public static class IslandChangeWarpCategorySlot extends WarpCategoryDoActionArgs {

        public int slot;
        public int maxSlot;

    }

    public static class IslandChangeWarpIcon extends IslandWarpDoActionArgs {

        public ItemStack icon;

    }

    public static class IslandChangeWarpLocation extends IslandWarpDoActionArgs {

        public Location location;

    }

    public static class IslandChangeWarpsLimit extends IslandDoActionArgs {

        public int warpsLimit;

    }

    public static class IslandChangeWorthBonus extends IslandDoActionArgs {

        public IslandChangeWorthBonusEvent.Reason reason;
        public BigDecimal worthBonus;

    }

    public static class IslandChangeRolePrivilege extends IslandDoActionArgs {

        public PlayerRole playerRole;

    }

    public static class IslandChat extends IslandDoActionArgs {

        public String message;

    }

    public static class IslandChunkReset extends PluginEventArgs {

        public Island island;
        public ChunkPosition chunkPosition;

    }

    public static class IslandClearFlags extends IslandDoActionArgs {

    }

    public static class IslandClearGeneratorRates extends IslandDoActionArgs {

        public Dimension dimension;

    }

    public static class IslandClearPlayerPrivileges extends IslandDoActionArgs {

        public SuperiorPlayer privilegedPlayer;

    }

    public static class IslandClearRatings extends IslandDoActionArgs {

    }

    public static class IslandClearRolesPrivileges extends IslandDoActionArgs {

    }

    public static class IslandClose extends IslandDoActionArgs {

    }

    public static class IslandCloseWarp extends IslandWarpDoActionArgs {

    }

    public static class IslandCoopPlayer extends IslandDoActionArgs {

        public SuperiorPlayer targetPlayer;

    }

    public static class IslandCreate extends IslandDoActionArgs {

        public String schematicName;
        public boolean canTeleport = true;

    }

    public static class IslandCreateWarpCategory extends IslandDoActionArgs {

        public String categoryName;

    }

    public static class IslandCreateWarp extends IslandDoActionArgs {

        public String warpName;
        public boolean openToPublic;
        public Location location;
        public WarpCategory warpCategory;

    }

    public static class IslandDeleteWarp extends IslandWarpDoActionArgs {

    }

    public static class IslandDisableFlag extends IslandDoActionArgs {

        public IslandFlag islandFlag;

    }

    public static class IslandDisband extends IslandDoActionArgs {

    }

    public static class IslandEnableFlag extends IslandDoActionArgs {

        public IslandFlag islandFlag;

    }

    public static class IslandEnter extends IslandDoActionArgs {

        public IslandEnterEvent.EnterCause enterCause;

    }

    public static class IslandEnterPortal extends IslandDoActionArgs {

        public PortalType portalType;
        public Dimension destination;
        public Schematic schematic;
        public boolean ignoreInvalidSchematic;

    }

    public static class IslandEnterProtected extends IslandEnter {

    }

    public static class IslandGenerateBlock extends PluginEventArgs {

        public Island island;
        public Location location;
        public Key block;
        public boolean placeBlock = true;

    }

    public static class IslandInvite extends IslandDoActionArgs {

        public SuperiorPlayer targetPlayer;

    }

    public static class IslandJoin extends IslandDoActionArgs {

        public IslandJoinEvent.Cause joinCause;

    }

    public static class IslandKick extends IslandDoActionArgs {

        public SuperiorPlayer targetPlayer;

    }

    public static class IslandLeave extends IslandDoActionArgs {

        public IslandLeaveEvent.LeaveCause leaveCause;
        public Location location;

    }

    public static class IslandLeaveProtected extends IslandLeave {

    }

    public static class IslandLockWorld extends IslandDoActionArgs {

        public Dimension dimension;

    }

    public static class IslandOpen extends IslandDoActionArgs {

    }

    public static class IslandOpenWarp extends IslandWarpDoActionArgs {

    }

    public static class IslandQuit extends IslandDoActionArgs {

    }

    public static class IslandRate extends IslandDoActionArgs {

        public SuperiorPlayer ratingPlayer;
        public Rating rating;

    }

    public static class IslandRemoveBlockLimit extends IslandDoActionArgs {

        public Key block;

    }

    public static class IslandRemoveEffect extends IslandDoActionArgs {

        public PotionEffectType effectType;

    }

    public static class IslandRemoveGeneratorRate extends IslandDoActionArgs {

        public Key block;
        public Dimension dimension;

    }

    public static class IslandRemoveRating extends IslandDoActionArgs {

        public SuperiorPlayer ratingPlayer;

    }

    public static class IslandRemoveRoleLimit extends IslandDoActionArgs {

        public PlayerRole playerRole;

    }

    public static class IslandRemoveVisitorHome extends IslandDoActionArgs {

    }

    public static class IslandRename extends IslandDoActionArgs {

        public String islandName;

    }

    public static class IslandRenameWarpCategory extends WarpCategoryDoActionArgs {

        public String categoryName;

    }

    public static class IslandRenameWarp extends IslandWarpDoActionArgs {

        public String warpName;

    }

    public static class IslandRestrictMove extends IslandDoActionArgs {

        public IslandRestrictMoveEvent.RestrictReason restrictReason;

    }

    public static class IslandSchematicPaste extends IslandDoActionArgs {

        public String schematicName;
        public Location location;

    }

    public static class IslandSetHome extends IslandDoActionArgs {

        public Location islandHome;
        public IslandSetHomeEvent.Reason reason;

    }

    public static class IslandSetVisitorHome extends IslandDoActionArgs {

        public Location islandVisitorHome;

    }

    public static class IslandTransfer extends IslandDoActionArgs {

        public SuperiorPlayer previousOwner;

    }

    public static class IslandUnban extends IslandDoActionArgs {

        public SuperiorPlayer unbannedPlayer;

    }

    public static class IslandUncoopPlayer extends IslandDoActionArgs {

        public SuperiorPlayer targetPlayer;
        public IslandUncoopPlayerEvent.UncoopReason uncoopReason;

    }

    public static class IslandUnlockWorld extends IslandDoActionArgs {

        public Dimension dimension;

    }

    public static class IslandUpgrade extends IslandDoActionArgs {

        public Upgrade upgrade;
        public UpgradeLevel nextLevel;
        public List<String> commands;
        public IslandUpgradeEvent.Cause upgradeCause;
        public UpgradeCost upgradeCost;

    }

    public static class IslandWarpTeleport extends IslandWarpDoActionArgs {

    }

    public static class IslandWorldReset extends IslandDoActionArgs {

        public Dimension dimension;

    }

    public static class IslandWorthCalculated extends IslandDoActionArgs {

        public BigDecimal islandLevel;
        public BigDecimal islandWorth;

    }

    public static class IslandWorthUpdate extends PluginEventArgs {

        public Island island;
        public BigDecimal oldWorth;
        public BigDecimal oldLevel;
        public BigDecimal newWorth;
        public BigDecimal newLevel;

    }

    public static class MissionComplete extends MissionArgs {

        public List<ItemStack> itemRewards;
        public List<String> commandRewards;

    }

    public static class MissionReset extends MissionArgs {

    }

    public static class PlayerChangeBorderColor extends PlayerDoActionArgs {

        public BorderColor borderColor;

    }

    public static class PlayerChangeLanguage extends PlayerDoActionArgs {

        public Locale language;

    }

    public static class PlayerChangeName extends PlayerDoActionArgs {

        public String newName;

    }

    public static class PlayerChangeRole extends PlayerDoActionArgs {

        public PlayerRole newRole;

    }

    public static class PlayerCloseMenu extends PlayerDoActionArgs {

        public MenuView<?, ?> menuView;
        public MenuView<?, ?> newMenuView;

    }

    public static class PlayerOpenMenu extends PlayerDoActionArgs {

        public MenuView<?, ?> menuView;

    }

    public static class PlayerReplace extends PlayerDoActionArgs {

        public SuperiorPlayer newPlayer;

    }

    public static class PlayerToggleBlocksStacker extends PlayerDoActionArgs {

    }

    public static class PlayerToggleBorder extends PlayerDoActionArgs {

    }

    public static class PlayerToggleBypass extends PlayerDoActionArgs {

    }

    public static class PlayerToggleFly extends PlayerDoActionArgs {

    }

    public static class PlayerTogglePanel extends PlayerDoActionArgs {

    }

    public static class PlayerToggleSpy extends PlayerDoActionArgs {

    }

    public static class PlayerToggleTeamChat extends PlayerDoActionArgs {

    }

    public static class PluginInitialized extends PluginDoActionArgs {

    }

    public static class PluginInitialize extends PluginDoActionArgs {

        public IslandsContainer islandsContainer;
        public PlayersContainer playersContainer;

    }

    public static class PluginLoadData extends PluginDoActionArgs {

    }

    public static class PostIslandCreate extends IslandDoActionArgs {

    }

    public static class PreIslandCreate extends PluginEventArgs {

        public SuperiorPlayer superiorPlayer;
        public String islandName;

    }

    public static class SendMessage extends PluginEventArgs {

        public CommandSender receiver;
        public String messageType;
        public IMessageComponent messageComponent;
        public Object[] args;

    }

    private static class StackedBlockChangeCountArgs extends PluginEventArgs {

        public Block block;
        public Player player;
        public int originalCount;
        public int newCount;

    }

    private static class IslandBankTransactionArgs extends PluginEventArgs {

        public Island island;
        public SuperiorPlayer superiorPlayer;
        public BigDecimal amount;
        public String failureReason;

    }

    private static class IslandDoActionArgs extends PluginEventArgs {

        public Island island;
        public SuperiorPlayer superiorPlayer;

    }

    private static class WarpCategoryDoActionArgs extends IslandDoActionArgs {

        public WarpCategory warpCategory;

    }

    private static class IslandWarpDoActionArgs extends IslandDoActionArgs {

        public IslandWarp islandWarp;

    }

    private static class MissionArgs extends PluginEventArgs {

        public SuperiorPlayer superiorPlayer;
        public IMissionsHolder missionsHolder;
        public Mission<?> mission;

    }

    private static class PlayerDoActionArgs extends PluginEventArgs {

        public SuperiorPlayer superiorPlayer;

    }

    private static class PluginDoActionArgs extends PluginEventArgs {

        public SuperiorSkyblock plugin;

    }

}
