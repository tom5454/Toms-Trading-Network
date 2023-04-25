package com.tom.trading;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.tom.trading.util.GameObject.GameRegistry;
import com.tom.trading.util.GameObject.GameRegistryBE;

public class Platform {
	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(Registry.ITEM);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(Registry.BLOCK);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE(Registry.BLOCK_ENTITY_TYPE);
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(Registry.MENU);

	public static Properties itemProp() {
		return new Properties().tab(TRADING_MOD_TAB);
	}

	public static <I extends Item> I registerItem(I item) {
		return item;
	}

	public static final CreativeModeTab TRADING_MOD_TAB = FabricItemGroupBuilder.build(new ResourceLocation(TradingNetworkMod.MODID, "tab"), () -> new ItemStack(Content.VENDING_MACHINE.get()));

	public static <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuSupplier<M> create) {
		return new MenuType<>(create);
	}
}
