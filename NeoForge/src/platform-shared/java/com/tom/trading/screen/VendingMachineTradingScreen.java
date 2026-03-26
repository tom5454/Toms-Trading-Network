package com.tom.trading.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.gui.PlatformContainerScreen;
import com.tom.trading.menu.VendingMachineTradingMenu;

public class VendingMachineTradingScreen extends PlatformContainerScreen<VendingMachineTradingMenu> {
	private static final Identifier gui = Identifier.tryBuild(TradingNetworkMod.MODID, "textures/gui/vending_machine_trading.png");
	private EditBox textF;

	public VendingMachineTradingScreen(VendingMachineTradingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor gr, final int mouseX, final int mouseY,
			final float a) {
		super.extractBackground(gr, mouseX, mouseY, a);
		gr.blit(RenderPipelines.GUI_TEXTURED, gui, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	protected void init() {
		super.init();

		textF = new EditBox(font, leftPos + 74, topPos + 66, 18, font.lineHeight, Component.translatable("narrator.toms_trading_network.vending_machine.trade_amount"));
		textF.setMaxLength(100);
		textF.setBordered(false);
		textF.setVisible(true);
		textF.setTextColor(0xFFFFFFFF);
		textF.setValue("1");
		addRenderableWidget(textF);

		addRenderableWidget(Button.builder(Component.translatable("button.toms_trading_network.vending_machine.trade"), b -> {
			int c;
			try {
				c = Integer.parseInt(textF.getValue());
				if(c < 0 || c > 128)c = 1;
			} catch (NumberFormatException e) {
				c = 1;
			}
			sendButtonClick(c);
		}).bounds(this.leftPos + 100, this.topPos + 60, 70, 20).build());
	}

	private void sendButtonClick(int pPageData) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, pPageData);
	}

	@Override
	protected List<Component> getTooltipFromContainerItem(ItemStack itemStack) {
		List<Component> tooltip = new ArrayList<>(super.getTooltipFromContainerItem(itemStack));
		Slot s = getSlotUnderMouse();
		if (s != null && s.getContainerSlot() < 8) {
			if ((menu.matchNBT & (1 << s.getContainerSlot())) == 0) {
				tooltip.add(Component.translatable("tooltip.toms_trading_network.ignoredNBT").withStyle(ChatFormatting.WHITE));
			}
		}
		return tooltip;
	}
}
