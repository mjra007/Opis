package mcp.mobius.opis.commands.server;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import mcp.mobius.opis.commands.IOpisCommand;
import mcp.mobius.opis.data.managers.EntityManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.MemoryConnection;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CommandKillAll extends CommandBase implements IOpisCommand {

	@Override
	public String getCommandName() {
		return "opis_killall";
	}

	@Override
	public String getCommandNameOpis() {
		return this.getCommandName();
	}	
	
	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if (astring.length < 1) return;
		
		String searchname = StringUtils.join(astring, " ").toLowerCase();
		int nkilled = 0;
		
		if (searchname.equals("Player")){
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("\u00A7oSeriously ? I can't, seriously, I can't. I should remove you from the OP list !")));
			return;
		}
			
		
		for (int dim : DimensionManager.getIDs()){
			World world = DimensionManager.getWorld(dim);
			if (world == null) continue;
			
			ArrayList copyList = new ArrayList(world.loadedEntityList);
			
			for (Object o : copyList){
				Entity ent  = (Entity)o;
				String name = EntityManager.getEntityName(ent).toLowerCase(); 
				
				if (name.equals(searchname)){
					ent.setDead();
					nkilled += 1;
				}
			}
		}

		icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("\u00A7oKilled %d entities of type %s", nkilled, searchname)));		
		
		/*
		World world = DimensionManager.getWorld(dim);
		if (world == null){
			PacketDispatcher.sendPacketToPlayer(			
			new Packet3Chat(ChatMessageComponent.createFromText(String.format("\u00A7oCannot find dim %d in world %d", dim))), 
			               (Player)icommandsender);
			return;
		}
		
		Entity entity = world.getEntityByID(eid);
		if (entity == null) {
			PacketDispatcher.sendPacketToPlayer(			
			new Packet3Chat(ChatMessageComponent.createFromText(String.format("\u00A7oCannot find entity %d in dim %d", eid, dim))), 
			               (Player)icommandsender);
			return;
		}
		
		entity.setDead();
		PacketDispatcher.sendPacketToPlayer(			
		new Packet3Chat(ChatMessageComponent.createFromText(String.format("\u00A7oKilled entity %d in dim %d", eid, dim))), 
		               (Player)icommandsender);
		return;
		*/		
	}

	@Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }	

	@Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
		if (sender instanceof DedicatedServer) return true;
		if ((sender instanceof EntityPlayerMP) && ((EntityPlayerMP)sender).playerNetServerHandler.netManager instanceof MemoryConnection) return true;
		if (!(sender instanceof DedicatedServer) && !(sender instanceof EntityPlayerMP)) return true;
        return MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(((EntityPlayerMP)sender).username);
    }

	@Override
	public String getDescription() {
		return "Kills all entities of the given name.";
	}

}