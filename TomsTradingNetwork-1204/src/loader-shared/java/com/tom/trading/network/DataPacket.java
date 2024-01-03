package com.tom.trading.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.tom.trading.TradingNetworkMod;

public record DataPacket(CompoundTag tag) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(TradingNetworkMod.MODID, "data");

	public DataPacket(FriendlyByteBuf pb) {
		this((CompoundTag) pb.readNbt(NbtAccounter.unlimitedHeap()));
	}

	@Override
	public void write(FriendlyByteBuf pb) {
		pb.writeNbt(tag);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
