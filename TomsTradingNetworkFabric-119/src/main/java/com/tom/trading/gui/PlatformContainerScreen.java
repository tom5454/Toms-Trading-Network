package com.tom.trading.gui;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public abstract class PlatformContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	public PlatformContainerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	public Slot getSlotUnderMouse() {
		return hoveredSlot;
	}
}
