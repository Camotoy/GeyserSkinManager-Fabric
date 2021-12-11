package olivermakesco.de.gsm.fabric;

import com.github.camotoy.geyserskinmanager.common.SkinDatabase;
import com.github.camotoy.geyserskinmanager.common.platform.BedrockSkinUtilityListener;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class FabricBedrockSkinUtilityListener extends BedrockSkinUtilityListener<ServerPlayer> {
    public MinecraftServer server;

    public FabricBedrockSkinUtilityListener(SkinDatabase database, BedrockSkinRetriever skinRetriever, MinecraftServer server) {
        super(database, skinRetriever);
        this.server = server;
    }

    @Override
    public void sendPluginMessage(byte[] payload, ServerPlayer player) {
        GeyserSkinManager.LOGGER.info("Sending skin data!");
        ServerPlayNetworking.send(player, FabricSkinEventListener.PacketID, new FriendlyByteBuf(Unpooled.wrappedBuffer(payload)));
    }

    @Override
    public UUID getUUID(ServerPlayer player) {
        return player.getUUID();
    }
}
