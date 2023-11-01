package com.tom.trading.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.util.IDataReceiver;

public class NetworkHandler {
	public static final SimpleChannel INSTANCE = ChannelBuilder
			.named(new ResourceLocation(TradingNetworkMod.MODID, "main"))
			.networkProtocolVersion(1)
			.simpleChannel().

			messageBuilder(DataPacket.class)
			.decoder(DataPacket::new)
			.encoder(DataPacket::toBytes)
			.consumerMainThread(NetworkHandler::handleData)
			.add();

	public static void init() {
		TradingNetworkMod.LOGGER.info("Initilaized Network Handler");
	}

	public static void handleData(DataPacket packet, CustomPayloadEvent.Context ctx) {
		if(ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ServerPlayer sender = ctx.getSender();
			if(sender.containerMenu instanceof IDataReceiver) {
				((IDataReceiver)sender.containerMenu).receive(packet.tag);
			}
		} else if(ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			if(Minecraft.getInstance().screen instanceof IDataReceiver) {
				((IDataReceiver)Minecraft.getInstance().screen).receive(packet.tag);
			}
		}
		ctx.setPacketHandled(true);
	}

	public static void sendDataToServer(CompoundTag tag) {
		INSTANCE.send(new DataPacket(tag), PacketDistributor.SERVER.noArg());
	}

	public static void sendTo(ServerPlayer pl, CompoundTag tag) {
		INSTANCE.send(new DataPacket(tag), PacketDistributor.PLAYER.with(pl));
	}
}
