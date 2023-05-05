package com.tom.trading.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.trading.Content;
import com.tom.trading.network.NetworkHandler;
import com.tom.trading.tile.VendingMachineBlockEntityBase;
import com.tom.trading.util.DataSlots;
import com.tom.trading.util.IDataReceiver;

public class VendingMachineConfigMenu extends AbstractFilteredMenu implements IDataReceiver {
	private VendingMachineBlockEntityBase machine;
	protected final Inventory pinv;

	public int inputCfg, outputCfg, matchNBT;
	public Runnable updateGui;

	public VendingMachineConfigMenu(int pContainerId, Inventory pPlayerInventory) {
		this(pContainerId, pPlayerInventory, new SimpleContainer(9), new SimpleContainer(9), new SimpleContainer(8));

		addDataSlot(DataSlots.set(c -> inputCfg = c).onUpdate(this::updateGui));
		addDataSlot(DataSlots.set(c -> outputCfg = c).onUpdate(this::updateGui));
		addDataSlot(DataSlots.set(c -> matchNBT = c));
	}

	public VendingMachineConfigMenu(int pContainerId, Inventory pPlayerInventory, VendingMachineBlockEntityBase machine) {
		this(pContainerId, pPlayerInventory, machine.getInputs(), machine.getOutputs(), machine.getConfig());
		this.machine = machine;

		addDataSlot(DataSlots.get(machine::getInputSides));
		addDataSlot(DataSlots.get(machine::getOutputSides));
		addDataSlot(DataSlots.get(machine::getMatchNBT));
	}

	private VendingMachineConfigMenu(int pContainerId, Inventory pPlayerInventory, Container input, Container output, Container config) {
		super(Content.VENDING_MACHINE_CONFIG_MENU.get(), pContainerId, pPlayerInventory);
		this.pinv = pPlayerInventory;

		for(int i = 0; i < 4; ++i) {
			this.addSlot(new PhantomSlot(config, i, 8 + (i % 2) * 18, 35 + (i / 2) * 18));
		}

		for(int i = 0; i < 4; ++i) {
			this.addSlot(new PhantomSlot(config, i + 4, 76 + (i % 2) * 18, 35 + (i / 2) * 18));
		}

		for(int i = 0; i < 2; ++i) {
			for(int j = 0; j < 4; ++j) {
				this.addSlot(new Slot(input, j + i * 4, 8 + j * 18, 82 + i * 18) {

					@Override
					public boolean mayPlace(ItemStack pStack) {
						for (int i = 0; i < 4; i++) {
							ItemStack o = config.getItem(i + 4).copy();
							if(compareItemStack(o, pStack, i + 4)) {
								return true;
							}
						}
						return false;
					}
				});
			}
		}

		for(int i = 0; i < 2; ++i) {
			for(int j = 0; j < 4; ++j) {
				this.addSlot(new Slot(output, j + i * 4, 98 + j * 18, 82 + i * 18) {

					@Override
					public boolean mayPlace(ItemStack pStack) {
						return false;
					}
				});
			}
		}

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 129 + i * 18));
			}
		}

		for(int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 187));
		}
	}

	public boolean compareItemStack(ItemStack pStack, ItemStack pOther, int slot) {
		return ItemStack.isSame(pStack, pOther) && ((matchNBT & (1 << slot)) == 0 || ItemStack.tagMatches(pStack, pOther));
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(pIndex);
		if (slot instanceof PhantomSlot) {
			slot.set(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (pIndex >= 8 && pIndex < 8 + 9 + 9) {
				if (!this.moveItemStackTo(itemstack1, 8 + 9 + 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (pIndex >= 8 + 9 + 9) {
				boolean in = false;
				for (int i = 0; i < 4; i++) {
					ItemStack o = this.slots.get(i + 4).getItem().copy();
					if(compareItemStack(o, slot.getItem(), i + 4)) {
						in = true;
						break;
					}
				}
				if(!in)return ItemStack.EMPTY;
				if (!this.moveItemStackTo(itemstack1, 8, 8 + 9, false)) {
					return ItemStack.EMPTY;
				}
			}
		}
		return itemstack;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return machine != null ? machine.isInRange(pPlayer) : true;
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType pClickType, Player pPlayer) {
		Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
		if (slot instanceof PhantomSlot) {
			slot.set(getCarried().copy());
			return;
		}
		super.clicked(slotId, dragType, pClickType, pPlayer);
	}

	@Override
	public boolean clickMenuButton(Player pPlayer, int pId) {
		if(pId == 0b0100_0000) {
			pPlayer.openMenu(new MenuProvider() {

				@Override
				public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
					return new VendingMachineTradingMenu(pContainerId, pPlayerInventory, machine);
				}

				@Override
				public Component getDisplayName() {
					return machine.getDisplayName();
				}
			});
			return true;
		} else if((pId & 0b0110_0000) != 0) {
			int slot = pId & 0xf;
			boolean c = (pId & 0b0001_0000) != 0;
			machine.setMatchNBT(slot, c);
			return true;
		} else if((pId & 0b0010_0000) != 0) {
			int c = pId & 0b11;
			int id = (pId >> 2) & 0b111;
			machine.setSides(id, c);
			return true;
		}
		return false;
	}

	@Override
	public void receive(CompoundTag tag) {
		if(pinv.player.isSpectator())return;
		if(tag.contains("setItemCount")) {
			CompoundTag t = tag.getCompound("setItemCount");
			int slotId = t.getInt("id");
			byte count = t.getByte("count");
			Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
			if (slot instanceof PhantomSlot) {
				ItemStack s = slot.getItem().copy();
				s.setCount(count);
				slot.set(s);
			}
		}
		if(tag.contains("setName")) {
			machine.setCustomName(Component.literal(tag.getString("setName")));
		}
		if(tag.contains("setPhantom")) {
			CompoundTag t = tag.getCompound("setPhantom");
			int slotId = t.getInt("id");
			ItemStack item = ItemStack.of(t.getCompound("item"));
			Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
			if (slot instanceof PhantomSlot) {
				if(!item.isEmpty()) {
					item.setCount(1);
					slot.set(item);
				}
			}
		}
	}

	public void setConfigCount(Slot slot, int count) {
		CompoundTag tag = new CompoundTag();
		CompoundTag t = new CompoundTag();
		tag.put("setItemCount", t);
		t.putInt("id", slot.index);
		t.putByte("count", (byte) count);
		NetworkHandler.sendDataToServer(tag);
	}

	public void setName(String name) {
		CompoundTag tag = new CompoundTag();
		tag.putString("setName", name);
		NetworkHandler.sendDataToServer(tag);
	}

	private void updateGui() {
		if(updateGui != null)updateGui.run();
	}
}
