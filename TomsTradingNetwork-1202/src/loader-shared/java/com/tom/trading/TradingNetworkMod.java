package com.tom.trading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

import com.tom.trading.block.AlwaysActivatableBlock;
import com.tom.trading.network.NetworkHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TradingNetworkMod.MODID)
public class TradingNetworkMod {
	public static final String MODID = "toms_trading_network";
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public TradingNetworkMod() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		if (FMLEnvironment.dist == Dist.CLIENT)TradingNetworkModClient.preInit();
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		// Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.register(this);

		Content.init();

		Platform.register();
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Tom's Trading Network Setup starting");
		NetworkHandler.init();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		TradingNetworkModClient.clientSetup();
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onClick(RightClickBlock event) {
		BlockState state = event.getLevel().getBlockState(event.getPos());
		if(!event.getEntity().isSecondaryUseActive() && state.getBlock() instanceof AlwaysActivatableBlock aab) {
			if(aab.onActivate(state, event.getLevel(), event.getPos(), event.getEntity(), event.getHand(), event.getHitVec())) {
				event.setCanceled(true);
			}
		}
	}

	public void enqueueIMC(InterModEnqueueEvent e) {
		/*if(ModList.get().isLoaded("theoneprobe"))
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> TheOneProbeHandler.create());*/
	}
}
