package com.tom.trading.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;

public class REIPlugin implements REIClientPlugin {

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerDraggableStackVisitor(new ReiGhostIngredientHandler());
	}
}
