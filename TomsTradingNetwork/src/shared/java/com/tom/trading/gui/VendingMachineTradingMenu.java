package com.tom.trading.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.trading.Content;
import com.tom.trading.tile.VendingMachineBlockEntityBase;
import com.tom.trading.util.DataSlots;

public class VendingMachineTradingMenu extends AbstractContainerMenu {
	private VendingMachineBlockEntityBase machine;
	public int state, matchNBT;
	public Runnable updateGui;

	public VendingMachineTradingMenu(int pContainerId, Inventory pPlayerInventory) {
		this(pContainerId, pPlayerInventory, new SimpleContainer(8));

		addDataSlot(DataSlots.set(c -> state = c).onUpdate(this::updateGui));
		addDataSlot(DataSlots.set(c -> matchNBT = c));
	}

	public VendingMachineTradingMenu(int pContainerId, Inventory pPlayerInventory, VendingMachineBlockEntityBase machine) {
		this(pContainerId, pPlayerInventory, machine.getConfig());
		this.machine = machine;

		addDataSlot(DataSlots.get(machine::getTradingState));
		addDataSlot(DataSlots.get(machine::getMatchNBT));
	}

	private VendingMachineTradingMenu(int pContainerId, Inventory pPlayerInventory, Container config) {
		super(Content.VENDING_MACHINE_TRADING_MENU.get(), pContainerId);

		for(int k = 0; k < 8; ++k) {
			this.addSlot(new Slot(config, k, 8 + k * 18 + (k > 3 ? 18 : 0), 35) {

				@Override
				public boolean mayPickup(Player pPlayer) {
					return false;
				}

				@Override
				public boolean mayPlace(ItemStack pStack) {
					return false;
				}
			});
		}

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for(int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
		}
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return machine != null ? machine.isInRange(pPlayer) : true;
	}

	private void updateGui() {
		if(updateGui != null)updateGui.run();
	}

	@Override
	public boolean clickMenuButton(Player pPlayer, int pId) {
		for(int i = 0;i<pId;i++) {
			int r = machine.tradeWith(pPlayer.getInventory());
			if(r != 0) {
				pPlayer.sendSystemMessage(Component.translatable("chat.toms_trading_network.vending_machine.trade_error", i, pId, Component.translatable("chat.toms_trading_network.vending_machine.trade_error." + r)));
				break;
			}
		}
		return true;
	}
}
