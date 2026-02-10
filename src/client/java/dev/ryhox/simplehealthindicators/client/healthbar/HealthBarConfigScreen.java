package dev.ryhox.simplehealthindicators.client.healthbar;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class HealthBarConfigScreen extends Screen {
    private static final int MODE_W = 110;
    private static final int MODE_H = 20;
    private static final int GAP = 6;

    private static final int ACTION_W = 200;

    private final Screen parent;
    private HealthBarState.Mode mode;

    private ButtonWidget barButton;
    private ButtonWidget heartsButton;
    private ButtonWidget numericButton;

    public HealthBarConfigScreen(Screen parent) {
        super(Text.literal("Simple Health Indicators"));
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

        barButton = addDrawableChild(ButtonWidget.builder(Text.empty(),
                        b -> setMode(HealthBarState.Mode.BAR))
                .dimensions(startX, buttonsY, MODE_W, MODE_H)
                .build());

        heartsButton = addDrawableChild(ButtonWidget.builder(Text.empty(),
                        b -> setMode(HealthBarState.Mode.HEARTS))
                .dimensions(startX + MODE_W + GAP, buttonsY, MODE_W, MODE_H)
                .build());

        numericButton = addDrawableChild(ButtonWidget.builder(Text.empty(),
                        b -> setMode(HealthBarState.Mode.NUMERIC))
                .dimensions(startX + (MODE_W + GAP) * 2, buttonsY, MODE_W, MODE_H)
                .build());

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Reset").formatted(Formatting.RED),
                        b -> setMode(HealthBarState.Mode.BAR))
                .dimensions(cx - ACTION_W / 2, buttonsY + MODE_H + 10, ACTION_W, MODE_H)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx - ACTION_W / 2, this.height - 28, ACTION_W, MODE_H)
                .build());

        refreshLabels();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
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

    private Text label(HealthBarState.Mode check, String label) {
        return mode == check
                ? Text.literal(label).formatted(Formatting.GREEN)
                : Text.literal(label);
    }
}
