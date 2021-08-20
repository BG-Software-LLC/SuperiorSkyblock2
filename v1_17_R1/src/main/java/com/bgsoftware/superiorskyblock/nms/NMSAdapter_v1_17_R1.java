package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.BiomeStorage;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_17_R1 implements NMSAdapter {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(BiomeStorage.class, BiomeBase[].class, "f");
    private static final ReflectField<BiomeStorage> BIOME_STORAGE = new ReflectField<>(
            "org.bukkit.craftbukkit.VERSION.generator.CustomChunkGenerator$CustomBiomeGrid", BiomeStorage.class, "biome");
    private static final ReflectField<Integer> PORTAL_TICKS = new ReflectField<>(Entity.class, int.class, "ah");

    private static final ReflectMethod<Boolean> ANIMAL_BREED_ITEM = new ReflectMethod<>(EntityAnimal.class, "n", net.minecraft.world.item.ItemStack.class);

    @Override
    public void registerCommand(BukkitCommand command) {
        ((CraftServer) plugin.getServer()).getCommandMap().register("superiorskyblock2", command);
    }

    @Override
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        IBlockData blockData = ((CraftBlockData) chunkSnapshot.getBlockData(x, y, z)).getState();
        Material type = chunkSnapshot.getBlockType(x, y, z);
        short data = (short) (Block.getCombinedId(blockData) >> 12 & 15);

        Location location = new Location(
                Bukkit.getWorld(chunkSnapshot.getWorldName()),
                (chunkSnapshot.getX() << 4) + x,
                y,
                (chunkSnapshot.getZ() << 4) + z
        );

        return Key.of(Key.of(type, data), location);
    }

    @Override
    public int getSpawnerDelay(CreatureSpawner creatureSpawner) {
        Location location = creatureSpawner.getLocation();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)((CraftWorld) location.getWorld()).getHandle().getTileEntity(blockPosition);
        return mobSpawner == null ? 0 : mobSpawner.getSpawner().d;
    }

    @Override
    public void setSpawnerDelay(CreatureSpawner creatureSpawner, int spawnDelay) {
        Location location = creatureSpawner.getLocation();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)((CraftWorld) location.getWorld()).getHandle().getTileEntity(blockPosition);
        mobSpawner.getSpawner().d = spawnDelay;
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        try {
            if(!plugin.getSettings().worldBordersEnabled)
                return;

            boolean disabled = !superiorPlayer.hasWorldBorderEnabled();

            WorldBorder worldBorder;

            if(disabled || island == null || (!plugin.getSettings().spawnWorldBorder && island.isSpawn())){
                worldBorder = ((CraftWorld) superiorPlayer.getWorld()).getHandle().getWorldBorder();
            }
            
            else {
                worldBorder = new WorldBorder();

                worldBorder.world = ((CraftWorld) superiorPlayer.getWorld()).getHandle();
                worldBorder.setSize((island.getIslandSize() * 2) + 1);

                org.bukkit.World.Environment environment = superiorPlayer.getWorld().getEnvironment();

                Location center = island.getCenter(environment);
                worldBorder.setCenter(center.getX(), center.getZ());

                switch (superiorPlayer.getBorderColor()) {
                    case GREEN:
                        worldBorder.transitionSizeBetween(worldBorder.getSize() - 0.1D, worldBorder.getSize(), Long.MAX_VALUE);
                        break;
                    case RED:
                        worldBorder.transitionSizeBetween(worldBorder.getSize(), worldBorder.getSize() - 1.0D, Long.MAX_VALUE);
                        break;
                }
            }

            ClientboundInitializeBorderPacket packetPlayOutWorldBorder = new ClientboundInitializeBorderPacket(worldBorder);
            ((CraftPlayer) superiorPlayer.asPlayer()).getHandle().b.sendPacket(packetPlayOutWorldBorder);
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer) {
        EntityPlayer entityPlayer = ((CraftPlayer) superiorPlayer.asPlayer()).getHandle();
        Optional<Property> optional = entityPlayer.getProfile().getProperties().get("textures").stream().findFirst();
        optional.ifPresent(property -> setSkinTexture(superiorPlayer, property));
    }

    @Override
    public Object getCustomHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        return new CustomTileEntityHopper(defaultHolder, title);
    }

    @Override
    public void clearInventory(OfflinePlayer offlinePlayer) {
        if(offlinePlayer.isOnline() || offlinePlayer instanceof Player){
            Player player = offlinePlayer instanceof Player ? (Player) offlinePlayer : offlinePlayer.getPlayer();
            player.getInventory().clear();
            player.getEnderChest().clear();
            return;
        }

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName());

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(World.f);
        EntityPlayer entity = new EntityPlayer(server, worldServer, profile);
        Player targetPlayer = entity.getBukkitEntity();

        targetPlayer.loadData();

        clearInventory(targetPlayer);

        //Setting the entity to the spawn location
        Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(org.bukkit.World.Environment.NORMAL);
        entity.t = ((CraftWorld) spawnLocation.getWorld()).getHandle();
        entity.setPositionRotation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        targetPlayer.saveData();
    }

    @Override
    public void playGeneratorSound(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        world.triggerEffect(1501, blockPosition, 0);
    }

    @Override
    public void playBreakAnimation(org.bukkit.block.Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));
    }

    @Override
    public void playPlaceSound(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        World world = ((CraftWorld) location.getWorld()).getHandle();
        SoundEffectType soundEffectType = world.getType(blockPosition).getStepSound();

        world.playSound(null, blockPosition, soundEffectType.getPlaceSound(),
                SoundCategory.e ,(soundEffectType.getVolume() + 1.0F) / 2.0F, soundEffectType.getPitch() * 0.8F);
    }

    @Override
    public void setBiome(ChunkGenerator.BiomeGrid biomeGrid, Biome biome) {
        BiomeStorage biomeStorage = BIOME_STORAGE.get(biomeGrid);
        BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(biomeStorage);

        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase((IRegistry<BiomeBase>) biomeStorage.e, biome);

        if(biomeBases == null)
            return;

        Arrays.fill(biomeBases, biomeBase);
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        return block.getBlockData();
    }

    @Override
    public Enchantment getGlowEnchant() {
        //noinspection NullableProblems
        return new Enchantment(NamespacedKey.minecraft("superior_glowing_enchant")) {
            @Override
            public String getName() {
                return "SuperiorSkyblockGlow";
            }

            @Override
            public int getMaxLevel() {
                return 1;
            }

            @Override
            public int getStartLevel() {
                return 0;
            }

            @Override
            public EnchantmentTarget getItemTarget() {
                return null;
            }

            @Override
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(ItemStack itemStack) {
                return true;
            }

            @Override
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
            }

            public Component displayName(int i) {
                return null;
            }

            public boolean isTradeable() {
                return false;
            }

            public boolean isDiscoverable() {
                return false;
            }

            public EnchantmentRarity getRarity() {
                return null;
            }

            public float getDamageIncrease(int i, EntityCategory entityCategory) {
                return 0;
            }

            public Set<EquipmentSlot> getActiveSlots() {
                return null;
            }
        };
    }

    @Override
    public boolean isChunkEmpty(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        return Arrays.stream(chunk.getSections()).allMatch(chunkSection -> chunkSection == null || chunkSection.c());
    }

    @Override
    public ItemStack[] getEquipment(EntityEquipment entityEquipment) {
        ItemStack[] itemStacks = new ItemStack[7];

        itemStacks[0] = new ItemStack(Material.ARMOR_STAND);
        itemStacks[1] = entityEquipment.getItemInMainHand();
        itemStacks[2] = entityEquipment.getItemInOffHand();
        itemStacks[3] = entityEquipment.getHelmet();
        itemStacks[4] = entityEquipment.getChestplate();
        itemStacks[5] = entityEquipment.getLeggings();
        itemStacks[6] = entityEquipment.getBoots();

        return itemStacks;
    }

    @Override
    public double[] getTPS() {
        //noinspection deprecation
        return MinecraftServer.getServer().recentTps;
    }

    @Override
    public void addPotion(PotionMeta potionMeta, PotionEffect potionEffect) {
        if(!potionMeta.hasCustomEffects())
            potionMeta.setColor(potionEffect.getType().getColor());
        potionMeta.addCustomEffect(potionEffect, true);
    }

    @Override
    public String getMinecraftKey(ItemStack itemStack) {
        return IRegistry.Z.getKey(CraftItemStack.asNMSCopy(itemStack).getItem()).toString();
    }

    @Override
    public CompoundTag getNMSCompound(ItemStack bukkitItem) {
        return CompoundTag.fromNBT(CraftItemStack.asNMSCopy(bukkitItem).save(new NBTTagCompound()));
    }

    @Override
    public boolean isAnimalFood(ItemStack itemStack, Animals animals) {
        EntityAnimal entityAnimal = ((CraftAnimals) animals).getHandle();
        if(ANIMAL_BREED_ITEM.isValid()){
            return ANIMAL_BREED_ITEM.invoke(entityAnimal, CraftItemStack.asNMSCopy(itemStack));
        }
        else {
            return entityAnimal.isBreedItem(CraftItemStack.asNMSCopy(itemStack));
        }
    }

    @Override
    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, duration, fadeOut);
    }

    @Override
    public void setCustomModel(ItemMeta itemMeta, int customModel) {
        itemMeta.setCustomModelData(customModel);
    }

    @Override
    public int getPortalTicks(org.bukkit.entity.Entity entity) {
        return PORTAL_TICKS.get(((CraftEntity) entity).getHandle());
    }

    private static class CustomTileEntityHopper extends TileEntityHopper {

        private final InventoryHolder holder;

        CustomTileEntityHopper(InventoryHolder holder, String title){
            super(BlockPosition.b, Blocks.a.getBlockData());
            this.holder = holder;
            this.setCustomName(new ChatMessage(title));
        }

        @Override
        public InventoryHolder getOwner() {
            return holder;
        }
    }

}
