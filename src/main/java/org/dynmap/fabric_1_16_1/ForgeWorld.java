package org.dynmap.fabric_1_16_1;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.border.WorldBorder;
import org.dynmap.DynmapChunk;
import org.dynmap.DynmapLocation;
import org.dynmap.DynmapWorld;
import org.dynmap.utils.MapChunkCache;
import org.dynmap.utils.Polygon;

import java.util.List;

public class ForgeWorld extends DynmapWorld {
    private WorldAccess world;
    private final boolean skylight;
    private final boolean isnether;
    private final boolean istheend;
    private final String env;
    private DynmapLocation spawnloc = new DynmapLocation();
    private static int maxWorldHeight = 256;    // Maximum allows world height

    public static int getMaxWorldHeight() {
        return maxWorldHeight;
    }

    public static void setMaxWorldHeight(int h) {
        maxWorldHeight = h;
    }

    public static String getWorldName(WorldAccess w) {
        RegistryKey<World> rk = w.getWorld().getRegistryKey();
        if (rk == World.OVERWORLD) {    // Overworld?
            return w.getWorld().getServer().getSaveProperties().getLevelName();
        } else if (rk == World.END) {
            return "DIM1";
        } else if (rk == World.NETHER) {
            return "DIM-1";
        } else {
            return rk.getValue().getNamespace() + "_" + rk.getValue().getPath();
        }
    }

    public ForgeWorld(WorldAccess w) {
        this(getWorldName(w), w.getWorld().getHeight(),
                w.getWorld().getSeaLevel(),
                w.getWorld().getRegistryKey() == World.END,
                w.getWorld().getRegistryKey() == World.NETHER,
                w.getWorld().getRegistryKey().getValue().getPath());
        setWorldLoaded(w);
    }

    public ForgeWorld(String name, int height, int sealevel, boolean nether, boolean the_end, String deftitle) {
        super(name, (height > maxWorldHeight) ? maxWorldHeight : height, sealevel);
        world = null;
        setTitle(deftitle);
        isnether = nether;
        istheend = the_end;
        skylight = !(isnether || istheend);

        if (isnether) {
            env = "nether";
        } else if (istheend) {
            env = "the_end";
        } else {
            env = "normal";
        }

    }

    /* Test if world is nether */
    @Override
    public boolean isNether() {
        return isnether;
    }

    public boolean isTheEnd() {
        return istheend;
    }

    /* Get world spawn location */
    @Override
    public DynmapLocation getSpawnLocation() {
        if (world != null) {
            spawnloc.x = world.getLevelProperties().getSpawnX();
            spawnloc.y = world.getLevelProperties().getSpawnY();
            spawnloc.z = world.getLevelProperties().getSpawnZ();
            spawnloc.world = this.getName();
        }
        return spawnloc;
    }

    /* Get world time */
    @Override
    public long getTime() {
        if (world != null)
            return world.getWorld().getTime();
        else
            return -1;
    }

    /* World is storming */
    @Override
    public boolean hasStorm() {
        if (world != null)
            return world.getWorld().isRaining();
        else
            return false;
    }

    /* World is thundering */
    @Override
    public boolean isThundering() {
        if (world != null)
            return world.getWorld().isThundering();
        else
            return false;
    }

    /* World is loaded */
    @Override
    public boolean isLoaded() {
        return (world != null);
    }

    /* Set world to unloaded */
    @Override
    public void setWorldUnloaded() {
        getSpawnLocation();
        world = null;
    }

    /* Set world to loaded */
    public void setWorldLoaded(WorldAccess w) {
        world = w;
        this.sealevel = w.getSeaLevel();   // Read actual current sealevel from world
        // Update lighting table
        for (int i = 0; i < 16; i++) {
            this.setBrightnessTableEntry(i, w.getWorld().getDimension().method_28516(i));
        }
    }

    /* Get light level of block */
    @Override
    public int getLightLevel(int x, int y, int z) {
        if (world != null)
            return world.getLightLevel(new BlockPos(x, y, z));
        else
            return -1;
    }

    /* Get highest Y coord of given location */
    @Override
    public int getHighestBlockYAt(int x, int z) {
        if (world != null) {
            return world.getWorld().getChunk(x >> 4, z >> 4).getHeightmap(Heightmap.Type.MOTION_BLOCKING).get(x & 15, z & 15);
        } else
            return -1;
    }

    /* Test if sky light level is requestable */
    @Override
    public boolean canGetSkyLightLevel() {
        return skylight;
    }

    /* Return sky light level */
    @Override
    public int getSkyLightLevel(int x, int y, int z) {
        if (world != null) {
            return world.getLightLevel(LightType.SKY, new BlockPos(x, y, z));
        } else
            return -1;
    }

    /**
     * Get world environment ID (lower case - normal, the_end, nether)
     */
    @Override
    public String getEnvironment() {
        return env;
    }

    /**
     * Get map chunk cache for world
     */
    @Override
    public MapChunkCache getChunkCache(List<DynmapChunk> chunks) {
        if (world != null) {
            ForgeMapChunkCache c = new ForgeMapChunkCache();
            c.setChunks(this, chunks);
            return c;
        }
        return null;
    }

    public World getWorld() {
        return world.getWorld();
    }

    @Override
    public Polygon getWorldBorder() {
        if (world != null) {
            WorldBorder wb = world.getWorldBorder();
            if ((wb != null) && (wb.getSize() < 5.9E7)) {
                Polygon p = new Polygon();
                p.addVertex(wb.getBoundWest(), wb.getBoundNorth());
                p.addVertex(wb.getBoundWest(), wb.getBoundSouth());
                p.addVertex(wb.getBoundEast(), wb.getBoundSouth());
                p.addVertex(wb.getBoundEast(), wb.getBoundNorth());
                return p;
            }
        }
        return null;
    }
}
