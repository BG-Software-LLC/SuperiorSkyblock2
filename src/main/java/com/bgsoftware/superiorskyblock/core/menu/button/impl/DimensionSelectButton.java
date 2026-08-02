package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuDimensionSelection;

public class DimensionSelectButton extends AbstractMenuViewButton<MenuDimensionSelection.View> {

    private DimensionSelectButton(AbstractMenuTemplateButton<MenuDimensionSelection.View> templateButton, MenuDimensionSelection.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuDimensionSelection.View> context) {
        menuView.accept(getTemplate().dimension);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuDimensionSelection.View> {

        private Dimension dimension;

        public Builder setDimension(Dimension dimension) {
            this.dimension = dimension;
            return this;
        }

        @Override
        public MenuTemplateButton<MenuDimensionSelection.View> build() {
            return new Template(this, dimension);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuDimensionSelection.View> {

        private final Dimension dimension;

        Template(AbstractBuilder<MenuDimensionSelection.View> builder, Dimension dimension) {
            super(builder, DimensionSelectButton.class, DimensionSelectButton::new);
            this.dimension = dimension;
        }

    }

}
