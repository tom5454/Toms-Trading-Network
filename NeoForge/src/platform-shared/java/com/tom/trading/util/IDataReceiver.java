package com.tom.trading.util;

import net.minecraft.world.level.storage.ValueInput;

public interface IDataReceiver {
	void receive(ValueInput tag);
}
