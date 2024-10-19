package com.tom.trading.tile;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class OwnableBlockEntity extends BlockEntity {
	private String ownerNameCache;
	private UUID owner;

	public OwnableBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}

	public void setOwner(Player p) {
		ownerNameCache = p.getScoreboardName();
		owner = p.getUUID();
		setChanged();
	}

	@Override
	protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
		super.saveAdditional(pTag, provider);
		if(ownerNameCache != null)pTag.putString("ownerNameCache", ownerNameCache);
		if(owner != null)pTag.putUUID("owner", owner);
	}

	@Override
	public void loadAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
		super.loadAdditional(pTag, provider);
		ownerNameCache = pTag.contains("ownerNameCache", Tag.TAG_STRING) ? pTag.getString("ownerNameCache") : null;
		owner = pTag.hasUUID("owner") ? pTag.getUUID("owner") : null;
	}

	public boolean canAccess(Player p) {
		return p.getUUID().equals(owner);
	}

	public String getOwnerNameCache() {
		return ownerNameCache;
	}
}
