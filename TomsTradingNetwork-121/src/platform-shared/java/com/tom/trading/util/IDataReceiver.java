package com.tom.trading.util;

import net.minecraft.nbt.CompoundTag;

public interface IDataReceiver {
	void receive(CompoundTag tag);
}
