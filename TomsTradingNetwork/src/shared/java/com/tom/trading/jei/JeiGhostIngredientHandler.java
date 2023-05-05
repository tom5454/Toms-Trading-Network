package com.tom.trading.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.trading.gui.AbstractFilteredMenu;
import com.tom.trading.gui.AbstractFilteredScreen;
import com.tom.trading.gui.PhantomSlot;

@SuppressWarnings("rawtypes")
public class JeiGhostIngredientHandler implements JeiGhostIngredientHandlerPlatform<AbstractFilteredScreen> {

	@SuppressWarnings("unchecked")
	@Override
	public List<Target<ItemStack>> getTargets(AbstractFilteredScreen gui, ItemStack stack, boolean doStart) {
		List<Target<ItemStack>> targets = new ArrayList<>();
		for (Slot slot : gui.getMenu().slots) {
			if (slot instanceof PhantomSlot) {
				targets.add(new SlotTarget(gui, slot));
			}
		}
		return targets;
	}


	@Override
	public void onComplete() {
	}

	private static class SlotTarget implements Target<ItemStack> {
		private Slot slot;
		private Rect2i area;
		private AbstractFilteredScreen gui;

		public SlotTarget(AbstractFilteredScreen gui, Slot slot) {
			this.slot = slot;
			this.gui = gui;
			this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
		}

		@Override
		public Rect2i getArea() {
			return area;
		}

		@Override
		public void accept(ItemStack ingredient) {
			((AbstractFilteredMenu) gui.getMenu()).setPhantom(slot, ingredient);
		}
	}
}
