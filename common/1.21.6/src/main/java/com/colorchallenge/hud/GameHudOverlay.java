package com.colorchallenge.hud;

import com.colorchallenge.state.ClientGameState;
import com.colorchallenge.state.GameState;
import com.colorchallenge.state.GameStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Collection;

@Environment(EnvType.CLIENT)
public class GameHudOverlay {
    private static final int MARGIN = 5;

    public static void render(GuiGraphics graphics, DeltaTracker tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Offset below active effect icons (each effect row is ~25px)
        int effectOffset = 0;
        if (mc.player != null) {
            Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
            int visibleCount = (int) effects.stream().filter(MobEffectInstance::showIcon).count();
            if (visibleCount > 0) {
                effectOffset = 25 * visibleCount + 2;
            }
        }

        int y = MARGIN + effectOffset;

        GameState state = ClientGameState.getState();
        if (state == GameState.NOT_STARTED) {
            return;
        }

        // Delivery count display
        int delivered = ClientGameState.getDeliveredCount();
        String deliveryText = String.format("%d / %d", delivered, GameStateManager.TARGET_DYE_COUNT);
        int deliveryWidth = mc.font.width(deliveryText);
        int deliveryColor = delivered >= GameStateManager.TARGET_DYE_COUNT ? 0xFF55FF55 : 0xFFFFFFFF;
        graphics.drawString(mc.font, deliveryText, screenWidth - deliveryWidth - MARGIN, y, deliveryColor, true);

        // Play time display
        int ticks = ClientGameState.getPlayTime();
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);
        int timeWidth = mc.font.width(timeText);
        graphics.drawString(mc.font, timeText, screenWidth - timeWidth - MARGIN, y + 12, 0xFFFFFFFF, true);

        // Ranking display (finished players)
        var finishedPlayers = ClientGameState.getFinishedPlayers();
        int nextY = y + 26;
        if (!finishedPlayers.isEmpty()) {
            for (int i = 0; i < finishedPlayers.size(); i++) {
                var entry = finishedPlayers.get(i);
                int ft = entry.finishTimeTicks() / 20;
                String rankText = String.format("#%d %s  %02d:%02d",
                        i + 1, entry.playerName(), ft / 60, ft % 60);
                int rankWidth = mc.font.width(rankText);
                int color = (i == 0) ? 0xFFFFD700 : 0xFFCCCCCC;
                graphics.drawString(mc.font, rankText, screenWidth - rankWidth - MARGIN, nextY, color, true);
                nextY += 11;
            }
        }

        // Delivery area direction marker
        DeliveryAreaMarkerRenderer.render(graphics, mc, screenWidth, screenHeight);
    }
}
