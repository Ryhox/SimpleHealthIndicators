package dev.ryhox.simplehealthindicators.client.healthbar.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarRenderStateAccess;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
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
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/container.png");
    @Unique private static final Identifier HEART_FULL_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/full.png");
    @Unique private static final Identifier HEART_HALF_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/half.png");

    @Unique private static final Identifier HEART_CONTAINER_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/container_hardcore.png");
    @Unique private static final Identifier HEART_FULL_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/hardcore_full.png");
    @Unique private static final Identifier HEART_HALF_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/hardcore_half.png");

    @Unique private static final Identifier HEART_FULL_POISON_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/poisoned_full.png");
    @Unique private static final Identifier HEART_HALF_POISON_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/poisoned_half.png");
    @Unique private static final Identifier HEART_FULL_POISON_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/poisoned_hardcore_full.png");
    @Unique private static final Identifier HEART_HALF_POISON_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/poisoned_hardcore_half.png");

    @Unique private static final Identifier HEART_FULL_WITHER_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/withered_full.png");
    @Unique private static final Identifier HEART_HALF_WITHER_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/withered_half.png");
    @Unique private static final Identifier HEART_FULL_WITHER_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/withered_hardcore_full.png");
    @Unique private static final Identifier HEART_HALF_WITHER_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/withered_hardcore_half.png");

    @Unique private static final Identifier HEART_FULL_ABS_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/absorbing_full.png");
    @Unique private static final Identifier HEART_HALF_ABS_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/absorbing_half.png");
    @Unique private static final Identifier HEART_FULL_ABS_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/absorbing_hardcore_full.png");
    @Unique private static final Identifier HEART_HALF_ABS_HC_TEX =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/heart/absorbing_hardcore_half.png");

    @Unique private static final int HEART_SIZE = 9;
    @Unique private static final int HEART_SPACING = 9;
    @Unique private static final float HEART_Z_CONTAINER = 0.0f;
    @Unique private static final float HEART_Z_FILL = 0.05f;
    @Unique private static final int HEARTS_PER_ROW = 10;
    @Unique private static final float HUD_Z_PUSH = 0.1f;

    // --------- BAR TEXTURES ---------
    @Unique private static final Identifier BAR_BG_TEX =
            Identifier.fromNamespaceAndPath("simplehealthindicators", "textures/healthbar/bar_bg.png");
    @Unique private static final Identifier BAR_FILL_TEX =
            Identifier.fromNamespaceAndPath("simplehealthindicators", "textures/healthbar/bar_fill.png");

    @Unique private static final int BAR_TEX_W = 64;
    @Unique private static final int BAR_TEX_H = 10;
    @Unique private static final int BAR_SKIP_L = 6;
    @Unique private static final int BAR_SKIP_R = 2;
    @Unique private static final float BAR_Z_BG = 0.0f;
    @Unique private static final float BAR_Z_FILL = 0.05f;

    @Unique private static final int MAX_LIGHT = 0xF000F0;

    @Shadow
    protected abstract boolean shouldShowName(T entity, double squaredDistanceToCamera);


    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void shi$capture(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (entity == null || state == null) return;

        HealthBarRenderStateAccess acc = (HealthBarRenderStateAccess) state;
        acc.shi$setHealth(entity.getHealth());
        acc.shi$setMaxHealth(entity.getMaxHealth());
        acc.shi$setAbsorption(entity.getAbsorptionAmount());

        acc.shi$setPoisoned(entity.hasEffect(MobEffects.POISON));
        acc.shi$setWithered(entity.hasEffect(MobEffects.WITHER));

        boolean hasLabel = this.shouldShowName(entity, state.distanceToCameraSq);
        acc.shi$setHasLabel(hasLabel);

        boolean hasScoreboardDisplay = false;
        if (entity instanceof Player player && entity.level() != null) {
            Scoreboard scoreboard = entity.level().getScoreboard();
            Objective belowName = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (belowName != null) {
                ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(player, belowName);
                hasScoreboardDisplay = score != null;
            }
        }
        acc.shi$setHasScoreboardDisplay(hasScoreboardDisplay);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD")
    )
    private void shi$render(
            S state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState,
            CallbackInfo ci
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (state == null) return;

        HealthBarRenderStateAccess acc = (HealthBarRenderStateAccess) state;
        float max = acc.shi$getMaxHealth();
        if (max <= 0f) return;

        float hp = Mth.clamp(acc.shi$getHealth(), 0f, max);
        float absorption = Math.max(0f, acc.shi$getAbsorption());
        float pct = Mth.clamp((hp + absorption) / max, 0f, 1f);

        matrices.pushPose();

        double y = state.boundingBoxHeight + BASE_Y;
        int extraLines = shi$getExtraNameplateLines(mc, state, acc);
        if (extraLines > 0) {
            float lineStepWorld = (mc.font.lineHeight + 1) * NAMETAG_SCALE;
            y += lineStepWorld * extraLines;
        }

        matrices.translate(0.0, y, 0.0);
        matrices.mulPose(cameraState.orientation);
        matrices.scale(NAMETAG_SCALE, NAMETAG_SCALE, NAMETAG_SCALE);

        matrices.translate(0.0, 0.0, HUD_Z_PUSH);

        switch (HealthBarState.MODE) {
            case BAR -> shi$bar(queue, matrices, pct, hp);
            case HEARTS -> shi$hearts(queue, matrices, hp, max, absorption, acc.shi$isPoisoned(), acc.shi$isWithered());
            case NUMERIC -> shi$numeric(queue, matrices, hp, max, absorption, acc.shi$isPoisoned(), acc.shi$isWithered());
        }

        matrices.popPose();
    }

    @Unique
    private static void shi$numeric(
            SubmitNodeCollector queue,
            PoseStack matrices,
            float hp,
            float max,
            float absorption,
            boolean poisoned,
            boolean withered
    ) {
        Minecraft mc = Minecraft.getInstance();
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
        Component text = Component.literal(numbers).append(Component.literal("♥").setStyle(Style.EMPTY.withColor(heartColor)));
        float x = -mc.font.width(text) / 2.0f;
        float y = -mc.font.lineHeight / 2.0f;

        matrices.pushPose();
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        FormattedCharSequence ordered = Language.getInstance().getVisualOrder(text);
        queue.submitText(
                matrices,
                x,
                y,
                ordered,
                false,
                Font.DisplayMode.NORMAL,
                MAX_LIGHT,
                0xFFFFFFFF,
                0,
                0
        );

        matrices.popPose();
    }

    @Unique
    private static void shi$bar(SubmitNodeCollector queue, PoseStack matrices, float pct, float hp) {
        pct = Mth.clamp(pct, 0f, 1f);

        final int w = BAR_TEX_W;
        final int h = BAR_TEX_H;
        final int skipL = BAR_SKIP_L;
        final int skipR = BAR_SKIP_R;

        int x = -w / 2;
        int y = -h / 2;

        int texInnerW = w - skipL - skipR;
        if (texInnerW <= 0) return;

        int fillTexW = Mth.floor(texInnerW * pct);
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
    private static void shi$hearts(SubmitNodeCollector queue, PoseStack matrices,
                                   float hp, float max, float absorption,
                                   boolean poisoned, boolean withered) {

        Minecraft mc = Minecraft.getInstance();
        boolean hardcore = mc.level != null && mc.level.getLevelData().isHardcore();

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

        int maxHearts = Mth.ceil(max / 2f);
        int fullHearts = Mth.floor(hp / 2f);
        boolean half = (hp % 2f) >= 1f || (hp > 0f && fullHearts == 0);

        int absHearts = Mth.ceil(absorption / 2f);
        int absFull = Mth.floor(absorption / 2f);
        boolean absHalf = (absorption % 2f) >= 1f;

        int yHealth = -10;
        int rowStride = HEART_SIZE + 2;

        int maxRows = Mth.ceil(maxHearts / (float) HEARTS_PER_ROW);

        for (int row = 0; row < maxRows; row++) {
            int rowStartIndex = row * HEARTS_PER_ROW;
            int rowHearts = Math.min(HEARTS_PER_ROW, maxHearts - rowStartIndex);

            int startX = -(rowHearts * HEART_SPACING / 2);

            int yRow = yHealth + row * rowStride;

            shi$submitHeartsBatch(queue, matrices, startX, yRow, rowHearts, containerTex, HEART_Z_CONTAINER, false);

            int fullInRow = Mth.clamp(fullHearts - rowStartIndex, 0, rowHearts);
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

            int absRows = Mth.ceil(absHearts / (float) HEARTS_PER_ROW);
            for (int row = 0; row < absRows; row++) {
                int rowStartIndex = row * HEARTS_PER_ROW;
                int rowHearts = Math.min(HEARTS_PER_ROW, absHearts - rowStartIndex);

                int startX = -(rowHearts * HEART_SPACING / 2);

                int yRow = yAbsBase + row * rowStride;

                shi$submitHeartsBatch(queue, matrices, startX, yRow, rowHearts, containerTex, HEART_Z_CONTAINER, false);

                int fullInRow = Mth.clamp(absFull - rowStartIndex, 0, rowHearts);
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
            SubmitNodeCollector queue,
            PoseStack matrices,
            Identifier tex, int x, int y, int w, int h,
            float u0, float v0, float u1, float v1,
            float z, boolean foreground
    ) {
        RenderType layer = shi$layerFor(tex, foreground);
        queue.submitCustomGeometry(matrices, layer, (entry, vc) -> {
            shi$vertex(vc, entry, x,     y,     z, u0, v1);
            shi$vertex(vc, entry, x + w, y,     z, u1, v1);
            shi$vertex(vc, entry, x + w, y + h, z, u1, v0);
            shi$vertex(vc, entry, x,     y + h, z, u0, v0);
        });
    }

    @Unique
    private static void shi$submitHeartsBatch(
            SubmitNodeCollector queue,
            PoseStack matrices,
            int startX, int y, int count, Identifier tex, float z, boolean foreground
    ) {
        if (count <= 0) return;

        RenderType layer = shi$layerFor(tex, foreground);
        queue.submitCustomGeometry(matrices, layer, (entry, vc) -> {
            for (int i = 0; i < count; i++) {
                int x = startX + i * HEART_SPACING;
                shi$quadTex01(entry, vc, x, y, HEART_SIZE, HEART_SIZE, z);
            }
        });
    }

    @Unique
    private static void shi$submitSingle(
            SubmitNodeCollector queue,
            PoseStack matrices,
            int x, int y, Identifier tex, float z, boolean foreground
    ) {
        RenderType layer = shi$layerFor(tex, foreground);
        queue.submitCustomGeometry(matrices, layer, (entry, vc) -> shi$quadTex01(entry, vc, x, y, HEART_SIZE, HEART_SIZE, z));
    }

    @Unique
    private static void shi$quadTex01(PoseStack.Pose entry, VertexConsumer vc, int x, int y, int w, int h, float z) {
        shi$vertex(vc, entry, x,     y,     z, 0f, 1f);
        shi$vertex(vc, entry, x + w, y,     z, 1f, 1f);
        shi$vertex(vc, entry, x + w, y + h, z, 1f, 0f);
        shi$vertex(vc, entry, x,     y + h, z, 0f, 0f);
    }

    @Unique
    private static void shi$vertex(VertexConsumer vc, PoseStack.Pose entry,
                                   float x, float y, float z,
                                   float u, float v) {
        vc.addVertex(entry, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(MAX_LIGHT)
                .setNormal(entry, 0f, 1f, 0f);
    }

    @Unique
    private static RenderType shi$layerFor(Identifier tex, boolean foreground) {
        if (foreground) {
            return RenderTypes.entityCutoutZOffset(tex);
        }
        return RenderTypes.entityCutout(tex);
    }

    @Unique
    private static int shi$getExtraNameplateLines(
            Minecraft mc,
            LivingEntityRenderState state,
            HealthBarRenderStateAccess acc
    ) {
        int lines = 0;

        if (state.nameTag != null) {
            int rendered = mc.font.split(state.nameTag, 150).size();
            lines += Math.max(0, rendered - 1);
        }

        if (acc.shi$hasScoreboardDisplay() || shi$hasServerBelowNameScore(mc, state)) lines += 1;

        return lines;
    }

    @Unique
    private static boolean shi$hasServerBelowNameScore(Minecraft mc, LivingEntityRenderState state) {
        if (mc.level == null) return false;
        if (!(state instanceof AvatarRenderState playerState)) return false;

        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective belowName = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
        if (belowName == null) return false;

        Entity entity = mc.level.getEntity(playerState.id);
        if (!(entity instanceof Player player)) return false;

        ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(player, belowName);
        return score != null;
    }


}
