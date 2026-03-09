package antighost.keybind;

import antighost.GhostBlockFixer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {

    private static KeyBinding forceResyncKey;

    public static void register() {
        forceResyncKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.antighost.resync",       // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,              // default: R
                "category.antighost"          // keybind category in Options
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (forceResyncKey.wasPressed()) {
                GhostBlockFixer.queueAreaResync(client);
                if (client.player != null) {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal("§b[AntiGhost] §fForce resync dikirim!"),
                        true // action bar, tidak mengganggu chat
                    );
                }
            }
        });
    }
}
