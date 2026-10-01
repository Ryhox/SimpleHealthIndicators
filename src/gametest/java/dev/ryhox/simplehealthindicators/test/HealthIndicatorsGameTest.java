package dev.ryhox.simplehealthindicators.test;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarConfigScreen;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarState;
import dev.ryhox.simplehealthindicators.client.healthbar.HealthBarRenderStateAccess;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class HealthIndicatorsGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext ctx) {
        try (TestSingleplayerContext sp = ctx.worldBuilder().adjustSettings(settings -> {
            settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            settings.setAllowCommands(true);
        }).create()) {
            sp.getConnection().waitForChunksRender();
            ctx.getInput().resizeWindow(1280, 720);
            ctx.runOnClient(mc -> {
                mc.options.guiScale().set(2);
                mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
            });
            command(ctx, "fill -12 99 -12 12 99 24 minecraft:grass_block",
                    "fill -12 100 -12 12 115 24 minecraft:air", "tp @s 0 100 0 0 0",
                    "time set day", "weather clear",
                    "summon minecraft:sheep 0 100 6 {NoAI:1b,NoGravity:1b,Health:5.0f,CustomName:'Health Test'}",
                    "summon minecraft:iron_golem -4 100 9 {NoAI:1b,NoGravity:1b,Health:55.0f}");
            ctx.waitTicks(30);
            sp.getConnection().waitForChunksRender();
            ctx.runOnClient(mc -> {
                if (!((Object) new LivingEntityRenderState() instanceof HealthBarRenderStateAccess))
                    throw new AssertionError("Health render state mixin was not applied");
                mc.player.setXRot(8);
            });
            for (HealthBarState.Mode mode : HealthBarState.Mode.values()) {
                ctx.runOnClient(mc -> HealthBarState.MODE = mode);
                ctx.waitTicks(5);
                ctx.takeScreenshot("26.3-health-" + mode.name().toLowerCase());
            }
            // Vanilla sends detailed effect and absorption updates to the
            // affected player; verify them using that real network path.
            command(ctx, "effect give @s minecraft:poison infinite 0 true",
                    "effect give @s minecraft:absorption infinite 0 true");
            ctx.runOnClient(mc -> {
                HealthBarState.MODE = HealthBarState.Mode.HEARTS;
                mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
                mc.player.setXRot(0);
            });
            ctx.waitTicks(8);
            ctx.runOnClient(mc -> {
                if (!mc.player.hasEffect(net.minecraft.world.effect.MobEffects.POISON) || mc.player.getAbsorptionAmount() <= 0)
                    throw new AssertionError("Player effect/absorption not synchronized");
            });
            ctx.takeScreenshot("26.3-health-poison-absorption");
            command(ctx, "effect clear @s minecraft:poison",
                    "effect give @s minecraft:wither infinite 0 true");
            ctx.waitTicks(8);
            ctx.takeScreenshot("26.3-health-wither-absorption");
            ctx.runOnClient(mc -> HealthBarState.MODE = HealthBarState.Mode.NUMERIC);
            ctx.waitTicks(5);
            ctx.takeScreenshot("26.3-player-health-numeric");
            ctx.runOnClient(mc -> {
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                mc.options.improvedTransparency().set(true);
                mc.player.setXRot(8);
                HealthBarState.MODE = HealthBarState.Mode.BAR;
                HealthBarState.EXTRA_LINES = 2;
            });
            ctx.waitTicks(8);
            ctx.takeScreenshot("26.3-health-transparency-offset");
            ctx.runOnClient(mc -> HealthBarState.EXTRA_LINES = 0);
            ctx.getInput().pressKey(InputConstants.KEY_F1);
            ctx.waitTicks(5);
            ctx.takeScreenshot("26.3-health-hidden-hud");
            ctx.getInput().pressKey(InputConstants.KEY_F1);
            ctx.setScreen(() -> new HealthBarConfigScreen(null));
            ctx.waitForScreen(HealthBarConfigScreen.class);
            ctx.takeScreenshot("26.3-health-config");
            ctx.getInput().pressKey(InputConstants.KEY_ESCAPE);
            ctx.waitTicks(5);
            ctx.runOnClient(mc -> HealthBarState.MODE = HealthBarState.Mode.BAR);
        }
    }

    private static void command(ClientGameTestContext ctx, String... commands) {
        ctx.runOnClient(mc -> { for (String command : commands) mc.player.connection.sendCommand(command); });
    }
}
