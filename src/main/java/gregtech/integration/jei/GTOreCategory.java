package gregtech.integration.jei;

import gregtech.api.gui.GuiTextures;
import gregtech.api.util.GTLog;
import gregtech.api.worldgen.bedrockOres.BedrockOreVeinHandler;
import gregtech.api.worldgen.config.BedrockOreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.integration.jei.recipe.primitive.BasicRecipeCategory;
import gregtech.integration.jei.utils.render.FluidStackTextRenderer;
import gregtech.integration.jei.utils.render.ItemStackTextRenderer;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

import static gregtech.api.GTValues.MODID_AR;

public class GTOreCategory extends BasicRecipeCategory<GTOreInfo, GTOreInfo> {

    protected final IDrawable slot;
    protected  final IDrawable fluidSlot;
    protected BedrockOreDepositDefinition definition;
    protected String veinName;
    protected int layer;
    protected int outputCount;
    protected int inputFluidCount;
    protected int weight;
    protected List<Integer> dimensionIDs;
    protected final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    protected final Map<Integer, String> namedDimensions = WorldGenRegistry.getNamedDimensions();
    private final Supplier<List<Integer>> dimension = this::getAllRegisteredDimensions;
    private final int NUM_OF_SLOTS = 3;
    private final int NUM_OF_FLUID_SLOTS = 2;
    private final int SLOT_WIDTH = 18;
    private final int SLOT_HEIGHT = 18;

    public GTOreCategory(int layer, IGuiHelper guiHelper) {
        super("ore_spawn_location_" + layer,
                "ore.spawnlocation.name",
                guiHelper.createBlankDrawable(176, 166),
                guiHelper);

        this.layer = layer;
        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
        this.fluidSlot = guiHelper.drawableBuilder(GuiTextures.FLUID_SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
    }

    @Nonnull
    @Override
    public String getTitle() {
        return super.getTitle() + "(Layer: "+layer+")";
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GTOreInfo recipeWrapper, @Nonnull IIngredients ingredients) {

        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
        int baseYPos = 37;


        for (int i = 0; i < recipeWrapper.getOutputCount(); i++) {
            int yPos = baseYPos + (i / NUM_OF_SLOTS) * SLOT_HEIGHT;
            int xPos = 22 + (i % NUM_OF_SLOTS) * SLOT_WIDTH;

            itemStackGroup.init(i, false,
                    new ItemStackTextRenderer(recipeWrapper.getOreWeight(i) * 100, -1),
                    xPos + 1, yPos + 1, 16, 16, 0, 0);
        }

        for (int i = 0; i < recipeWrapper.getFluidInputCount(); i++) {
            int yPos = baseYPos + (i / NUM_OF_FLUID_SLOTS) * SLOT_HEIGHT;
            int xPos = 94 + (i % NUM_OF_FLUID_SLOTS) * SLOT_WIDTH;

            fluidStackGroup.init(i, true,
                    new FluidStackTextRenderer(50, true, SLOT_WIDTH, SLOT_HEIGHT, fluidSlot),
                    xPos + 1, yPos + 1, 16, 16, 0, 0);
        }

        itemStackGroup.addTooltipCallback(recipeWrapper::addTooltip);
        fluidStackGroup.addTooltipCallback(recipeWrapper::addFluidTooltip);
        itemStackGroup.set(ingredients);
        fluidStackGroup.set(ingredients);
        layer = recipeWrapper.getLayer();
        weight = recipeWrapper.getWeight();
        veinName = recipeWrapper.getVeinName();
        outputCount = recipeWrapper.getOutputCount();
        inputFluidCount = recipeWrapper.getFluidInputCount();
        definition = recipeWrapper.getDefinition();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull GTOreInfo recipe) {
        return recipe;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {

        int baseXPos = 22;
        int baseFluidXPos = 94;
        int baseYPos = 19;
        int dimDisplayPos = 80;
        int dimDisplayLength;
        String dimName;
        String fullDimName;

        //Selected Ore
        int yPos = 0;
        for (int i = 0; i < outputCount; i++) {
            yPos = baseYPos + SLOT_HEIGHT + (i / NUM_OF_SLOTS) * SLOT_HEIGHT;
            int xPos = baseXPos + (i % NUM_OF_SLOTS) * SLOT_WIDTH;

            this.slot.draw(minecraft, xPos, yPos);
        }

        //Special Fluid
        for (int i = 0; i < inputFluidCount; i++) {
            yPos = baseYPos + SLOT_HEIGHT + (i / NUM_OF_FLUID_SLOTS) * SLOT_HEIGHT;
            int xPos = baseFluidXPos + (i % NUM_OF_FLUID_SLOTS) * SLOT_WIDTH;

            this.fluidSlot.draw(minecraft, xPos, yPos);
        }

        minecraft.fontRenderer.drawString("Ores:", baseXPos, baseYPos, 0x111111);
        minecraft.fontRenderer.drawString("Fluids:", baseFluidXPos, baseYPos, 0x111111);

        //base positions set to position of last rendered slot for later use.
        //Must account for the fact that yPos is the top corner of the slot, so add in another slot height
        baseYPos = yPos + SLOT_HEIGHT*2;

        drawVeinName(minecraft.fontRenderer);

        //Begin Drawing information, depending on how many rows of ore outputs were created
        //Give room for 5 lines of 5 ores each, so 25 unique ores in the vein
        //73 is SLOT_HEIGHT * (NUM_OF_SLOTS - 1) + 1
        if (baseYPos < SLOT_HEIGHT * NUM_OF_SLOTS) {
            baseYPos = 91;
        }

        int operations = BedrockOreVeinHandler.getOperationsPerLayer(definition.getLayer());
        String maxOperations = operations >= 1000 ? operations/1000+"k" : Integer.toString(operations);

        //Create the Size
        minecraft.fontRenderer.drawString("Size: " + 50 + " - " + maxOperations, baseXPos, baseYPos, 0x111111);

        //Create the Weight
        minecraft.fontRenderer.drawString("Vein Weight: " + weight, baseXPos, baseYPos + FONT_HEIGHT, 0x111111);

        //Create the Dimensions
        minecraft.fontRenderer.drawString("Dimensions: ", baseXPos, baseYPos + (2 * FONT_HEIGHT), 0x111111);

        dimensionIDs = dimension.get();

        //Will attempt to write dimension IDs in a single line, separated by commas. If the list is so long such that it
        //would run off the end of the page, the list is continued on a new line.
        for (int i = 0; i < dimensionIDs.size(); i++) {

            //If the dimension name is included, append it to the dimension number
            if (namedDimensions.containsKey(dimensionIDs.get(i))) {
                dimName = namedDimensions.get(dimensionIDs.get(i));
                fullDimName = i == dimensionIDs.size() - 1 ?
                        dimName : dimName + ", ";
            }
            //If the dimension name is not included, just add the dimension number
            else {
                fullDimName = i == dimensionIDs.size() - 1 ?
                        Integer.toString(dimensionIDs.get(i)) :
                        dimensionIDs.get(i) + ", ";
            }

            //Find the length of the dimension name string
            dimDisplayLength = minecraft.fontRenderer.getStringWidth(fullDimName);

            //If the length of the string would go off the edge of screen, instead increment the y position
            if (dimDisplayLength > (176 - dimDisplayPos)) {
                baseYPos = baseYPos + FONT_HEIGHT;
                dimDisplayPos = 80;
            }

            minecraft.fontRenderer.drawString(fullDimName, dimDisplayPos, baseYPos + (3 * FONT_HEIGHT), 0x111111);

            //Increment the dimension name display position
            dimDisplayPos = dimDisplayPos + dimDisplayLength;
        }

    }

    private void drawVeinName(final FontRenderer fontRenderer) {
        final int maxVeinNameLength = 176;

        String veinNameToDraw = veinName;

        //Account for really long names
        if (fontRenderer.getStringWidth(veinNameToDraw) > maxVeinNameLength) {
            veinNameToDraw = fontRenderer.trimStringToWidth(veinName, maxVeinNameLength - 3, false) + "...";
        }

        //Ensure that the vein name is centered
        int startPosition = (maxVeinNameLength - fontRenderer.getStringWidth(veinNameToDraw)) / 2;

        fontRenderer.drawString(veinNameToDraw, startPosition, 1, 0x111111);
    }

    public List<Integer> getAllRegisteredDimensions() {
        List<Integer> dims = new ArrayList<>();
         /*
         Gather the registered dimensions here instead of at the top of the class to catch very late registered dimensions
         such as Advanced Rocketry
          */
        Map<DimensionType, IntSortedSet> dimMap = DimensionManager.getRegisteredDimensions();
        dimMap.values().stream()
                .flatMap(Collection::stream)
                .mapToInt(Integer::intValue)
                .filter(num -> definition.getDimensionFilter().test(DimensionManager.createProviderFor(num)))
                .forEach(dims::add);

        //Slight cleanup of the list if Advanced Rocketry is installed
        if (Loader.isModLoaded(MODID_AR)) {
            try {
                int[] spaceDims = DimensionManager.getDimensions(DimensionType.byName("space"));

                //Remove Space from the dimension list
                for (int spaceDim : spaceDims) {
                    if (dims.contains(spaceDim)) {
                        dims.remove((Integer) spaceDim);
                    }
                }
            } catch (IllegalArgumentException e) {
                GTLog.logger.error("Something went wrong with AR JEI integration, No DimensionType found");
                GTLog.logger.error(e);
            }
        }

        return dims;
    }
}
