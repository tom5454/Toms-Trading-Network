package com.tom.trading;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import com.tom.trading.util.GameObject.GameRegistry;
import com.tom.trading.util.GameObject.GameRegistryBE;

public class Platform {
	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(ForgeRegistries.ITEMS);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(ForgeRegistries.BLOCKS);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE();
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(ForgeRegistries.MENU_TYPES);

	public static CreativeModeTab STORAGE_MOD_TAB;
	private static List<Item> tabItems = new ArrayList<>();

	public static Properties itemProp() {
		return new Properties();
	}

	public static <I extends Item> I registerItem(I item) {
		tabItems.add(item);
		return item;
	}

	public static void register() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(Platform::makeTab);
		ITEMS.register();
		BLOCKS.register();
		BLOCK_ENTITY.register();
		MENU_TYPE.register();
	}

	private static void makeTab(CreativeModeTabEvent.Register evt) {
		STORAGE_MOD_TAB = evt.registerCreativeModeTab(new ResourceLocation(TradingNetworkMod.MODID, "tab"), b -> {
			b.icon(() -> new ItemStack(Content.VENDING_MACHINE.get()));
			b.displayItems((param, out) -> {
				tabItems.forEach(out::accept);
			});
		});
	}

	public static <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuSupplier<M> create) {
		return new MenuType<>(create, FeatureFlags.VANILLA_SET);
	}

	public static TagKey<Item> getItemTag(ResourceLocation name) {
		return ItemTags.create(name);
	}
}
