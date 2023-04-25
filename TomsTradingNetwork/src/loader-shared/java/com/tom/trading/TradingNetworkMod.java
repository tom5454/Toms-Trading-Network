package com.tom.trading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.tom.trading.block.AlwaysActivatableBlock;
import com.tom.trading.network.NetworkHandler;
import com.tom.trading.top.TheOneProbeHandler;

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
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> TradingNetworkModClient::preInit);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

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
		if(ModList.get().isLoaded("theoneprobe"))
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> TheOneProbeHandler.create());
	}
}
