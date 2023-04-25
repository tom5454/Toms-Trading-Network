package com.tom.trading.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.trading.Content;
import com.tom.trading.block.VendingMachineBlock;
import com.tom.trading.gui.VendingMachineConfigMenu;
import com.tom.trading.gui.VendingMachineTradingMenu;
import com.tom.trading.util.BasicContainer;
import com.tom.trading.util.BlockFaceDirection;

public abstract class VendingMachineBlockEntityBase extends OwnableBlockEntity implements MenuProvider, Nameable {
	private BasicContainer config = new BasicContainer(8);
	private BasicContainer inputs = new BasicContainer(8);
	private BasicContainer outputs = new BasicContainer(8);

	private int inputSides, outputSides;
	private Component name;
	private Boolean hasInputs;

	public VendingMachineBlockEntityBase(BlockPos pPos, BlockState pBlockState) {
		super(Content.VENDING_MACHINE_TILE.get(), pPos, pBlockState);
		inputs.addListener(c -> hasInputs = null);
		ContainerListener l = c -> setChanged();
		config.addListener(l);
		inputs.addListener(l);
		outputs.addListener(l);
	}

	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		inputs.fromTag(pTag.getList("Inputs", Tag.TAG_COMPOUND));
		outputs.fromTag(pTag.getList("Outputs", Tag.TAG_COMPOUND));
		config.fromTag(pTag.getList("Config", Tag.TAG_COMPOUND));
		inputSides = pTag.getInt("inputSides");
		outputSides = pTag.getInt("outputSides");
		if (pTag.contains("CustomName", 8)) {
			this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag pTag) {
		super.saveAdditional(pTag);
		pTag.put("Inputs", inputs.createTag());
		pTag.put("Outputs", outputs.createTag());
		pTag.put("Config", config.createTag());
		pTag.putInt("inputSides", inputSides);
		pTag.putInt("outputSides", outputSides);
		if (this.name != null) {
			pTag.putString("CustomName", Component.Serializer.toJson(this.name));
		}
	}

	public BasicContainer getInputs() {
		return inputs;
	}

	public BasicContainer getOutputs() {
		return outputs;
	}

	public Runnable consumeInputs() {
		return consumeInputs(inputs, 4);
	}

	public Runnable consumeInputs(Container inputs, int start) {
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
					if(!s.isEmpty() && ItemStack.isSameItemSameTags(s, o)) {
						int d = Math.min(rem, s.getCount());
						final int fj = j;
						actions.add(() -> inputs.removeItem(fj, d));
						rem -= d;
						s.shrink(d);
						if(rem < 1)break;
					}
				}
			}
			if(rem > 0)return null;
		}
		return () -> actions.forEach(Runnable::run);
	}

	public Runnable addOutput() {
		return addOutput(outputs, 0);
	}

	public Runnable refundInput() {
		return addOutput(outputs, 4);
	}

	private Runnable addOutput(Container outputs, int start) {
		ItemStack[] modArray = new ItemStack[outputs.getContainerSize()];
		for (int i = 0; i < modArray.length; i++) {
			modArray[i] = outputs.getItem(i).copy();
		}
		List<Runnable> actions = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			ItemStack o = config.getItem(i + start).copy();
			for (int j = 0; j < modArray.length; j++) {
				ItemStack s = modArray[j];
				if(modArray[j].isEmpty()) {
					ItemStack s2 = o.copy();
					modArray[j] = s2;
					o.setCount(0);
					final int fj = j;
					actions.add(() -> outputs.setItem(fj, s2));
				} else if(ItemStack.isSameItemSameTags(o, modArray[j])) {
					int m = Math.min(outputs.getMaxStackSize(), o.getMaxStackSize());
					int c = Math.min(o.getCount(), m - s.getCount());
					if(c > 0) {
						s.grow(c);
						o.shrink(c);
						final int fj = j;
						actions.add(() -> outputs.setItem(fj, s));
					}
				}
				if(o.isEmpty())break;
			}
			if(!o.isEmpty())return null;
		}
		return () -> actions.forEach(Runnable::run);
	}

	public boolean canInput(ItemStack stack, Direction dir) {
		Direction facing = getBlockState().getValue(VendingMachineBlock.FACING);
		BlockFaceDirection d = BlockFaceDirection.getHorizontalFace(facing, dir);
		if(d == BlockFaceDirection.FRONT)return false;
		if ((inputSides & (1 << d.ordinal())) != 0) {
			if(stack.isEmpty())return true;
			for (int i = 0; i < 4; i++) {
				ItemStack o = config.getItem(i + 4).copy();
				if(ItemStack.isSameItemSameTags(o, stack)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean canOutput(Direction dir) {
		Direction facing = getBlockState().getValue(VendingMachineBlock.FACING);
		BlockFaceDirection d = BlockFaceDirection.getHorizontalFace(facing, dir);
		if(d == BlockFaceDirection.FRONT)return false;
		return (outputSides & (1 << d.ordinal())) != 0;
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

	public int getTradingState() {
		if(hasInputs == null)hasInputs = consumeInputs() != null;
		return hasInputs ? 1 : 0;
	}

	public void setSides(int id, int config) {
		if((config & 1) != 0)inputSides |= (1 << id);
		else inputSides &=~ (1 << id);
		if((config & 2) != 0)outputSides |= (1 << id);
		else outputSides &=~ (1 << id);
		setChanged();
	}

	public int tradeWith(Container c) {
		Runnable commitGetPlayer = consumeInputs(c, 0);
		if(commitGetPlayer == null)return 1;
		Runnable commitGetInv = consumeInputs();
		if(commitGetInv == null)return 2;
		Runnable commitGive = addOutput(c, 4);
		if(commitGive == null)return 3;
		Runnable commitStore = addOutput();
		if(commitStore == null)return 4;
		commitGetPlayer.run();
		commitGetInv.run();
		commitGive.run();
		commitStore.run();
		return 0;
	}
}
