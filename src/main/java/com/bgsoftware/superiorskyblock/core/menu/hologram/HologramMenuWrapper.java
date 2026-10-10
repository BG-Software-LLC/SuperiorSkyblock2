package com.bgsoftware.superiorskyblock.core.menu.hologram;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.menu.button.click.ButtonClickContextImpl;
import com.bgsoftware.superiorskyblock.core.menu.layout.RegularHologramMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.nms.NMSHologramMenus;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HologramMenuWrapper<V extends MenuView<V, ?>> implements NMSHologramMenus.InputListener {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    // All of these maps are only accessed from the main thread.
    private static final Map<UUID, HologramMenuWrapper<?>> MENUS_BY_PLAYERS = new HashMap<>();

    /* How far can the cursor go past the edges of the panel. */
    private static final float EDGE_MARGIN = 0.2f;
    /* The distance between the cursor and the tooltip that is shown above it. */
    private static final float TOOLTIP_OFFSET = 0.15f;
    private static final int TRANSPARENT_COLOR = 0;
    private static final long CLICK_DELAY = 250L;
    /* The maximum pitch of the viewer that is used as the center of the panel. */
    private static final float MAX_BASE_PITCH = 40f;
    private static final double MAX_MOVE_DISTANCE_SQUARED = 1D;

    private final UUID playerUUID;
    private final V menuView;
    private final RegularHologramMenuLayoutImpl<V> menuLayout;
    private final HologramMenuStyle style;

    private final List<ButtonArea> buttonAreas = new ArrayList<>();
    @Nullable
    private ButtonArea hoveredButton;

    @Nullable
    private NMSHologramMenus.Renderer renderer;
    private Location startLocation;
    private float baseYaw;
    private float basePitch;
    private float maxCursorX;
    private float maxCursorY;
    private int cursorId;
    /* The identifier of the tooltip, or -1 if none of the buttons has a tooltip. */
    private int tooltipId = -1;
    private long lastClick;

    @Nullable
    public static HologramMenuWrapper<?> getByPlayer(UUID playerUUID) {
        return MENUS_BY_PLAYERS.get(playerUUID);
    }

    public static void tickAll() {
        if (MENUS_BY_PLAYERS.isEmpty())
            return;

        for (HologramMenuWrapper<?> hologramMenu : new ArrayList<>(MENUS_BY_PLAYERS.values()))
            hologramMenu.tick();
    }

    public static void closeAll() {
        for (HologramMenuWrapper<?> hologramMenu : new ArrayList<>(MENUS_BY_PLAYERS.values()))
            hologramMenu.close(false);
    }

    @SuppressWarnings("unchecked")
    public HologramMenuWrapper(V menuView) {
        this.playerUUID = menuView.getInventoryViewer().getUniqueId();
        this.menuView = menuView;
        this.menuLayout = (RegularHologramMenuLayoutImpl<V>) menuView.getMenu().getLayout();
        this.style = this.menuLayout.getStyle();
    }

    public V getMenuView() {
        return this.menuView;
    }

    public boolean isOpen() {
        return this.renderer != null;
    }

    public boolean open(Player player) {
        NMSHologramMenus nmsHologramMenus = plugin.getNMSHologramMenus().orElse(null);
        if (nmsHologramMenus == null)
            return false;

        if (this.renderer != null) {
            // Already opened, we only need to draw the menu again.
            this.renderer.clear();
        } else {
            HologramMenuWrapper<?> oldHologramMenu = MENUS_BY_PLAYERS.get(this.playerUUID);
            if (oldHologramMenu != null && oldHologramMenu.renderer != null) {
                if (oldHologramMenu.style.getDistance() == this.style.getDistance()) {
                    // We take over the renderer of the old menu, so the view of the player is not changed.
                    takeOver(oldHologramMenu);
                } else {
                    // The renderer of the old menu draws its elements in a different distance.
                    oldHologramMenu.close(false);
                }
            }

            if (this.renderer == null) {
                this.startLocation = player.getLocation();
                this.baseYaw = this.startLocation.getYaw();
                this.basePitch = Math.max(-MAX_BASE_PITCH, Math.min(MAX_BASE_PITCH, this.startLocation.getPitch()));

                Location origin = player.getEyeLocation();
                origin.setPitch(0f);

                this.renderer = nmsHologramMenus.createRenderer(player, origin, this.style.getDistance(), new InputDelegate(this.playerUUID));
            }
        }

        MENUS_BY_PLAYERS.put(this.playerUUID, this);

        draw(player);

        this.lastClick = System.currentTimeMillis();

        return true;
    }

    private void takeOver(HologramMenuWrapper<?> oldHologramMenu) {
        this.renderer = oldHologramMenu.renderer;
        this.startLocation = oldHologramMenu.startLocation;
        this.baseYaw = oldHologramMenu.baseYaw;
        this.basePitch = oldHologramMenu.basePitch;

        oldHologramMenu.renderer = null;
        oldHologramMenu.hoveredButton = null;

        this.renderer.clear();
    }

    private void draw(Player player) {
        HologramMenuStyle style = this.style;

        String title = this.menuLayout.getTitle(this.menuView);
        List<String> bodyLines = this.menuLayout.getBodyLines(player);

        this.buttonAreas.clear();
        this.hoveredButton = null;
        this.tooltipId = -1;

        // We first calculate the sizes of all the sections of the menu.

        List<ButtonsRow> rows = new ArrayList<>();
        float buttonsWidth = 0f;
        float buttonsHeight = 0f;
        for (List<Integer> rowSlots : this.menuLayout.getRows()) {
            ButtonsRow row = createButtonsRow(rowSlots);
            if (row == null)
                continue;

            if (!rows.isEmpty())
                buttonsHeight += style.getRowsSpacing();
            buttonsHeight += row.height;
            buttonsWidth = Math.max(buttonsWidth, row.width);
            rows.add(row);
        }

        boolean hasTitle = !title.isEmpty();

        float contentHeight = 0f;
        int sectionsCount = 0;
        if (hasTitle) {
            contentHeight += style.getTitleHeight();
            ++sectionsCount;
        }
        if (!bodyLines.isEmpty()) {
            contentHeight += style.getBodyLineHeight() * bodyLines.size();
            ++sectionsCount;
        }
        if (!rows.isEmpty()) {
            contentHeight += buttonsHeight;
            ++sectionsCount;
        }
        if (sectionsCount > 1)
            contentHeight += style.getSectionsSpacing() * (sectionsCount - 1);

        float panelWidth = Math.max(style.getMinWidth(), buttonsWidth + style.getPadding() * 2);
        float panelHeight = contentHeight + style.getPadding() * 2;

        this.maxCursorX = panelWidth / 2 + EDGE_MARGIN;
        this.maxCursorY = panelHeight / 2 + EDGE_MARGIN;

        ItemStack backgroundItem = style.getBackgroundItem();
        if (backgroundItem != null) {
            this.renderer.addItem(backgroundItem, 0f, 0f, panelWidth, panelHeight);
        } else {
            this.renderer.addRectangle(0f, 0f, panelWidth, panelHeight, style.getBackgroundColor());
        }

        // The sections are drawn from the top of the panel to its bottom.
        float currentY = contentHeight / 2;

        if (hasTitle) {
            this.renderer.addText(title, 0f, currentY - style.getTitleHeight() / 2, style.getTitleScale(), style.isTitleShadowed());
            currentY -= style.getTitleHeight() + style.getSectionsSpacing();
        }

        if (!bodyLines.isEmpty()) {
            for (String bodyLine : bodyLines) {
                this.renderer.addText(bodyLine, 0f, currentY - style.getBodyLineHeight() / 2, style.getBodyScale(), style.isBodyShadowed());
                currentY -= style.getBodyLineHeight();
            }
            currentY -= style.getSectionsSpacing();
        }

        boolean hasTooltips = false;

        for (ButtonsRow row : rows) {
            // The buttons of the row are drawn from left to right, with the row being centered in the panel.
            float currentX = -row.width / 2;
            float y = currentY - row.height / 2;
            for (ButtonArea buttonArea : row.buttons) {
                buttonArea.x = currentX + buttonArea.width / 2;
                buttonArea.y = y;
                currentX += buttonArea.width + style.getButtonsSpacing();

                if (buttonArea.hologramButton == null)
                    continue;

                drawButton(buttonArea);
                this.buttonAreas.add(buttonArea);
                hasTooltips |= buttonArea.tooltip != null;
            }
            currentY -= row.height + style.getRowsSpacing();
        }

        // The tooltip and the cursor are added last, so they are rendered in front of everything else.

        if (hasTooltips)
            this.tooltipId = this.renderer.addTooltip("", 0f, 0f, style.getTooltipScale(), TRANSPARENT_COLOR);

        this.cursorId = this.renderer.addText(style.getCursorText(), getCursorX(player), getCursorY(player), style.getCursorScale(), true);

        this.renderer.show();
    }

    /**
     * Create a row of buttons without positioning them.
     *
     * @return The row, or null if it has no buttons to draw.
     */
    @Nullable
    private ButtonsRow createButtonsRow(List<Integer> rowSlots) {
        HologramButton defaultButton = this.style.getDefaultButton();

        ButtonsRow row = new ButtonsRow();
        boolean hasButtons = false;

        for (int slot : rowSlots) {
            MenuTemplateButton<V> templateButton = this.menuLayout.getButton(slot);

            ItemStack buttonItem = templateButton.createViewButton(this.menuView).createViewItem();
            if (buttonItem != null && buttonItem.getType() == Material.AIR)
                buttonItem = null;

            HologramButton hologramButton = templateButton.getButtonHologram();

            ButtonArea buttonArea;
            if (hologramButton != null) {
                buttonArea = new ButtonArea(slot, hologramButton, buttonItem, hologramButton.getWidth(), hologramButton.getHeight());
            } else if (buttonItem != null) {
                // Buttons that only have an item are squares.
                buttonArea = new ButtonArea(slot, defaultButton, buttonItem, defaultButton.getHeight(), defaultButton.getHeight());
            } else {
                // Slots without buttons are left as empty space in the row.
                buttonArea = new ButtonArea(slot, null, null, defaultButton.getHeight(), defaultButton.getHeight());
            }

            if (buttonArea.hologramButton != null) {
                hasButtons = true;
                if (this.style.isTooltipEnabled())
                    buttonArea.tooltip = createTooltip(buttonArea.hologramButton, buttonItem);
            }

            if (!row.buttons.isEmpty())
                row.width += this.style.getButtonsSpacing();
            row.width += buttonArea.width;
            row.height = Math.max(row.height, buttonArea.height);
            row.buttons.add(buttonArea);
        }

        return hasButtons ? row : null;
    }

    private void drawButton(ButtonArea buttonArea) {
        HologramButton hologramButton = buttonArea.hologramButton;

        buttonArea.rectangleId = this.renderer.addRectangle(buttonArea.x, buttonArea.y,
                buttonArea.width, buttonArea.height, hologramButton.getColor());

        String label = hologramButton.getLabel();
        boolean hasLabel = label != null && !label.isEmpty();

        float labelX = buttonArea.x;

        if (buttonArea.item != null) {
            float itemX = buttonArea.x;
            if (hasLabel) {
                // The item is drawn in a square in the left side of the button, and the label in the rest of it.
                itemX += (buttonArea.height - buttonArea.width) / 2;
                labelX += buttonArea.height / 2;
            }
            this.renderer.addItem(buttonArea.item, itemX, buttonArea.y, hologramButton.getItemScale(), hologramButton.getItemScale());
        }

        if (hasLabel)
            this.renderer.addText(label, labelX, buttonArea.y, hologramButton.getLabelScale(), hologramButton.isLabelShadowed());
    }

    @Nullable
    private static String createTooltip(HologramButton hologramButton, @Nullable ItemStack buttonItem) {
        List<String> tooltip = hologramButton.getTooltip();

        if (tooltip == null) {
            // By default, the tooltip is the same as the one the item has in inventories.
            ItemMeta itemMeta = buttonItem == null ? null : buttonItem.getItemMeta();
            if (itemMeta == null)
                return null;

            tooltip = new ArrayList<>();
            if (itemMeta.hasDisplayName())
                tooltip.add(itemMeta.getDisplayName());
            if (itemMeta.hasLore())
                tooltip.addAll(itemMeta.getLore());
        }

        return tooltip.isEmpty() ? null : String.join("\n", tooltip);
    }

    private void tick() {
        if (this.renderer == null)
            return;

        Player player = this.menuView.getInventoryViewer().asPlayer();
        if (player == null || !player.isOnline() || player.isDead() || hasMoved(player)) {
            close(true);
            return;
        }

        float cursorX = getCursorX(player);
        float cursorY = getCursorY(player);

        this.renderer.moveElement(this.cursorId, cursorX, cursorY);

        ButtonArea hoveredButton = findButton(cursorX, cursorY);
        if (hoveredButton != this.hoveredButton)
            setHoveredButton(hoveredButton);

        // The tooltip follows the cursor.
        if (hoveredButton != null && hoveredButton.tooltip != null)
            this.renderer.moveElement(this.tooltipId, cursorX, cursorY + TOOLTIP_OFFSET);
    }

    private void setHoveredButton(@Nullable ButtonArea hoveredButton) {
        if (this.hoveredButton != null)
            this.renderer.setColor(this.hoveredButton.rectangleId, this.hoveredButton.hologramButton.getColor());

        if (hoveredButton != null)
            this.renderer.setColor(hoveredButton.rectangleId, hoveredButton.hologramButton.getHoveredColor());

        this.renderer.setText(this.cursorId, hoveredButton == null ? this.style.getCursorText() : this.style.getHoveredCursorText());

        if (this.tooltipId != -1) {
            if (hoveredButton != null && hoveredButton.tooltip != null) {
                this.renderer.setText(this.tooltipId, hoveredButton.tooltip);
                this.renderer.setColor(this.tooltipId, this.style.getTooltipColor());
            } else {
                // The tooltip cannot be removed, therefore it is hidden.
                this.renderer.setText(this.tooltipId, "");
                this.renderer.setColor(this.tooltipId, TRANSPARENT_COLOR);
            }
        }

        this.hoveredButton = hoveredButton;
    }

    private boolean hasMoved(Player player) {
        Location location = player.getLocation();
        return location.getWorld() != this.startLocation.getWorld() ||
                location.distanceSquared(this.startLocation) > MAX_MOVE_DISTANCE_SQUARED;
    }

    private float getCursorX(Player player) {
        return project(wrapDegrees(player.getLocation().getYaw() - this.baseYaw), this.maxCursorX);
    }

    private float getCursorY(Player player) {
        // The pitch grows when looking down, therefore we need to flip it.
        return project(this.basePitch - player.getLocation().getPitch(), this.maxCursorY);
    }

    /**
     * Convert an angle the player turned his head by to the distance of the cursor from the center of the panel.
     */
    private float project(float angle, float max) {
        if (angle >= 90f)
            return max;
        if (angle <= -90f)
            return -max;

        float value = (float) (Math.tan(Math.toRadians(angle)) * this.style.getDistance());
        return Math.max(-max, Math.min(max, value));
    }

    private static float wrapDegrees(float angle) {
        angle %= 360f;
        if (angle >= 180f)
            angle -= 360f;
        if (angle < -180f)
            angle += 360f;
        return angle;
    }

    @Nullable
    private ButtonArea findButton(float cursorX, float cursorY) {
        for (ButtonArea buttonArea : this.buttonAreas) {
            if (Math.abs(cursorX - buttonArea.x) <= buttonArea.width / 2 &&
                    Math.abs(cursorY - buttonArea.y) <= buttonArea.height / 2)
                return buttonArea;
        }

        return null;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onClick(boolean rightClick) {
        if (this.renderer == null || this.hoveredButton == null)
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastClick < CLICK_DELAY)
            return;

        Player player = this.menuView.getInventoryViewer().asPlayer();
        if (player == null)
            return;

        this.lastClick = currentTime;

        Menu menu = this.menuView.getMenu();
        try (ButtonClickContextImpl ctx = ButtonClickContextImpl.obtain((MenuView) this.menuView, player,
                this.hoveredButton.slot, rightClick ? ClickType.RIGHT : ClickType.LEFT)) {
            menu.onClick(ctx);
        }
    }

    @Override
    public void onExit() {
        close(true);
    }

    /**
     * Close the hologram menu.
     *
     * @param notifyMenu Whether to notify the menu about the view being closed.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void close(boolean notifyMenu) {
        if (this.renderer == null)
            return;

        NMSHologramMenus.Renderer renderer = this.renderer;
        this.renderer = null;
        this.hoveredButton = null;

        MENUS_BY_PLAYERS.remove(this.playerUUID, this);

        renderer.destroy();

        if (notifyMenu) {
            Menu menu = this.menuView.getMenu();
            menu.onClose(this.menuView);
        }
    }

    /**
     * The inputs of the player are delegated to the menu that is currently opened for him.
     * This is done as menus can take over the renderers of other menus.
     */
    private static class InputDelegate implements NMSHologramMenus.InputListener {

        private final UUID playerUUID;

        InputDelegate(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public void onClick(boolean rightClick) {
            HologramMenuWrapper<?> hologramMenu = MENUS_BY_PLAYERS.get(this.playerUUID);
            if (hologramMenu != null)
                hologramMenu.onClick(rightClick);
        }

        @Override
        public void onExit() {
            HologramMenuWrapper<?> hologramMenu = MENUS_BY_PLAYERS.get(this.playerUUID);
            if (hologramMenu != null)
                hologramMenu.onExit();
        }

    }

    private static class ButtonsRow {

        private final List<ButtonArea> buttons = new ArrayList<>();
        private float width;
        private float height;

    }

    private static class ButtonArea {

        private final int slot;
        /* The appearance of the button, or null if this is an empty space between buttons. */
        @Nullable
        private final HologramButton hologramButton;
        @Nullable
        private final ItemStack item;
        private final float width;
        private final float height;
        @Nullable
        private String tooltip;
        private float x;
        private float y;
        private int rectangleId;

        ButtonArea(int slot, @Nullable HologramButton hologramButton, @Nullable ItemStack item, float width, float height) {
            this.slot = slot;
            this.hologramButton = hologramButton;
            this.item = item;
            this.width = width;
            this.height = height;
        }

    }

}
