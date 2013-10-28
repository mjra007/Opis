package mcp.mobius.opis.overlay;

import java.awt.Point;
import java.util.ArrayList;

import net.minecraft.util.MathHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import mapwriter.api.IMwChunkOverlay;
import mapwriter.api.IMwDataProvider;
import mapwriter.map.MapView;
import mcp.mobius.opis.data.ChunksData;
import mcp.mobius.opis.data.CoordinatesChunk;
import mcp.mobius.opis.network.Packet_ReqMeanTimeInDim;
import mcp.mobius.opis.network.Packet_UnregisterPlayer;

public class OverlayMeanTime implements IMwDataProvider {

	public class ChunkOverlay implements IMwChunkOverlay{

		Point coord;
		int nentities;
		double time;
		double minTime;
		double maxTime;
		boolean selected;
		
		public ChunkOverlay(int x, int z, int nentities, double time, double mintime, double maxtime, boolean selected){
			this.coord     = new Point(x, z);
			this.nentities = nentities;
			this.time      = time;
			this.minTime   = mintime;
			this.maxTime   = maxtime;
			this.selected  = selected;
		}
		
		@Override
		public Point getCoordinates() {	return this.coord; }

		@Override
		public int getColor() {
			//System.out.printf("%s\n", this.maxTime);
			double scaledTime = this.time / this.maxTime;
			int    red        = MathHelper.ceiling_double_int(scaledTime * 255.0);
			int    blue       = 255 - MathHelper.ceiling_double_int(scaledTime * 255.0);
			//System.out.printf("%s\n", red);
			
			return (200 << 24) + (red << 16) + (blue); 
		}
		
		@Override
		public float getFilling() {	return 1.0f; }

		@Override
		public boolean hasBorder() { return true; }

		@Override
		public float getBorderWidth() { return 0.5f; }

		@Override
		public int getBorderColor() { return this.selected ? 0xffffffff : 0xff000000; }
		
	}		
	
	CoordinatesChunk selectedChunk = null;
	
	@Override
	public ArrayList<IMwChunkOverlay> getChunksOverlay(int dim, double centerX,	double centerZ, double minX, double minZ, double maxX, double maxZ) {
		ArrayList<IMwChunkOverlay> overlays = new ArrayList<IMwChunkOverlay>();
		
		double minTime = 9999;
		double maxTime = 0;

		for (CoordinatesChunk chunk : ChunksData.chunkMeanTime.keySet()){
			minTime = Math.min(minTime, ChunksData.chunkMeanTime.get(chunk).updateTime);
			maxTime = Math.max(maxTime, ChunksData.chunkMeanTime.get(chunk).updateTime);
		}
		
		for (CoordinatesChunk chunk : ChunksData.chunkMeanTime.keySet()){
			if (this.selectedChunk != null)
				overlays.add(new ChunkOverlay(chunk.chunkX, chunk.chunkZ, ChunksData.chunkMeanTime.get(chunk).nentities, ChunksData.chunkMeanTime.get(chunk).updateTime, minTime, maxTime, chunk.equals(this.selectedChunk)));
			else
				overlays.add(new ChunkOverlay(chunk.chunkX, chunk.chunkZ, ChunksData.chunkMeanTime.get(chunk).nentities, ChunksData.chunkMeanTime.get(chunk).updateTime, minTime, maxTime, false));
		}
		return overlays;
	}

	@Override
	public String getStatusString(int dim, int bX, int bY, int bZ) {
		int xChunk = bX >> 4;
		int zChunk = bZ >> 4;
		CoordinatesChunk chunkCoord = new CoordinatesChunk(dim, xChunk, zChunk);
		
		if (ChunksData.chunkMeanTime.containsKey(chunkCoord))
			return String.format(", %.5f ms", ChunksData.chunkMeanTime.get(chunkCoord).updateTime/1000.0);
		else
			return "";
	}

	@Override
	public void onMiddleClick(int dim, int bX, int bZ, MapView mapview) {
		int xChunk = bX >> 4;
		int zChunk = bZ >> 4;		
		CoordinatesChunk clickedChunk = new CoordinatesChunk(dim, xChunk, zChunk); 
		
		if (ChunksData.chunkMeanTime.containsKey(clickedChunk)){
			if (this.selectedChunk == null)
				this.selectedChunk = clickedChunk;
			else if (this.selectedChunk.equals(clickedChunk))
				this.selectedChunk = null;
			else
				this.selectedChunk = clickedChunk;
		} else {
			this.selectedChunk = null;
		}
	}

	@Override
	public void onDimensionChanged(int dimension, MapView mapview) {
		PacketDispatcher.sendPacketToServer(Packet_ReqMeanTimeInDim.create(dimension));		
	}

	@Override
	public void onMapCenterChanged(double vX, double vZ, MapView mapview) {
	}

	@Override
	public void onZoomChanged(int level, MapView mapview) {
	}

	@Override
	public void onOverlayActivated(MapView mapview) {
		PacketDispatcher.sendPacketToServer(Packet_ReqMeanTimeInDim.create(mapview.getDimension()));			
	}

	@Override
	public void onOverlayDeactivated(MapView mapview) {
		PacketDispatcher.sendPacketToServer(Packet_UnregisterPlayer.create());		
	}

}