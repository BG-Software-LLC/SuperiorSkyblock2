package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBiomes;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class BiomeButton extends SuperiorMenuButton<MenuBiomes> {

    private final SoundWrapper accessSound;
    private final List<String> accessCommands;
    private final TemplateItem lackPermissionItem;
    private final List<String> lackPermissionCommands;
    private final Biome biome;

    private BiomeButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                        String requiredPermission, SoundWrapper lackPermissionSound,
                        TemplateItem lackPermissionItem, List<String> lackPermissionCommands, Biome biome) {
        super(buttonItem, null, null, requiredPermission, lackPermissionSound);
        this.accessSound = clickSound;
        this.accessCommands = commands == null ? Collections.emptyList() : Collections.unmodifiableList(commands);
        this.lackPermissionItem = lackPermissionItem;
        this.lackPermissionCommands = lackPermissionCommands == null ? Collections.emptyList() :
                Collections.unmodifiableList(lackPermissionCommands);
        this.biome = biome;
    }

    public List<String> getLackPermissionCommands() {
        return lackPermissionCommands;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuBiomes superiorMenu) {
        ItemStack buttonItem = null;

        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();

        if (requiredPermission == null || inventoryViewer.hasPermission(requiredPermission)) {
            buttonItem = super.getButtonItem(superiorMenu);
        } else if (lackPermissionItem != null) {
            buttonItem = lackPermissionItem.build(inventoryViewer);
        }

        if (buttonItem == null || !MenuBiomes.currentBiomeGlow)
            return buttonItem;

        Island island = inventoryViewer.getIsland();

        if (island == null || island.getBiome() != biome)
            return buttonItem;

        return new ItemBuilder(buttonItem).withEnchant(EnchantsUtils.getGlowEnchant(), 1).build();
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuBiomes superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        EventResult<Biome> event = EventsCaller.callIslandBiomeChangeEvent(clickedPlayer,
                superiorMenu.getTargetIsland(), this.biome);

        if (event.isCancelled()) {
            if (lackPermissionSound != null)
                lackPermissionSound.playSound(clickEvent.getWhoClicked());
            return;
        }

        if (accessSound != null)
            accessSound.playSound(clickEvent.getWhoClicked());

        accessCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));

        superiorMenu.getTargetIsland().setBiome(event.getResult());
        Message.CHANGED_BIOME.send(clickedPlayer, event.getResult().name().toLowerCase());

        Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, BiomeButton, MenuBiomes> {

        private final Biome biome;
        private TemplateItem noAccessItem = null;
        private List<String> noAccessCommands = null;

        public Builder(Biome biome) {
            this.biome = biome;
        }

        public Builder setAccessItem(TemplateItem accessItem) {
            this.buttonItem = accessItem;
            return this;
        }

        public Builder setNoAccessItem(TemplateItem noAccessItem) {
            this.noAccessItem = noAccessItem;
            return this;
        }

        public Builder setAccessSound(SoundWrapper accessSound) {
            this.clickSound = accessSound;
            return this;
        }

        public Builder setNoAccessSound(SoundWrapper noAccessSound) {
            this.lackPermissionSound = noAccessSound;
            return this;
        }

        public Builder setAccessCommands(List<String> accessCommands) {
            this.commands = accessCommands;
            return this;
        }

        public Builder setNoAccessCommands(List<String> noAccessCommands) {
            this.noAccessCommands = noAccessCommands;
            return this;
        }

        @Override
        public BiomeButton build() {
            return new BiomeButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, noAccessItem, noAccessCommands, biome);
        }

    }

}
