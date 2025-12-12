package com.tom.trading.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import com.tom.trading.util.BasicContainer;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;

public enum VendingMachineComponentProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public Identifier getUid() {
		return JadePlugin.VENDING_MACHINE;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("config")) {
			BasicContainer c = new BasicContainer(8);
			ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, Minecraft.getInstance().level.registryAccess(), accessor.getServerData());
			c.loadItems(in.listOrEmpty("config", ItemStackWithSlot.CODEC));
			byte state = in.getByteOr("state", (byte) 0);
			if(accessor.showDetails()) {
				ITooltip t = JadeUI.tooltip();
				t.add(Component.translatable("label.toms_trading_network.vending_machine.cost"));
				for (int i = 0; i < 4; i++) {
					ItemStack is = c.getItem(i);
					if(!is.isEmpty()) {
						var icon = JadeUI.item(is, 1f).size(18, 18).offset(0, -1);
						t.add(icon);
						is.getTooltipLines(TooltipContext.of(accessor.getLevel()), accessor.getPlayer(), TooltipFlag.Default.NORMAL).forEach(t::add);
					}
				}
				BoxStyle b = BoxStyle.transparent().copy();
				//b.borderColor = new int[] {0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFFFF0000};
				b.borderWidth = 1;
				tooltip.add(JadeUI.box(t, b));

				t = JadeUI.tooltip();
				t.add(Component.translatable("label.toms_trading_network.vending_machine.result"));
				for (int i = 4; i < 8; i++) {
					ItemStack is = c.getItem(i);
					if(!is.isEmpty()) {
						var icon = JadeUI.item(is, 1f).size(18, 18).offset(0, -1);
						t.add(icon);
						is.getTooltipLines(TooltipContext.of(accessor.getLevel()), accessor.getPlayer(), TooltipFlag.Default.NORMAL).forEach(t::add);
					}
				}
				b = BoxStyle.transparent().copy();
				//b.borderColor = new int[] {0xFF00FF00, 0xFF00FF00, 0xFF00FF00, 0xFF00FF00};
				b.borderWidth = 1;
				tooltip.add(JadeUI.box(t, b));
			} else {
				tooltip.add(Component.translatable("label.toms_trading_network.vending_machine.cost"));
				tooltip.add(Component.empty());
				for (int i = 0; i < c.getContainerSize(); i++) {
					ItemStack is = c.getItem(i);
					if(!is.isEmpty()) {
						var icon = JadeUI.item(is, 0.5f).size(10, 10).offset(0, -1);
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
