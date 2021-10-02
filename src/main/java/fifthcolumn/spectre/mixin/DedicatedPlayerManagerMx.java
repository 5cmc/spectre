package fifthcolumn.spectre.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(DedicatedPlayerManager.class)
public class DedicatedPlayerManagerMx extends PlayerManager {
    public DedicatedPlayerManagerMx(MinecraftServer minecraftServer, DynamicRegistryManager.Impl impl, WorldSaveHandler worldSaveHandler, int i) {
        super(minecraftServer, impl, worldSaveHandler, i);
    }

    @Inject(method = "isWhitelisted", at = @At("INVOKE"))
    public boolean isWhitelisted(GameProfile profile) {
        return this.isWhitelistEnabled() || this.isOperator(profile);
    }
}
