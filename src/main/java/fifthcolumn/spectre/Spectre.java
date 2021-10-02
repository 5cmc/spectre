package fifthcolumn.spectre;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Spectre implements ModInitializer {

    private final ConcurrentMap<UUID, SpectreEntry> spectrePlayers = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerManager playerManager = server.getPlayerManager();
            GameProfile playerProfile = handler.player.getGameProfile();

            // Make the server secure by adding the first player to join as operator and enable the server whitelist
            if (!playerManager.isWhitelistEnabled() || playerManager.getWhitelist().isEmpty()) {
                playerManager.setWhitelistEnabled(true);
                playerManager.addToOperators(playerProfile);
            }

            // Check if the player is whitelisted
            if (!playerManager.getWhitelist().isAllowed(playerProfile)) {
                spectrePlayers.put(playerProfile.getId(), new SpectreEntry(playerProfile, handler.player.interactionManager.getGameMode()));
                handler.player.changeGameMode(GameMode.SPECTATOR);
            }
        });

        // On every tick, check players we've spectre'd and release them to regular play if they've been whitelisted
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (spectrePlayers.isEmpty()) {
                return;
            }
            PlayerManager playerManager = server.getPlayerManager();
            spectrePlayers.forEach((profileId, entry) -> {
                if (playerManager.isWhitelisted(entry.profile)) {
                    ServerPlayerEntity player = playerManager.getPlayer(profileId);
                    if (player != null) {
                        player.changeGameMode(entry.gameMode);
                    }
                    spectrePlayers.remove(profileId);
                }
            });


        });
    }

    public static class SpectreEntry {
        public final GameProfile profile;
        public final GameMode gameMode;

        public SpectreEntry(GameProfile profile, GameMode gameMode) {
            this.profile = profile;
            this.gameMode = gameMode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SpectreEntry that = (SpectreEntry) o;

            if (!profile.equals(that.profile)) return false;
            return gameMode == that.gameMode;
        }

        @Override
        public int hashCode() {
            int result = profile.hashCode();
            result = 31 * result + gameMode.hashCode();
            return result;
        }
    }
}
