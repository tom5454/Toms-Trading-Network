package com.tom.trading.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

import com.tom.trading.TradingNetworkMod;

public class GameObject<T> {
	private final ResourceLocation id;
	private final T value;

	private GameObject(ResourceLocation id, T value) {
		this.id = id;
		this.value = value;
	}

	/*public static <V, T extends V> GameObject<T> register(Registry<V> registry, ResourceLocation resourceLocation, T value) {
		Registry.register(registry, resourceLocation, value);
		return new GameObject<>(value);
	}*/

	public T get() {
		return value;
	}

	public ResourceLocation getId() {
		return id;
	}

	public static class GameRegistry<T> {
		protected final Registry<T> registry;

		public GameRegistry(Registry<T> registry) {
			this.registry = registry;
		}

		public <I extends T> GameObject<I> register(final String name, final Supplier<? extends I> sup) {
			I obj = sup.get();
			ResourceLocation id = new ResourceLocation(TradingNetworkMod.MODID, name);
			Registry.register(registry, id, obj);
			return new GameObject<>(id, obj);
		}
	}

	public static class GameRegistryBE extends GameRegistry<BlockEntityType<?>> {
		private List<GameObjectBlockEntity<?>> blockEntities = new ArrayList<>();

		public GameRegistryBE(Registry<BlockEntityType<?>> registry) {
			super(registry);
		}

		@SuppressWarnings("unchecked")
		public <BE extends BlockEntity, I extends BlockEntityType<BE>> GameObjectBlockEntity<BE> registerBE(String name, BlockEntitySupplier<BE> sup, GameObject<? extends Block>... blocks) {
			GameObjectBlockEntity<BE> e = new GameObjectBlockEntity<>(this, name, new ArrayList<>(Arrays.asList(blocks)), sup);
			blockEntities.add(e);
			return e;
		}

		public void register() {
			blockEntities.forEach(GameObjectBlockEntity::register);
		}
	}

	public static class GameObjectBlockEntity<T extends BlockEntity> extends GameObject<BlockEntityType<T>> {
		private BlockEntityType<T> value;
		private List<GameObject<? extends Block>> blocks;
		private BlockEntitySupplier<T> factory;
		private GameRegistryBE registry;

		public GameObjectBlockEntity(GameRegistryBE registry, String name, List<GameObject<? extends Block>> blocks, BlockEntitySupplier<T> factory) {
			super(new ResourceLocation(TradingNetworkMod.MODID, name), null);
			this.blocks = blocks;
			this.factory = factory;
			this.registry = registry;
		}

		protected void register() {
			value = BlockEntityType.Builder.<T>of(factory, blocks.stream().map(GameObject::get).toArray(Block[]::new)).build(null);
			Registry.register(registry.registry, getId(), value);
		}

		@Override
		public BlockEntityType<T> get() {
			return value;
		}

		@SuppressWarnings("unchecked")
		public void addBlocks(GameObject<? extends Block>... blocks) {
			this.blocks.addAll(Arrays.asList(blocks));
		}
	}
}
