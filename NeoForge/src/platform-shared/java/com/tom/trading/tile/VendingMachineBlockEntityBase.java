package com.tom.trading.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.trading.Content;
import com.tom.trading.block.VendingMachineBlock;
import com.tom.trading.menu.VendingMachineConfigMenu;
import com.tom.trading.menu.VendingMachineTradingMenu;
import com.tom.trading.util.BasicContainer;
import com.tom.trading.util.BlockFaceDirection;
import com.tom.trading.util.TradeResult;

public abstract class VendingMachineBlockEntityBase extends OwnableBlockEntity implements MenuProvider, Nameable {
	private BasicContainer config = new BasicContainer(8);
	private BasicContainer inputs = new BasicContainer(8);
	private BasicContainer outputs = new BasicContainer(8);

	private int inputSides, outputSides, autoSides, matchNBT = 0xff;
	private Component name;
	private Boolean hasInputs;
	private boolean creativeMode;

	public VendingMachineBlockEntityBase(BlockPos pPos, BlockState pBlockState) {
		super(Content.VENDING_MACHINE_TILE.get(), pPos, pBlockState);
		inputs.addListener(c -> hasInputs = null);
		config.addListener(c -> hasInputs = null);
		ContainerListener l = c -> setChanged();
		config.addListener(l);
		inputs.addListener(l);
		outputs.addListener(l);
	}

	@Override
	public void loadAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
		super.loadAdditional(pTag, provider);
		inputs.fromTag(pTag.getList("Inputs", Tag.TAG_COMPOUND), provider);
		outputs.fromTag(pTag.getList("Outputs", Tag.TAG_COMPOUND), provider);
		config.fromTag(pTag.getList("Config", Tag.TAG_COMPOUND), provider);
		inputSides = pTag.getInt("inputSides");
		outputSides = pTag.getInt("outputSides");
		autoSides = pTag.getInt("autoSides");
		matchNBT = pTag.contains("matchNBT") ? pTag.getInt("matchNBT") : 0xff;
		creativeMode = pTag.getBoolean("Creative");
		if (pTag.contains("CustomName", 8)) {
			this.name = Component.Serializer.fromJson(pTag.getString("CustomName"), provider);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
		super.saveAdditional(pTag, provider);
		pTag.put("Inputs", inputs.createTag(provider));
		pTag.put("Outputs", outputs.createTag(provider));
		pTag.put("Config", config.createTag(provider));
		pTag.putInt("inputSides", inputSides);
		pTag.putInt("outputSides", outputSides);
		pTag.putInt("autoSides", autoSides);
		pTag.putInt("matchNBT", matchNBT);
		pTag.putBoolean("Creative", creativeMode);
		if (this.name != null) {
			pTag.putString("CustomName", Component.Serializer.toJson(this.name, provider));
		}
	}

	public BasicContainer getInputs() {
		return inputs;
	}

	public BasicContainer getOutputs() {
		return outputs;
	}

	public Runnable consumeInputs(List<ItemStack> items) {
		return consumeInputs(inputs, 4, items);
	}

	public Runnable consumeInputs(Container inputs, int start, List<ItemStack> items) {
		ItemStack[] modArray = new ItemStack[inputs.getContainerSize()];
		for (int i = 0; i < modArray.length; i++) {
			modArray[i] = inputs.getItem(i).copy();
		}
		List<Runnable> actions = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			ItemStack o = config.getItem(i + start);
			int rem = o.getCount();
			if(rem > 0) {
				for(int j = 0;j < modArray.length; j++) {
					ItemStack s = modArray[j];
					if(!s.isEmpty() && compareItemStack(s, o, i + start)) {
						int d = Math.min(rem, s.getCount());
						final int fj = j;
						actions.add(() -> inputs.removeItem(fj, d));
						rem -= d;
						items.add(s.split(d));
						if(rem < 1)break;
					}
				}
			}
			if(rem > 0)return null;
		}
		return () -> actions.forEach(Runnable::run);
	}

	public Runnable addOutput(List<ItemStack> items) {
		return addOutput(outputs, items, false);
	}

	private Runnable addOutput(Container outputs, List<ItemStack> items, boolean drop) {
		ItemStack[] modArray = new ItemStack[outputs.getContainerSize()];
		for (int i = 0; i < modArray.length; i++) {
			modArray[i] = outputs.getItem(i).copy();
		}
		List<Runnable> actions = new ArrayList<>();
		for (int i = 0; i < items.size(); i++) {
			ItemStack o = items.get(i).copy();
			for (int j = 0; j < modArray.length; j++) {
				if(ItemStack.isSameItemSameComponents(o, modArray[j])) {
					ItemStack s = modArray[j];
					int m = Math.min(outputs.getMaxStackSize(), o.getMaxStackSize());
					int c = Math.min(o.getCount(), m - s.getCount());
					if(c > 0) {
						ItemStack ins = o.copy();
						ins.setCount(c);
						o.shrink(c);
						s.grow(c);
						actions.add(() -> {
							ItemStack is = HopperBlockEntity.addItem(null, outputs, ins, null);
							if (!is.isEmpty()) {
								if (drop)
									Block.popResource(level, worldPosition, is);
								//never?
							}
						});
					}
				}
				if(o.isEmpty())break;
			}
			if(!o.isEmpty()) {
				for (int j = 0; j < modArray.length; j++) {
					if(modArray[j].isEmpty()) {
						ItemStack s2 = o.copy();
						modArray[j] = s2;
						o.setCount(0);
						actions.add(() -> {
							ItemStack is = HopperBlockEntity.addItem(null, outputs, s2, null);
							if (!is.isEmpty()) {
								if (drop)
									Block.popResource(level, worldPosition, is);
							}
						});
					}
				}
			}
			if(!o.isEmpty())return null;
		}
		return () -> actions.forEach(Runnable::run);
	}

	public boolean canInput(ItemStack stack, Direction dir) {
		return canInputFrom(dir) && canInputItem(stack);
	}

	public boolean canInputItem(ItemStack stack) {
		if(stack.isEmpty())return false;
		for (int i = 0; i < 4; i++) {
			ItemStack o = config.getItem(i + 4).copy();
			if(compareItemStack(stack, o, i + 4)) {
				return true;
			}
		}
		return false;
	}

	public boolean compareItemStack(ItemStack stack, ItemStack template, int slot) {
		if(template.getItem() == Content.TAG_FILTER.get()) {
			TagKey<Item> tag = template.get(Content.TAG_COMPONENT.get());
			if(tag == null)return false;
			return stack.is(tag);
		}
		if((matchNBT & (1 << slot)) != 0)return ItemStack.isSameItemSameComponents(stack, template);
		else return ItemStack.isSameItem(stack, template);
	}

	public boolean canInputFrom(Direction dir) {
		Direction facing = getBlockState().getValue(VendingMachineBlock.FACING);
		BlockFaceDirection d = BlockFaceDirection.getHorizontalFace(facing, dir);
		if(d == BlockFaceDirection.FRONT)return false;
		return (inputSides & (1 << d.ordinal())) != 0;
	}

	public boolean canOutput(Direction dir) {
		Direction facing = getBlockState().getValue(VendingMachineBlock.FACING);
		BlockFaceDirection d = BlockFaceDirection.getHorizontalFace(facing, dir);
		if(d == BlockFaceDirection.FRONT)return false;
		return (outputSides & (1 << d.ordinal())) != 0;
	}

	public boolean isAutoSide(Direction dir) {
		Direction facing = getBlockState().getValue(VendingMachineBlock.FACING);
		BlockFaceDirection d = BlockFaceDirection.getHorizontalFace(facing, dir);
		if(d == BlockFaceDirection.FRONT)return false;
		return (autoSides & (1 << d.ordinal())) != 0;
	}

	public boolean isInRange(Player pPlayer) {
		return !remove && pPlayer.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0D;
	}

	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		if(canAccess(pPlayer)) {
			return new VendingMachineConfigMenu(pContainerId, pPlayerInventory, this);
		} else {
			return new VendingMachineTradingMenu(pContainerId, pPlayerInventory, this);
		}
	}

	public void setCustomName(Component pName) {
		this.name = pName;
		setChanged();
	}

	@Override
	public Component getName() {
		return this.name != null ? this.name : this.getDefaultName();
	}

	@Override
	public Component getDisplayName() {
		return this.getName();
	}

	@Override
	public Component getCustomName() {
		return this.name;
	}

	protected Component getDefaultName() {
		return Content.VENDING_MACHINE.get().getName();
	}

	public BasicContainer getConfig() {
		return config;
	}

	public int getInputSides() {
		return inputSides;
	}

	public int getOutputSides() {
		return outputSides;
	}

	public int getAutoSides() {
		return autoSides;
	}

	public int getMatchNBT() {
		return matchNBT;
	}

	public boolean isCreativeMode() {
		return creativeMode;
	}

	public int getTradingState() {
		if(hasInputs == null)hasInputs = creativeMode || consumeInputs(new ArrayList<>()) != null;
		return hasInputs ? 1 : 0;
	}

	public void setSides(int id, int config, boolean auto) {
		if((config & 1) != 0)inputSides |= (1 << id);
		else inputSides &=~ (1 << id);
		if((config & 2) != 0)outputSides |= (1 << id);
		else outputSides &=~ (1 << id);
		if (auto && config != 0) autoSides |= (1 << id);
		else autoSides &=~ (1 << id);
		setChanged();
	}

	public void setMatchNBT(int slot, boolean config) {
		if(config)matchNBT |= (1 << slot);
		else matchNBT &=~ (1 << slot);
	}

	public TradeResult tradeWith(Container c) {
		if (creativeMode) {
			List<ItemStack> playerItems = new ArrayList<>();
			Runnable commitGetPlayer = consumeInputs(c, 0, playerItems);
			if(commitGetPlayer == null)return TradeResult.TRADER_MISSING_INPUT;
			List<ItemStack> machineItems = new ArrayList<>();
			for (int i = 4; i < 8; i++) {
				ItemStack o = config.getItem(i);
				int rem = o.getCount();
				if(rem > 0 && o.getItem() != Content.TAG_FILTER.get())
					machineItems.add(o.copy());
			}
			Runnable commitGive = addOutput(c, machineItems, true);
			if(commitGive == null)return TradeResult.TRADER_NO_SPACE;
			commitGetPlayer.run();
			commitGive.run();
			return TradeResult.SUCCESS;
		}
		List<ItemStack> playerItems = new ArrayList<>();
		List<ItemStack> machineItems = new ArrayList<>();
		Runnable commitGetPlayer = consumeInputs(c, 0, playerItems);
		if(commitGetPlayer == null)return TradeResult.TRADER_MISSING_INPUT;
		Runnable commitGetInv = consumeInputs(machineItems);
		if(commitGetInv == null)return TradeResult.MACHINE_MISSING_INPUT;
		Runnable commitGive = addOutput(c, machineItems, true);
		if(commitGive == null)return TradeResult.TRADER_NO_SPACE;
		Runnable commitStore = addOutput(playerItems);
		if(commitStore == null)return TradeResult.MACHINE_NO_SPACE;
		commitGetPlayer.run();
		commitGetInv.run();
		commitGive.run();
		commitStore.run();
		return TradeResult.SUCCESS;
	}

	@Override
	public boolean canAccess(Player p) {
		if (creativeMode && !p.getAbilities().instabuild)return false;
		return super.canAccess(p);
	}

	public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T beIn) {
		if (!(beIn instanceof VendingMachineBlockEntity be))return;
		if (level.getGameTime() % 20 != Math.abs(pos.hashCode()) % 20)return;
		for (Direction d : Direction.values()) {
			if (be.isAutoSide(d)) {
				if (be.canInputFrom(d)) {
					be.pullItemsFrom(pos.relative(d), d.getOpposite());
				}
				if (be.canOutput(d)) {
					be.pushItemsTo(pos.relative(d), d.getOpposite());
				}
			}
		}
	}

	public void setCreativeMode(boolean b) {
		this.creativeMode = b;
		hasInputs = null;
		setChanged();
	}
}
