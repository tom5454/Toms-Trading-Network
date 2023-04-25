package com.tom.trading.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;

import com.tom.trading.tile.VendingMachineBlockEntity;
import com.tom.trading.util.BasicContainer;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum VendingMachineProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return JadePlugin.VENDING_MACHINE;
	}

	@Override
	public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity t, boolean showDetails) {
		VendingMachineBlockEntity te = (VendingMachineBlockEntity) t;
		data.put("config", te.getConfig().createTag());
		data.putByte("state", (byte) te.getTradingState());
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("config")) {
			BasicContainer c = new BasicContainer(8);
			c.fromTag(accessor.getServerData().getList("config", Tag.TAG_COMPOUND));
			byte state = accessor.getServerData().getByte("state");
			IElementHelper elements = tooltip.getElementHelper();
			tooltip.add(Component.translatable("label.toms_trading_network.vending_machine.cost"));
			tooltip.add(Component.empty());
			for (int i = 0; i < c.getContainerSize(); i++) {
				ItemStack is = c.getItem(i);
				if(!is.isEmpty()) {
					IElement icon = elements.item(is, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1));
					icon.message(null);
					tooltip.append(icon);
				}
				if(i == 3) {
					tooltip.add(Component.translatable("label.toms_trading_network.vending_machine.result"));
					tooltip.add(Component.empty());
				}
			}
			if(state == 0) {
				tooltip.add(Component.translatable("label.toms_trading_network.vending_machine.noItems"));
			}
		} else {
			tooltip.append(Component.translatable("jade.toms_trading_network.noServerInfo"));
		}
	}
}
