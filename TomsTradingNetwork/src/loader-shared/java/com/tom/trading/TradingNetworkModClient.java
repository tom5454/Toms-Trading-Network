package com.tom.trading;

import net.minecraft.client.gui.screens.MenuScreens;

import com.tom.trading.gui.VendingMachineConfigScreen;
import com.tom.trading.gui.VendingMachineTradingScreen;

public class TradingNetworkModClient {

	public static void preInit() {

	}

	public static void clientSetup() {
		MenuScreens.register(Content.VENDING_MACHINE_CONFIG_MENU.get(), VendingMachineConfigScreen::new);
		MenuScreens.register(Content.VENDING_MACHINE_TRADING_MENU.get(), VendingMachineTradingScreen::new);
	}

}
