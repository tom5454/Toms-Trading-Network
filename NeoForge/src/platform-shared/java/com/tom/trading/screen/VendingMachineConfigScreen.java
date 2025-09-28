package com.tom.trading.screen;

import java.util.EnumMap;

import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.trading.Content;
import com.tom.trading.TradingNetworkMod;
import com.tom.trading.menu.VendingMachineConfigMenu;
import com.tom.trading.menu.slot.PhantomSlot;
import com.tom.trading.screen.widget.IOMode;
import com.tom.trading.screen.widget.IOSideModeButton;
import com.tom.trading.screen.widget.ToggleButton;
import com.tom.trading.util.BlockFaceDirection;
import com.tom.trading.util.PopupMenuManager;
import com.tom.trading.util.PopupMenuManager.ButtonElement;
import com.tom.trading.util.PopupMenuManager.PopupElement;
import com.tom.trading.util.PopupMenuManager.TextFieldElement;

public class VendingMachineConfigScreen extends AbstractFilteredScreen<VendingMachineConfigMenu> {
	private static final ResourceLocation gui = ResourceLocation.tryBuild(TradingNetworkMod.MODID, "textures/gui/vending_machine_config.png");
	private static final ResourceLocation tagBg = ResourceLocation.tryBuild(TradingNetworkMod.MODID, "icons/tag_filter_bg");
	private EditBox nameBox;
	private Component title;
	private EnumMap<BlockFaceDirection, IOSideModeButton> sideCfgButtons = new EnumMap<>(BlockFaceDirection.class);
	private PopupMenuManager popup = new PopupMenuManager(this);
	private ToggleButton creativeBtn;

	public VendingMachineConfigScreen(VendingMachineConfigMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		this.title = pTitle;
		pMenu.updateGui = this::updateGui;
	}

	@Override
	protected void renderBg(GuiGraphics gr, float pPartialTick, int pMouseX, int pMouseY) {
		gr.blit(RenderPipelines.GUI_TEXTURED, gui, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

		for (int i = 0;i<8;i++) {
			Slot s = menu.getSlot(i);
			if(!s.getItem().isEmpty() && ((menu.matchNBT & (1 << i)) == 0 || s.getItem().getItem() == Content.TAG_FILTER.get())) {
				gr.blitSprite(RenderPipelines.GUI_TEXTURED, tagBg, this.leftPos + s.x - 1, this.topPos + s.y - 1, 18, 18);
			}
		}

		gr.blitSprite(RenderPipelines.GUI_TEXTURED, minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(ResourceLocation.tryBuild(TradingNetworkMod.MODID, "block/vending_machine_front")), this.leftPos + 134, this.topPos + 46, 16, 16);
	}

	@Override
	protected void init() {
		imageWidth = 176;
		imageHeight = 211;
		inventoryLabelY = imageHeight - 92;
		sideCfgButtons.clear();
		super.init();

		nameBox = new EditBox(font, this.leftPos + 7, this.topPos + 7, 110, 16, Component.translatable("narrator.toms_trading_network.vending_machine_name"));
		this.addRenderableWidget(this.nameBox);
		nameBox.setValue(title.getString());
		nameBox.setResponder(this::onNameChanged);

		for(BlockFaceDirection d : BlockFaceDirection.values()) {
			if(d == BlockFaceDirection.FRONT)continue;
			IOSideModeButton btn = new IOSideModeButton(this.leftPos + 118 + d.getX() * 16, this.topPos + 30 + d.getY() * 16, d, menu::setSides);
			sideCfgButtons.put(d, btn);
			addRenderableWidget(btn);
		}

		addRenderableWidget(Button.builder(Component.translatable("button.toms_trading_network.vending_machine.open_trading"), b -> {
			sendButtonClick(0b0100_0000);
		}).bounds(this.leftPos + 120, this.topPos + 7, 50, 20).build());

		creativeBtn = addRenderableWidget(ToggleButton.builder(this.leftPos + this.imageWidth - 18, this.topPos - 18).
				name(Component.translatable("narrator.toms_trading_network.vending_machine.creative_mode")).
				iconOff(ResourceLocation.tryBuild(TradingNetworkMod.MODID, "icons/creative_off")).
				iconOn(ResourceLocation.tryBuild(TradingNetworkMod.MODID, "icons/creative_on")).
				build(s -> {
					sendButtonClick(0b0010_0000 | (s ? 1 : 0));
				}));
		creativeBtn.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_trading_network.creative_mode.off")), Tooltip.create(Component.translatable("tooltip.toms_trading_network.creative_mode.on")));

		if (minecraft.player.getAbilities().instabuild) {
			addRenderableWidget(creativeBtn);
		}
		updateGui();
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
		super.render(gr, pMouseX, pMouseY, pPartialTick);
		if(popup.render(gr, font, pMouseX, pMouseY)) {
			this.renderTooltip(gr, pMouseX, pMouseY);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics gr, int pMouseX, int pMouseY) {
		gr.drawString(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF404040, false);

		gr.drawString(font, Component.translatable("label.toms_trading_network.vending_machine.cost"), this.inventoryLabelX, 26, 0xFF404040, false);
		gr.drawString(font, Component.translatable("label.toms_trading_network.vending_machine.result"), 76, 26, 0xFF404040, false);
		gr.drawString(font, Component.translatable("label.toms_trading_network.vending_machine.input"), this.inventoryLabelX, 72, 0xFF404040, false);
		gr.drawString(font, Component.translatable("label.toms_trading_network.vending_machine.output"), 98, 72, 0xFF404040, false);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
		if(popup.mouseClick(mouseButtonEvent))return true;
		Slot clicked = getSlotUnderMouse();
		if(mouseButtonEvent.button() == 1 && clicked instanceof PhantomSlot && !clicked.getItem().isEmpty()) {
			int popupSlot = clicked.getContainerSlot();
			boolean tagFilter = clicked.getItem().getItem() == Content.TAG_FILTER.get();
			var tags = clicked.getItem().getTags().toList();
			nameBox.setFocused(false);
			popup.open(mouseButtonEvent.x(), mouseButtonEvent.y(),
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
		return super.mouseClicked(mouseButtonEvent, bl);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if(popup.keyPressed(keyEvent))return true;
		if (keyEvent.key() == 256) {
			if(nameBox.isFocused())nameBox.setFocused(false);
			else this.minecraft.player.closeContainer();
			return true;
		} else if(keyEvent.key() == GLFW.GLFW_KEY_TAB) {
			return super.keyPressed(keyEvent);
		}

		return !this.nameBox.keyPressed(keyEvent) && !this.nameBox.canConsumeInput() ? super.keyPressed(keyEvent) : true;
	}

	@Override
	public boolean charTyped(CharacterEvent characterEvent) {
		if(popup.charTyped(characterEvent))return true;
		return super.charTyped(characterEvent);
	}

	private void updateGui() {
		for(BlockFaceDirection d : BlockFaceDirection.values()) {
			if(d == BlockFaceDirection.FRONT)continue;
			IOSideModeButton b = sideCfgButtons.get(d);
			b.setState(getFacingState(d));
			b.setAutoMode(isAuto(d));
		}
		creativeBtn.setState(menu.creativeMode != 0);
	}

	private IOMode getFacingState(BlockFaceDirection d) {
		boolean input = (menu.inputCfg & (1 << d.ordinal())) != 0;
		boolean output = (menu.outputCfg & (1 << d.ordinal())) != 0;

		if (input && output)return IOMode.IO;
		if (input)return IOMode.INPUT;
		if (output)return IOMode.OUTPUT;
		return IOMode.OFF;
	}

	private boolean isAuto(BlockFaceDirection d) {
		return (menu.autoCfg & (1 << d.ordinal())) != 0;
	}
}
