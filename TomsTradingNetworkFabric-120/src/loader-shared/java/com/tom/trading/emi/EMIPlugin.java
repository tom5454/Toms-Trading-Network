package com.tom.trading.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

public class EMIPlugin implements EmiPlugin {

	@Override
	public void register(EmiRegistry registry) {
		registry.addGenericDragDropHandler(new EmiGhostIngredientHandler());
	}
}
