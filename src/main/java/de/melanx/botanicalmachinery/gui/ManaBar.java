package de.melanx.botanicalmachinery.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.melanx.botanicalmachinery.config.LibXClientConfig;
import de.melanx.botanicalmachinery.core.LibResources;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/*
 * This class is inspired by Cyclics EnergyBar
 */
public class ManaBar {

    private final Screen parent;
    public int x = 153;
    public int y = 15;
    public final int capacity;
    private final int width = 16;
    private final int height = 62;
    public int guiLeft;
    public int guiTop;

    public ManaBar(Screen parent, int capacity) {
        this.parent = parent;
        this.capacity = capacity;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return this.guiLeft + this.x < mouseX && mouseX < this.guiLeft + this.x + this.width
                && this.guiTop + this.y < mouseY && mouseY < this.guiTop + this.y + this.height;
    }

    public void draw(PoseStack ms, float mana) {
        int relX;
        int relY;
        RenderSystem.setShaderTexture(0, LibResources.MANA_BAR);
        relX = this.guiLeft + this.x;
        relY = this.guiTop + this.y;
        Screen.blit(ms, relX, relY, 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.setShaderTexture(0, LibResources.MANA_BAR_CURRENT);
        relX += 1;
        relY += 1;
        float pct = Math.min(mana / this.capacity, 1.0F);
        int relHeight = (int) ((this.height - 2) * pct);
        Screen.blit(ms, relX, relY + (this.height - 2 - relHeight), 0, 0, this.width - 2, relHeight, this.width - 2, this.height - 2);
    }

    public void renderHoveredToolTip(PoseStack ms, int mouseX, int mouseY, int mana) {
        if (this.isMouseOver(mouseX, mouseY) && LibXClientConfig.numericalMana) {
            Component text = Component.literal(String.format("%s / %s Mana", mana, this.capacity));
            this.parent.renderTooltip(ms, text, mouseX, mouseY);
        }
    }
}
