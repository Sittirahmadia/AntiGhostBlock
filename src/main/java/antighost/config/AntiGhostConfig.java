package antighost.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class AntiGhostConfig {

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("antighost.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── tuneable values ───────────────────────────────────────────────────────
    public int  maxPerTick        = 16;   // max blocks resynced per tick
    public boolean resyncOnSpawn  = true; // resync on respawn / dimension change
    public boolean resyncOnKnock  = true; // resync on knockback
    public boolean debugLog       = false; // print resync count to console
    // ─────────────────────────────────────────────────────────────────────────

    // Singleton
    private static AntiGhostConfig instance;

    public static AntiGhostConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    public static AntiGhostConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(r, AntiGhostConfig.class);
                return instance;
            } catch (IOException e) {
                System.err.println("[AntiGhostBlock] Gagal baca config, pakai default. " + e.getMessage());
            }
        }
        instance = new AntiGhostConfig();
        instance.save();
        return instance;
    }

    public void save() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, w);
        } catch (IOException e) {
            System.err.println("[AntiGhostBlock] Gagal simpan config. " + e.getMessage());
        }
    }
}
