package antighost;

import antighost.config.AntiGhostConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.LinkedHashSet;
import java.util.Set;

public class GhostBlockFixer {

    private static final Set<BlockPos> pendingResync = new LinkedHashSet<>();

    /** Place block → queue placed pos + 6 face neighbors */
    public static void queueResync(BlockPos placed) {
        pendingResync.add(placed.toImmutable());
        for (Direction dir : Direction.values()) {
            pendingResync.add(placed.offset(dir).toImmutable());
        }
    }

    /** Queue 3×3×3 area around player — for keybind, knockback, respawn */
    public static void queueAreaResync(MinecraftClient client) {
        if (client.player == null) return;
        BlockPos center = client.player.getBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    queueResync(center.add(dx, dy, dz));
                }
            }
        }
    }

    /**
     * Drain the queue every client tick.
     *
     * Strategy for MC 1.21 (RequestBlockUpdateC2SPacket removed in 1.20.5):
     * For each queued pos, if the client world shows a non-air solid block
     * that the server is unlikely to have confirmed (i.e. we just placed it
     * and got no S2C block-update back yet), we forcibly set it to AIR on
     * the client world.  The server will immediately send the authoritative
     * BlockUpdateS2CPacket to correct us if we're wrong — so worst case
     * we see a one-tick flicker, best case the ghost block vanishes.
     */
    public static void tick(MinecraftClient client) {
        if (pendingResync.isEmpty()) return;
        if (client.player == null || client.world == null) {
            pendingResync.clear();
            return;
        }

        ClientWorld world = client.world;
        int max   = AntiGhostConfig.get().maxPerTick;
        int count = 0;
        var iter  = pendingResync.iterator();

        while (iter.hasNext() && count < max) {
            BlockPos pos = iter.next();
            BlockState state = world.getBlockState(pos);

            // Only clear blocks that are solid and non-air —
            // air positions cannot be ghost blocks
            if (!state.isAir() && state.getBlock() != Blocks.BEDROCK) {
                // NOTIFY_ALL triggers a visual re-render and neighbor updates,
                // which forces the server to push the real state back to us
                world.setBlockState(pos, Blocks.AIR.getDefaultState(),
                        Block.NOTIFY_ALL | Block.FORCE_STATE);
            }

            iter.remove();
            count++;
        }

        if (AntiGhostConfig.get().debugLog && count > 0) {
            System.out.println("[AntiGhostBlock] Cleared " + count
                    + " potential ghost blocks. Queued: " + pendingResync.size());
        }
    }
}
