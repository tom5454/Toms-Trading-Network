package com.tom.trading.jei;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;

public interface JeiGhostIngredientHandlerPlatform<T extends Screen> extends IGhostIngredientHandler<T> {

	@Override
	default <I> List<Target<I>> getTargets(T gui, I ingredient, boolean doStart) {
		if (ingredient instanceof ItemStack stack) {
			return getTargets(gui, stack, doStart);
		}
		return Collections.emptyList();
	}

	@Override
	default <I> List<Target<I>> getTargetsTyped(T gui, ITypedIngredient<I> ingredient, boolean doStart) {
		if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
			ItemStack stack = (ItemStack) ingredient.getIngredient();
			return getTargets(gui, stack, doStart);
		}
		return Collections.emptyList();
	}

	<I> List<Target<I>> getTargets(T gui, ItemStack stack, boolean doStart);
}
