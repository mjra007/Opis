package mcp.mobius.opis;

import java.util.logging.Logger;

import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import mcp.mobius.mobiuscore.profiler.ProfilerRegistrar;
import mcp.mobius.opis.client.OpisClientEventHandler;
import mcp.mobius.opis.client.OpisClientTickHandler;
import mcp.mobius.opis.commands.client.CommandClientTest;
import mcp.mobius.opis.commands.server.CommandAmountEntities;
import mcp.mobius.opis.commands.server.CommandChunkDump;
import mcp.mobius.opis.commands.server.CommandChunkList;
import mcp.mobius.opis.commands.server.CommandDataDump;
import mcp.mobius.opis.commands.server.CommandFrequency;
import mcp.mobius.opis.commands.server.CommandHandler;
import mcp.mobius.opis.commands.server.CommandHelp;
import mcp.mobius.opis.commands.server.CommandKill;
import mcp.mobius.opis.commands.server.CommandKillAll;
import mcp.mobius.opis.commands.server.CommandMeanModTime;
import mcp.mobius.opis.commands.server.CommandReset;
import mcp.mobius.opis.commands.server.CommandStart;
import mcp.mobius.opis.commands.server.CommandStop;
import mcp.mobius.opis.commands.server.CommandTPS;
import mcp.mobius.opis.commands.server.CommandTicks;
import mcp.mobius.opis.commands.server.CommandTimingEntities;
import mcp.mobius.opis.commands.server.CommandTimingTileEntities;
import mcp.mobius.opis.data.holders.CoordinatesBlock;
import mcp.mobius.opis.data.server.EntityProfiler;
import mcp.mobius.opis.data.server.TileEntityProfiler;
import mcp.mobius.opis.network.OpisConnectionHandler;
import mcp.mobius.opis.network.OpisPacketHandler;
import mcp.mobius.opis.network.Packet251Extended;
import mcp.mobius.opis.proxy.ProxyServer;
import mcp.mobius.opis.server.OpisPlayerTracker;
import mcp.mobius.opis.server.OpisServerTickHandler;
import mcp.mobius.opis.tools.ModIdentification;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid="Opis", name="Opis", version="1.2.0_alpha")
@NetworkMod(channels={"Opis", "Opis_Chunk"},clientSideRequired=false, serverSideRequired=false, connectionHandler=OpisConnectionHandler.class, packetHandler=OpisPacketHandler.class)

public class modOpis {

	@Instance("Opis")
	public static modOpis instance;	

	public static Logger log = Logger.getLogger("Opis");	

	@SidedProxy(clientSide="mcp.mobius.opis.proxy.ProxyClient", serverSide="mcp.mobius.opis.proxy.ProxyServer")
	public static ProxyServer proxy;		

	public static int profilerDelay    = 1;
	public static boolean profilerRun  = false; 
	public static int profilerMaxTicks = 250;
	public static boolean microseconds = true;
	
	public static CoordinatesBlock selectedBlock = null;
	
	public  Configuration config = null;	
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());

		profilerDelay    = config.get(Configuration.CATEGORY_GENERAL, "profiler.delay", 1).getInt();
		profilerMaxTicks = config.get(Configuration.CATEGORY_GENERAL, "profiler.maxpts", 250).getInt();
		microseconds     = config.get(Configuration.CATEGORY_GENERAL, "display.microseconds", true).getBoolean(true);		
		
		config.save();
		
		MinecraftForge.EVENT_BUS.register(new OpisClientEventHandler());
		Packet.addIdClassMapping(251, true, true, Packet251Extended.class);
	}	
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		TickRegistry.registerTickHandler(new OpisServerTickHandler(), Side.SERVER);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
        ModIdentification.init();
		proxy.init();
	}	
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		event.registerServerCommand(new CommandChunkList());
		event.registerServerCommand(new CommandFrequency());
		event.registerServerCommand(new CommandStart());
		event.registerServerCommand(new CommandStop());		
		event.registerServerCommand(new CommandTimingTileEntities());
		event.registerServerCommand(new CommandMeanModTime());
		event.registerServerCommand(new CommandDataDump());
		event.registerServerCommand(new CommandTicks());
		event.registerServerCommand(new CommandTimingEntities());
		event.registerServerCommand(new CommandAmountEntities());
		event.registerServerCommand(new CommandKill());
		event.registerServerCommand(new CommandKillAll());		
		event.registerServerCommand(new CommandReset());
		event.registerServerCommand(new CommandHandler());		
		
		event.registerServerCommand(new CommandClientTest());
		
		//event.registerServerCommand(new CommandHelp());		
		//event.registerServerCommand(new CommandTPS());		
		
		GameRegistry.registerPlayerTracker(new OpisPlayerTracker());
	}	
}
