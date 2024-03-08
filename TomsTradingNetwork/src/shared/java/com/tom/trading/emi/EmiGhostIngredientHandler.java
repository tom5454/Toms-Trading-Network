package com.tom.trading.emi;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.trading.gui.AbstractFilteredMenu;
import com.tom.trading.gui.AbstractFilteredScreen;
import com.tom.trading.gui.PhantomSlot;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;

@SuppressWarnings("rawtypes")
public class EmiGhostIngredientHandler implements EmiDragDropHandler<Screen> {

	private static ItemStack getStack(EmiIngredient ing) {
		return ing.getEmiStacks().stream().map(EmiStack::getItemStack).filter(s -> !s.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
	}

	@Override
	public boolean dropStack(Screen screen, EmiIngredient stackIn, int x, int y) {
		if(!(screen instanceof AbstractFilteredScreen scr))return false;
		ItemStack stack = getStack(stackIn);
		if(stack.isEmpty())return false;
		var targets = getTargets(scr, stack);
		for (DropTarget b : targets) {
			if (b.getArea().contains(x, y)) {
				b.accept(stack);
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(Screen screen, EmiIngredient dragged, PoseStack matrices, int mouseX, int mouseY, float delta) {
		if(!(screen instanceof AbstractFilteredScreen scr))return;
		ItemStack stack = getStack(dragged);
		if(stack.isEmpty())return;
		var targets = getTargets(scr, stack);
		for (DropTarget t : targets) {
			Bounds b = t.getArea();
			GuiComponent.fill(matrices, b.x(), b.y(), b.x() + b.width(), b.y() + b.height(), 0x8822BB33);
		}
	}

	private <I> List<DropTarget> getTargets(AbstractFilteredScreen gui, ItemStack stack) {
		List<DropTarget> targets = new ArrayList<>();
		for (Slot slot : gui.getMenu().slots) {
			if (slot instanceof PhantomSlot) {
				targets.add(new SlotTarget(gui, slot));
			}
		}
		return targets;
	}

	private static class SlotTarget implements DropTarget {
		private Slot slot;
		private Bounds area;
		private AbstractFilteredScreen gui;

		public SlotTarget(AbstractFilteredScreen gui, Slot slot) {
			this.slot = slot;
			this.gui = gui;
			this.area = new Bounds(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
		}

		@Override
		public Bounds getArea() {
			return area;
		}

		@Override
		public void accept(ItemStack ingredient) {
			((AbstractFilteredMenu) gui.getMenu()).setPhantom(slot, ingredient);
		}
	}

	interface DropTarget {
		Bounds getArea();
		void accept(ItemStack stack);
	}
}
