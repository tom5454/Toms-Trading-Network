package com.tom.trading.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.util.IDataReceiver;

public class NetworkHandler {
	@SubscribeEvent
	public static void register(final RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(TradingNetworkMod.MODID);

		registrar.play(DataPacket.ID, DataPacket::new, handler -> handler
				.client(NetworkHandler::handleDataClient)
				.server(NetworkHandler::handleDataServer));
	}

	public static void handleDataServer(DataPacket packet, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			ServerPlayer sender = (ServerPlayer) context.player().orElseThrow();
			if(sender.containerMenu instanceof IDataReceiver) {
				((IDataReceiver)sender.containerMenu).receive(packet.tag());
			}
		});
	}

	public static void handleDataClient(DataPacket packet, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			if(Minecraft.getInstance().screen instanceof IDataReceiver) {
				((IDataReceiver)Minecraft.getInstance().screen).receive(packet.tag());
			}
		});
	}

	public static void sendDataToServer(CompoundTag tag) {
		PacketDistributor.SERVER.noArg().send(new DataPacket(tag));
	}

	public static void sendTo(ServerPlayer pl, CompoundTag tag) {
		PacketDistributor.PLAYER.with(pl).send(new DataPacket(tag));
	}
}
