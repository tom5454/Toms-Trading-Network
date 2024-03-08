package com.tom.trading.util;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LimitedContainer implements Container {
	private final Container delegate;
	private final int sizeLimit;

	public LimitedContainer(Container delegate, int sizeLimit) {
		this.delegate = delegate;
		this.sizeLimit = sizeLimit;
	}

	@Override
	public void clearContent() {
		delegate.clearContent();
	}

	@Override
	public int getContainerSize() {
		return Math.min(sizeLimit, delegate.getContainerSize());
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public ItemStack getItem(int pSlot) {
		return delegate.getItem(pSlot);
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		return delegate.removeItem(pSlot, pAmount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		return delegate.removeItemNoUpdate(pSlot);
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		delegate.setItem(pSlot, pStack);
	}

	@Override
	public int getMaxStackSize() {
		return delegate.getMaxStackSize();
	}

	@Override
	public void setChanged() {
		delegate.setChanged();
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return delegate.stillValid(pPlayer);
	}

	@Override
	public void startOpen(Player pPlayer) {
		delegate.startOpen(pPlayer);
	}

	@Override
	public void stopOpen(Player pPlayer) {
		delegate.stopOpen(pPlayer);
	}

	@Override
	public boolean canPlaceItem(int pIndex, ItemStack pStack) {
		return delegate.canPlaceItem(pIndex, pStack);
	}

	@Override
	public int countItem(Item pItem) {
		return delegate.countItem(pItem);
	}

	@Override
	public boolean hasAnyOf(Set<Item> pSet) {
		return delegate.hasAnyOf(pSet);
	}

	@Override
	public boolean hasAnyMatching(Predicate<ItemStack> p_216875_) {
		return delegate.hasAnyMatching(p_216875_);
	}

}
