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

    public DeliveryStatusScreen() {
        super(Component.translatable("screen.colorchallenge.delivery_status"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        Set<DyeColor> delivered = ClientGameState.getDeliveredDyes();
        DyeColor[] colors = DyeColor.values();
        int rows = (colors.length + COLS - 1) / COLS;

        int gridWidth = COLS * CELL_SIZE + (COLS - 1) * GAP;
        int gridHeight = rows * CELL_SIZE + (rows - 1) * GAP;
        int startX = (this.width - gridWidth) / 2;
        int startY = (this.height - gridHeight) / 2;

        for (int i = 0; i < colors.length; i++) {
            DyeColor color = colors[i];
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * (CELL_SIZE + GAP);
            int y = startY + row * (CELL_SIZE + GAP);

            boolean isDelivered = delivered.contains(color);

            // Background
            int bgColor = isDelivered ? 0x8000AA00 : 0x80333333;
            graphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);

            // Border
            int borderColor = isDelivered ? 0xFF00FF00 : 0xFF666666;
            graphics.fill(x, y, x + CELL_SIZE, y + 1, borderColor);
            graphics.fill(x, y + CELL_SIZE - 1, x + CELL_SIZE, y + CELL_SIZE, borderColor);
            graphics.fill(x, y, x + 1, y + CELL_SIZE, borderColor);
            graphics.fill(x + CELL_SIZE - 1, y, x + CELL_SIZE, y + CELL_SIZE, borderColor);

            // Render dye item icon (16x16, centered in cell)
            ItemStack dyeStack = new ItemStack(DyeItem.byColor(color));
            graphics.renderItem(dyeStack, x + (CELL_SIZE - 16) / 2, y + (CELL_SIZE - 16) / 2);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
