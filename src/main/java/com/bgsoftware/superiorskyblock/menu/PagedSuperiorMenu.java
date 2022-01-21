package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class PagedSuperiorMenu<M extends PagedSuperiorMenu<M, T>, T> extends SuperiorMenu<M> {

    private final boolean acceptNull;
    protected Consumer<SuperiorPlayer> onPageMove = null;
    private List<T> objects;

    protected int currentPage = 1;

    public PagedSuperiorMenu(PagedMenuPattern<M, T> menuPattern, SuperiorPlayer superiorPlayer) {
        this(menuPattern, superiorPlayer, false);
    }

    public PagedSuperiorMenu(PagedMenuPattern<M, T> menuPattern, SuperiorPlayer superiorPlayer, boolean acceptNull) {
        super(menuPattern, superiorPlayer);
        this.acceptNull = acceptNull;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public List<T> getPagedObjects() {
        if (objects == null)
            objects = requestObjects();

        return Collections.unmodifiableList(objects);
    }

    public void movePage(int newPage) {
        Preconditions.checkArgument(newPage >= 1, "invalid page " + newPage);

        this.currentPage = newPage;

        if (onPageMove != null)
            onPageMove.accept(inventoryViewer);

        previousMove = false;
        open(previousMenu);
    }

    @Override
    public boolean preButtonClick(SuperiorMenuButton<M> menuButton, InventoryClickEvent clickEvent) {
        if (!(menuButton instanceof PagedObjectButton))
            return true;

        PagedObjectButton<M, T> pagedObjectButton = (PagedObjectButton<M, T>) menuButton;

        objects = requestObjects();

        int objectIndex = pagedObjectButton.getObjectIndex() + (objects.size() * (currentPage - 1));

        if (objectIndex >= objects.size()) {
            return acceptNull;
        }

        pagedObjectButton.updateObject(objects.get(objectIndex));

        return true;
    }

    protected abstract List<T> requestObjects();

    protected void setPageMoveRunnable(Consumer<SuperiorPlayer> onPageMove) {
        this.onPageMove = onPageMove;
    }

}
