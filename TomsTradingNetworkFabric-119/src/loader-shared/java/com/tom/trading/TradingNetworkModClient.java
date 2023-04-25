package com.tom.trading;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.nbt.CompoundTag;

import com.tom.trading.gui.VendingMachineConfigScreen;
import com.tom.trading.gui.VendingMachineTradingScreen;
import com.tom.trading.network.NetworkHandler;
import com.tom.trading.util.IDataReceiver;

public class TradingNetworkModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.DATA_S2C, (mc, h, buf, rp) -> {
			CompoundTag tag = buf.readAnySizeNbt();
			mc.submit(() -> {
				if(mc.screen instanceof IDataReceiver) {
					((IDataReceiver)mc.screen).receive(tag);
				}
			});
		});

		MenuScreens.register(Content.VENDING_MACHINE_CONFIG_MENU.get(), VendingMachineConfigScreen::new);
		MenuScreens.register(Content.VENDING_MACHINE_TRADING_MENU.get(), VendingMachineTradingScreen::new);
	}
}
