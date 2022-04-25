package gregtech.api.worldgen.bedrockOres;

import gregtech.api.GTValues;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.CPacketOreVeinList;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTLog;
import gregtech.api.util.XSTR;
import gregtech.api.worldgen.bedrockFluids.ChunkPosDimension;
import gregtech.api.worldgen.config.BedrockOreDepositDefinition;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
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
    public static HashMap<ChunkPosDimension, OreVeinWorldEntry> veinCache = new HashMap<>();

    public static final int VEIN_CHUNK_SIZE = 4; // veins are 4x4 chunk squares

    public static final int MAXIMUM_VEIN_OPERATIONS = 100_000;

    /**
     * Gets the OreVeinWorldInfo object associated with the given chunk
     *
     * @param world  The world to retrieve
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return The OreVeinWorldInfo corresponding with the given chunk
     */
    @Nullable
    public static OreVeinWorldEntry getOreVeinWorldEntry(@Nonnull World world, int chunkX, int chunkZ) {
        if (world.isRemote)
            return null;

        ChunkPosDimension coords = new ChunkPosDimension(world.provider.getDimension(), chunkX / VEIN_CHUNK_SIZE, chunkZ / VEIN_CHUNK_SIZE);

        OreVeinWorldEntry worldEntry = veinCache.get(coords);
        if (worldEntry == null) {
            BedrockOreDepositDefinition definition = null;

            int query = world.getChunk(chunkX / VEIN_CHUNK_SIZE, chunkZ / VEIN_CHUNK_SIZE).getRandomWithSeed(90210).nextInt();

            Biome biome = world.getBiomeForCoordsBody(new BlockPos(chunkX << 4, 64, chunkZ << 4));
            int totalWeight = getTotalWeight(world.provider, biome);
            if (totalWeight > 0) {
                int weight = Math.abs(query % totalWeight);
                for (Map.Entry<BedrockOreDepositDefinition, Integer> entry : veinList.entrySet()) {
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

            int maximumYield = 0;
            if (definition != null) {
                maximumYield = random.nextInt(definition.getMaximumYield() - definition.getMinimumYield()) + definition.getMinimumYield();
                maximumYield = Math.min(maximumYield, definition.getMaximumYield());
            }

            worldEntry = new OreVeinWorldEntry(definition, maximumYield, MAXIMUM_VEIN_OPERATIONS);
            veinCache.put(coords, worldEntry);
        }
        return worldEntry;
    }

    /**
     * Gets the total weight of all veins for the given dimension ID and biome type
     *
     * @param provider The WorldProvider whose dimension to check
     * @param biome    The biome type to check
     * @return The total weight associated with the dimension/biome pair
     */
    public static int getTotalWeight(@Nonnull WorldProvider provider, Biome biome) {
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
            if (entry.getKey().getDimensionFilter().test(provider)) {
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
            for (Map.Entry<ChunkPosDimension, OreVeinWorldEntry> entry : BedrockOreVeinHandler.veinCache.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null)
                    packetMap.put(entry.getValue(), entry.getValue().getDefinition().getWeight());
            }
            NetworkHandler.channel.sendToAll(new CPacketOreVeinList(packetMap).toFMLPacket());
        }
    }

    /**
     * gets the Ore yield in a specific chunk
     *
     * @param world  the world to retrieve it from
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return yield in the vein
     */
    public static int getOreYield(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null) return 0;
        return info.getOreYield();
    }

    /**
     * Gets the yield of Ore in the chunk after the vein is completely depleted
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return yield of Ore post depletion
     */
    public static int getDepletedOreYield(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null || info.getDefinition() == null) return 0;
        return info.getDefinition().getDepletedYield();
    }

    /**
     * Gets the current operations remaining in a specific chunk's vein
     *
     * @param world  The world to test
     * @param chunkX X coordinate of desired chunk
     * @param chunkZ Z coordinate of desired chunk
     * @return amount of operations in the given chunk
     */
    public static int getOperationsRemaining(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
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
    public static Material getOreInChunk(World world, int chunkX, int chunkZ) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
        if (info == null || info.getDefinition() == null) return null;
        return info.getDefinition().getStoredOre();
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
    public static void depleteVein(World world, int chunkX, int chunkZ, int amount, boolean ignoreVeinStats) {
        OreVeinWorldEntry info = getOreVeinWorldEntry(world, chunkX, chunkZ);
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

    public static class OreVeinWorldEntry {
        private BedrockOreDepositDefinition vein;
        private int OreYield;
        private int operationsRemaining;

        public OreVeinWorldEntry(BedrockOreDepositDefinition vein, int OreYield, int operationsRemaining) {
            this.vein = vein;
            this.OreYield = OreYield;
            this.operationsRemaining = operationsRemaining;
        }

        private OreVeinWorldEntry() {
        }

        public BedrockOreDepositDefinition getDefinition() {
            return this.vein;
        }

        public int getOreYield() {
            return this.OreYield;
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
            tag.setInteger("OreYield", OreYield);
            tag.setInteger("oreOperationsRemaining", operationsRemaining);
            if (vein != null) {
                tag.setString("oreVein", vein.getDepositName());
            }
            return tag;
        }

        @Nonnull
        public static OreVeinWorldEntry readFromNBT(@Nonnull NBTTagCompound tag) {
            OreVeinWorldEntry info = new OreVeinWorldEntry();
            info.OreYield = tag.getInteger("OreYield");
            info.operationsRemaining = tag.getInteger("oreOperationsRemaining");

            if (tag.hasKey("oreVein")) {
                String s = tag.getString("oreVein");
                for (BedrockOreDepositDefinition definition : veinList.keySet()) {
                    if (s.equalsIgnoreCase(definition.getDepositName()))
                        info.vein = definition;
                }
            }
            return info;
        }
    }
}
