package com.noadsch12.mixin;

import com.noadsch12.ui.screens.ClientSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

@Mixin(InGameHud.class)
public class ScoreboardMixin {

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void customScoreboardRender(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if (!ClientSettingsScreen.BetterScoreboardEnabled) return;
        // Cancel vanilla immediately so its "inner squares" never render
        ci.cancel();

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        Scoreboard scoreboard = client.world.getScoreboard();

        // 1. Get entries using your confirmed method
        //Collection<ScoreboardEntry> collection = scoreboard.getScoreboardEntries(objective);
        Collection<ScoreboardEntry> collection = scoreboard.getScoreboardEntries(objective);
        List<ScoreboardEntry> list = collection.stream()
                .filter(entry -> entry.owner() != null && !entry.owner().startsWith("#"))
                .sorted((e1, e2) -> Integer.compare(e2.value(), e1.value())) // Sort by score descending
                .limit(15)
                .toList();

        if (list.isEmpty()) return;

        // 2. Pre-calculate Names and Widths
        Text title = objective.getDisplayName();
        int titleWidth = textRenderer.getWidth(title);
        int maxWidth = titleWidth;

        for (ScoreboardEntry entry : list) {
            Text nameText = resolveName(scoreboard, entry, objective);
            int nameWidth = textRenderer.getWidth(nameText);
            int scoreWidth = textRenderer.getWidth(Integer.toString(entry.value()));
            // Width = Name + Space + Score
            maxWidth = Math.max(maxWidth, nameWidth + scoreWidth + 15);
        }

        int rowCount = list.size();
        int rowHeight = 9;
        int padding = 3;
        int totalHeight = (rowCount + 1) * (rowHeight + 2) + padding * 2;
        int width = maxWidth + 10;

        int x2 = context.getScaledWindowWidth() - 4;
        int x1 = x2 - width;
        int y1 = (context.getScaledWindowHeight() / 2) - (totalHeight / 2);
        int y2 = y1 + totalHeight;

        // 3. Draw Background (Using your Matrix methods)
        int r = 8;
        int bgColor = 0x50000000;

        context.getMatrices().pushMatrix();
        context.translate(0, 0);

        // Alpha-Safe Background (No Overlaps)
        // Center Body
        context.fill(x1, y1 + r, x2, y2 - r, bgColor);
        // Top cap
        context.fill(x1 + r, y1, x2 - r, y1 + r, bgColor);
        // Bottom cap
        context.fill(x1 + r, y2 - r, x2 - r, y2, bgColor);

        // Corners
        drawCorner(context, x1 + r, y1 + r, r, bgColor, 180);
        drawCorner(context, x2 - r, y1 + r, r, bgColor, 270);
        drawCorner(context, x1 + r, y2 - r, r, bgColor, 90);
        drawCorner(context, x2 - r, y2 - r, r, bgColor, 0);

        context.getMatrices().popMatrix();

        // 4. Render Text
        int currentY = y1 + padding;

        // Render Title
        context.drawText(textRenderer, title, x1 + (width / 2) - (titleWidth / 2), currentY, 0xFFFFFFFF, false);

        currentY += rowHeight + 2;

        for (ScoreboardEntry entry : list) {
            Text resolvedName = resolveName(scoreboard, entry, objective);
            String scoreValue = Integer.toString(entry.value());

            // DRAW NAME with shadow for better visibility
            context.drawTextWithShadow(textRenderer, resolvedName, x1 + 5, currentY, 0xFFFFFFFF);

            // DRAW SCORE (Right Aligned)
            int scoreWidth = textRenderer.getWidth(scoreValue);
            context.drawTextWithShadow(textRenderer, scoreValue, x2 - scoreWidth - 5, currentY, 0xFFFF5555);

            currentY += rowHeight + 2;
        }
    }

    @Unique
    private Text resolveName(Scoreboard scoreboard, ScoreboardEntry entry, ScoreboardObjective objective) {
        String owner = entry.owner();

        // First try display text
        Text displayText = entry.display();
        if (displayText != null && !displayText.getString().isEmpty() && !displayText.getString().equals("§")) {
            return displayText;
        }

        // Find the team that has this owner as a player (not the team with owner as the name)
        Team team = scoreboard.getScoreHolderTeam(owner);

        // If there's a team, decorate the owner name with team formatting
        if (team != null) {
            Text ownerText = Text.literal(owner);
            Text decorated = Team.decorateName(team, ownerText);
            return decorated;
        }

        // Then try the entry name
        Text nameText = entry.name();
        if (!nameText.getString().equals("§") && !nameText.getString().isEmpty()) {
            return nameText;
        }

        // Fallback: use owner text
        return Text.literal(owner);
    }

    @Unique
    private void drawCorner(DrawContext context, int cx, int cy, int r, int color, int angle) {
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                if (Math.sqrt(i * i + j * j) <= r) {
                    int x = (angle == 180 || angle == 90) ? cx - i - 1 : cx + i;
                    int y = (angle == 180 || angle == 270) ? cy - j - 1 : cy + j;
                    context.fill(x, y, x + 1, y + 1, color);
                }
            }
        }
    }
}