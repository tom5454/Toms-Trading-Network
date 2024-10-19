package com.tom.trading;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

import com.tom.trading.block.VendingMachineBlock;
import com.tom.trading.item.TagFilterItem;
import com.tom.trading.menu.VendingMachineConfigMenu;
import com.tom.trading.menu.VendingMachineTradingMenu;
import com.tom.trading.tile.VendingMachineBlockEntity;
import com.tom.trading.tile.VendingMachineBlockEntityBase;
import com.tom.trading.util.GameObject;
import com.tom.trading.util.GameObject.GameObjectBlockEntity;

public class Content {
	public static final GameObject<VendingMachineBlock> VENDING_MACHINE = blockWithItem("vending_machine", VendingMachineBlock::new);
	public static final GameObject<TagFilterItem> TAG_FILTER = Platform.ITEMS.register("tag_filter", TagFilterItem::new);

	public static final GameObjectBlockEntity<VendingMachineBlockEntityBase> VENDING_MACHINE_TILE = blockEntity("vending_machine.tile", VendingMachineBlockEntity::new, VENDING_MACHINE);

	public static final GameObject<MenuType<VendingMachineTradingMenu>> VENDING_MACHINE_TRADING_MENU = menu("vending_machine.menu_trading", VendingMachineTradingMenu::new);
	public static final GameObject<MenuType<VendingMachineConfigMenu>> VENDING_MACHINE_CONFIG_MENU = menu("vending_machine.menu_config", VendingMachineConfigMenu::new);

	public static final GameObject<DataComponentType<TagKey<Item>>> TAG_COMPONENT = Platform.DATA_COMPONENT_TYPES.register("tag", () -> DataComponentType.<TagKey<Item>>builder().persistent(TagKey.codec(Registries.ITEM)).build());

	private static <B extends Block> GameObject<B> blockWithItem(String name, Supplier<B> create) {
		return blockWithItem(name, create, b -> new BlockItem(b, new Item.Properties()));
	}

	private static <B extends Block, I extends Item> GameObject<B> blockWithItem(String name, Supplier<B> create, Function<Block, I> createItem) {
		GameObject<B> re = Platform.BLOCKS.register(name, create);
		item(name, () -> createItem.apply(re.get()));
		return re;
	}

	private static <I extends Item> GameObject<I> item(String name, Supplier<I> fact) {
		return Platform.ITEMS.register(name, () -> Platform.addItemToTab(fact.get()));
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	private static <BE extends BlockEntity> GameObjectBlockEntity<BE> blockEntity(String name, BlockEntitySupplier<? extends BE> create, GameObject<? extends Block>... blocks) {
		return (GameObjectBlockEntity<BE>) Platform.BLOCK_ENTITY.registerBE(name, create, blocks);
	}

	private static <M extends AbstractContainerMenu> GameObject<MenuType<M>> menu(String name, MenuSupplier<M> create) {
		return Platform.MENU_TYPE.register(name, () -> new MenuType<>(create, FeatureFlags.VANILLA_SET));
	}

	public static void init() {
	}
}
