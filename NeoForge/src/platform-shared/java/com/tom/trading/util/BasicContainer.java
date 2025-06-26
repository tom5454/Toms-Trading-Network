package com.tom.trading.util;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BasicContainer extends SimpleContainer {

	public BasicContainer(int pSize) {
		super(pSize);
	}

	public BasicContainer(ItemStack... pItems) {
		super(pItems);
	}

	public void loadItems(ValueInput.TypedInputList<ItemStackWithSlot> pContainerNbt) {
		for(int i = 0; i < this.getContainerSize(); ++i) {
			this.setItem(i, ItemStack.EMPTY);
		}

		for (final ItemStackWithSlot itemStackWithSlot : pContainerNbt) {
			if (itemStackWithSlot.isValidInContainer(getContainerSize())) {
				this.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
			}
		}
	}

	public void storeItems(ValueOutput.TypedOutputList<ItemStackWithSlot> output) {
		for (int i = 0; i < this.getContainerSize(); ++i) {
			ItemStack itemstack = this.getItem(i);
			if (!itemstack.isEmpty()) {
				output.add(new ItemStackWithSlot(i, itemstack));
			}
		}
	}
}
