package com.tom.trading;

import java.util.Collections;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;

import com.tom.trading.network.DataPacket;
import com.tom.trading.screen.VendingMachineConfigScreen;
import com.tom.trading.screen.VendingMachineTradingScreen;
import com.tom.trading.util.IDataReceiver;
import com.tom.trading.util.KeyUtil;

public class TradingNetworkModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(DataPacket.ID, (p, c) -> {
			if(Minecraft.getInstance().screen instanceof IDataReceiver d) {
				d.receive(TagValueInput.create(ProblemReporter.DISCARDING, c.player().registryAccess(), p.tag()));
			}
		});

		MenuScreens.register(Content.VENDING_MACHINE_CONFIG_MENU.get(), VendingMachineConfigScreen::new);
		MenuScreens.register(Content.VENDING_MACHINE_TRADING_MENU.get(), VendingMachineTradingScreen::new);

		ItemTooltipCallback.EVENT.register((s, c, t, l) -> {
			Collections.addAll(l, tooltipExt);
		});
	}

	public static void tooltip(String key, List<Component> tooltip, Object... args) {
		tooltip(key, true, tooltip, args);
	}

	public static void tooltip(String key, boolean shift, List<Component> tooltip, Object... args) {
		if(KeyUtil.hasShiftDown()) {
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
