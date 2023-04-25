package com.tom.trading.tile;

import java.util.EnumMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class VendingMachineBlockEntity extends VendingMachineBlockEntityBase implements SidedStorageBlockEntity {
	private EnumMap<Direction, Handler> itemCaps = new EnumMap<>(Direction.class);
	private Storage<ItemVariant> inputWr = FilteringStorage.insertOnlyOf(InventoryStorage.of(getInputs(), null));
	private Storage<ItemVariant> outputWr = FilteringStorage.extractOnlyOf(InventoryStorage.of(getOutputs(), null));

	public VendingMachineBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(pPos, pBlockState);
		for(Direction d : Direction.values()) {
			itemCaps.put(d, new Handler(d));
		}
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		if(side == null)return null;
		return itemCaps.get(side);
	}

	public class Handler extends FilteringStorage<ItemVariant> {
		private final Direction dir;

		public Handler(Direction dir) {
			super(new CombinedStorage<>(List.of(inputWr, outputWr)));
			this.dir = dir;
		}

		@Override
		protected boolean canInsert(ItemVariant resource) {
			return canInput(resource.toStack(), dir);
		}

		@Override
		protected boolean canExtract(ItemVariant resource) {
			return canOutput(dir);
		}

		@Override
		public boolean supportsInsertion() {
			return canInput(ItemStack.EMPTY, dir) && super.supportsInsertion();
		}

		@Override
		public boolean supportsExtraction() {
			return canOutput(dir) && super.supportsExtraction();
		}
	}
}
