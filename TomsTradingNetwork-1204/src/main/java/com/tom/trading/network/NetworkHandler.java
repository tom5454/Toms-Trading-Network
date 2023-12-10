package com.tom.trading.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.util.IDataReceiver;

public class NetworkHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(TradingNetworkMod.MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
			);

	public static void init() {
		INSTANCE.registerMessage(0, DataPacket.class, DataPacket::toBytes, DataPacket::new, NetworkHandler::handleData);
		TradingNetworkMod.LOGGER.info("Initilaized Network Handler");
	}

	public static void handleData(DataPacket packet, NetworkEvent.Context ctx) {
		if(ctx.getDirection() == PlayNetworkDirection.PLAY_TO_SERVER) {
			ctx.enqueueWork(() -> {
				ServerPlayer sender = ctx.getSender();
				if(sender.containerMenu instanceof IDataReceiver) {
					((IDataReceiver)sender.containerMenu).receive(packet.tag);
				}
			});
		} else if(ctx.getDirection() == PlayNetworkDirection.PLAY_TO_CLIENT) {
			ctx.enqueueWork(() -> {
				if(Minecraft.getInstance().screen instanceof IDataReceiver) {
					((IDataReceiver)Minecraft.getInstance().screen).receive(packet.tag);
				}
			});
		}
		ctx.setPacketHandled(true);
	}

	public static void sendDataToServer(CompoundTag tag) {
		INSTANCE.sendToServer(new DataPacket(tag));
	}

	public static void sendTo(ServerPlayer pl, CompoundTag tag) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> pl), new DataPacket(tag));
	}
}
