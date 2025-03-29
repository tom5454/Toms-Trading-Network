package com.tom.trading.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.trading.menu.slot.PhantomSlot;
import com.tom.trading.network.NetworkHandler;
import com.tom.trading.util.IDataReceiver;

public abstract class AbstractFilteredMenu extends AbstractContainerMenu implements IDataReceiver {
	protected final Inventory pinv;

	protected AbstractFilteredMenu(MenuType<?> type, int wid, Inventory pinv) {
		super(type, wid);
		this.pinv = pinv;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType click, Player player) {
		Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
		if (slot instanceof PhantomSlot) {
			ItemStack s = getCarried().copy();
			if(!s.isEmpty())s.setCount(1);
			slot.set(s);
			return;
		}
		super.clicked(slotId, dragType, click, player);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return !(slot instanceof PhantomSlot);
	}

	public void setPhantom(Slot slot, ItemStack ingredient) {
		CompoundTag tag = new CompoundTag();
		CompoundTag t = new CompoundTag();
		tag.put("setPhantom", t);
		t.putInt("id", slot.index);
		t.put("item", ingredient.save(pinv.player.registryAccess()));
		NetworkHandler.sendDataToServer(tag);
	}

	@Override
	public void receive(CompoundTag tag) {
		if(pinv.player.isSpectator())return;
		tag.getCompound("setPhantom").ifPresent(t -> {
			int slotId = t.getIntOr("id", -1);

			ItemStack item = ItemStack.parse(pinv.player.registryAccess(), t.getCompoundOrEmpty("item")).orElse(ItemStack.EMPTY);
			Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
			if (slot instanceof PhantomSlot) {
				if(!item.isEmpty()) {
					item.setCount(1);
					slot.set(item);
				}
			}
		});
	}
}
