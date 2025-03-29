package com.tom.trading.tile;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
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
		if(owner != null)pTag.store("owner", UUIDUtil.CODEC, owner);
	}

	@Override
	public void loadAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
		super.loadAdditional(pTag, provider);
		ownerNameCache = pTag.getStringOr("ownerNameCache", null);
		owner = pTag.read("owner", UUIDUtil.CODEC).orElse(null);
	}

	public boolean canAccess(Player p) {
		return p.getUUID().equals(owner);
	}

	public String getOwnerNameCache() {
		return ownerNameCache;
	}
}
