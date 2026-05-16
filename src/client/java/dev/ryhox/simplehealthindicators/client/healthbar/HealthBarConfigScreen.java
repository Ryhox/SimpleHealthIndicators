package dev.ryhox.simplehealthindicators.client.healthbar;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HealthBarConfigScreen extends Screen {
    private static final int MODE_W = 110;
    private static final int MODE_H = 20;
    private static final int GAP = 6;

    private static final int ACTION_W = 200;

    private final Screen parent;
    private HealthBarState.Mode mode;

    private Button barButton;
    private Button heartsButton;
    private Button numericButton;

    public HealthBarConfigScreen(Screen parent) {
        super(Component.literal("Simple Health Indicators"));
        this.parent = parent;
        this.mode = HealthBarState.MODE;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        int totalW = MODE_W * 3 + GAP * 2;
        int startX = cx - totalW / 2;
        int buttonsY = cy - 20;

        barButton = addRenderableWidget(Button.builder(Component.empty(),
                        _ -> setMode(HealthBarState.Mode.BAR))
                .bounds(startX, buttonsY, MODE_W, MODE_H)
                .build());

        heartsButton = addRenderableWidget(Button.builder(Component.empty(),
                        _ -> setMode(HealthBarState.Mode.HEARTS))
                .bounds(startX + MODE_W + GAP, buttonsY, MODE_W, MODE_H)
                .build());

        numericButton = addRenderableWidget(Button.builder(Component.empty(),
                        _ -> setMode(HealthBarState.Mode.NUMERIC))
                .bounds(startX + (MODE_W + GAP) * 2, buttonsY, MODE_W, MODE_H)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Reset").withStyle(ChatFormatting.RED),
                        _ -> setMode(HealthBarState.Mode.BAR))
                .bounds(cx - ACTION_W / 2, buttonsY + MODE_H + 10, ACTION_W, MODE_H)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Done"), _ -> onClose())
                .bounds(cx - ACTION_W / 2, this.height - 28, ACTION_W, MODE_H)
                .build());

        refreshLabels();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void setMode(HealthBarState.Mode newMode) {
        this.mode = newMode;
        HealthBarState.MODE = newMode;
        HealthBarConfig.saveMode(newMode);
        refreshLabels();
    }

    private void refreshLabels() {
        barButton.setMessage(label(HealthBarState.Mode.BAR, "Bar"));
        heartsButton.setMessage(label(HealthBarState.Mode.HEARTS, "Hearts"));
        numericButton.setMessage(label(HealthBarState.Mode.NUMERIC, "Numeric"));
    }

    private Component label(HealthBarState.Mode check, String label) {
        return mode == check
                ? Component.literal(label).withStyle(ChatFormatting.GREEN)
                : Component.literal(label);
    }
}
