package com.tom.trading.jade;

import net.minecraft.resources.Identifier;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.block.VendingMachineBlock;
import com.tom.trading.tile.VendingMachineBlockEntity;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
	public static final Identifier VENDING_MACHINE = Identifier.tryBuild(TradingNetworkMod.MODID, "vending_machine");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(VendingMachineDataProvider.INSTANCE, VendingMachineBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(VendingMachineComponentProvider.INSTANCE, VendingMachineBlock.class);
	}
}
