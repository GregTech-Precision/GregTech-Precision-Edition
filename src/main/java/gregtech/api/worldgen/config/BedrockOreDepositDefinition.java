package gregtech.api.worldgen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.worldgen.bedrockOres.BedrockOreVeinHandler;
import net.minecraft.init.Items;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private final List<Material> storedOres = new ArrayList<>(); // the Ore which the vein contains
    private final ConcurrentHashMap<Material, Integer> oreWeights = new ConcurrentHashMap<>();
    private int maxOresWeight;
    private int layer = 0;
    private final List<FluidStack> specialFluids = new ArrayList<>();

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
        if(configRoot.has("yield")) {
            this.yields[0] = configRoot.get("yield").getAsJsonObject().get("min").getAsInt();
            this.yields[1] = configRoot.get("yield").getAsJsonObject().get("max").getAsInt();
        } else {
            this.yields[0] = 1;
            this.yields[1] = 1;
        }

        // amount of Ore the vein gets depleted by
        if(configRoot.has("depletion")) {
            this.depletionAmount = configRoot.get("depletion").getAsJsonObject().get("amount").getAsInt();
            // the chance [0, 100] that the vein will deplete by depletionAmount
            this.depletionChance = Math.max(0, Math.min(100, configRoot.get("depletion").getAsJsonObject().get("chance").getAsInt()));
            // yield after the vein is depleted
            if (configRoot.get("depletion").getAsJsonObject().has("depleted_yield")) {
                this.depletedYield = configRoot.get("depletion").getAsJsonObject().get("depleted_yield").getAsInt();
            } else {
                this.depletedYield = 1;
            }
        } else {
            this.depletionAmount = 1;
            this.depletionChance = 100;
        }

        // Zero Layer Vein
       if(configRoot.has("layer")){
           this.layer = configRoot.get("layer").getAsInt();
       } else {
           this.layer = 0;
       }

       if(layer > 0){
           specialFluids.add(new FluidStack(getFluidByName("drilling_fluid"), 50));
       }

       if(configRoot.has("additional_fluids") || configRoot.has("fluids")){
           boolean resetFluids = configRoot.has("fluids");
           if(resetFluids){
               this.specialFluids.clear();
           }
           JsonArray array = configRoot.get(resetFluids ? "fluids" : "additional_fluids").getAsJsonArray();
           array.forEach(element -> {
               JsonObject obj = element.getAsJsonObject();
               Fluid fluid = null;
               int amount = 50;
               if(obj.has("fluid")){
                   fluid = getFluidByName(obj.get("fluid").getAsString());
               }
               if(obj.has("amount")){
                   amount = obj.get("amount").getAsInt();
               }
               if(fluid != null && amount > 0){
                   this.specialFluids.add(new FluidStack(fluid, amount));
               }
           });
       }

        // Second Layer Ores
        if(configRoot.has("ores")) {
            JsonArray array = configRoot.getAsJsonArray("ores");
            if (array != null && array.size() > 0) {
                array.forEach(ore -> {
                    JsonObject obj = ore.getAsJsonObject();
                    Material newOre = getMaterialByName(obj.get("ore").getAsString());
                    if(OreDictUnifier.get(OrePrefix.crushed, newOre).getItem() != Items.AIR) {
                        this.storedOres.add(newOre);
                        int weight = 1;
                        if (obj.has("weight")) {
                            weight = obj.get("weight").getAsInt();
                        }
                        maxOresWeight += weight;
                        this.oreWeights.put(newOre, weight);
                    }
                });
            }
        }

        // vein name for JEI display
        if (configRoot.has("name")) {
            this.assignedName = configRoot.get("name").getAsString();
        }
        // vein description for JEI display
        if (configRoot.has("description")) {
            this.description = configRoot.get("description").getAsString();
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

    public static Fluid getFluidByName(String name) {
        Fluid fluid = FluidRegistry.getFluid(name);
        if (fluid == null)
            throw new IllegalArgumentException("Fluid with name " + name + " not found!");
        return fluid;
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

    public List<Material> getStoredOres() {
        return storedOres;
    }

    public List<FluidStack> getSpecialFluids() { return specialFluids; }

    public int getLayer(){
        return layer;
    }

    public int getOreWeight(Material ore){
        return oreWeights.getOrDefault(ore, 1);
    }

    public int getAllOresWeight(){
        return maxOresWeight;
    }

    public Map<Material, Integer> getOreWeights(){
        return oreWeights;
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
        if (!this.storedOres.equals(objDeposit.storedOres))
            return false;
        if(!this.oreWeights.equals(objDeposit.oreWeights))
            return false;
        if(this.layer != objDeposit.layer)
            return false;
        if(!this.specialFluids.equals(objDeposit.specialFluids))
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
