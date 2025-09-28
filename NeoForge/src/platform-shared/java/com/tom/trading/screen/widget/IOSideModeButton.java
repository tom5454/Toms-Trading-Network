package com.tom.trading.screen.widget;

import java.util.Locale;
import java.util.stream.Stream;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.util.BlockFaceDirection;
import com.tom.trading.util.ComponentJoiner;

public class IOSideModeButton extends EnumCycleButton<IOMode> {
	private static final ResourceLocation AUTO_INPUT = ResourceLocation.tryBuild(TradingNetworkMod.MODID, "icons/side_input_auto");
	private static final ResourceLocation AUTO_OUTPUT = ResourceLocation.tryBuild(TradingNetworkMod.MODID, "icons/side_output_auto");
	private static final ResourceLocation AUTO_IO = ResourceLocation.tryBuild(TradingNetworkMod.MODID, "icons/side_io_auto");
	private boolean autoMode;
	private Listener sendUpdate;
	private BlockFaceDirection side;

	public IOSideModeButton(int x, int y, BlockFaceDirection side, Listener sendUpdate) {
		super(x, y, Component.translatable(""), "side", IOMode.values(), (b, m) -> {
			b.setState(m);
			((IOSideModeButton)b).send();
		});
		this.sendUpdate = sendUpdate;
		this.side = side;
	}

	@Override
	public void onPress(InputWithModifiers input) {
		if (input.hasShiftDown()) {
			autoMode = !autoMode;
			send();
		} else
			super.onPress(input);
	}

	private void send() {
		sendUpdate.set(side, getState(), autoMode);
	}

	@Override
	public ResourceLocation getIcon() {
		if (autoMode) {
			switch (getState()) {
			case INPUT:
				return AUTO_INPUT;

			case IO:
				return AUTO_IO;

			case OUTPUT:
				return AUTO_OUTPUT;

			case OFF:
			default:
				break;
			}
		}
		return super.getIcon();
	}

	public void setAutoMode(boolean autoMode) {
		this.autoMode = autoMode;
		updateTooltip();
	}

	private void updateTooltip() {
		setTooltip(Tooltip.create(
				Stream.of(
						Component.translatable("tooltip.toms_trading_network.side." + side.name().toLowerCase(Locale.ROOT)),
						Component.translatable("tooltip.toms_trading_network.side_config." + getState().name().toLowerCase(Locale.ROOT)),
						Component.translatable("tooltip.toms_trading_network.side_config_auto." + (autoMode ? "on" : "off"))
						).collect(ComponentJoiner.joining(Component.empty(), Component.literal("\n")))));
	}

	@Override
	public void setState(IOMode state) {
		if (state == IOMode.OFF)autoMode = false;
		super.setState(state);
		updateTooltip();
	}

	public static interface Listener {
		void set(BlockFaceDirection dir, IOMode mode, boolean auto);
	}
}
