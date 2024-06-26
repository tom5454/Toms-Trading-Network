package com.tom.trading.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec2;

import com.tom.trading.tile.VendingMachineBlockEntity;
import com.tom.trading.util.BasicContainer;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum VendingMachineProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return JadePlugin.VENDING_MACHINE;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		VendingMachineBlockEntity te = (VendingMachineBlockEntity) accessor.getBlockEntity();
		data.put("config", te.getConfig().createTag(accessor.getLevel().registryAccess()));
		data.putByte("state", (byte) te.getTradingState());
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("config")) {
			BasicContainer c = new BasicContainer(8);
			c.fromTag(accessor.getServerData().getList("config", Tag.TAG_COMPOUND), Minecraft.getInstance().level.registryAccess());
			byte state = accessor.getServerData().getByte("state");
			IElementHelper elements = IElementHelper.get();
			if(accessor.showDetails()) {
				ITooltip t = elements.tooltip();
				t.add(Component.translatable("label.toms_trading_network.vending_machine.cost"));
				for (int i = 0; i < 4; i++) {
					ItemStack is = c.getItem(i);
					if(!is.isEmpty()) {
						IElement icon = elements.item(is, 1f).size(new Vec2(18, 18)).translate(new Vec2(0, -1));
						icon.message(null);
						t.add(icon);
						is.getTooltipLines(TooltipContext.of(accessor.getLevel()), accessor.getPlayer(), TooltipFlag.Default.NORMAL).forEach(t::add);
					}
				}
				BoxStyle.GradientBorder b = BoxStyle.getTransparent().clone();
				b.borderColor = new int[] {0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFFFF0000};
				b.borderWidth = 1;
				tooltip.add(elements.box(t, b));

				t = elements.tooltip();
				t.add(Component.translatable("label.toms_trading_network.vending_machine.result"));
				for (int i = 4; i < 8; i++) {
					ItemStack is = c.getItem(i);
					if(!is.isEmpty()) {
						IElement icon = elements.item(is, 1f).size(new Vec2(18, 18)).translate(new Vec2(0, -1));
						icon.message(null);
						t.add(icon);
						is.getTooltipLines(TooltipContext.of(accessor.getLevel()), accessor.getPlayer(), TooltipFlag.Default.NORMAL).forEach(t::add);
					}
				}
				b = BoxStyle.getTransparent().clone();
				b.borderColor = new int[] {0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00};
				b.borderWidth = 1;
				tooltip.add(elements.box(t, b));
			} else {
				tooltip.add(Component.translatable("label.toms_trading_network.vending_machine.cost"));
				tooltip.add(Component.empty());
				for (int i = 0; i < c.getContainerSize(); i++) {
					ItemStack is = c.getItem(i);
					if(!is.isEmpty()) {
						IElement icon = elements.item(is, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1));
						tooltip.append(icon);
					}
					if(i == 3) {
						tooltip.add(Component.translatable("label.toms_trading_network.vending_machine.result"));
						tooltip.add(Component.empty());
					}
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
