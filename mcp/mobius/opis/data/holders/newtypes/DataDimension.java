package mcp.mobius.opis.data.holders.newtypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import mcp.mobius.mobiuscore.profiler.ProfilerSection;
import mcp.mobius.opis.data.holders.ISerializable;
import mcp.mobius.opis.data.profilers.ProfilerDimTick;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;


/* Data holder for infos about dimensions */

public class DataDimension implements ISerializable {

	public int    dim;
	public String name;
	public int    players;
	public int    forced;
	public int    loaded;
	public int    entities;
	public int    mobs;
	public int    neutral;
	public DataTiming update;
	
	public DataDimension fill(int dim){
		WorldServer  world = DimensionManager.getWorld(dim);
		
		this.dim      = dim;
		this.name     = world.provider.getDimensionName();
		this.players  = world.playerEntities.size();
		this.forced   = world.getPersistentChunks().size();
		this.loaded   = world.getChunkProvider().getLoadedChunkCount();
		
		HashMap<Integer, DescriptiveStatistics> data = ((ProfilerDimTick)(ProfilerSection.DIMENSION_TICK.getProfiler())).data;
		this.update  = new DataTiming(data.containsKey(dim) ? data.get(dim).getGeometricMean() : 0.0D);		
		
		this.mobs     = 0;
		this.neutral  = 0;
		this.entities = world.loadedEntityList.size();		
		
		for (Entity entity : (ArrayList<Entity>)world.loadedEntityList){
			if (entity instanceof EntityMob)
				this.mobs += 1;
			if (entity instanceof EntityAnimal)
				this.neutral += 1;
		}
		
		return this;
	}
	
	@Override
	public void writeToStream(DataOutputStream stream) throws IOException {
		stream.writeInt(dim);
		stream.writeInt(players);
		stream.writeInt(forced);
		stream.writeInt(loaded);
		stream.writeInt(entities);
		stream.writeInt(mobs);
		stream.writeInt(neutral);
		//stream.writeDouble(update);
		this.update.writeToStream(stream);
		Packet.writeString(name, stream);
	}

	public static DataDimension readFromStream(DataInputStream stream) throws IOException {
		DataDimension retVal = new DataDimension();
		retVal.dim     = stream.readInt();
		retVal.players = stream.readInt();
		retVal.forced  = stream.readInt();
		retVal.loaded  = stream.readInt();
		retVal.entities= stream.readInt();
		retVal.mobs    = stream.readInt();
		retVal.neutral = stream.readInt();
		//retVal.update  = stream.readDouble();
		retVal.update  = DataTiming.readFromStream(stream);
		retVal.name    = Packet.readString(stream, 255);
		return retVal;
	}
}
