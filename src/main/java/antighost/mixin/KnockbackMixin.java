package antighost.mixin;

import antighost.GhostBlockFixer;
import antighost.config.AntiGhostConfig;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class KnockbackMixin {

    /**
     * The server sends EntityVelocityUpdateS2CPacket whenever it overrides
     * an entity's velocity — including knockback from explosions and hits.
     * We intercept this for the local player and queue an area resync,
     * because during Crystal PvP the player is often flung through
     * just-placed obsidian that hasn't synced yet.
     */
    @Inject(method = "onEntityVelocityUpdate", at = @At("TAIL"))
    private void onKnockback(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (!AntiGhostConfig.get().resyncOnKnock) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Only react if this packet targets our own player
        if (packet.getEntityId() != client.player.getId()) return;

        // Threshold: ignore tiny velocity nudges, only react to real knockback
        double vx = packet.getVelocityX() / 8000.0;
        double vy = packet.getVelocityY() / 8000.0;
        double vz = packet.getVelocityZ() / 8000.0;
        double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);

        if (speed > 0.15) {
            GhostBlockFixer.queueAreaResync(client);
        }
    }
}
