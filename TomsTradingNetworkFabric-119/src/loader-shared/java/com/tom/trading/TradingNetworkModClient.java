package com.tom.trading;

import java.util.Collections;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

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

		ItemTooltipCallback.EVENT.register((s, c, l) -> {
			Collections.addAll(l, tooltipExt);
		});
	}

	public static void tooltip(String key, List<Component> tooltip, Object... args) {
		tooltip(key, true, tooltip, args);
	}

	public static void tooltip(String key, boolean shift, List<Component> tooltip, Object... args) {
		if(Screen.hasShiftDown()) {
			String[] sp = I18n.get("tooltip.toms_trading_network." + key, args).split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(Component.literal(sp[i]));
			}
		} else if(shift) {
			tooltip.add(Component.translatable("tooltip.toms_trading_network.hold_shift_for_info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}

	private static Component[] tooltipExt = new Component[0];
	public static void setTooltip(Component... string) {
		tooltipExt = string;
	}
}
