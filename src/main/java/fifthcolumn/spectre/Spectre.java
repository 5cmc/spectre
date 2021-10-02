package fifthcolumn.spectre;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.GameMode;

public class Spectre implements ModInitializer {
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
                handler.player.changeGameMode(GameMode.SPECTATOR);
            }
        });
    }
}
