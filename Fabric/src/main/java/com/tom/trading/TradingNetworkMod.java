package com.tom.trading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;

import com.tom.trading.block.AlwaysActivatableBlock;
import com.tom.trading.network.DataPacket;
import com.tom.trading.tile.OwnableBlockEntity;
import com.tom.trading.util.IDataReceiver;

public class TradingNetworkMod implements ModInitializer {
	public static final String MODID = "toms_trading_network";
	private static final Logger LOGGER = LogManager.getLogger(MODID);

	private static final String OPAC_MOD_ID = "openpartiesandclaims";
	private static final Identifier OPAC_PROTECTION_PHASE = Identifier.tryBuild(OPAC_MOD_ID, "protection");

	@Override
	public void onInitialize() {
		LOGGER.info("Tom's Trading Network starting");
		Content.init();

		Platform.DATA_COMPONENT_TYPES.runRegistration();
		Platform.BLOCKS.runRegistration();
		Platform.ITEMS.runRegistration();
		Platform.BLOCK_ENTITY.runRegistration();
		Platform.MENU_TYPE.runRegistration();

		PayloadTypeRegistry.playS2C().register(DataPacket.ID, DataPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(DataPacket.ID, DataPacket.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DataPacket.ID, (p, c) -> {
			if(c.player().containerMenu instanceof IDataReceiver d) {
				d.receive(TagValueInput.create(ProblemReporter.DISCARDING, c.player().registryAccess(), p.tag()));
			}
		});

		Identifier rl = Identifier.tryBuild(MODID, "use_block");
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
		if (FabricLoader.getInstance().isModLoaded(OPAC_MOD_ID))
			UseBlockCallback.EVENT.addPhaseOrdering(rl, OPAC_PROTECTION_PHASE);

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
			if (be instanceof OwnableBlockEntity o) {
				if (!o.canAccess(player) && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
					return false;
			}
			return true;
		});
	}
}
