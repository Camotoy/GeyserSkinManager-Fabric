package olivermakesco.de.gsm.fabric;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.common.SkinEntry;
import com.github.camotoy.geyserskinmanager.common.platform.SkinEventListener;
import com.google.common.collect.Lists;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

public class FabricSkinEventListener extends SkinEventListener<ServerPlayer, MinecraftServer> {
    public boolean showSkins;
    public static ResourceLocation PacketID = new ResourceLocation(Constants.MOD_PLUGIN_MESSAGE_NAME);
    public FabricBedrockSkinUtilityListener listener;

    public FabricSkinEventListener(File skinDatabaseLocation, Logger logger, boolean showSkins, MinecraftServer server) {
        super(skinDatabaseLocation, logger::warn);
        this.showSkins = showSkins;

        this.listener = new FabricBedrockSkinUtilityListener(this.database, this.skinRetriever, server);

        ServerPlayConnectionEvents.JOIN.register(this::onServerConnected);
    }

    public void onServerConnected(ServerGamePacketListenerImpl event, PacketSender packetSender, MinecraftServer server) {
        if (!skinRetriever.isBedrockPlayer(event.getPlayer().getUUID())){
            this.listener.onModdedPlayerConfirm(event.getPlayer());
        }

        boolean shouldApply = true;
        if (showSkins) {
            Map<String,Collection<Property>> propertyMap = event.player.getGameProfile().getProperties().asMap();
            for (String k : propertyMap.keySet()) {
                if (Objects.equals(k, "textures")) {
                    shouldApply = false;
                    break;
                }
            }
        }

        RawSkin skin = this.skinRetriever.getBedrockSkin(event.getPlayer().getUUID());
        if (skin != null && showSkins && shouldApply) {
            uploadOrRetrieveSkin(event.getPlayer(), null, skin);
        }
        if (skin != null || skinRetriever.isBedrockPlayer(event.getPlayer().getUUID())) {
            this.listener.onBedrockPlayerJoin(event.getPlayer(), skin);
        }
    }

    @Override
    public UUID getUUID(ServerPlayer player) {
        return player.getUUID();
    }

    @Override
    public void onSuccess(ServerPlayer player, MinecraftServer server, SkinEntry skinEntry) {
        if (!showSkins) return;

        player.getGameProfile().getProperties().put("textures",new Property("textures", skinEntry.getJavaSkinValue(), skinEntry.getJavaSkinSignature()));

        //Code from Floodgate-Fabric
        for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
            if (otherPlayer == player) {
                continue;
            }
            boolean loadedInWorld = otherPlayer.getCommandSenderWorld().getEntity(player.getId()) != null;
            if (loadedInWorld) {
                // Player is loaded in this world
                otherPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
            }
            otherPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, (Collection<ServerPlayer>) player));
            otherPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, (Collection<ServerPlayer>) player));

            if (loadedInWorld) {
                // Copied from EntityTrackerEntry
                Packet<?> spawnPacket = player.getAddEntityPacket();
                otherPlayer.connection.send(spawnPacket);
                if (!player.getEntityData().isEmpty()) {
                    otherPlayer.connection.send(new ClientboundSetEntityDataPacket(player.getId(), player.getEntityData(), true));
                }

                Collection<AttributeInstance> collection = player.getAttributes().getDirtyAttributes();
                if (!collection.isEmpty()) {
                    otherPlayer.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), collection));
                }

                otherPlayer.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));

                List<Pair<EquipmentSlot, ItemStack>> equipmentList = Lists.newArrayList();
                EquipmentSlot[] slots = EquipmentSlot.values();

                for (EquipmentSlot equipmentSlot : slots) {
                    ItemStack itemStack = player.getItemBySlot(equipmentSlot);
                    if (!itemStack.isEmpty()) {
                        equipmentList.add(Pair.of(equipmentSlot, itemStack.copy()));
                    }
                }

                if (!equipmentList.isEmpty()) {
                    otherPlayer.connection.send(new ClientboundSetEquipmentPacket(player.getId(), equipmentList));
                }

                for (MobEffectInstance mobEffectInstance : player.getActiveEffects()) {
                    otherPlayer.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), mobEffectInstance));
                }

                if (!player.getPassengers().isEmpty()) {
                    otherPlayer.connection.send(new ClientboundSetPassengersPacket(player));
                }

                if (player.getVehicle() != null) {
                    otherPlayer.connection.send(new ClientboundSetPassengersPacket(player.getVehicle()));
                }
            }
        }
    }
}
