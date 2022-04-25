package gregtech.api.worldgen.config;

import appeng.recipes.ores.OreDictionaryHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gregtech.api.GregTechAPI;
import gregtech.api.items.OreDictNames;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.api.worldgen.bedrockOres.BedrockOreVeinHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.advancements.critereon.OredictItemPredicate;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class BedrockOreDepositDefinition implements IWorldgenDefinition {

    private final String depositName;

    private int weight; // weight value for determining which vein will appear
    private String assignedName; // vein name for JEI display
    private String description; // vein description for JEI display
    private final int[] yields = new int[2]; // the [minimum, maximum) yields
    private int depletionAmount; // amount of Ore the vein gets drained by
    private int depletionChance; // the chance [0, 100] that the vein will deplete by 1
    private int depletedYield; // yield after the vein is depleted

    private Material storedOres; // the Ore which the vein contains

    private Function<Biome, Integer> biomeWeightModifier = biome -> 0; // weighting of biomes
    private Predicate<WorldProvider> dimensionFilter = WorldProvider::isSurfaceWorld; // filtering of dimensions

    public BedrockOreDepositDefinition(String depositName) {
        this.depositName = depositName;
    }

    @Override
    public boolean initializeFromConfig(@Nonnull JsonObject configRoot) {
        // the weight value for determining which vein will appear
        this.weight = configRoot.get("weight").getAsInt();
        // the [minimum, maximum) yield of the vein
        this.yields[0] = configRoot.get("yield").getAsJsonObject().get("min").getAsInt();
        this.yields[1] = configRoot.get("yield").getAsJsonObject().get("max").getAsInt();
        // amount of Ore the vein gets depleted by
        this.depletionAmount = configRoot.get("depletion").getAsJsonObject().get("amount").getAsInt();
        // the chance [0, 100] that the vein will deplete by depletionAmount
        this.depletionChance = Math.max(0, Math.min(100, configRoot.get("depletion").getAsJsonObject().get("chance").getAsInt()));

        // the Ore which the vein contains
        Material material = getMaterialByName(configRoot.get("ore").getAsString());
            if(material != null){
                this.storedOres = material;
        } else {
            GTLog.logger.error("Bedrock Ore Vein {} cannot have a null Ore!", this.depositName, new RuntimeException());
            return false;
        }

        // vein name for JEI display
        if (configRoot.has("name")) {
            this.assignedName = configRoot.get("name").getAsString();
        }
        // vein description for JEI display
        if (configRoot.has("description")) {
            this.description = configRoot.get("description").getAsString();
        }
        // yield after the vein is depleted
        if (configRoot.get("depletion").getAsJsonObject().has("depleted_yield")) {
            this.depletedYield = configRoot.get("depletion").getAsJsonObject().get("depleted_yield").getAsInt();
        }
        // additional weighting changes determined by biomes
        if (configRoot.has("biome_modifier")) {
            this.biomeWeightModifier = WorldConfigUtils.createBiomeWeightModifier(configRoot.get("biome_modifier"));
        }
        // filtering of dimensions to determine where the vein can generate
        if (configRoot.has("dimension_filter")) {
            this.dimensionFilter = WorldConfigUtils.createWorldPredicate(configRoot.get("dimension_filter"));
        }
        BedrockOreVeinHandler.addOreDeposit(this);
        return true;
    }

    public static Material getMaterialByName(String name) {
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObject(name);
        if (material == null)
            throw new IllegalArgumentException("Material with name " + name + " not found!");
        return material;
    }


    //This is the file name
    @Override
    public String getDepositName() {
        return depositName;
    }

    public String getAssignedName() {
        return assignedName;
    }

    public String getDescription() {
        return description;
    }

    public int getWeight() {
        return weight;
    }

    @SuppressWarnings("unused")
    public int[] getYields() {
        return yields;
    }

    public int getMinimumYield() {
        return yields[0];
    }

    public int getMaximumYield() {
        return yields[1];
    }

    public int getDepletionAmount() {
        return depletionAmount;
    }

    public int getDepletionChance() {
        return depletionChance;
    }

    public int getDepletedYield() {
        return depletedYield;
    }

    public Material getStoredOre() {
        return storedOres;
    }

    public Function<Biome, Integer> getBiomeWeightModifier() {
        return biomeWeightModifier;
    }

    public Predicate<WorldProvider> getDimensionFilter() {
        return dimensionFilter;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BedrockOreDepositDefinition))
            return false;

        BedrockOreDepositDefinition objDeposit = (BedrockOreDepositDefinition) obj;
        if (this.weight != objDeposit.getWeight())
            return false;
        if (this.getMinimumYield() != objDeposit.getMinimumYield())
            return false;
        if (this.getMaximumYield() != objDeposit.getMaximumYield())
            return false;
        if (this.depletionAmount != objDeposit.getDepletionAmount())
            return false;
        if (this.depletionChance != objDeposit.getDepletionChance())
            return false;
        if (!this.storedOres.equals(objDeposit.getStoredOre()))
            return false;
        if ((this.assignedName == null && objDeposit.getAssignedName() != null) ||
                (this.assignedName != null && objDeposit.getAssignedName() == null) ||
                (this.assignedName != null && objDeposit.getAssignedName() != null && !this.assignedName.equals(objDeposit.getAssignedName())))
            return false;
        if ((this.description == null && objDeposit.getDescription() != null) ||
                (this.description != null && objDeposit.getDescription() == null) ||
                (this.description != null && objDeposit.getDescription() != null && !this.description.equals(objDeposit.getDescription())))
            return false;
        if (this.depletedYield != objDeposit.getDepletedYield())
            return false;
        if ((this.biomeWeightModifier == null && objDeposit.getBiomeWeightModifier() != null) ||
                (this.biomeWeightModifier != null && objDeposit.getBiomeWeightModifier() == null) ||
                (this.biomeWeightModifier != null && objDeposit.getBiomeWeightModifier() != null && !this.biomeWeightModifier.equals(objDeposit.getBiomeWeightModifier())))
            return false;
        if ((this.dimensionFilter == null && objDeposit.getDimensionFilter() != null) ||
                (this.dimensionFilter != null && objDeposit.getDimensionFilter() == null) ||
                (this.dimensionFilter != null && objDeposit.getDimensionFilter() != null && !this.dimensionFilter.equals(objDeposit.getDimensionFilter())))
            return false;

        return super.equals(obj);
    }
}
