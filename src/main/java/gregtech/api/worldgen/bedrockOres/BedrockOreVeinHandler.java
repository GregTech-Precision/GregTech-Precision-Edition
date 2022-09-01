package gregtech.api.worldgen.bedrockOres;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gregtech.api.GTValues;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.CPacketOreVeinList;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTLog;
import gregtech.api.util.XSTR;
import gregtech.api.worldgen.bedrockFluids.ChunkPosDimension;
import gregtech.api.worldgen.config.BedrockOreDepositDefinition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BedrockOreVeinHandler {

    public final static LinkedHashMap<BedrockOreDepositDefinition, Integer> veinList = new LinkedHashMap<>();
    private final static Map<Integer, HashMap<Integer, Integer>> totalWeightMap = new HashMap<>();
    public static HashBasedTable<ChunkPosDimension, Integer, OreVeinWorldEntry> veinCache = HashBasedTable.create();
    private static LinkedHashMap<String, BedrockOreDepositDefinition> nameVein = new LinkedHashMap<>();

    public static final int VEIN_CHUNK_SIZE = 4; // veins are 4x4 chunk squares

    public static List<Integer> operationsPerLayer = Arrays.asList(10000, 200000);

    /**
     * Gets the OreVeinWorldInfo object associated with the given chunk
     *
     * @param world  The world to retrieve
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return The OreVeinWorldInfo corresponding with the given chunk
     */
    @Nullable
    public static OreVeinWorldEntry getOreVeinWorldEntry(@Nonnull World world, int chunkX, int chunkZ, int layer) {
        if (world.isRemote)
            return null;

        ChunkPosDimension coords = new ChunkPosDimension(world.provider.getDimension(), chunkX / VEIN_CHUNK_SIZE, chunkZ / VEIN_CHUNK_SIZE);

        if(veinCache.contains(coords, layer))
             return veinCache.get(coords, layer);

        BedrockOreDepositDefinition definition = null;
        OreVeinWorldEntry worldEntry;

        int query = world.getChunk(chunkX / VEIN_CHUNK_SIZE, chunkZ / VEIN_CHUNK_SIZE).getRandomWithSeed(90210).nextInt();

        Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX << 4, 64, chunkZ << 4));
        int totalWeight = getTotalWeight(world.provider, biome, layer);
        if (totalWeight > 0) {
            int weight = Math.abs(query % totalWeight);
            for (Map.Entry<BedrockOreDepositDefinition, Integer> entry : veinList.entrySet()) {
                if(entry.getKey().getLayer() != layer)
                    continue;
                int veinWeight = entry.getValue() + entry.getKey().getBiomeWeightModifier().apply(biome);
                if (veinWeight > 0 && entry.getKey().getDimensionFilter().test(world.provider)) {
                    weight -= veinWeight;
                    if (weight < 0) {
                        definition = entry.getKey();
                        break;
                    }
                }
            }
        }

        Random random = new XSTR(31L * 31 * chunkX + chunkZ * 31L + Long.hashCode(world.getSeed()));

        int operations = 0;
        if (definition != null) {
            int r = definition.getMaximumOperations() - definition.getMinimumOperations();
            operations = (r == 0 ? 0 : random.nextInt(r)) + definition.getMinimumOperations();
            operations = Math.min(operations, definition.getMaximumOperations());
        }

        worldEntry = new OreVeinWorldEntry(definition, operations);
        veinCache.put(coords, layer, worldEntry);
        return worldEntry;
    }

    public static BedrockOreDepositDefinition getDepositByName(String name){
        if(nameVein.containsKey(name))
            return nameVein.get(name);

        for(BedrockOreDepositDefinition definition : veinList.keySet()){
            if(name.equalsIgnoreCase(definition.getDepositName()))
            {
                nameVein.put(name, definition);
                return definition;
            }
        }
        return null;
    }

    /**
     * Gets the total weight of all veins for the given dimension ID and biome type
     *
     * @param provider The WorldProvider whose dimension to check
     * @param biome    The biome type to check
     * @return The total weight associated with the dimension/biome pair
     */
    public static int getTotalWeight(@Nonnull WorldProvider provider, Biome biome, int layer) {
        int dim = provider.getDimension();
        if (!totalWeightMap.containsKey(dim)) {
            totalWeightMap.put(dim, new HashMap<>());
        }

        Map<Integer, Integer> dimMap = totalWeightMap.get(dim);
        int biomeID = Biome.getIdForBiome(biome);

        if (dimMap.containsKey(biomeID)) {
            return dimMap.get(biomeID);
        }

        int totalWeight = 0;
        for (Map.Entry<BedrockOreDepositDefinition, Integer> entry : veinList.entrySet()) {
            if (entry.getKey().getDimensionFilter().test(provider) && entry.getKey().getLayer() == layer) {
                totalWeight += entry.getKey().getBiomeWeightModifier().apply(biome);
                totalWeight += entry.getKey().getWeight();
            }
        }

        // make sure the vein can generate if no biome weighting is added
        if (totalWeight == 0 && !veinList.isEmpty())
            GTLog.logger.error("Bedrock Ore Vein weight was 0 in biome {}", biome.biomeName);

        dimMap.put(biomeID, totalWeight);
        return totalWeight;
    }

    /**
     * Adds a vein to the pool of veins
     *
     * @param definition the vein to add
     */
    public static void addOreDeposit(BedrockOreDepositDefinition definition) {
        veinList.put(definition, definition.getWeight());
    }

    public static void recalculateChances(boolean mutePackets) {
        totalWeightMap.clear();
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && !mutePackets) {
            HashMap<OreVeinWorldEntry, Integer> packetMap = new HashMap<>();
            for (Table.Cell<ChunkPosDimension, Integer, OreVeinWorldEntry> entry : BedrockOreVeinHandler.veinCache.cellSet()) {
                if (entry.getRowKey() != null && entry.getValue() != null)
                    packetMap.put(entry.getValue(), entry.getValue().getDefinition().getWeight());
            }
            NetworkHandler.channel.sendToAll(new CPacketOreVeinList(packetMap).toFMLPacket());
        }
    }

    /**
     * Gets the current operations remaining in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return amount of operations in the given chunk
     */
    public static int getOperationsRemaining(World world, int chunkX, int chunkZ, int layer) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ, layer);
        if (info == null) return 0;
        return info.getOperationsRemaining();
    }

    /**
     * Gets the Ore in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return Ore in given chunk
     */
    @Nullable
    public static List<Material> getOresInChunk(World world, int chunkX, int chunkZ, int layer) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ, layer);
        if (info == null || info.getDefinition() == null) return null;
        return info.getDefinition().getStoredOres();
    }

    /**
     * Depletes Ore from a given chunk
     *
     * @param world           World whose chunk to drain
     * @param chunkX          Chunk x
     * @param chunkZ          Chunk z
     * @param amount          the amount of Ore to deplete the vein by
     * @param ignoreVeinStats whether to ignore the vein's depletion data, if false ignores amount
     */
    public static void depleteVein(World world, int chunkX, int chunkZ, int layer, int amount, boolean ignoreVeinStats) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ, layer);
        if (info == null) return;

        if (ignoreVeinStats) {
            info.decreaseOperations(amount);
            return;
        }

        BedrockOreDepositDefinition definition = info.getDefinition();

        // prevent division by zero, veins that never deplete don't need updating
        if (definition == null || definition.getDepletionChance() == 0)
            return;

        if (definition.getDepletionChance() == 100 || GTValues.RNG.nextInt(100) <= definition.getDepletionChance()) {
            info.decreaseOperations(definition.getDepletionAmount());
            BedrockOreVeinSaveData.setDirty();
        }
    }

    /**
     *
     * @param layer
     * @return maximum amount of operations per this vein layer
     */
    public static int getOperationsPerLayer(int layer){
        if(operationsPerLayer.size() <= layer)
            return -1;
        return operationsPerLayer.get(layer);
    }

    public static class OreVeinWorldEntry {
        private BedrockOreDepositDefinition vein;
        private int operationsRemaining;

        public OreVeinWorldEntry(BedrockOreDepositDefinition vein, int operations) {
            this.vein = vein;
            this.operationsRemaining = operations;
        }

        private OreVeinWorldEntry() {}

        public BedrockOreDepositDefinition getDefinition() {
            return this.vein;
        }

        public int getOperationsRemaining() {
            return this.operationsRemaining;
        }

        @SuppressWarnings("unused")
        public void setOperationsRemaining(int amount) {
            this.operationsRemaining = amount;
        }

        public void decreaseOperations(int amount) {
            operationsRemaining = Math.max(0, operationsRemaining - amount);
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("operationsRemaining", operationsRemaining);
            if (vein != null) {
                tag.setString("vein", vein.getDepositName());
            }
            return tag;
        }

        @Nonnull
        public static OreVeinWorldEntry readFromNBT(@Nonnull NBTTagCompound tag) {
            OreVeinWorldEntry info = new OreVeinWorldEntry();
            info.operationsRemaining = tag.getInteger("operationsRemaining");

            if (tag.hasKey("vein")) {
                info.vein = BedrockOreVeinHandler.getDepositByName(tag.getString("vein"));
            }

            return info;
        }
    }
}
