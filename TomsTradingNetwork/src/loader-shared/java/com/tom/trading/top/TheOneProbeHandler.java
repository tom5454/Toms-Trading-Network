package com.tom.trading.top;

import java.util.function.Function;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.tile.VendingMachineBlockEntity;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;

public class TheOneProbeHandler implements Function<ITheOneProbe, Void>, IProbeInfoProvider {
	public static ITheOneProbe theOneProbeImp;

	public static TheOneProbeHandler create() {
		return new TheOneProbeHandler();
	}

	@Override
	public Void apply(ITheOneProbe input) {
		theOneProbeImp = input;
		theOneProbeImp.registerProvider(this);
		return null;
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
		BlockEntity te = world.getBlockEntity(data.getPos());
		if(te instanceof VendingMachineBlockEntity be) {
			probeInfo.text(Component.translatable("label.toms_trading_network.vending_machine.cost"));
			IProbeInfo itemBar = probeInfo.horizontal();
			for (int i = 0; i < be.getConfig().getContainerSize(); i++) {
				ItemStack is = be.getConfig().getItem(i);
				if(!is.isEmpty())
					itemBar.item(is);
				if(i == 3) {
					probeInfo.text(Component.translatable("label.toms_trading_network.vending_machine.result"));
					itemBar = probeInfo.horizontal();
				}
			}
			int state = be.getTradingState();
			if(state == 0) {
				probeInfo.text(Component.translatable("label.toms_trading_network.vending_machine.noItems"));
			}
		}
	}

	@Override
	public ResourceLocation getID() {
		return new ResourceLocation(TradingNetworkMod.MODID, "top");
	}
}
