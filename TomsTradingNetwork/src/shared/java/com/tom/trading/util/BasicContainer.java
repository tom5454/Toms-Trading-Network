package com.tom.trading.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class BasicContainer extends SimpleContainer {

	public BasicContainer(int pSize) {
		super(pSize);
	}

	public BasicContainer(ItemStack... pItems) {
		super(pItems);
	}

	@Override
	public void fromTag(ListTag pContainerNbt) {
		for(int i = 0; i < this.getContainerSize(); ++i) {
			this.setItem(i, ItemStack.EMPTY);
		}

		for(int k = 0; k < pContainerNbt.size(); ++k) {
			CompoundTag compoundtag = pContainerNbt.getCompound(k);
			int j = compoundtag.getByte("Slot") & 255;
			if (j >= 0 && j < this.getContainerSize()) {
				this.setItem(j, ItemStack.of(compoundtag));
			}
		}

	}

	@Override
	public ListTag createTag() {
		ListTag listtag = new ListTag();

		for(int i = 0; i < this.getContainerSize(); ++i) {
			ItemStack itemstack = this.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundTag compoundtag = new CompoundTag();
				compoundtag.putByte("Slot", (byte)i);
				itemstack.save(compoundtag);
				listtag.add(compoundtag);
			}
		}

		return listtag;
	}
}
