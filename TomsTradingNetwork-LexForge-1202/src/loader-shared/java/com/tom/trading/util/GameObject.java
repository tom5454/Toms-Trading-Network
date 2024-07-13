package com.tom.trading.util;

import java.util.Arrays;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import com.tom.trading.TradingNetworkMod;

public class GameObject<T> {
	private final RegistryObject<T> value;

	protected GameObject(RegistryObject<T> value) {
		this.value = value;
	}

	public T get() {
		return value.get();
	}

	public static class GameRegistry<T> {
		protected final DeferredRegister<T> handle;

		public GameRegistry(IForgeRegistry<T> reg) {
			handle = DeferredRegister.create(reg, TradingNetworkMod.MODID);
		}

		public <I extends T> GameObject<I> register(final String name, final Supplier<? extends I> sup) {
			return new GameObject<>(handle.register(name, sup));
		}

		public void register() {
			handle.register(FMLJavaModLoadingContext.get().getModEventBus());
		}
	}

	public ResourceLocation getId() {
		return value.getId();
	}

	public static class GameRegistryBE extends GameRegistry<BlockEntityType<?>> {

		public GameRegistryBE() {
			super(ForgeRegistries.BLOCK_ENTITY_TYPES);
		}

		@SuppressWarnings("unchecked")
		public <BE extends BlockEntity, I extends BlockEntityType<BE>> GameObjectBlockEntity<BE> registerBE(String name, BlockEntitySupplier<BE> sup, GameObject<? extends Block>... blocks) {
			return new GameObjectBlockEntity<>(handle.register(name, () -> {
				return BlockEntityType.Builder.<BE>of(sup, Arrays.stream(blocks).map(GameObject::get).toArray(Block[]::new)).build(null);
			}));
		}
	}

	public static class GameObjectBlockEntity<T extends BlockEntity> extends GameObject<BlockEntityType<T>> {

		protected GameObjectBlockEntity(RegistryObject<BlockEntityType<T>> value) {
			super(value);
		}

	}
}
