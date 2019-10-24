package com.the_beast_unleashed.chunkpurge.operators;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.the_beast_unleashed.chunkpurge.ModChunkPurge;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.logging.log4j.Level;

/*
 * A class to handle the unloading of excess chunks from a WorldServer.
 * Excess loaded chunks are those that are currently not within a player's view distance,
 * forced by a chunk loader, or loaded by the world's spawn area.
 */
public class WorldChunkUnloader
{
	
	private World world;
	private HashSet<ChunkPos> chunksToUnload;
	private long initialTime;
	
	public WorldChunkUnloader (World world)
	{
		
		this.world = world;
		
	}
	
	
	/*
	 * A flood fill algorithm to find the shape of the loaded chunks surrounding a player-occupied chunk, or seed.
	 * Will not return chunks that are further than radiusLimit from the seed. Set radiusLimit to 0 in order to
	 * ignore any limit.
	 * 
	 * 1. Set Q to the empty queue.
	 * 2. If the color of node is not equal to target-color, return.
	 * 3. Add node to Q.
	 * 4. For each element N of Q:
	 * 5.     If the color of N is equal to target-color:
	 * 6.         Set w and e equal to N.
	 * 7.         Move w to the west until the color of the node to the west of w no longer matches target-color.
	 * 8.         Move e to the east until the color of the node to the east of e no longer matches target-color.
	 * 9.         For each node n between w and e:
	 * 10.             Set the color of n to replacement-color.
	 * 11.             If the color of the node to the north of n is target-color, add that node to Q.
	 * 12.             If the color of the node to the south of n is target-color, add that node to Q.
	 * 13. Continue looping until Q is exhausted.
	 * 14. Return
	 */
	private HashSet<ChunkPos> groupedChunksFinder(HashSet<ChunkPos> loadedChunks, ChunkPos seed, int radiusLimit)
	{
		
		LinkedList<ChunkPos> queue = new LinkedList<ChunkPos>();
		HashSet<ChunkPos> groupedChunks = new HashSet<ChunkPos>();
		
		if (!loadedChunks.contains(seed)) return groupedChunks;
		queue.add(seed);
		
		while (!queue.isEmpty())
		{
			
			ChunkPos chunk = queue.remove();
			
			if (!groupedChunks.contains(chunk))
			{
				int west, east;
				
				for (west = chunk.x;
						loadedChunks.contains(new ChunkPos(west-1, chunk.z))
								&& (radiusLimit == 0 || Math.abs(west-1 - seed.x) <= radiusLimit);
						--west);
				
				for (east = chunk.x;
						loadedChunks.contains(new ChunkPos(east+1, chunk.z))
								&& (radiusLimit == 0 || Math.abs(east+1 - seed.x) <= radiusLimit);
						++east);
				
				for (int x = west; x <= east; ++x)
				{
					
					groupedChunks.add(new ChunkPos(x, chunk.z));
					
					if (loadedChunks.contains(new ChunkPos(x, chunk.z+1))
							&& (radiusLimit == 0 || Math.abs(chunk.z+1 - seed.z) <= radiusLimit))
					{
						
						queue.add(new ChunkPos (x, chunk.z+1));
						
					}
					
					if (loadedChunks.contains(new ChunkPos(x, chunk.z-1))
							&& (radiusLimit == 0 || Math.abs(chunk.z-1 - seed.z) <= radiusLimit))
					{
						
						queue.add(new ChunkPos (x, chunk.z-1));
						
					}
					
				}
				
			}
			
		}
		
		return groupedChunks;
	}
	
	/*
	 * Populate chunksToUnload with chunks that are isolated from all players, chunk loaders, and the spawn.
	 * 
	 * Use a flood-fill algorithm to find the set of all loaded chunks in the world which link back
	 * to a chunk watcher through other loaded chunks. The idea is to find the isolated chunks
	 * which do NOT link back to a valid chunk watcher, and unload those. 
	 * 
	 * This is a better alternative to simply unloading all chunks outside of a player's view radius.
	 * Unloading chunks while not unloading their neighbors would result in tps-spikes due to the breaking
	 * of energy nets and the like. This approach should reduce the severity of those tps-spikes.
	 */
	private void populateChunksToUnload()
	{
		
		chunksToUnload = new HashSet<>();
		
		if (world.getChunkProvider() instanceof ChunkProviderServer)
		{
			// The set of chunks that are currently loaded in this world by all mechanisms.
			HashSet<ChunkPos> loadedChunks = new HashSet<>();
			
			// The set of chunks that are loaded as a result of players.
			HashSet<ChunkPos> playerLoadedChunks = new HashSet<>();
			// The set of chunks that are loaded due to chunk loading tickets.
			HashSet<ChunkPos> forceLoadedChunks = new HashSet<>();
			// The set of chunks that are loaded as a result of the world spawn area.
			HashSet<ChunkPos> spawnLoadedChunks = new HashSet<>();
			
			List<EntityPlayer> listPlayers = world.playerEntities;
			
			int radiusLimit;
			
			final int CHUNK_WIDTH = 16;
			
			// The expected radius of loaded chunks around a player
			final int PLAYER_RADIUS = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getViewDistance();
			// The expected radius of loaded chunks around a chunk-loading ticket
			final int TICKET_RADIUS = 0;
			// The expected radius of loaded chunks around the spawn chunk.
			final int SPAWN_RADIUS = 8;
			
			// Add these values to our above expectations to prevent the flood filling algorithm from returning
			// chunks outside of the resulting radius.
			// Each is separate so you can configure them independently
			int PLAYER_LIMIT = ModChunkPurge.config.pradius;
			int TICKET_LIMIT = ModChunkPurge.config.tradius;
			int SPAWN_LIMIT = ModChunkPurge.config.sradius;
			
			
			// Want to deal with chunk coordinates, not chunk objects.
			for (Chunk chunk : ((ChunkProviderServer) world.getChunkProvider()).getLoadedChunks())
			{
				
				loadedChunks.add(chunk.getPos());
				
			}
			
			radiusLimit = (int) Math.ceil(PLAYER_RADIUS + PLAYER_LIMIT);
			
			for (EntityPlayer player : listPlayers)
			{
				
				if (!(player instanceof FakePlayer))
				{
					
					ChunkPos playerChunkCoords = new ChunkPos(player.chunkCoordX, player.chunkCoordZ);
					playerLoadedChunks.addAll(groupedChunksFinder(loadedChunks, playerChunkCoords, radiusLimit));
					
				}
				
			}
			
			radiusLimit = (int) Math.ceil(TICKET_RADIUS + TICKET_LIMIT);
			
			for (ChunkPos coord : world.getPersistentChunks().keySet())
			{
				
				forceLoadedChunks.addAll(groupedChunksFinder(loadedChunks, coord, radiusLimit));
				
			}
			
			radiusLimit = (int) Math.ceil(SPAWN_RADIUS + SPAWN_LIMIT);
			
			if (world.provider.canRespawnHere() && world.provider.getDimensionType().shouldLoadSpawn())
			{

				ChunkPos spawnChunkCoords = new ChunkPos(
						(int) Math.floor(world.getSpawnPoint().getX() / CHUNK_WIDTH),
						(int) Math.floor(world.getSpawnPoint().getZ() / CHUNK_WIDTH));
				
				spawnLoadedChunks.addAll(groupedChunksFinder(loadedChunks, spawnChunkCoords, radiusLimit));
				
			}
			
			for (ChunkPos coord : loadedChunks)
			{
				
				if (!playerLoadedChunks.contains(coord)
						&& !forceLoadedChunks.contains(coord)
						&& !spawnLoadedChunks.contains(coord))
				{
					
					chunksToUnload.add(coord);
					
				}
				
			}
			
		}
		
	}
	
	/*
	 * Analyze the chunks that are currently loaded in this world. Select loaded chunks that are isolated from any chunk watchers, 
	 * and queue these isolated chunks for unloading.
	 */
	
	public void unloadChunks()
	{
		initialTime = MinecraftServer.getCurrentTimeMillis();
		
		populateChunksToUnload();
		
		if (this.world.getChunkProvider() instanceof ChunkProviderServer)
		{
			ChunkProviderServer chunkProviderServer = (ChunkProviderServer) this.world.getChunkProvider();
			
			for (ChunkPos coord : chunksToUnload)
			{
				Chunk chunk = chunkProviderServer.getLoadedChunk(coord.x, coord.z);
				if (chunk != null) {
					chunkProviderServer.queueUnload(chunk);
				}
			}
			
		}
		
		if (ModChunkPurge.config.debug)
		{
			ModChunkPurge.log.log(Level.INFO, "Queued " + chunksToUnload.size()
					+ " chunks for unload in " + this.world.provider.getDimensionType().getName()
					+ " (" + this.world.provider.getDimension()
					+ ") in " + (MinecraftServer.getCurrentTimeMillis() - this.initialTime)
					+ " milliseconds.");
		}
		
	}
	
}
