package com.tom.trading.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.TradingNetworkModClient;

public class VendingMachineTradingScreen extends PlatformContainerScreen<VendingMachineTradingMenu> {
	private static final ResourceLocation gui = ResourceLocation.tryBuild(TradingNetworkMod.MODID, "textures/gui/vending_machine_trading.png");
	private PlatformEditBox textF;

	public VendingMachineTradingScreen(VendingMachineTradingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	@Override
	protected void renderBg(GuiGraphics gr, float pPartialTick, int pMouseX, int pMouseY) {
		gr.blit(gui, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void init() {
		super.init();

		textF = new PlatformEditBox(font, leftPos + 74, topPos + 66, 18, font.lineHeight, Component.translatable("narrator.toms_trading_network.vending_machine.trade_amount"));
		textF.setMaxLength(100);
		textF.setBordered(false);
		textF.setVisible(true);
		textF.setTextColor(16777215);
		textF.setValue("1");
		addRenderableWidget(textF);

		addRenderableWidget(new PlatformButton(this.leftPos + 100, this.topPos + 60, 70, 20, Component.translatable("button.toms_trading_network.vending_machine.trade"), b -> {
			int c;
			try {
				c = Integer.parseInt(textF.getValue());
				if(c < 0 || c > 128)c = 1;
			} catch (NumberFormatException e) {
				c = 1;
			}
			sendButtonClick(c);
		}));
	}

	private void sendButtonClick(int pPageData) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, pPageData);
	}

	@Override
	public void render(GuiGraphics gr, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBackground(gr, pMouseX, pMouseY, pPartialTick);
		super.render(gr, pMouseX, pMouseY, pPartialTick);
		Slot s = getSlotUnderMouse();
		if(s != null && s.getContainerSlot() < 8) {
			if((menu.matchNBT & (1 << s.getContainerSlot())) == 0) {
				TradingNetworkModClient.setTooltip(Component.translatable("tooltip.toms_trading_network.ignoredNBT").withStyle(ChatFormatting.WHITE));
			}
		}
		this.renderTooltip(gr, pMouseX, pMouseY);
		TradingNetworkModClient.setTooltip();
	}
}
