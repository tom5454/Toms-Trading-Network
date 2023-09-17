package com.tom.trading;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.tom.trading.util.GameObject.GameRegistry;
import com.tom.trading.util.GameObject.GameRegistryBE;

public class Platform {
	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(BuiltInRegistries.ITEM);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(BuiltInRegistries.BLOCK);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE(BuiltInRegistries.BLOCK_ENTITY_TYPE);
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(BuiltInRegistries.MENU);

	private static List<Item> tabItems = new ArrayList<>();

	public static <I extends Item> I addItemToTab(I item) {
		tabItems.add(item);
		return item;
	}

	public static final CreativeModeTab TRADING_MOD_TAB = FabricItemGroup.builder().title(Component.translatable("itemGroup.toms_trading_network.tab")).icon(() -> new ItemStack(Content.VENDING_MACHINE.get())).displayItems((p, out) -> {
		tabItems.forEach(out::accept);
	}).build();

	public static CompoundTag readNbtTag(FriendlyByteBuf buf) {
		return (CompoundTag) buf.readNbt(NbtAccounter.unlimitedHeap());
	}
}
