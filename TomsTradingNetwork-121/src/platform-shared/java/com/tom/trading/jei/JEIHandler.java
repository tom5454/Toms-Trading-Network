package com.tom.trading.jei;

import net.minecraft.resources.ResourceLocation;

import com.tom.trading.TradingNetworkMod;
import com.tom.trading.gui.AbstractFilteredScreen;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;

@JeiPlugin
public class JEIHandler implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.tryBuild(TradingNetworkMod.MODID, "jei");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGhostIngredientHandler(AbstractFilteredScreen.class, new JeiGhostIngredientHandler());
	}
}
