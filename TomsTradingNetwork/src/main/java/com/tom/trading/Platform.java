package com.tom.trading;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.registries.ForgeRegistries;

import com.tom.trading.util.GameObject.GameRegistry;
import com.tom.trading.util.GameObject.GameRegistryBE;

public class Platform {
	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(ForgeRegistries.ITEMS);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(ForgeRegistries.BLOCKS);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE();
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(ForgeRegistries.MENU_TYPES);

	public static Properties itemProp() {
		return new Properties().tab(TRADING_MOD_TAB);
	}

	public static <I extends Item> I registerItem(I item) {
		return item;
	}

	public static void register() {
		ITEMS.register();
		BLOCKS.register();
		BLOCK_ENTITY.register();
		MENU_TYPE.register();
	}

	public static final CreativeModeTab TRADING_MOD_TAB = new CreativeModeTab("toms_trading_network.tab") {

		@Override
		public ItemStack makeIcon() {
			return new ItemStack(Content.VENDING_MACHINE.get());
		}
	};

	public static <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuSupplier<M> create) {
		return new MenuType<>(create);
	}

	public static TagKey<Item> getItemTag(ResourceLocation name) {
		return ItemTags.create(name);
	}
}
