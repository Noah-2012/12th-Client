package com.noadsch12.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

@Mixin(ChatHud.class)
public abstract class ChatAnimationMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;
    @Shadow public abstract int getWidth();
    @Shadow public abstract double getChatScale();

    @Unique
    private final Map<String, Identifier> skinCache = new HashMap<>();
    @Unique
    private final Set<String> loadingSkins = new HashSet<>();
    @Unique
    private final Set<String> failedSkins = Collections.synchronizedSet(new HashSet<>());

    @Unique
    private long lastMessageTime = 0;

    @Unique
    // Das \\s* erlaubt beliebig viele Leerzeichen am Anfang, bevor das < kommt
    private static final Pattern PLAYER_NAME_PATTERN_SERVER1 = Pattern.compile("^\\s*([^:]+):");

    @Unique
    private static final Pattern PLAYER_NAME_PATTERN_VANILLA = Pattern.compile("^\\s*<([^>]+)>\\s");

    @Unique
    private static final int HEAD_SIZE = 8;

    @Inject(method = "addVisibleMessage", at = @At("TAIL"))
    private void onAddMessage(ChatHudLine line, CallbackInfo ci) {
        this.lastMessageTime = System.currentTimeMillis();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void startAnimation(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        context.getMatrices().pushMatrix();

        if (!visibleMessages.isEmpty()) {
            long delta = System.currentTimeMillis() - lastMessageTime;
            long duration = 300;

            if (delta < duration) {
                float progress = (float) delta / duration;
                float ease = 1.0f - (float) Math.pow(1.0 - progress, 4);
                float yOffset = 12.0f * (1.0f - ease);
                context.getMatrices().translate(0.0f, yOffset);
            }
        }
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), argsOnly = true)
    private Text addSpaceBeforeMessage(Text text) {
        // Wir erstellen ein neues Text-Objekt:
        // 4 Leerzeichen entsprechen ca. 12-16 Pixeln Platz für den Kopf
        return Text.literal("  ").append(text);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderPlayerHeads(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        if (visibleMessages.isEmpty() || client.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN) return;

        // 1. Chat-Abmessungen exakt wie Minecraft berechnen
        int chatWidth = (int) (this.getWidth()); // Methode aus ChatHud
        int chatHeight = (int) (focused ? client.inGameHud.getChatHud().getHeight() : client.inGameHud.getChatHud().getHeight() * client.options.getChatHeightUnfocused().getValue());

        int windowHeight = context.getScaledWindowHeight();
        int chatX = 2;
        int chatY = windowHeight - 40;

        // 2. SCISSOR START: Alles außerhalb dieses Bereichs wird weggeschnitten
        // Wir setzen den Bereich auf die exakte Chat-Box
        if (focused) {
            context.enableScissor(0, windowHeight - 40 - chatHeight, chatWidth + 20, windowHeight - 40);
        } else {
            context.enableScissor(0, windowHeight - 90 - chatHeight, chatWidth + 20, windowHeight - 40);
        }

        for (int i = 0; i < visibleMessages.size(); i++) {
            ChatHudLine.Visible line = visibleMessages.get(i);
            if (line == null) continue;

            int opacity = getMessageOpacity(line, currentTick);
            if (opacity <= 5) continue;

            int lineY = chatY - (i * 9) - 9;

            // Namen extrahieren
            StringBuilder sb = new StringBuilder();
            line.content().accept((index, style, codePoint) -> {
                sb.append(Character.toChars(codePoint));
                return true;
            });

            String playerName = extractPlayerName(sb.toString());

            if (playerName != null) {
                Identifier skinTexture = getPlayerSkinTexture(playerName);
                if (skinTexture != null) {
                    int color = (opacity << 24) | 0xFFFFFF;

                    context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTexture,
                            chatX, lineY,
                            8, 8,   // Zielgröße
                            8, 8,   // UV Gesicht
                            64, 64,   // UV Größe
                            64, 64,
                            color);
                }
            }
        }

        // 3. SCISSOR ENDE: Wichtig, sonst wird das restliche GUI auch abgeschnitten!
        context.disableScissor();
    }

    @Unique
    private int getMessageOpacity(ChatHudLine.Visible line, int currentTick) {
        // 1. Alter der Nachricht berechnen
        int age = currentTick - line.addedTime();

        // Minecraft Standard: 200 Ticks (10 Sek) volle Sichtbarkeit
        if (age < 200) {
            return 255;
        }

        // 2. Ausfaden berechnen
        // Nach 200 Ticks wird der Wert innerhalb von 40 Ticks von 255 auf 0 gesenkt
        // Formel: 255 - (Zeitüberschreitung * 255 / Fading-Dauer)
        int opacity = 255 - (age - 200) * 255 / 40;

        // Wert zwischen 0 und 255 halten
        return Math.max(0, Math.min(255, opacity));
    }

    @Unique
    private String extractPlayerName(String message) {
        Matcher matcher1 = PLAYER_NAME_PATTERN_SERVER1.matcher(message);
        Matcher matcher2 = PLAYER_NAME_PATTERN_VANILLA.matcher(message);
        if (matcher1.find()) {
            return matcher1.group(1);
        } else if (matcher2.find()) {
            return matcher2.group(1);
        }
        return null;
    }

    private Identifier getPlayerSkinTexture(String playerName) {
        // 1. Falls der Name ungültig ist, sofort abbrechen
        if (playerName == null || playerName.isEmpty()) return null;

        // 2. Prüfen, ob der Skin bereits im Cache ist
        if (skinCache.containsKey(playerName)) {
            return skinCache.get(playerName);
        }

        // 3. Negative Caching: Wenn der Skin vor kurzem fehlgeschlagen ist, nicht erneut versuchen
        if (failedSkins.contains(playerName)) {
            return null; // Hier könnte man auch einen Standard-Steve-Identifier zurückgeben
        }

        // 4. Prüfen, ob wir gerade laden
        if (loadingSkins.contains(playerName)) {
            return null;
        }

        loadingSkins.add(playerName);

        CompletableFuture.runAsync(() -> {
            try {
                // Namen bereinigen: Nur Buchstaben, Zahlen und Unterstriche erlauben
                // Das entfernt Emojis und Präfixe wie + oder . (häufig bei Bedrock-Spielern)
                String cleanName = playerName.replaceAll("[^a-zA-Z0-9_]", "");

                if (cleanName.isEmpty()) {
                    throw new FileNotFoundException("Ungültiger Name nach Bereinigung");
                }

                URL url = new URL("https://minotar.net/avatar/" + cleanName + "/64.png");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Minecraft-Client-Mod");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new FileNotFoundException("Server antwortete mit Code: " + responseCode);
                }

                try (InputStream is = connection.getInputStream()) {
                    NativeImage image = NativeImage.read(is);

                    client.execute(() -> {
                        try {
                            NativeImageBackedTexture texture = new NativeImageBackedTexture(
                                    () -> "skin_texture_" + playerName.toLowerCase(Locale.ROOT),
                                    image
                            );

                            Identifier id = Identifier.of("noadsch-chat", "skins/" + playerName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", ""));
                            client.getTextureManager().registerTexture(id, texture);

                            skinCache.put(playerName, id);
                        } catch (Exception e) {
                            failedSkins.add(playerName);
                        } finally {
                            loadingSkins.remove(playerName);
                        }
                    });
                }
            } catch (Exception e) {
                // Fehler im Log unterdrücken und in failedSkins speichern
                failedSkins.add(playerName);
                loadingSkins.remove(playerName);
                // Wir loggen nur echte Fehler, keine "nicht gefunden" Meldungen
                if (!(e instanceof FileNotFoundException)) {
                    // Optional: System.err.println("Fehler beim Laden von " + playerName + ": " + e.getMessage());
                }
            }
        });

        return null;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void endAnimation(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }
}