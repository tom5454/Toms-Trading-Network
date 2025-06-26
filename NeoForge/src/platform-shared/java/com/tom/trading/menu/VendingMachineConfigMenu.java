package com.tom.trading.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.tom.trading.Content;
import com.tom.trading.menu.slot.PhantomSlot;
import com.tom.trading.network.NetworkHandler;
import com.tom.trading.screen.widget.IOMode;
import com.tom.trading.tile.VendingMachineBlockEntityBase;
import com.tom.trading.util.BlockFaceDirection;
import com.tom.trading.util.DataSlots;
import com.tom.trading.util.IDataReceiver;

public class VendingMachineConfigMenu extends AbstractFilteredMenu implements IDataReceiver {
	private VendingMachineBlockEntityBase machine;
	protected final Inventory pinv;

	public int inputCfg, outputCfg, autoCfg, matchNBT, creativeMode;
	public Runnable updateGui;

	public VendingMachineConfigMenu(int pContainerId, Inventory pPlayerInventory) {
		this(pContainerId, pPlayerInventory, new SimpleContainer(9), new SimpleContainer(9), new SimpleContainer(8));

		addDataSlot(DataSlots.set(c -> inputCfg = c).onUpdate(this::updateGui));
		addDataSlot(DataSlots.set(c -> outputCfg = c).onUpdate(this::updateGui));
		addDataSlot(DataSlots.set(c -> matchNBT = c));
		addDataSlot(DataSlots.set(c -> autoCfg = c).onUpdate(this::updateGui));
		addDataSlot(DataSlots.set(c -> creativeMode = c).onUpdate(this::updateGui));
	}

	public VendingMachineConfigMenu(int pContainerId, Inventory pPlayerInventory, VendingMachineBlockEntityBase machine) {
		this(pContainerId, pPlayerInventory, machine.getInputs(), machine.getOutputs(), machine.getConfig());
		this.machine = machine;

		addDataSlot(DataSlots.get(machine::getInputSides));
		addDataSlot(DataSlots.get(machine::getOutputSides));
		addDataSlot(DataSlots.get(machine::getMatchNBT));
		addDataSlot(DataSlots.get(machine::getAutoSides));
		addDataSlot(DataSlots.get(() -> machine.isCreativeMode() ? 1 : 0));
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
		if((matchNBT & (1 << slot)) != 0)return ItemStack.isSameItemSameComponents(pStack, pOther);
		else return ItemStack.isSameItem(pStack, pOther);
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
		return machine != null ? machine.isInRange(pPlayer) && machine.canAccess(pPlayer) : true;
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
		} else if((pId & 0b1110_0000) == 0b0010_0000) {
			if (pPlayer.getAbilities().instabuild) {
				machine.setCreativeMode((pId & 1) != 0);
			}
		} else if((pId & 0b0110_0000) == 0b0110_0000) {
			int slot = pId & 0xf;
			boolean c = (pId & 0b0001_0000) != 0;
			machine.setMatchNBT(slot, c);
			return true;
		}
		return false;
	}

	@Override
	public void receive(ValueInput tag) {
		if(pinv.player.isSpectator())return;
		tag.child("setItemCount").ifPresent(t -> {
			int slotId = t.getIntOr("id", 0);
			byte count = t.getByteOr("count", (byte) 0);
			Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
			if (slot instanceof PhantomSlot) {
				ItemStack s = slot.getItem().copy();
				s.setCount(Mth.clamp(count, 1, s.getMaxStackSize()));
				slot.set(s);
			}
		});
		tag.getString("setName").ifPresent(s -> {
			machine.setCustomName(Component.literal(s));
		});
		tag.read("setSide", SidesObject.CODEC).ifPresent(e -> {
			machine.setSides(e.dir().ordinal(), e.newState().ordinal(), e.auto());
		});
		super.receive(tag);
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

	public void setSides(BlockFaceDirection dir, IOMode newState, boolean auto) {
		TagValueOutput tag = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
		tag.store("setSide", SidesObject.CODEC, new SidesObject(dir, newState, auto));
		NetworkHandler.sendDataToServer(tag.buildResult());
	}

	private void updateGui() {
		if(updateGui != null)updateGui.run();
	}

	public static record SidesObject(BlockFaceDirection dir, IOMode newState, boolean auto) {

		public static final MapCodec<SidesObject> MAP_CODEC = RecordCodecBuilder.mapCodec(
				b -> b.group(
						BlockFaceDirection.CODEC.fieldOf("dir").forGetter(SidesObject::dir),
						IOMode.CODEC.fieldOf("mode").forGetter(SidesObject::newState),
						Codec.BOOL.fieldOf("auto").forGetter(SidesObject::auto)
						)
				.apply(b, SidesObject::new)
				);

		public static final Codec<SidesObject> CODEC = MAP_CODEC.codec();
	}
}
