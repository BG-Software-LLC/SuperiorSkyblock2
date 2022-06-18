package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
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
        return this.currentPage;
    }

    public List<T> getPagedObjects() {
        if (this.objects == null)
            updatePagedObjects();

        return Collections.unmodifiableList(this.objects);
    }

    public void updatePagedObjects() {
        this.objects = requestObjects();
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

        SuperiorMenuPattern<M> menuPattern = getMenuPattern();

        if (!(menuPattern instanceof PagedMenuPattern))
            return false;

        PagedObjectButton<M, T> pagedObjectButton = (PagedObjectButton<M, T>) menuButton;

        objects = requestObjects();
        int objectsPerPage = ((PagedMenuPattern<M, T>) menuPattern).getObjectsPerPage();

        int objectIndex = pagedObjectButton.getObjectIndex() + (objectsPerPage * (currentPage - 1));

        if (objectIndex >= objects.size()) {
            if (acceptNull)
                pagedObjectButton.updateObject(null);
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
