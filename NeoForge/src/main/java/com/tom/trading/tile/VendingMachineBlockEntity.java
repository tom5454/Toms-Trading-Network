package com.tom.trading.tile;

import java.util.EnumMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

import com.tom.trading.util.PredicatingHandler;

public class VendingMachineBlockEntity extends VendingMachineBlockEntityBase {
	private EnumMap<Direction, ResourceHandler<ItemResource>> itemCaps = new EnumMap<>(Direction.class);
	private ResourceHandler<ItemResource> inputWr = VanillaContainerWrapper.of(getInputs());
	private ResourceHandler<ItemResource> outputWr = VanillaContainerWrapper.of(getOutputs());
	private EnumMap<Direction, BlockCapabilityCache<ResourceHandler<ItemResource>, Direction>> sideCache = new EnumMap<>(Direction.class);

	public VendingMachineBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(pPos, pBlockState);
		for(Direction d : Direction.values()) {
			itemCaps.put(d, new CombinedResourceHandler<>(
					new PredicatingHandler<>(inputWr, d, this::canInput, null),
					new PredicatingHandler<>(outputWr, d, null, this::canOutput)
					));
		}
	}

	private boolean canInput(Direction d, ItemResource resource) {
		return canInput(resource.toStack(), d);
	}

	public ResourceHandler<ItemResource> getInventory(@Nullable Direction side) {
		return itemCaps.get(side);
	}

	public void pullItemsFrom(BlockPos relative, Direction opposite) {
		ResourceHandler<ItemResource> cap = getHandler(relative, opposite);
		if (cap != null) {
			ResourceHandlerUtil.move(cap, inputWr, r -> canInputItem(r.toStack()), 64, null);
		}
	}

	public void pushItemsTo(BlockPos relative, Direction opposite) {
		ResourceHandler<ItemResource> cap = getHandler(relative, opposite);
		if (cap != null) {
			ResourceHandlerUtil.move(outputWr, cap, r -> true, 64, null);
		}
	}

	private ResourceHandler<ItemResource> getHandler(BlockPos relative, Direction d) {
		return sideCache.computeIfAbsent(d, __ -> {
			return BlockCapabilityCache.create(Capabilities.Item.BLOCK, (ServerLevel) level, relative, d, () -> !isRemoved(), () -> {});
		}).getCapability();
	}
}
