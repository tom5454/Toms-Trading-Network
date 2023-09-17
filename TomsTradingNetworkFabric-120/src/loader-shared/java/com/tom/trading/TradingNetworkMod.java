package com.tom.trading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.trading.block.AlwaysActivatableBlock;
import com.tom.trading.network.NetworkHandler;
import com.tom.trading.util.IDataReceiver;

public class TradingNetworkMod implements ModInitializer {
	public static final String MODID = "toms_trading_network";
	private static final Logger LOGGER = LogManager.getLogger(MODID);

	@Override
	public void onInitialize() {
		LOGGER.info("Tom's Trading Network starting");
		Content.init();

		Platform.BLOCK_ENTITY.register();

		ServerPlayNetworking.registerGlobalReceiver(NetworkHandler.DATA_C2S, (s, p, h, buf, rp) -> {
			CompoundTag tag = Platform.readNbtTag(buf);
			s.submit(() -> {
				if(p.containerMenu instanceof IDataReceiver) {
					((IDataReceiver)p.containerMenu).receive(tag);
				}
			});
		});

		ResourceLocation rl = new ResourceLocation(MODID, "use_block");
		UseBlockCallback.EVENT.register(rl, (player, world, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();
			BlockState state = world.getBlockState(pos);
			if(!player.isSecondaryUseActive() && state.getBlock() instanceof AlwaysActivatableBlock aab) {
				if(aab.onActivate(state, world, pos, player, hand, hitResult)) {
					return InteractionResult.SUCCESS;
				}
			}
			return InteractionResult.PASS;
		});
		UseBlockCallback.EVENT.addPhaseOrdering(rl, Event.DEFAULT_PHASE);
	}
}
