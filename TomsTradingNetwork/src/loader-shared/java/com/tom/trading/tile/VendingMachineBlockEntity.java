package com.tom.trading.tile;

import java.util.EnumMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class VendingMachineBlockEntity extends VendingMachineBlockEntityBase {
	private EnumMap<Direction, LazyOptional<IItemHandler>> itemCaps = new EnumMap<>(Direction.class);
	private InvWrapper inputWr = new InvWrapper(getInputs());
	private InvWrapper outputWr = new InvWrapper(getOutputs());

	public VendingMachineBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(pPos, pBlockState);
		for(Direction d : Direction.values()) {
			itemCaps.put(d, LazyOptional.of(() -> new Handler(d)));
		}
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if(cap == ForgeCapabilities.ITEM_HANDLER) {
			if(side == null)return LazyOptional.empty();
			return itemCaps.get(side).cast();
		}
		return super.getCapability(cap, side);
	}

	public class Handler implements IItemHandler {
		private final Direction dir;

		public Handler(Direction dir) {
			this.dir = dir;
		}

		@Override
		public int getSlots() {
			return inputWr.getSlots() + outputWr.getSlots();
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return slot < inputWr.getSlots() ? inputWr.getStackInSlot(slot) : outputWr.getStackInSlot(slot - inputWr.getSlots());
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if (slot >= inputWr.getSlots() || !canInput(stack, dir)) {
				return stack;
			}
			return inputWr.insertItem(slot, stack, simulate);
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot < inputWr.getSlots() || !canOutput(dir)) {
				return ItemStack.EMPTY;
			}
			return outputWr.extractItem(slot - inputWr.getSlots(), amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return slot < inputWr.getSlots() && canInput(stack, dir);
		}
	}
}
