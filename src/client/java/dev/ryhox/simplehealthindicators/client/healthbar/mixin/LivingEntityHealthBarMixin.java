package dev.ryhox.simplehealthindicators.client.healthbar.mixin;

import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarRenderStateAccess;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityHealthBarMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    // ---------- CONFIG ----------
    @Unique private static final double BASE_Y = 0.75;
    @Unique private static final float NAMETAG_SCALE = 0.025f;

    // --------- HEART TEXTURES ---------
    @Unique private static final Identifier HEART_CONTAINER_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/container.png");
    @Unique private static final Identifier HEART_FULL_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/full.png");
    @Unique private static final Identifier HEART_HALF_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/half.png");

    @Unique private static final Identifier HEART_CONTAINER_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/container_hardcore.png");
    @Unique private static final Identifier HEART_FULL_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/hardcore_full.png");
    @Unique private static final Identifier HEART_HALF_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/hardcore_half.png");

    @Unique private static final Identifier HEART_FULL_POISON_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/poisoned_full.png");
    @Unique private static final Identifier HEART_HALF_POISON_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/poisoned_half.png");
    @Unique private static final Identifier HEART_FULL_POISON_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/poisoned_hardcore_full.png");
    @Unique private static final Identifier HEART_HALF_POISON_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/poisoned_hardcore_half.png");

    @Unique private static final Identifier HEART_FULL_WITHER_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/withered_full.png");
    @Unique private static final Identifier HEART_HALF_WITHER_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/withered_half.png");
    @Unique private static final Identifier HEART_FULL_WITHER_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/withered_hardcore_full.png");
    @Unique private static final Identifier HEART_HALF_WITHER_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/withered_hardcore_half.png");

    @Unique private static final Identifier HEART_FULL_ABS_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/absorbing_full.png");
    @Unique private static final Identifier HEART_HALF_ABS_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/absorbing_half.png");
    @Unique private static final Identifier HEART_FULL_ABS_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/absorbing_hardcore_full.png");
    @Unique private static final Identifier HEART_HALF_ABS_HC_TEX =
            Identifier.ofVanilla("textures/gui/sprites/hud/heart/absorbing_hardcore_half.png");

    @Unique private static final int HEART_SIZE = 9;
    @Unique private static final int HEART_SPACING = 9;
    @Unique private static final float HEART_Z_CONTAINER = 0.0f;
    @Unique private static final float HEART_Z_FILL = 0.05f;
    @Unique private static final int HEARTS_PER_ROW = 10;
    @Unique private static final float HUD_Z_PUSH = 0.1f;

    // --------- BAR TEXTURES ---------
    @Unique private static final Identifier BAR_BG_TEX =
            Identifier.of("simplehealthindicators", "textures/healthbar/bar_bg.png");
    @Unique private static final Identifier BAR_FILL_TEX =
            Identifier.of("simplehealthindicators", "textures/healthbar/bar_fill.png");

    @Unique private static final int BAR_TEX_W = 64;
    @Unique private static final int BAR_TEX_H = 10;
    @Unique private static final int BAR_SKIP_L = 6;
    @Unique private static final int BAR_SKIP_R = 2;
    @Unique private static final float BAR_Z_BG = 0.0f;
    @Unique private static final float BAR_Z_FILL = 0.05f;

    @Unique private static final int MAX_LIGHT = LightmapTextureManager.MAX_LIGHT_COORDINATE;

    @Shadow
    protected abstract boolean hasLabel(T entity, double squaredDistanceToCamera);


    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void shi$capture(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (entity == null || state == null) return;

        HealthBarRenderStateAccess acc = (HealthBarRenderStateAccess) state;
        acc.shi$setHealth(entity.getHealth());
        acc.shi$setMaxHealth(entity.getMaxHealth());
        acc.shi$setAbsorption(entity.getAbsorptionAmount());

        acc.shi$setPoisoned(entity.hasStatusEffect(StatusEffects.POISON));
        acc.shi$setWithered(entity.hasStatusEffect(StatusEffects.WITHER));

        boolean hasLabel = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.getEntityRenderDispatcher() != null) {
            double d = mc.getEntityRenderDispatcher().getSquaredDistanceToCamera(entity);
            hasLabel = this.hasLabel(entity, d);
        }
        acc.shi$setHasLabel(hasLabel);

        boolean hasScoreboardDisplay = false;
        if (entity instanceof PlayerLikeEntity player && entity.getEntityWorld() != null) {
            Scoreboard scoreboard = entity.getEntityWorld().getScoreboard();
            ScoreboardObjective belowName = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
            if (belowName != null) {
                ReadableScoreboardScore score = scoreboard.getScore(player, belowName);
                hasScoreboardDisplay = score != null;
            }
        }
        acc.shi$setHasScoreboardDisplay(hasScoreboardDisplay);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void shi$render(
            S state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState,
            CallbackInfo ci
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        if (state == null) return;

        HealthBarRenderStateAccess acc = (HealthBarRenderStateAccess) state;
        float max = acc.shi$getMaxHealth();
        if (max <= 0f) return;

        float hp = MathHelper.clamp(acc.shi$getHealth(), 0f, max);
        float absorption = Math.max(0f, acc.shi$getAbsorption());
        float pct = MathHelper.clamp((hp + absorption) / max, 0f, 1f);

        matrices.push();

        double y = state.height + BASE_Y;
        int extraLines = shi$getExtraNameplateLines(mc, state, acc);
        if (extraLines > 0) {
            float lineStepWorld = (mc.textRenderer.fontHeight + 1) * NAMETAG_SCALE; // +1 matches vanilla-ish spacing
            y += lineStepWorld * extraLines;
        }

        matrices.translate(0.0, y, 0.0);
        matrices.multiply(cameraState.orientation);
        matrices.scale(NAMETAG_SCALE, NAMETAG_SCALE, NAMETAG_SCALE);

        matrices.translate(0.0, 0.0, HUD_Z_PUSH);

        switch (HealthBarState.MODE) {
            case BAR -> shi$bar(queue, matrices, pct, hp);
            case HEARTS -> shi$hearts(queue, matrices, hp, max, absorption, acc.shi$isPoisoned(), acc.shi$isWithered());
            case NUMERIC -> shi$numeric(queue, matrices, hp, max, absorption, acc.shi$isPoisoned(), acc.shi$isWithered());
        }

        matrices.pop();
    }

    @Unique
    private static void shi$numeric(
            OrderedRenderCommandQueue queue,
            MatrixStack matrices,
            float hp,
            float max,
            float absorption,
            boolean poisoned,
            boolean withered
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float shownHp = Math.max(0f, hp + absorption);
        float shownMax = Math.max(0f, max);

        int heartColor;
        if (withered) {
            heartColor = 0xFF555555;
        } else if (poisoned) {
            heartColor = 0xFF55AA55;
        } else if (absorption > 0f) {
            heartColor = 0xFFFFFF55;
        } else {
            heartColor = 0xFFAA0000;
        }
        String numbers = String.format(java.util.Locale.ROOT, "%.2f/%.2f", shownHp, shownMax);
        Text text = Text.literal(numbers).append(Text.literal("\u2665").setStyle(Style.EMPTY.withColor(heartColor)));
        float x = -mc.textRenderer.getWidth(text) / 2.0f;
        float y = -mc.textRenderer.fontHeight / 2.0f;

        matrices.push();
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(180));

        OrderedText ordered = text.asOrderedText();
        queue.submitText(
                matrices,
                x,
                y,
                ordered,
                false,
                net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                MAX_LIGHT,
                0xFFFFFFFF,
                0,
                0
        );

        matrices.pop();
    }

    @Unique
    private static void shi$bar(OrderedRenderCommandQueue queue, MatrixStack matrices, float pct, float hp) {
        pct = MathHelper.clamp(pct, 0f, 1f);

        final int w = BAR_TEX_W;
        final int h = BAR_TEX_H;
        final int skipL = BAR_SKIP_L;
        final int skipR = BAR_SKIP_R;

        int x = -w / 2;
        int y = -h / 2;

        int texInnerW = w - skipL - skipR;
        if (texInnerW <= 0) return;

        int fillTexW = MathHelper.floor(texInnerW * pct);
        if (pct > 0f && hp > 0f && fillTexW == 0) fillTexW = 1;

        shi$submitTexturedRect(queue, matrices, BAR_BG_TEX, x, y, w, h, 0f, 0f, 1f, 1f, BAR_Z_BG, false);

        if (fillTexW > 0) {
            int fx = x + skipL;
            int fw = fillTexW;

            float u0 = skipL / (float) w;
            float u1 = (skipL + fillTexW) / (float) w;

            if (fillTexW >= texInnerW) u1 = (skipL + texInnerW) / (float) w - 0.0005f;

            shi$submitTexturedRect(queue, matrices, BAR_FILL_TEX, fx, y, fw, h, u0, 0f, u1, 1f, BAR_Z_FILL, true);
        }
    }

    @Unique
    private static void shi$hearts(OrderedRenderCommandQueue queue, MatrixStack matrices,
                                   float hp, float max, float absorption,
                                   boolean poisoned, boolean withered) {

        MinecraftClient mc = MinecraftClient.getInstance();
        boolean hardcore = mc.world != null && mc.world.getLevelProperties().isHardcore();

        Identifier containerTex = hardcore ? HEART_CONTAINER_HC_TEX : HEART_CONTAINER_TEX;

        Identifier fullTex;
        Identifier halfTex;

        if (withered) {
            fullTex = hardcore ? HEART_FULL_WITHER_HC_TEX : HEART_FULL_WITHER_TEX;
            halfTex = hardcore ? HEART_HALF_WITHER_HC_TEX : HEART_HALF_WITHER_TEX;
        } else if (poisoned) {
            fullTex = hardcore ? HEART_FULL_POISON_HC_TEX : HEART_FULL_POISON_TEX;
            halfTex = hardcore ? HEART_HALF_POISON_HC_TEX : HEART_HALF_POISON_TEX;
        } else {
            fullTex = hardcore ? HEART_FULL_HC_TEX : HEART_FULL_TEX;
            halfTex = hardcore ? HEART_HALF_HC_TEX : HEART_HALF_TEX;
        }

        int maxHearts = MathHelper.ceil(max / 2f);
        int fullHearts = MathHelper.floor(hp / 2f);
        boolean half = (hp % 2f) >= 1f || (hp > 0f && fullHearts == 0);

        int absHearts = MathHelper.ceil(absorption / 2f);
        int absFull = MathHelper.floor(absorption / 2f);
        boolean absHalf = (absorption % 2f) >= 1f;

        int yHealth = -10;
        int rowStride = HEART_SIZE + 2;

// Deine Welt-/Billboard-Koordinate ist bei dir anscheinend so:
// y++ = "hoch", y-- = "runter"  (weil es aktuell nach unten stapelt)
        int maxRows = MathHelper.ceil(maxHearts / (float) HEARTS_PER_ROW);

// Für Absorption: links ausrichten am linken Rand der "vollen" Health-Row-Breite (max 10 Herzen)
        int baseLeftX = -((HEARTS_PER_ROW * HEART_SPACING) / 2); // fixer linker Rand


// --- HEALTH (stack UP) ---
        for (int row = 0; row < maxRows; row++) {
            int rowStartIndex = row * HEARTS_PER_ROW;
            int rowHearts = Math.min(HEARTS_PER_ROW, maxHearts - rowStartIndex);

            int startX = baseLeftX;

            int yRow = yHealth + row * rowStride; // <-- stack nach oben

            shi$submitHeartsBatch(queue, matrices, startX, yRow, rowHearts, containerTex, HEART_Z_CONTAINER, false);

            int fullInRow = MathHelper.clamp(fullHearts - rowStartIndex, 0, rowHearts);
            if (fullInRow > 0) {
                shi$submitHeartsBatch(queue, matrices, startX, yRow, fullInRow, fullTex, HEART_Z_FILL, true);
            }

            if (half) {
                int halfIndex = fullHearts;
                if (halfIndex >= rowStartIndex && halfIndex < rowStartIndex + rowHearts) {
                    int x = startX + (halfIndex - rowStartIndex) * HEART_SPACING;
                    shi$submitSingle(queue, matrices, x, yRow, halfTex, HEART_Z_FILL, true);
                }
            }
        }

        if (absHearts > 0) {
            Identifier absFullTex = hardcore ? HEART_FULL_ABS_HC_TEX : HEART_FULL_ABS_TEX;
            Identifier absHalfTex = hardcore ? HEART_HALF_ABS_HC_TEX : HEART_HALF_ABS_TEX;

            int yAbsBase = yHealth + maxRows * rowStride;

            int absRows = MathHelper.ceil(absHearts / (float) HEARTS_PER_ROW);
            for (int row = 0; row < absRows; row++) {
                int rowStartIndex = row * HEARTS_PER_ROW;
                int rowHearts = Math.min(HEARTS_PER_ROW, absHearts - rowStartIndex);

                int startX = baseLeftX;

                int yRow = yAbsBase + row * rowStride;

                shi$submitHeartsBatch(queue, matrices, startX, yRow, rowHearts, containerTex, HEART_Z_CONTAINER, false);

                int fullInRow = MathHelper.clamp(absFull - rowStartIndex, 0, rowHearts);
                if (fullInRow > 0) {
                    shi$submitHeartsBatch(queue, matrices, startX, yRow, fullInRow, absFullTex, HEART_Z_FILL, true);
                }

                if (absHalf) {
                    int halfIndex = absFull;
                    if (halfIndex >= rowStartIndex && halfIndex < rowStartIndex + rowHearts) {
                        int x = startX + (halfIndex - rowStartIndex) * HEART_SPACING;
                        shi$submitSingle(queue, matrices, x, yRow, absHalfTex, HEART_Z_FILL, true);
                    }
                }
            }
        }

    }

    @Unique
    private static void shi$submitTexturedRect(
            OrderedRenderCommandQueue queue,
            MatrixStack matrices,
            Identifier tex, int x, int y, int w, int h,
            float u0, float v0, float u1, float v1,
            float z, boolean foreground
    ) {
        RenderLayer layer = shi$layerFor(tex, foreground);
        queue.submitCustom(matrices, layer, (entry, vc) -> {
            shi$vertex(vc, entry, x,     y,     z, u0, v1);
            shi$vertex(vc, entry, x + w, y,     z, u1, v1);
            shi$vertex(vc, entry, x + w, y + h, z, u1, v0);
            shi$vertex(vc, entry, x,     y + h, z, u0, v0);
        });
    }

    @Unique
    private static void shi$submitHeartsBatch(
            OrderedRenderCommandQueue queue,
            MatrixStack matrices,
            int startX, int y, int count, Identifier tex, float z, boolean foreground
    ) {
        if (count <= 0) return;

        RenderLayer layer = shi$layerFor(tex, foreground);
        queue.submitCustom(matrices, layer, (entry, vc) -> {
            for (int i = 0; i < count; i++) {
                int x = startX + i * HEART_SPACING;
                shi$quadTex01(entry, vc, x, y, HEART_SIZE, HEART_SIZE, z);
            }
        });
    }

    @Unique
    private static void shi$submitSingle(
            OrderedRenderCommandQueue queue,
            MatrixStack matrices,
            int x, int y, Identifier tex, float z, boolean foreground
    ) {
        RenderLayer layer = shi$layerFor(tex, foreground);
        queue.submitCustom(matrices, layer, (entry, vc) -> shi$quadTex01(entry, vc, x, y, HEART_SIZE, HEART_SIZE, z));
    }

    @Unique
    private static void shi$quadTex01(MatrixStack.Entry entry, VertexConsumer vc, int x, int y, int w, int h, float z) {
        shi$vertex(vc, entry, x,     y,     z, 0f, 1f);
        shi$vertex(vc, entry, x + w, y,     z, 1f, 1f);
        shi$vertex(vc, entry, x + w, y + h, z, 1f, 0f);
        shi$vertex(vc, entry, x,     y + h, z, 0f, 0f);
    }

    @Unique
    private static void shi$vertex(VertexConsumer vc, MatrixStack.Entry entry,
                                   float x, float y, float z,
                                   float u, float v) {
        vc.vertex(entry, x, y, z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(MAX_LIGHT)
                .normal(0f, 1f, 0f);
    }

    @Unique
    private static RenderLayer shi$layerFor(Identifier tex, boolean foreground) {
        if (foreground) {
            return RenderLayers.entityCutoutNoCullZOffset(tex);
        }
        return RenderLayers.entityCutoutNoCull(tex);
    }

    @Unique
    private static int shi$getExtraNameplateLines(
            MinecraftClient mc,
            LivingEntityRenderState state,
            HealthBarRenderStateAccess acc
    ) {
        int lines = 0;

        if (state.displayName != null) {
            int rendered = mc.textRenderer.wrapLines(state.displayName, 150).size();
            lines += Math.max(0, rendered - 1);
        }

        // BELOW_NAME line (vanilla scoreboard objective)
        if (acc.shi$hasScoreboardDisplay() || shi$hasServerBelowNameScore(mc, state)) lines += 1;

        return lines;
    }


    @Unique
    private static int shi$countLines(Text text) {
        String s = text.getString();
        if (s.isEmpty()) return 1;

        int lines = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') lines++;
        }
        return lines;
    }

    @Unique
    private static boolean shi$hasServerBelowNameScore(MinecraftClient mc, LivingEntityRenderState state) {
        if (mc.world == null) return false;
        if (!(state instanceof PlayerEntityRenderState playerState)) return false;

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective belowName = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        if (belowName == null) return false;

        Entity entity = mc.world.getEntityById(playerState.id);
        if (!(entity instanceof PlayerLikeEntity player)) return false;

        ReadableScoreboardScore score = scoreboard.getScore(player, belowName);
        return score != null;
    }


}

