package com.colorchallenge.hud;

import com.colorchallenge.state.ClientGameState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

@Environment(EnvType.CLIENT)
public class DeliveryStatusScreen extends Screen {
    private static final int CELL_SIZE = 28;
    private static final int GAP = 4;
    private static final int COLS = 4;
    private static final DyeColor[] DYE_ORDER = DyeColor.values();

    public DeliveryStatusScreen() {
        super(Component.translatable("screen.colorchallenge.delivery_status"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        Set<DyeColor> delivered = ClientGameState.getDeliveredDyes();

        int gridWidth = COLS * CELL_SIZE + (COLS - 1) * GAP;
        int rows = (DYE_ORDER.length + COLS - 1) / COLS;
        int gridHeight = rows * CELL_SIZE + (rows - 1) * GAP;

        int startX = (this.width - gridWidth) / 2;
        int startY = (this.height - gridHeight) / 2;

        // Title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, startY - 16, 0xFFFFFF);

        for (int i = 0; i < DYE_ORDER.length; i++) {
            DyeColor color = DYE_ORDER[i];
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * (CELL_SIZE + GAP);
            int y = startY + row * (CELL_SIZE + GAP);

            boolean isDelivered = delivered.contains(color);
            int bgColor = isDelivered ? 0xFF22AA22 : 0xFF555555;
            graphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);

            // Render dye item icon centered
            ItemStack dyeStack = new ItemStack(DyeItem.byColor(color));
            int iconX = x + (CELL_SIZE - 16) / 2;
            int iconY = y + (CELL_SIZE - 16) / 2;
            graphics.renderItem(dyeStack, iconX, iconY);
        }

        // Delivery count
        String countText = delivered.size() + " / " + DYE_ORDER.length;
        graphics.drawCenteredString(this.font, countText, this.width / 2, startY + gridHeight + 8, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
