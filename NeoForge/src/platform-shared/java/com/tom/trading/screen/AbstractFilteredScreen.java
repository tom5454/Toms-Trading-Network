package com.tom.trading.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.tom.trading.gui.PlatformContainerScreen;
import com.tom.trading.menu.AbstractFilteredMenu;

public abstract class AbstractFilteredScreen<T extends AbstractFilteredMenu> extends PlatformContainerScreen<T> {

	public AbstractFilteredScreen(T p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

	public AbstractFilteredScreen(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
		super(menu, inventory, title, imageWidth, imageHeight);
	}
}
