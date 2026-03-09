package antighost;

import antighost.config.AntiGhostConfig;
import antighost.keybind.KeybindHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

public class AntiGhostBlock implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Load config (creates antighost.json on first run)
        AntiGhostConfig config = AntiGhostConfig.get();

        // Register keybind (default: R)
        KeybindHandler.register();

        // Main tick loop — drains the resync queue
        ClientTickEvents.END_CLIENT_TICK.register(GhostBlockFixer::tick);

        // Resync on respawn / dimension change
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (config.resyncOnSpawn) {
                GhostBlockFixer.queueAreaResync(client);
            }
        });

        // Also resync when the player object is fully ready after respawn
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            System.out.println("[AntiGhostBlock] v2.0 loaded — config di: config/antighost.json");
        });

        System.out.println("[AntiGhostBlock] v2.0 Singularity Engine aktif.");
    }
}
