package com.tom.trading;

import java.util.Collections;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import com.tom.trading.screen.VendingMachineConfigScreen;
import com.tom.trading.screen.VendingMachineTradingScreen;
import com.tom.trading.util.KeyUtil;

public class TradingNetworkModClient {

	public static void preInit(IEventBus bus) {
		bus.addListener(TradingNetworkModClient::registerScreens);
	}

	public static void clientSetup() {
		NeoForge.EVENT_BUS.register(TradingNetworkModClient.class);
	}

	private static void registerScreens(RegisterMenuScreensEvent ev) {
		ev.register(Content.VENDING_MACHINE_CONFIG_MENU.get(), VendingMachineConfigScreen::new);
		ev.register(Content.VENDING_MACHINE_TRADING_MENU.get(), VendingMachineTradingScreen::new);
	}

	public static void tooltip(String key, List<Component> tooltip, Object... args) {
		tooltip(key, true, tooltip, args);
	}

	public static void tooltip(String key, boolean addShift, List<Component> tooltip, Object... args) {
		if(KeyUtil.hasShiftDown()) {
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
