package dev.ryhox.simplehealthindicators.client.healthbar;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class HealthBarCommand {
    private HealthBarCommand() {}

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("healthbar")
                            .then(argument("type", StringArgumentType.word())
                                    .suggests((ctx, b) -> {
                                        b.suggest("bar");
                                        b.suggest("hearts");
                                        b.suggest("numeric");
                                        return b.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        String type = StringArgumentType.getString(ctx, "type");
                                        HealthBarState.MODE = HealthBarState.Mode.parse(type);
                                        HealthBarConfig.saveMode(HealthBarState.MODE);
                                        ctx.getSource().sendFeedback(Text.literal("Healthbar: " + HealthBarState.MODE.name().toLowerCase()));
                                        return 1;
                                    })
                            )
            );
        });
    }
}
