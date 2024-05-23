package com.tom.trading.gui;

import java.util.EnumMap;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.trading.Content;
import com.tom.trading.TradingNetworkMod;
import com.tom.trading.util.BlockFaceDirection;
import com.tom.trading.util.ComponentJoiner;
import com.tom.trading.util.PopupMenuManager;
import com.tom.trading.util.PopupMenuManager.ButtonElement;
import com.tom.trading.util.PopupMenuManager.PopupElement;
import com.tom.trading.util.PopupMenuManager.TextFieldElement;

public class VendingMachineConfigScreen extends AbstractFilteredScreen<VendingMachineConfigMenu> {
	private static final ResourceLocation gui = ResourceLocation.tryBuild(TradingNetworkMod.MODID, "textures/gui/vending_machine_config.png");
	private PlatformEditBox nameBox;
	private Component title;
	private EnumMap<BlockFaceDirection, GuiButton> sideCfgButtons = new EnumMap<>(BlockFaceDirection.class);
	private PopupMenuManager popup = new PopupMenuManager(this);

	public VendingMachineConfigScreen(VendingMachineConfigMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.title = pTitle;
		pMenu.updateGui = this::updateGui;
	}

	@Override
	protected void renderBg(GuiGraphics gr, float pPartialTick, int pMouseX, int pMouseY) {
		gr.blit(gui, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		for (int i = 0;i<8;i++) {
			Slot s = menu.getSlot(i);
			if(!s.getItem().isEmpty() && ((menu.matchNBT & (1 << i)) == 0 || s.getItem().getItem() == Content.TAG_FILTER.get())) {
				gr.blit(gui, this.leftPos + s.x - 1, this.topPos + s.y - 1, 176, 16, 18, 18);
			}
		}

		gr.blit(this.leftPos + 134, this.topPos + 46, 0, 16, 16, minecraft.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(ResourceLocation.tryBuild(TradingNetworkMod.MODID, "block/vending_machine_front")));
	}

	@Override
	protected void init() {
		imageWidth = 176;
		imageHeight = 211;
		inventoryLabelY = imageHeight - 92;
		sideCfgButtons.clear();
		super.init();

		nameBox = new PlatformEditBox(font, this.leftPos + 7, this.topPos + 7, 110, 16, Component.translatable("narrator.toms_trading_network.vending_machine_name"));
		this.addRenderableWidget(this.nameBox);
		nameBox.setValue(title.getString());
		nameBox.setResponder(this::onNameChanged);

		for(BlockFaceDirection d : BlockFaceDirection.values()) {
			if(d == BlockFaceDirection.FRONT)continue;
			GuiButton btn = new GuiButton(this.leftPos + 118 + d.getX() * 16, this.topPos + 30 + d.getY() * 16, 0, b -> {
				int ns = (getFacingState(d) + 1) % 4;
				//0b 0 0 0 0    0 0 0  0
				//0b 0 0 1 d    d d ns ns
				sendButtonClick(ns | (d.ordinal() << 2) | 0b0010_0000);
			});
			btn.texture = gui;
			btn.texX = 176;
			sideCfgButtons.put(d, btn);
			addRenderableWidget(btn);
			btn.tooltipFactory = s -> Tooltip.create(
					Stream.of(
							Component.translatable("tooltip.toms_trading_network.side." + d.name().toLowerCase()),
							Component.translatable("tooltip.toms_trading_network.side_config" + s)
							).collect(ComponentJoiner.joining(Component.empty(), Component.literal("\n"))));
		}
		updateGui();

		addRenderableWidget(new PlatformButton(this.leftPos + 120, this.topPos + 7, 50, 20, Component.translatable("button.toms_trading_network.vending_machine.open_trading"), b -> {
			sendButtonClick(0b0100_0000);
		}));
	}

	private void onNameChanged(String name) {
		this.menu.setName(name);
		this.title = Component.literal(name);
	}

	private void sendMatchNBT(int slot, boolean config) {
		sendButtonClick(0b0110_0000 | (config ? 0b0001_0000 : 0) | (slot & 0xf));
	}

	private void sendButtonClick(int pPageData) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, pPageData);
	}

	@Override
	public void render(GuiGraphics gr, int pMouseX, int pMouseY, float pPartialTick) {
		this.renderBackground(gr, pMouseX, pMouseY, pPartialTick);
		super.render(gr, pMouseX, pMouseY, pPartialTick);
		if(popup.render(gr, font, pMouseX, pMouseY)) {
			this.renderTooltip(gr, pMouseX, pMouseY);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics gr, int pMouseX, int pMouseY) {
		gr.drawString(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

		gr.drawString(font, Component.translatable("label.toms_trading_network.vending_machine.cost"), this.inventoryLabelX, 26, 4210752, false);
		gr.drawString(font, Component.translatable("label.toms_trading_network.vending_machine.result"), 76, 26, 4210752, false);
		gr.drawString(font, Component.translatable("label.toms_trading_network.vending_machine.input"), this.inventoryLabelX, 72, 4210752, false);
		gr.drawString(font, Component.translatable("label.toms_trading_network.vending_machine.output"), 98, 72, 4210752, false);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if(popup.mouseClick(pMouseX, pMouseY, pButton))return true;
		Slot clicked = getSlotUnderMouse();
		if(pButton == 1 && clicked instanceof PhantomSlot && !clicked.getItem().isEmpty()) {
			int popupSlot = clicked.getContainerSlot();
			boolean tagFilter = clicked.getItem().getItem() == Content.TAG_FILTER.get();
			var tags = clicked.getItem().getTags().toList();
			nameBox.setFocus(false);
			popup.open(pMouseX, pMouseY,
					new TextFieldElement(
							() -> Component.translatable("tooltip.toms_trading_network.item_count"),
							s -> {
								try {
									int c = Integer.parseInt(s);
									menu.setConfigCount(clicked, c);
								} catch (NumberFormatException e) {
								}
							},
							font, Integer.toString(clicked.getItem().getCount())
							),
					new ButtonElement(
							() -> tagFilter ? Component.literal("").append(Component.translatable("tooltip.toms_trading_network.match_nbt", CommonComponents.GUI_NO).withStyle(ChatFormatting.DARK_GRAY)) : Component.translatable("tooltip.toms_trading_network.match_nbt", (menu.matchNBT & (1 << popupSlot)) != 0 ? CommonComponents.GUI_YES : CommonComponents.GUI_NO),
									() -> {
										menu.matchNBT ^= (1 << popupSlot);
										sendMatchNBT(popupSlot, (menu.matchNBT & (1 << popupSlot)) != 0);
									}
							),
					new ButtonElement(
							() -> tagFilter || tags.isEmpty() ? Component.literal("").append(Component.translatable("tooltip.toms_trading_network.to_tag_filter").withStyle(ChatFormatting.DARK_GRAY)) : Component.translatable("tooltip.toms_trading_network.to_tag_filter"),
									() -> {
										if(!tags.isEmpty()) {
											popup.replace(tags.stream().map(s -> new ButtonElement(() -> Component.literal(s.location().toString()), () -> {
												ItemStack st = new ItemStack(Content.TAG_FILTER.get(), clicked.getItem().getCount());
												st.applyComponents(DataComponentPatch.builder().set(Content.TAG_COMPONENT.get(), s).build());
												menu.setPhantom(clicked, st);
												popup.close();
											})).toArray(PopupElement[]::new));
										}
									}
							)
					);
			return true;
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if(popup.keyPressed(pKeyCode, pScanCode, pModifiers))return true;
		if (pKeyCode == 256) {
			if(nameBox.isFocused())nameBox.setFocus(false);
			else this.minecraft.player.closeContainer();
			return true;
		} else if(pKeyCode == GLFW.GLFW_KEY_TAB) {
			return super.keyPressed(pKeyCode, pScanCode, pModifiers);
		}

		return !this.nameBox.keyPressed(pKeyCode, pScanCode, pModifiers) && !this.nameBox.canConsumeInput() ? super.keyPressed(pKeyCode, pScanCode, pModifiers) : true;
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		if(popup.charTyped(pCodePoint, pModifiers))return true;
		return super.charTyped(pCodePoint, pModifiers);
	}

	private void updateGui() {
		for(BlockFaceDirection d : BlockFaceDirection.values()) {
			if(d == BlockFaceDirection.FRONT)continue;
			GuiButton b = sideCfgButtons.get(d);
			b.setState(getFacingState(d));
		}
	}

	private int getFacingState(BlockFaceDirection d) {
		return ((menu.inputCfg & (1 << d.ordinal())) != 0 ? 1 : 0) | ((menu.outputCfg & (1 << d.ordinal())) != 0 ? 2 : 0);
	}
}
