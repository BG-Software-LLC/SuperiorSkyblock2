package com.bgsoftware.superiorskyblock.gui.menus;

import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class YamlScroll extends YamlMenu {

    private int page;
    private int scrollWidth;

    private List<Button> list;
    private List<Integer> slots;

    public YamlScroll(Player player, YamlConfiguration file) {
        super(player, file);

        page = 0;
        scrollWidth = file.getInt("scroll_width", 1);

        slots = new ArrayList<>();
        file.getStringList("list_positions").forEach(position -> slots.add(coordsToSlot(position)));

        addAction("prev_page", (p, t) -> prevPage());
        addAction("next_page", (p, t) -> nextPage());
        addAction("first_page", (p, t) -> setPage(0));
        addAction("last_page", (p, t) -> setPage(getPages()));
    }

    @Override
    protected void update() {
        updatePage();
        updateScroll();

        super.update();
    }

    protected void updateScroll() {
        slots.forEach(this::removeButton);

        for (int i = 0; i < getPageSize(); i++) {
            try {
                int slot = slots.get(i);
                Button button = list.get(i + page * scrollWidth);

                setButton(slot, button);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
    }

    private void updatePage() {
        if (page < 0)
            page = 0;
        if (page > getPages())
            page = getPages();
    }

    public int getPages() {
        int pages = 0;
        int items = list.size();

        while (items > 0) {
            pages++;
            items -= scrollWidth;
        }

        return pages <= 0 ? 0 : pages - 1;
    }

    public int getPageSize() {
        return slots.size();
    }

    public void setPage(int page) {
        this.page = page;
        updatePage();
    }

    public void nextPage() {
        setPage(page + 1);
    }

    public void prevPage() {
        setPage(page - 1);
    }

    public void setList(List<Button> list) {
        this.list = list;
    }
}
