package com.bgsoftware.superiorskyblock.core.menu.hologram;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.button.click.ButtonClickContextImpl;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CurrentPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.NextPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.PreviousPageButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.RegularHologramMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.nms.NMSHologramMenus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Hologram menus are handled by a single thread of their own, so many open menus do not slow down the server.
 * Only building the contents of menus and running the logic of their buttons is done on the main thread,
 * as these may use the API of the server and of other plugins.
 */
public class HologramMenuWrapper<V extends MenuView<V, ?>> implements NMSHologramMenus.InputListener {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ScheduledExecutorService MENUS_THREAD = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "SuperiorSkyblock Hologram Menus");
        thread.setDaemon(true);
        return thread;
    });

    // Only changed from the menus thread, but read from the main thread as well.
    private static final Map<UUID, HologramMenuWrapper<?>> MENUS_BY_PLAYERS = new ConcurrentHashMap<>();
    // Work for the main thread that was created while the plugin is being disabled.
    private static final Queue<Runnable> SHUTDOWN_MAIN_THREAD_TASKS = new ConcurrentLinkedQueue<>();
    private static volatile boolean shuttingDown = false;

    private static final long TICK_INTERVAL = 50L;
    private static final long SHUTDOWN_TIMEOUT = 5L;
    private static final int TRANSPARENT_COLOR = 0;
    /* The maximum pitch of the viewer that is used as the center of the panel. */
    private static final float MAX_BASE_PITCH = 40f;
    private static final double MAX_MOVE_DISTANCE_SQUARED = 1D;
    /* The ticks after opening a menu in which the position of the viewer may still change, see hasMoved. */
    private static final int MOVE_SETTLE_TICKS = 5;
    /* The layers of the elements of menus. Elements of higher layers are rendered in front. */
    private static final int BACKGROUND_LAYER = 0;
    private static final int BUTTONS_LAYER = 1;
    private static final int CONTENT_LAYER = 2;
    private static final int TOOLTIP_LAYER = 3;
    private static final int CURSOR_LAYER = 4;
    /* The size of the objects of paged menus while they are out of the menu, as elements cannot have no size. */
    private static final float HIDDEN_SIZE = 0.01f;

    private final UUID playerUUID;
    private final V menuView;
    private final RegularHologramMenuLayoutImpl<V> menuLayout;
    private final HologramMenuStyle style;

    // The renderer is also read from the main thread. All the other fields are only accessed from the menus thread.
    @Nullable
    private volatile NMSHologramMenus.Renderer renderer;
    private Player player;
    private final List<ButtonArea> buttonAreas = new ArrayList<>();
    // The elements of the menu, except for the elements of the buttons of the objects of paged menus.
    private final List<Integer> elementIds = new ArrayList<>();
    // The buttons of the objects of paged menus, which slide when changing pages.
    private final List<ButtonArea> pagedButtons = new ArrayList<>();
    @Nullable
    private ButtonArea hoveredButton;
    /* The page that was drawn, or 0 if the menu is not paged. */
    private int drawnPage;
    private float drawnPanelWidth;
    private float drawnPanelHeight;
    /* Buttons cannot be clicked until this time, as they are moving. */
    private long animationEndTime;
    private Location startLocation;
    private int moveSettleTicks;
    private float baseYaw;
    private float basePitch;
    private float maxCursorX;
    private float maxCursorY;
    private int cursorId;
    /* Whether the cursor is drawn as it is when it is on a button. */
    private boolean cursorHovered;
    /* The identifier of the tooltip, or -1 if none of the buttons has a tooltip. */
    private int tooltipId = -1;
    private long lastClick;

    @Nullable
    public static HologramMenuWrapper<?> getByPlayer(UUID playerUUID) {
        return MENUS_BY_PLAYERS.get(playerUUID);
    }

    public static void startTicking() {
        MENUS_THREAD.scheduleAtFixedRate(() -> runSafely(HologramMenuWrapper::tickAll),
                TICK_INTERVAL, TICK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private static void tickAll() {
        if (MENUS_BY_PLAYERS.isEmpty())
            return;

        for (HologramMenuWrapper<?> hologramMenu : MENUS_BY_PLAYERS.values())
            hologramMenu.tick();
    }

    /**
     * Close all the menus, and stop the menus thread. Called from the main thread when the plugin is disabled.
     */
    public static void closeAll() {
        shuttingDown = true;

        MENUS_THREAD.shutdown();
        try {
            if (!MENUS_THREAD.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS))
                MENUS_THREAD.shutdownNow();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }

        // The menus thread is stopped, so the menus that are still open are closed from the main thread.
        for (HologramMenuWrapper<?> hologramMenu : new ArrayList<>(MENUS_BY_PLAYERS.values()))
            hologramMenu.closeInternal(false);

        Runnable mainThreadTask;
        while ((mainThreadTask = SHUTDOWN_MAIN_THREAD_TASKS.poll()) != null)
            runSafely(mainThreadTask);
    }

    private static void runOnMenusThread(Runnable task) {
        try {
            MENUS_THREAD.execute(() -> runSafely(task));
        } catch (RejectedExecutionException ignored) {
            // The plugin is being disabled, and all the menus are closed by closeAll.
        }
    }

    private static void runOnMenusThreadLater(Runnable task, int ticks) {
        try {
            MENUS_THREAD.schedule(() -> runSafely(task), ticks * TICK_INTERVAL, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ignored) {
            // The plugin is being disabled, and all the menus are closed by closeAll.
        }
    }

    private static void runOnMainThread(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }

        // BukkitExecutor runs tasks on the calling thread while the plugin is being disabled, which must not happen here.
        if (!shuttingDown) {
            try {
                Bukkit.getScheduler().runTask(plugin, task);
                return;
            } catch (IllegalPluginAccessException ignored) {
                // The plugin is disabled.
            }
        }

        SHUTDOWN_MAIN_THREAD_TASKS.add(task);
    }

    private static void runSafely(Runnable task) {
        try {
            task.run();
        } catch (Throwable error) {
            Log.error(error, "An unexpected error occurred while handling hologram menus:");
        }
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

    /**
     * Open the menu. Called from the main thread; the menu is drawn later by the menus thread.
     */
    public boolean open(Player player) {
        NMSHologramMenus nmsHologramMenus = plugin.getNMSHologramMenus().orElse(null);
        if (nmsHologramMenus == null)
            return false;

        // The contents of the menu are built on the main thread, as they may use placeholders and items of other plugins.
        Layout layout = buildLayout(player);

        Location startLocation = player.getLocation();
        Location eyeLocation = player.getEyeLocation();

        // A renderer is prepared in case the menu needs a new one. It does not send anything until it is shown.
        Location origin = eyeLocation.clone();
        origin.setPitch(0f);
        NMSHologramMenus.Renderer newRenderer = nmsHologramMenus.createRenderer(player, origin, this.style.getDistance(),
                this.style.isCrosshairHidden(), new InputDelegate(this.playerUUID));

        // The blocks that may hide the menu can only be collected on the main thread. They are read by the menus thread.
        NMSHologramMenus.CapturedBlocks capturedBlocks = nmsHologramMenus.captureBlocks(eyeLocation,
                layout.getReach(this.style.getDistance()));

        runOnMenusThread(() -> openInternal(player, layout, newRenderer, capturedBlocks, startLocation));

        return true;
    }

    private void openInternal(Player player, Layout layout, NMSHologramMenus.Renderer newRenderer,
                              NMSHologramMenus.CapturedBlocks capturedBlocks, Location startLocation) {
        // The menu that was shown before, when the objects of a paged menu slide to the new page.
        HologramMenuWrapper<?> previousPage = null;
        int slideDirection = 0;

        if (this.renderer != null) {
            // Already opened, we only need to draw the menu again.
            this.renderer.clear();
        } else {
            HologramMenuWrapper<?> oldHologramMenu = MENUS_BY_PLAYERS.get(this.playerUUID);
            if (oldHologramMenu != null && oldHologramMenu.renderer != null) {
                if (oldHologramMenu.style.getDistance() == this.style.getDistance() &&
                        oldHologramMenu.style.isCrosshairHidden() == this.style.isCrosshairHidden()) {
                    // We take over the renderer of the old menu, so the view of the player is not changed.
                    if (isPageChange(oldHologramMenu, layout)) {
                        previousPage = oldHologramMenu;
                        slideDirection = Integer.signum(layout.page - oldHologramMenu.drawnPage);
                        takeOver(oldHologramMenu, false);
                    } else {
                        takeOver(oldHologramMenu, true);
                    }
                } else {
                    // The renderer of the old menu draws its elements in a different distance, or shows a different view.
                    oldHologramMenu.closeInternal(false);
                }
            }

            if (this.renderer == null) {
                this.startLocation = startLocation;
                this.moveSettleTicks = MOVE_SETTLE_TICKS;
                this.baseYaw = startLocation.getYaw();
                this.basePitch = Math.max(-MAX_BASE_PITCH, Math.min(MAX_BASE_PITCH, startLocation.getPitch()));
                this.renderer = newRenderer;
            }
        }

        this.player = player;

        MENUS_BY_PLAYERS.put(this.playerUUID, this);

        draw(layout, capturedBlocks, slideDirection);

        if (previousPage != null)
            slideToPage(previousPage, slideDirection);

        this.lastClick = System.currentTimeMillis();
    }

    /**
     * @param clear Whether to remove the elements of the old menu.
     */
    private void takeOver(HologramMenuWrapper<?> oldHologramMenu, boolean clear) {
        this.renderer = oldHologramMenu.renderer;
        this.startLocation = oldHologramMenu.startLocation;
        this.moveSettleTicks = oldHologramMenu.moveSettleTicks;
        this.baseYaw = oldHologramMenu.baseYaw;
        this.basePitch = oldHologramMenu.basePitch;

        oldHologramMenu.renderer = null;
        oldHologramMenu.hoveredButton = null;

        if (clear)
            this.renderer.clear();
    }

    /**
     * Get whether the menu shows another page of the same paged menu as an old menu, with the same layout.
     */
    private boolean isPageChange(HologramMenuWrapper<?> oldHologramMenu, Layout layout) {
        return this.style.getPageAnimationDuration() > 0 && oldHologramMenu.menuView == this.menuView &&
                layout.page != 0 && oldHologramMenu.drawnPage != 0 && oldHologramMenu.drawnPage != layout.page &&
                oldHologramMenu.drawnPanelWidth == layout.panelWidth && oldHologramMenu.drawnPanelHeight == layout.panelHeight;
    }

    /**
     * Slide the objects of the old page out of the menu, and the objects of the new page into it.
     * The new page was already drawn, with its objects shrunk at the side they slide in from.
     *
     * @param slideDirection 1 when moving to the next page, -1 when moving to the previous page.
     */
    private void slideToPage(HologramMenuWrapper<?> previousPage, int slideDirection) {
        NMSHologramMenus.Renderer renderer = this.renderer;
        int duration = this.style.getPageAnimationDuration();
        float distance = this.style.getPageAnimationDistance();

        // Everything except the objects was drawn again, so the old elements can be removed.
        renderer.removeElements(previousPage.elementIds);

        // The objects of the old page shrink while sliding out, and are removed afterwards.
        List<Integer> oldObjectElementIds = new ArrayList<>();
        for (ButtonArea buttonArea : previousPage.pagedButtons) {
            for (PlacedElement element : buttonArea.elements) {
                renderer.animateElement(element.id, element.x - slideDirection * distance, element.y, HIDDEN_SIZE, duration);
                oldObjectElementIds.add(element.id);
            }
        }

        List<PlacedElement> newObjectElements = new ArrayList<>();
        for (ButtonArea buttonArea : this.pagedButtons)
            newObjectElements.addAll(buttonArea.elements);

        // The objects of the new page start sliding a tick after they are shown, so the viewer sees them move.
        runOnMenusThreadLater(() -> {
            for (PlacedElement element : newObjectElements)
                renderer.animateElement(element.id, element.x, element.y, 1f, duration);
        }, 1);

        // Elements that were removed in the meantime are ignored by the renderer.
        runOnMenusThreadLater(() -> renderer.removeElements(oldObjectElementIds), duration + 1);

        // Buttons cannot be clicked while they are moving.
        this.animationEndTime = System.currentTimeMillis() + (duration + 1) * TICK_INTERVAL;
    }

    /**
     * Build the contents of the menu, and calculate the sizes of its sections. Called from the main thread.
     */
    private Layout buildLayout(Player player) {
        HologramMenuStyle style = this.style;

        Layout layout = new Layout();
        layout.title = this.menuLayout.getTitle(this.menuView);
        layout.bodyLines = this.menuLayout.getBodyLines(player);

        PagedObjects pagedObjects = null;
        if (this.menuLayout instanceof PagedMenuLayout && this.menuView instanceof PagedMenuView) {
            PagedMenuView<?, ?, ?> pagedMenuView = (PagedMenuView<?, ?, ?>) this.menuView;
            layout.page = pagedMenuView.getCurrentPage();
            pagedObjects = new PagedObjects(layout.page, pagedMenuView.getPagedObjects(),
                    ((PagedMenuLayout<?>) this.menuLayout).getObjectsPerPageCount());
        }

        float buttonsWidth = 0f;
        float buttonsHeight = 0f;
        for (List<Integer> rowSlots : this.menuLayout.getRows()) {
            ButtonsRow row = createButtonsRow(rowSlots, pagedObjects);
            if (row == null)
                continue;

            if (!layout.rows.isEmpty())
                buttonsHeight += style.getRowsSpacing();
            buttonsHeight += row.height;
            buttonsWidth = Math.max(buttonsWidth, row.width);
            layout.rows.add(row);
        }

        float contentHeight = 0f;
        int sectionsCount = 0;
        if (!layout.title.isEmpty()) {
            contentHeight += style.getTitleHeight();
            ++sectionsCount;
        }
        if (!layout.bodyLines.isEmpty()) {
            contentHeight += style.getBodyLineHeight() * layout.bodyLines.size();
            ++sectionsCount;
        }
        if (!layout.rows.isEmpty()) {
            contentHeight += buttonsHeight;
            ++sectionsCount;
        }
        if (sectionsCount > 1)
            contentHeight += style.getSectionsSpacing() * (sectionsCount - 1);

        layout.contentHeight = contentHeight;
        layout.panelWidth = Math.max(style.getMinWidth(), buttonsWidth + style.getPadding() * 2);
        layout.panelHeight = contentHeight + style.getPadding() * 2;

        if (style.isCursorLimitedToMenu()) {
            layout.maxCursorX = layout.panelWidth / 2 + style.getCursorMenuMargin();
            layout.maxCursorY = layout.panelHeight / 2 + style.getCursorMenuMargin();
        } else {
            // The edges of the screen, on the plane of the panel. The cursor can always reach the edges of the panel,
            // even when the panel is larger than the screen.
            float screenHalfHeight = (float) (style.getDistance() * Math.tan(Math.toRadians(style.getScreenFov() / 2)));
            float screenEdgeMargin = style.getCursorScreenMargin() * style.getCursorScale();
            layout.maxCursorX = Math.max(screenHalfHeight * style.getScreenAspectRatio() - screenEdgeMargin, layout.panelWidth / 2);
            layout.maxCursorY = Math.max(screenHalfHeight - screenEdgeMargin, layout.panelHeight / 2);
        }

        return layout;
    }

    /**
     * Draw the menu. Called from the menus thread.
     */
    private void draw(Layout layout, NMSHologramMenus.CapturedBlocks capturedBlocks, int slideDirection) {
        HologramMenuStyle style = this.style;
        NMSHologramMenus.Renderer renderer = this.renderer;

        this.buttonAreas.clear();
        this.elementIds.clear();
        this.pagedButtons.clear();
        this.hoveredButton = null;
        this.tooltipId = -1;

        this.maxCursorX = layout.maxCursorX;
        this.maxCursorY = layout.maxCursorY;

        // The menu is drawn closer to the player when there are blocks between him and the menu,
        // so they do not hide it. The whole area the cursor can be in should be visible.
        renderer.setScale(renderer.getMaxScale(capturedBlocks, this.maxCursorX * 2, this.maxCursorY * 2));

        renderer.setLayer(BACKGROUND_LAYER);

        ItemStack backgroundItem = style.getBackgroundItem();
        if (backgroundItem != null) {
            this.elementIds.add(renderer.addItem(backgroundItem, 0f, 0f, layout.panelWidth, layout.panelHeight));
        } else {
            this.elementIds.add(renderer.addRectangle(0f, 0f, layout.panelWidth, layout.panelHeight, style.getBackgroundColor()));
        }

        renderer.setLayer(CONTENT_LAYER);

        // The sections are drawn from the top of the panel to its bottom.
        float currentY = layout.contentHeight / 2;

        if (!layout.title.isEmpty()) {
            this.elementIds.add(renderer.addText(layout.title, 0f, currentY - style.getTitleHeight() / 2,
                    style.getTitleScale(), style.isTitleShadowed()));
            currentY -= style.getTitleHeight() + style.getSectionsSpacing();
        }

        if (!layout.bodyLines.isEmpty()) {
            for (String bodyLine : layout.bodyLines) {
                this.elementIds.add(renderer.addText(bodyLine, 0f, currentY - style.getBodyLineHeight() / 2,
                        style.getBodyScale(), style.isBodyShadowed()));
                currentY -= style.getBodyLineHeight();
            }
            currentY -= style.getSectionsSpacing();
        }

        boolean hasTooltips = false;

        for (ButtonsRow row : layout.rows) {
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

                if (!buttonArea.paged) {
                    for (PlacedElement element : buttonArea.elements)
                        this.elementIds.add(element.id);
                } else {
                    this.pagedButtons.add(buttonArea);
                    if (slideDirection != 0) {
                        // The objects of a new page are shown shrunk at the side they slide in from.
                        for (PlacedElement element : buttonArea.elements) {
                            renderer.animateElement(element.id, element.x + slideDirection * style.getPageAnimationDistance(),
                                    element.y, HIDDEN_SIZE, 0);
                        }
                    }
                }
            }
            currentY -= row.height + style.getRowsSpacing();
        }

        if (hasTooltips) {
            renderer.setLayer(TOOLTIP_LAYER);
            this.tooltipId = renderer.addTooltip("", 0f, 0f, style.getTooltipScale(), TRANSPARENT_COLOR);
            this.elementIds.add(this.tooltipId);
        }

        renderer.setLayer(CURSOR_LAYER);
        this.cursorHovered = false;
        float cursorX = getCursorX(renderer) + getCursorTextOffsetX();
        float cursorY = getCursorY(renderer) + getCursorTextOffsetY();
        ItemStack cursorItem = style.getCursorItem();
        if (cursorItem != null) {
            this.cursorId = renderer.addItem(cursorItem, cursorX, cursorY, style.getCursorScale(), style.getCursorScale());
        } else {
            this.cursorId = renderer.addText(style.getCursorText(), cursorX, cursorY, style.getCursorScale(), style.isCursorShadowed());
        }
        this.elementIds.add(this.cursorId);

        renderer.show();

        this.drawnPage = layout.page;
        this.drawnPanelWidth = layout.panelWidth;
        this.drawnPanelHeight = layout.panelHeight;
    }

    /**
     * Create a row of buttons without positioning them. Called from the main thread.
     *
     * @return The row, or null if it has no buttons to draw.
     */
    @Nullable
    private ButtonsRow createButtonsRow(List<Integer> rowSlots, @Nullable PagedObjects pagedObjects) {
        HologramButton defaultButton = this.style.getDefaultButton();

        ButtonsRow row = new ButtonsRow();
        boolean hasButtons = false;

        for (int slot : rowSlots) {
            MenuTemplateButton<V> templateButton = this.menuLayout.getButton(slot);
            MenuViewButton<V> viewButton = templateButton.createViewButton(this.menuView);

            boolean paged = pagedObjects != null && viewButton instanceof PagedMenuViewButton;
            String pagePlaceholder = pagedObjects == null ? null : pagedObjects.getPlaceholder(viewButton);

            ItemStack buttonItem = paged ? pagedObjects.createItem(templateButton, (PagedMenuViewButton<?, ?>) viewButton) :
                    createViewItem(viewButton);
            if (buttonItem != null && buttonItem.getType() == Material.AIR)
                buttonItem = null;

            if (buttonItem != null && pagePlaceholder != null) {
                buttonItem = new ItemBuilder(buttonItem).replaceAll("{0}", pagePlaceholder)
                        .build(this.menuView.getInventoryViewer());
            }

            HologramButton hologramButton = templateButton.getButtonHologram();

            ButtonArea buttonArea;
            if (paged && buttonItem == null) {
                // Slots of paged objects without objects are left as empty space with the size of their buttons,
                // so the layout of the menu is the same in all pages.
                buttonArea = hologramButton != null ?
                        new ButtonArea(slot, null, null, hologramButton.getWidth(), hologramButton.getHeight()) :
                        new ButtonArea(slot, null, null, defaultButton.getHeight(), defaultButton.getHeight());
            } else if (hologramButton != null) {
                buttonArea = new ButtonArea(slot, hologramButton, buttonItem, hologramButton.getWidth(), hologramButton.getHeight());
            } else if (buttonItem != null) {
                // Buttons that only have an item are squares.
                buttonArea = new ButtonArea(slot, defaultButton, buttonItem, defaultButton.getHeight(), defaultButton.getHeight());
            } else {
                // Slots without buttons are left as empty space in the row.
                buttonArea = new ButtonArea(slot, null, null, defaultButton.getHeight(), defaultButton.getHeight());
            }

            buttonArea.paged = paged;

            if (buttonArea.hologramButton != null) {
                hasButtons = true;
                buttonArea.label = replacePlaceholder(buttonArea.hologramButton.getLabel(), pagePlaceholder);
                if (this.style.isTooltipEnabled())
                    buttonArea.tooltip = replacePlaceholder(createTooltip(buttonArea.hologramButton, buttonItem), pagePlaceholder);
            }

            if (!row.buttons.isEmpty())
                row.width += this.style.getButtonsSpacing();
            row.width += buttonArea.width;
            row.height = Math.max(row.height, buttonArea.height);
            row.buttons.add(buttonArea);
        }

        return hasButtons ? row : null;
    }

    @Nullable
    private static ItemStack createViewItem(MenuViewButton<?> viewButton) {
        try {
            return viewButton.createViewItem();
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while setting up menu:");
            return null;
        }
    }

    @Nullable
    private static String replacePlaceholder(@Nullable String text, @Nullable String placeholder) {
        return text == null || placeholder == null ? text : text.replace("{0}", placeholder);
    }

    private void drawButton(ButtonArea buttonArea) {
        NMSHologramMenus.Renderer renderer = this.renderer;
        HologramButton hologramButton = buttonArea.hologramButton;

        renderer.setLayer(BUTTONS_LAYER);
        buttonArea.rectangleId = renderer.addRectangle(buttonArea.x, buttonArea.y,
                buttonArea.width, buttonArea.height, hologramButton.getColor());
        buttonArea.elements.add(new PlacedElement(buttonArea.rectangleId, buttonArea.x, buttonArea.y));

        renderer.setLayer(CONTENT_LAYER);

        String label = buttonArea.label;
        boolean hasLabel = label != null && !label.isEmpty();

        float labelX = buttonArea.x;

        if (buttonArea.item != null) {
            float itemX = buttonArea.x;
            if (hasLabel) {
                // The item is drawn in a square in the left side of the button, and the label in the rest of it.
                itemX += (buttonArea.height - buttonArea.width) / 2;
                labelX += buttonArea.height / 2;
            }
            int itemId = renderer.addItem(buttonArea.item, itemX, buttonArea.y, hologramButton.getItemScale(), hologramButton.getItemScale());
            buttonArea.elements.add(new PlacedElement(itemId, itemX, buttonArea.y));
        }

        if (hasLabel) {
            int labelId = renderer.addText(label, labelX, buttonArea.y, hologramButton.getLabelScale(), hologramButton.isLabelShadowed());
            buttonArea.elements.add(new PlacedElement(labelId, labelX, buttonArea.y));
        }
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

    /**
     * Update the cursor of the menu. Called from the menus thread.
     */
    private void tick() {
        NMSHologramMenus.Renderer renderer = this.renderer;
        if (renderer == null)
            return;

        // The state of the player is only read, which is safe enough to do outside the main thread.
        if (!renderer.isViewerConnected() || this.player.isDead() || hasMoved(this.player)) {
            closeInternal(true);
            return;
        }

        renderer.tick();

        float cursorX = getCursorX(renderer);
        float cursorY = getCursorY(renderer);

        renderer.moveElement(this.cursorId, cursorX + getCursorTextOffsetX(), cursorY + getCursorTextOffsetY());

        ButtonArea hoveredButton = findButton(cursorX, cursorY);
        if (hoveredButton != this.hoveredButton)
            setHoveredButton(hoveredButton, cursorX, cursorY);

        // The tooltip follows the cursor.
        if (hoveredButton != null && hoveredButton.tooltip != null)
            renderer.moveElement(this.tooltipId, cursorX, cursorY + this.style.getTooltipOffset());
    }

    private void setHoveredButton(@Nullable ButtonArea hoveredButton, float cursorX, float cursorY) {
        NMSHologramMenus.Renderer renderer = this.renderer;
        HologramMenuStyle style = this.style;

        if (this.hoveredButton != null)
            renderer.setColor(this.hoveredButton.rectangleId, this.hoveredButton.hologramButton.getColor());

        if (hoveredButton != null)
            renderer.setColor(hoveredButton.rectangleId, hoveredButton.hologramButton.getHoveredColor());

        this.cursorHovered = hoveredButton != null;

        ItemStack cursorItem = this.cursorHovered ? style.getHoveredCursorItem() : style.getCursorItem();
        if (cursorItem != null) {
            renderer.setItem(this.cursorId, cursorItem);
        } else {
            renderer.setText(this.cursorId, this.cursorHovered ? style.getHoveredCursorText() : style.getCursorText());
        }

        if (style.getHoveredCursorScale() != style.getCursorScale()) {
            // The cursor is resized over a tick, the same way it is moved.
            renderer.animateElement(this.cursorId, cursorX + getCursorTextOffsetX(), cursorY + getCursorTextOffsetY(),
                    this.cursorHovered ? style.getHoveredCursorScale() / style.getCursorScale() : 1f, 1);
        }

        if (this.tooltipId != -1) {
            if (hoveredButton != null && hoveredButton.tooltip != null) {
                renderer.setText(this.tooltipId, hoveredButton.tooltip);
                renderer.setColor(this.tooltipId, this.style.getTooltipColor());
            } else {
                // The tooltip cannot be removed, therefore it is hidden.
                renderer.setText(this.tooltipId, "");
                renderer.setColor(this.tooltipId, TRANSPARENT_COLOR);
            }
        }

        this.hoveredButton = hoveredButton;
    }

    private boolean hasMoved(Player player) {
        Location location = player.getLocation();

        if (location.getWorld() != this.startLocation.getWorld())
            return true;

        if (this.moveSettleTicks > 0) {
            // Positions the viewer sent right before his view was locked, for example while falling, may only be
            // handled by the server now. They do not close the menu; the position it was opened at follows them.
            --this.moveSettleTicks;
            this.startLocation = location;
            return false;
        }

        return location.distanceSquared(this.startLocation) > MAX_MOVE_DISTANCE_SQUARED;
    }

    /**
     * The text of the cursor is drawn away from the point it clicks, so the tip of an arrow is on that point.
     */
    private float getCursorTextOffsetX() {
        return this.style.getCursorOffsetX() * getCurrentCursorScale();
    }

    private float getCursorTextOffsetY() {
        return this.style.getCursorOffsetY() * getCurrentCursorScale();
    }

    private float getCurrentCursorScale() {
        return this.cursorHovered ? this.style.getHoveredCursorScale() : this.style.getCursorScale();
    }

    private float getCursorX(NMSHologramMenus.Renderer renderer) {
        return project(wrapDegrees(renderer.getViewerYaw() - this.baseYaw), this.maxCursorX);
    }

    private float getCursorY(NMSHologramMenus.Renderer renderer) {
        // The pitch grows when looking down, therefore we need to flip it.
        return project(this.basePitch - renderer.getViewerPitch(), this.maxCursorY);
    }

    /**
     * Convert an angle the player turned his head by to the distance of the cursor from the center of the panel.
     */
    private float project(float angle, float max) {
        if (angle >= 90f)
            return max;
        if (angle <= -90f)
            return -max;

        float value = (float) (Math.tan(Math.toRadians(angle)) * this.style.getDistance() * this.style.getCursorSpeed());
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

    /**
     * Called from the menus thread. The logic of the clicked button is run on the main thread.
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onClick(boolean rightClick) {
        if (this.renderer == null || this.hoveredButton == null)
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastClick < this.style.getClickDelay() || currentTime < this.animationEndTime)
            return;

        this.lastClick = currentTime;

        int clickedSlot = this.hoveredButton.slot;
        ClickType clickType = rightClick ? ClickType.RIGHT : ClickType.LEFT;

        runOnMainThread(() -> {
            // The menu may have been closed while waiting for the main thread.
            if (!isOpen() || MENUS_BY_PLAYERS.get(this.playerUUID) != this)
                return;

            Player player = this.menuView.getInventoryViewer().asPlayer();
            if (player == null)
                return;

            Menu menu = this.menuView.getMenu();
            try (ButtonClickContextImpl ctx = ButtonClickContextImpl.obtain((MenuView) this.menuView, player, clickedSlot, clickType)) {
                menu.onClick(ctx);
            }
        });
    }

    /**
     * Called from the menus thread.
     */
    @Override
    public void onExit() {
        closeInternal(true);
    }

    /**
     * Close the hologram menu. Can be called from any thread.
     *
     * @param notifyMenu Whether to notify the menu about the view being closed.
     */
    public void close(boolean notifyMenu) {
        runOnMenusThread(() -> closeInternal(notifyMenu));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void closeInternal(boolean notifyMenu) {
        NMSHologramMenus.Renderer renderer = this.renderer;
        if (renderer == null)
            return;

        this.renderer = null;
        this.hoveredButton = null;

        MENUS_BY_PLAYERS.remove(this.playerUUID, this);

        Runnable mainThreadTask = renderer.destroy();

        runOnMainThread(() -> {
            mainThreadTask.run();
            if (notifyMenu) {
                Menu menu = this.menuView.getMenu();
                menu.onClose(this.menuView);
            }
        });
    }

    /**
     * The inputs of the player are delegated to the menu that is currently opened for him.
     * This is done as menus can take over the renderers of other menus.
     * Inputs are received from the threads of the network, and are handled by the menus thread.
     */
    private static class InputDelegate implements NMSHologramMenus.InputListener {

        private final UUID playerUUID;

        InputDelegate(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public void onClick(boolean rightClick) {
            runOnMenusThread(() -> {
                HologramMenuWrapper<?> hologramMenu = MENUS_BY_PLAYERS.get(this.playerUUID);
                if (hologramMenu != null)
                    hologramMenu.onClick(rightClick);
            });
        }

        @Override
        public void onExit() {
            runOnMenusThread(() -> {
                HologramMenuWrapper<?> hologramMenu = MENUS_BY_PLAYERS.get(this.playerUUID);
                if (hologramMenu != null)
                    hologramMenu.onExit();
            });
        }

    }

    /**
     * The contents of a menu and the sizes of its sections, built on the main thread and drawn by the menus thread.
     */
    private static class Layout {

        /* The page of paged menus, or 0 if the menu is not paged. */
        private int page;
        private String title;
        private List<String> bodyLines;
        private final List<ButtonsRow> rows = new ArrayList<>();
        private float contentHeight;
        private float panelWidth;
        private float panelHeight;
        private float maxCursorX;
        private float maxCursorY;

        /**
         * Get how far from the eyes of the player the menu and the cursor can reach.
         */
        double getReach(float distance) {
            return Math.sqrt(distance * distance + this.maxCursorX * this.maxCursorX + this.maxCursorY * this.maxCursorY);
        }

    }

    /**
     * The objects of the current page of a paged menu, used while building its contents on the main thread.
     */
    private static class PagedObjects {

        private final int page;
        private final List<?> objects;
        private final int objectsPerPage;

        PagedObjects(int page, List<?> objects, int objectsPerPage) {
            this.page = page;
            this.objects = objects;
            this.objectsPerPage = objectsPerPage;
        }

        /**
         * Create the item of the button of a paged object, the same way it is done in inventory-based menus.
         */
        @Nullable
        @SuppressWarnings({"rawtypes", "unchecked"})
        ItemStack createItem(MenuTemplateButton<?> templateButton, PagedMenuViewButton viewButton) {
            PagedMenuTemplateButton<?, ?> pagedTemplateButton = (PagedMenuTemplateButton<?, ?>) templateButton;
            int objectIndex = pagedTemplateButton.getButtonIndex() + this.objectsPerPage * (this.page - 1);

            if (objectIndex >= this.objects.size())
                // Slots without objects show the item of empty slots, if there is one.
                return pagedTemplateButton.getNullItem();

            viewButton.updateObject(this.objects.get(objectIndex));
            return createViewItem(viewButton);
        }

        /**
         * Get the value of {0} in the items, labels and tooltips of the buttons that change pages, if the button is one.
         */
        @Nullable
        String getPlaceholder(MenuViewButton<?> viewButton) {
            if (viewButton instanceof PreviousPageButton)
                return Formatters.COLOR_FORMATTER.format(this.page == 1 ? "&c" : "&a");
            if (viewButton instanceof NextPageButton)
                return Formatters.COLOR_FORMATTER.format(this.objects.size() > this.page * this.objectsPerPage ? "&a" : "&c");
            if (viewButton instanceof CurrentPageButton)
                return String.valueOf(this.page);
            return null;
        }

    }

    private static class PlacedElement {

        private final int id;
        private final float x;
        private final float y;

        PlacedElement(int id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
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
        private String label;
        @Nullable
        private String tooltip;
        /* Whether the button shows an object of a paged menu. */
        private boolean paged;
        private float x;
        private float y;
        private int rectangleId;
        private final List<PlacedElement> elements = new ArrayList<>();

        ButtonArea(int slot, @Nullable HologramButton hologramButton, @Nullable ItemStack item, float width, float height) {
            this.slot = slot;
            this.hologramButton = hologramButton;
            this.item = item;
            this.width = width;
            this.height = height;
        }

    }

}
