package com.tom.trading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;

import com.tom.trading.block.AlwaysActivatableBlock;
import com.tom.trading.network.NetworkHandler;
import com.tom.trading.tile.OwnableBlockEntity;
import com.tom.trading.tile.VendingMachineBlockEntity;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TradingNetworkMod.MODID)
public class TradingNetworkMod {
	public static final String MODID = "toms_trading_network";
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public TradingNetworkMod(IEventBus bus) {
		// Register the setup method for modloading
		bus.addListener(this::setup);
		// Register the enqueueIMC method for modloading
		bus.addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		if (FMLEnvironment.getDist() == Dist.CLIENT)TradingNetworkModClient.preInit(bus);
		// Register the doClientStuff method for modloading
		bus.addListener(this::doClientStuff);
		bus.addListener(this::registerCapabilities);
		bus.register(NetworkHandler.class);

		// Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.register(this);

		Content.init();

		Platform.register(bus);
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Tom's Trading Network Setup starting");
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

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBreak(BreakEvent event) {
		BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
		if (be instanceof OwnableBlockEntity o) {
			if (!o.canAccess(event.getPlayer()) && !event.getPlayer().hasPermissions(2))
				event.setCanceled(true);
		}
	}

	public void enqueueIMC(InterModEnqueueEvent e) {
		if(ModList.get().isLoaded("theoneprobe"))
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> {
				try {
					return Class.forName("com.tom.trading.top.TheOneProbeHandler").getDeclaredMethod("create").invoke(null);
				} catch (Exception e1) {
					e1.printStackTrace();
					return null;
				}
			});
	}

	private void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, Content.VENDING_MACHINE_TILE.get(), (be, side) -> ((VendingMachineBlockEntity) be).getInventory(side));
	}
}
