package com.tom.trading.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.trading.TradingNetworkMod;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

public class GuiButton extends PlatformButton {
	public static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(TradingNetworkMod.MODID, "textures/gui/buttons.png");

	public ResourceLocation texture;
	public int tile;
	private int state;
	public int texX = 0;
	public int texY = 0;
	public Int2ObjectFunction<Tooltip> tooltipFactory;

	public GuiButton(int x, int y, int tile, OnPress pressable) {
		super(x, y, 16, 16, null, pressable);
		this.tile = tile;
		this.texture = BUTTON_TEXTURES;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void renderWidget(GuiGraphics st, int mouseX, int mouseY, float pt) {
		if (this.visible) {
			int x = getX();
			int y = getY();
			this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
			st.blit(texture, x, y, texX + state * 16, texY + tile * 16, this.width, this.height);
		}
	}

	public static class CompositeButton extends GuiButton {
		public int texY_button = 16;
		public CompositeButton(int x, int y, int tile, OnPress pressable) {
			super(x, y, tile, pressable);
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderWidget(GuiGraphics st, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				int x = getX();
				int y = getY();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
				this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
				int i = this.getYImage(this.isHoveredOrFocused());
				st.blit(texture, x, y, texX + i * 16, this.texY_button, this.width, this.height);
				st.blit(texture, x, y, texX + tile * 16 + getState() * 16, texY, this.width, this.height);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}

	public void setState(int state) {
		this.state = state;
		if(tooltipFactory != null)setTooltip(tooltipFactory.apply(state));
	}

	public int getState() {
		return state;
	}
}