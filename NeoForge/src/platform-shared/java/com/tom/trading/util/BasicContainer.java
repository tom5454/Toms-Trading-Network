package com.tom.trading.util;

import net.minecraft.core.HolderLookup;
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
	public void fromTag(ListTag pContainerNbt, HolderLookup.Provider prov) {
		for(int i = 0; i < this.getContainerSize(); ++i) {
			this.setItem(i, ItemStack.EMPTY);
		}

		for(int k = 0; k < pContainerNbt.size(); ++k) {
			CompoundTag compoundtag = pContainerNbt.getCompoundOrEmpty(k);
			int j = compoundtag.getByteOr("Slot", (byte) 0) & 255;
			if (j >= 0 && j < this.getContainerSize()) {
				this.setItem(j, ItemStack.parse(prov, compoundtag.getCompoundOrEmpty("item")).orElse(ItemStack.EMPTY));
			}
		}

	}

	@Override
	public ListTag createTag(HolderLookup.Provider prov) {
		ListTag listtag = new ListTag();

		for(int i = 0; i < this.getContainerSize(); ++i) {
			ItemStack itemstack = this.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundTag compoundtag = new CompoundTag();
				compoundtag.putByte("Slot", (byte)i);
				compoundtag.put("item", itemstack.save(prov));
				listtag.add(compoundtag);
			}
		}

		return listtag;
	}
}
