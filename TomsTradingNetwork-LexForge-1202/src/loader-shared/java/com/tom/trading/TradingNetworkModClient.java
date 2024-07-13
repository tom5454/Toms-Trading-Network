package com.tom.trading;

import java.util.Collections;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.tom.trading.gui.VendingMachineConfigScreen;
import com.tom.trading.gui.VendingMachineTradingScreen;

public class TradingNetworkModClient {

	public static void preInit() {

	}

	public static void clientSetup() {
		MenuScreens.register(Content.VENDING_MACHINE_CONFIG_MENU.get(), VendingMachineConfigScreen::new);
		MenuScreens.register(Content.VENDING_MACHINE_TRADING_MENU.get(), VendingMachineTradingScreen::new);
		MinecraftForge.EVENT_BUS.register(TradingNetworkModClient.class);
	}

	public static void tooltip(String key, List<Component> tooltip, Object... args) {
		tooltip(key, true, tooltip, args);
	}

	public static void tooltip(String key, boolean addShift, List<Component> tooltip, Object... args) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.get("tooltip.toms_trading_network." + key, args).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(Component.literal(sp[i]));
			}
		} else if(addShift) {
			tooltip.add(Component.translatable("tooltip.toms_trading_network.hold_shift_for_info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}

	private static Component[] tooltipExt = new Component[0];
	public static void setTooltip(Component... string) {
		tooltipExt = string;
	}

	@SubscribeEvent
	public static void getTooltip(ItemTooltipEvent evt) {
		Collections.addAll(evt.getToolTip(), tooltipExt);
	}
}
