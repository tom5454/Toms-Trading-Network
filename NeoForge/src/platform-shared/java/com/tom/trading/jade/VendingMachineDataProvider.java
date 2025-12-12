package com.tom.trading.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.TagValueOutput;

import com.tom.trading.tile.VendingMachineBlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum VendingMachineDataProvider implements IServerDataProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public Identifier getUid() {
		return JadePlugin.VENDING_MACHINE;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		VendingMachineBlockEntity te = (VendingMachineBlockEntity) accessor.getBlockEntity();
		TagValueOutput tag = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, accessor.getLevel().registryAccess());
		te.getConfig().storeItems(tag.list("config", ItemStackWithSlot.CODEC));
		tag.putByte("state", (byte) te.getTradingState());
		tag.buildResult().forEach(data::put);
	}
}
