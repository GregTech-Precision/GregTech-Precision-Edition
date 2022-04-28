package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.init.Items;

import java.util.HashMap;
import java.util.Map;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.COMPONENT_ASSEMBLER;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class ComponentRecipes {

    public static void register() {

        //Motors Start--------------------------------------------------------------------------------------------------

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtSingle, Tin, 2)
                .input(stick, Iron, 2)
                .input(stick, IronMagnetic)
                .input(wireGtSingle, Copper, 4)
                .outputs(ELECTRIC_MOTOR_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtSingle, Tin, 2)
                .input(stick, Steel, 2)
                .input(stick, SteelMagnetic)
                .input(wireGtSingle, Copper, 4)
                .outputs(ELECTRIC_MOTOR_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtSingle, Copper, 2)
                .input(stick, Aluminium, 2)
                .input(stick, SteelMagnetic)
                .input(wireGtDouble, Cupronickel, 4)
                .outputs(ELECTRIC_MOTOR_MV.getStackForm())
                .duration(100).EUt(VA[MV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtDouble, Silver, 2)
                .input(stick, StainlessSteel, 2)
                .input(stick, SteelMagnetic)
                .input(wireGtDouble, Electrum, 4)
                .outputs(ELECTRIC_MOTOR_HV.getStackForm())
                .duration(100).EUt(VA[HV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtDouble, Aluminium, 2)
                .input(stick, Titanium, 2)
                .input(stick, NeodymiumMagnetic)
                .input(wireGtDouble, Kanthal, 4)
                .outputs(ELECTRIC_MOTOR_EV.getStackForm())
                .duration(100).EUt(VA[EV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtDouble, Tungsten, 2)
                .input(stick, TungstenSteel, 2)
                .input(stick, NeodymiumMagnetic)
                .input(wireGtDouble, Graphene, 4)
                .outputs(ELECTRIC_MOTOR_IV.getStackForm())
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(stickLong, SamariumMagnetic)
                .input(stickLong, HSSS, 2)
                .input(ring, HSSS, 2)
                .input(round, HSSS, 4)
                .input(wireFine, Ruridit, 64)
                .input(cableGtSingle, NiobiumTitanium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .fluidInputs(Lubricant.getFluid(250))
                .output(ELECTRIC_MOTOR_LuV)
                .duration(600).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(stickLong, SamariumMagnetic)
                .input(stickLong, Osmiridium, 4)
                .input(ring, Osmiridium, 4)
                .input(round, Osmiridium, 8)
                .input(wireFine, Europium, 64)
                .input(wireFine, Europium, 32)
                .input(cableGtSingle, VanadiumGallium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .output(ELECTRIC_MOTOR_ZPM)
                .duration(600).EUt(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(stickLong, SamariumMagnetic)
                .input(stickLong, Tritanium, 4)
                .input(ring, Tritanium, 4)
                .input(round, Tritanium, 8)
                .input(wireFine, Americium, 64)
                .input(wireFine, Americium, 64)
                .input(cableGtSingle, YttriumBariumCuprate, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .output(ELECTRIC_MOTOR_UV)
                .duration(600).EUt(100000).buildAndRegister();



        //Conveyors Start-----------------------------------------------------------------------------------------------
        final Map<String, Material> rubberMaterials = new HashMap<String, Material>() {{
            put("rubber", Rubber);
            put("silicone_rubber", SiliconeRubber);
            put("styrene_butadiene_rubber", StyreneButadieneRubber);
        }};

        for (Map.Entry<String, Material> materialEntry : rubberMaterials.entrySet()) {
            Material material = materialEntry.getValue();
            String name = materialEntry.getKey();

            COMPONENT_ASSEMBLER.recipeBuilder()
                    .input(cableGtSingle, Tin)
                    .input(plate, materialEntry.getValue(), 6)
                    .inputs(ELECTRIC_MOTOR_LV.getStackForm(2))
                    .circuitMeta(1)
                    .outputs(CONVEYOR_MODULE_LV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            COMPONENT_ASSEMBLER.recipeBuilder()
                    .input(cableGtSingle, Copper)
                    .input(plate, materialEntry.getValue(), 6)
                    .inputs(ELECTRIC_MOTOR_MV.getStackForm(2))
                    .circuitMeta(1)
                    .outputs(CONVEYOR_MODULE_MV.getStackForm())
                    .duration(100).EUt(VA[MV]).buildAndRegister();

            COMPONENT_ASSEMBLER.recipeBuilder()
                    .input(cableGtSingle, Gold)
                    .input(plate, materialEntry.getValue(), 6)
                    .inputs(ELECTRIC_MOTOR_HV.getStackForm(2))
                    .circuitMeta(1)
                    .outputs(CONVEYOR_MODULE_HV.getStackForm())
                    .duration(100).EUt(VA[HV]).buildAndRegister();

            COMPONENT_ASSEMBLER.recipeBuilder()
                    .input(cableGtSingle, Aluminium)
                    .input(plate, materialEntry.getValue(), 6)
                    .inputs(ELECTRIC_MOTOR_EV.getStackForm(2))
                    .circuitMeta(1)
                    .outputs(CONVEYOR_MODULE_EV.getStackForm())
                    .duration(100).EUt(VA[EV]).buildAndRegister();

            if (!materialEntry.getValue().equals(Rubber))
                COMPONENT_ASSEMBLER.recipeBuilder()
                        .input(cableGtSingle, Tungsten)
                        .input(plate, materialEntry.getValue(), 6)
                        .inputs(ELECTRIC_MOTOR_IV.getStackForm(2))
                        .circuitMeta(1)
                        .outputs(CONVEYOR_MODULE_IV.getStackForm())
                        .duration(100).EUt(VA[IV]).buildAndRegister();


            //Pumps Start---------------------------------------------------------------------------------------------------

            COMPONENT_ASSEMBLER.recipeBuilder()
                    .input(cableGtSingle, Tin)
                    .input(pipeNormalFluid, Bronze)
                    .input(screw, Tin)
                    .input(rotor, Tin)
                    .input(ring, materialEntry.getValue(), 2)
                    .inputs(ELECTRIC_MOTOR_LV.getStackForm())
                    .outputs(ELECTRIC_PUMP_LV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            COMPONENT_ASSEMBLER.recipeBuilder()
                    .input(cableGtSingle, Copper)
                    .input(pipeNormalFluid, Steel)
                    .input(screw, Bronze)
                    .input(rotor, Bronze)
                    .input(ring, materialEntry.getValue(), 2)
                    .inputs(ELECTRIC_MOTOR_MV.getStackForm())
                    .outputs(ELECTRIC_PUMP_MV.getStackForm())
                    .duration(100).EUt(VA[MV]).buildAndRegister();

            COMPONENT_ASSEMBLER.recipeBuilder()
                    .input(cableGtSingle, Gold)
                    .input(pipeNormalFluid, StainlessSteel)
                    .input(screw, Steel)
                    .input(rotor, Steel)
                    .input(ring, materialEntry.getValue(), 2)
                    .inputs(ELECTRIC_MOTOR_HV.getStackForm())
                    .outputs(ELECTRIC_PUMP_HV.getStackForm())
                    .duration(100).EUt(VA[HV]).buildAndRegister();

            COMPONENT_ASSEMBLER.recipeBuilder()
                    .input(cableGtSingle, Aluminium)
                    .input(pipeNormalFluid, Titanium)
                    .input(screw, StainlessSteel)
                    .input(rotor, StainlessSteel)
                    .input(ring, materialEntry.getValue(), 2)
                    .inputs(ELECTRIC_MOTOR_EV.getStackForm())
                    .outputs(ELECTRIC_PUMP_EV.getStackForm())
                    .duration(100).EUt(VA[EV]).buildAndRegister();

            if (!materialEntry.getValue().equals(Rubber))
                COMPONENT_ASSEMBLER.recipeBuilder()
                        .input(cableGtSingle, Tungsten)
                        .input(pipeNormalFluid, TungstenSteel)
                        .input(screw, TungstenSteel)
                        .input(rotor, TungstenSteel)
                        .input(ring, materialEntry.getValue(), 2)
                        .inputs(ELECTRIC_MOTOR_IV.getStackForm())
                        .outputs(ELECTRIC_PUMP_IV.getStackForm())
                        .duration(100).EUt(VA[IV]).buildAndRegister();
        }

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_LuV, 2)
                .input(plate, HSSS, 2)
                .input(ring, HSSS, 4)
                .input(round, HSSS, 16)
                .input(screw, HSSS, 4)
                .input(cableGtSingle, NiobiumTitanium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .fluidInputs(Lubricant.getFluid(250))
                .fluidInputs(StyreneButadieneRubber.getFluid(L * 8))
                .output(CONVEYOR_MODULE_LuV)
                .duration(600).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_ZPM, 2)
                .input(plate, Osmiridium, 2)
                .input(ring, Osmiridium, 4)
                .input(round, Osmiridium, 16)
                .input(screw, Osmiridium, 4)
                .input(cableGtSingle, VanadiumGallium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .fluidInputs(StyreneButadieneRubber.getFluid(L * 16))
                .output(CONVEYOR_MODULE_ZPM)
                .duration(600).EUt(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_UV, 2)
                .input(plate, Tritanium, 2)
                .input(ring, Tritanium, 4)
                .input(round, Tritanium, 16)
                .input(screw, Tritanium, 4)
                .input(cableGtSingle, YttriumBariumCuprate, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(StyreneButadieneRubber.getFluid(L * 24))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .output(CONVEYOR_MODULE_UV)
                .duration(600).EUt(100000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_LuV)
                .input(pipeSmallFluid, NiobiumTitanium)
                .input(plate, HSSS, 2)
                .input(screw, HSSS, 8)
                .input(ring, SiliconeRubber, 4)
                .input(rotor, HSSS)
                .input(cableGtSingle, NiobiumTitanium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .fluidInputs(Lubricant.getFluid(250))
                .output(ELECTRIC_PUMP_LuV)
                .duration(600).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_ZPM)
                .input(pipeNormalFluid, Polybenzimidazole)
                .input(plate, Osmiridium, 2)
                .input(screw, Osmiridium, 8)
                .input(ring, SiliconeRubber, 8)
                .input(rotor, Osmiridium)
                .input(cableGtSingle, VanadiumGallium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .output(ELECTRIC_PUMP_ZPM)
                .duration(600).EUt(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_UV)
                .input(pipeLargeFluid, Naquadah)
                .input(plate, Tritanium, 2)
                .input(screw, Tritanium, 8)
                .input(ring, SiliconeRubber, 16)
                .input(rotor, NaquadahAlloy)
                .input(cableGtSingle, YttriumBariumCuprate, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .output(ELECTRIC_PUMP_UV)
                .duration(600).EUt(100000).buildAndRegister();

        //Fluid Regulators----------------------------------------------------------------------------------------------

        COMPONENT_ASSEMBLER.recipeBuilder()
                .inputs(ELECTRIC_PUMP_LV.getStackForm())
                .input(circuit, Tier.LV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_LV.getStackForm())
                .EUt(VA[LV])
                .duration(400)
                .buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .inputs(ELECTRIC_PUMP_MV.getStackForm())
                .input(circuit, Tier.MV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_MV.getStackForm())
                .EUt(VA[MV])
                .duration(350)
                .buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .inputs(ELECTRIC_PUMP_HV.getStackForm())
                .input(circuit, Tier.HV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_HV.getStackForm())
                .EUt(VA[HV])
                .duration(300)
                .buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .inputs(ELECTRIC_PUMP_EV.getStackForm())
                .input(circuit, Tier.EV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_EV.getStackForm())
                .EUt(VA[EV])
                .duration(250)
                .buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .inputs(ELECTRIC_PUMP_IV.getStackForm())
                .input(circuit, Tier.IV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_IV.getStackForm())
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .inputs(ELECTRIC_PUMP_LuV.getStackForm())
                .input(circuit, Tier.LuV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_LUV.getStackForm())
                .EUt(VA[LuV])
                .duration(150)
                .buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .inputs(ELECTRIC_PUMP_ZPM.getStackForm())
                .input(circuit, Tier.ZPM, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_ZPM.getStackForm())
                .EUt(VA[ZPM])
                .duration(100)
                .buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .inputs(ELECTRIC_PUMP_UV.getStackForm())
                .input(circuit, Tier.UV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_UV.getStackForm())
                .EUt(VA[UV])
                .duration(50)
                .buildAndRegister();

        //Voiding Covers Start-----------------------------------------------------------------------------------------

        ModHandler.addShapedRecipe(true, "cover_item_voiding", COVER_ITEM_VOIDING.getStackForm(), "SDS", "dPw", " E ", 'S', new UnificationEntry(screw, Steel), 'D', COVER_ITEM_DETECTOR.getStackForm(), 'P', new UnificationEntry(pipeNormalItem, Brass), 'E', Items.ENDER_PEARL);

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(screw, Steel, 2)
                .inputs(COVER_ITEM_DETECTOR.getStackForm())
                .input(pipeNormalItem, Brass)
                .input(Items.ENDER_PEARL)
                .outputs(COVER_ITEM_VOIDING.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(COVER_ITEM_VOIDING)
                .input(circuit, Tier.MV, 1)
                .outputs(COVER_ITEM_VOIDING_ADVANCED.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "cover_fluid_voiding", COVER_FLUID_VOIDING.getStackForm(), "SDS", "dPw", " E ", 'S', new UnificationEntry(screw, Steel), 'D', COVER_FLUID_DETECTOR.getStackForm(), 'P', new UnificationEntry(pipeNormalFluid, Bronze), 'E', Items.ENDER_PEARL);

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(screw, Steel, 2)
                .inputs(COVER_FLUID_DETECTOR.getStackForm())
                .input(pipeNormalFluid, Bronze)
                .input(Items.ENDER_PEARL)
                .outputs(COVER_FLUID_VOIDING.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(COVER_FLUID_VOIDING)
                .input(circuit, Tier.MV, 1)
                .outputs(COVER_FLUID_VOIDING_ADVANCED.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        //Pistons Start-------------------------------------------------------------------------------------------------

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Steel, 2)
                .input(cableGtSingle, Tin, 2)
                .input(plate, Steel, 3)
                .input(gearSmall, Steel)
                .inputs(ELECTRIC_MOTOR_LV.getStackForm())
                .outputs(ELECTRIC_PISTON_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Aluminium, 2)
                .input(cableGtSingle, Copper, 2)
                .input(plate, Aluminium, 3)
                .input(gearSmall, Aluminium)
                .inputs(ELECTRIC_MOTOR_MV.getStackForm())
                .outputs(ELECTRIC_PISTON_MV.getStackForm())
                .duration(100).EUt(VA[MV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, StainlessSteel, 2)
                .input(cableGtSingle, Gold, 2)
                .input(plate, StainlessSteel, 3)
                .input(gearSmall, StainlessSteel)
                .inputs(ELECTRIC_MOTOR_HV.getStackForm())
                .outputs(ELECTRIC_PISTON_HV.getStackForm())
                .duration(100).EUt(VA[HV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Titanium, 2)
                .input(cableGtSingle, Aluminium, 2)
                .input(plate, Titanium, 3)
                .input(gearSmall, Titanium)
                .inputs(ELECTRIC_MOTOR_EV.getStackForm())
                .outputs(ELECTRIC_PISTON_EV.getStackForm())
                .duration(100).EUt(VA[EV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, TungstenSteel, 2)
                .input(cableGtSingle, Tungsten, 2)
                .input(plate, TungstenSteel, 3)
                .input(gearSmall, TungstenSteel)
                .inputs(ELECTRIC_MOTOR_IV.getStackForm())
                .outputs(ELECTRIC_PISTON_IV.getStackForm())
                .duration(100).EUt(VA[IV]).buildAndRegister();


        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_LuV)
                .input(plate, HSSS, 4)
                .input(ring, HSSS, 4)
                .input(round, HSSS, 16)
                .input(stick, HSSS, 4)
                .input(gear, HSSS)
                .input(gearSmall, HSSS, 2)
                .input(cableGtSingle, NiobiumTitanium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .fluidInputs(Lubricant.getFluid(250))
                .output(ELECTRIC_PISTON_LUV)
                .duration(600).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_ZPM)
                .input(plate, Osmiridium, 4)
                .input(ring, Osmiridium, 4)
                .input(round, Osmiridium, 16)
                .input(stick, Osmiridium, 4)
                .input(gear, Osmiridium)
                .input(gearSmall, Osmiridium, 2)
                .input(cableGtSingle, VanadiumGallium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .output(ELECTRIC_PISTON_ZPM)
                .duration(600).EUt(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(ELECTRIC_MOTOR_UV)
                .input(plate, Tritanium, 4)
                .input(ring, Tritanium, 4)
                .input(round, Tritanium, 16)
                .input(stick, Tritanium, 4)
                .input(gear, NaquadahAlloy)
                .input(gearSmall, NaquadahAlloy, 2)
                .input(cableGtSingle, YttriumBariumCuprate, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .output(ELECTRIC_PISTON_UV)
                .duration(600).EUt(100000).buildAndRegister();



        //Robot Arms Start ---------------------------------------------------------------------------------------------

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtSingle, Tin, 3)
                .input(stick, Steel, 2)
                .inputs(ELECTRIC_MOTOR_LV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_LV.getStackForm())
                .input(circuit, Tier.LV)
                .outputs(ROBOT_ARM_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtSingle, Copper, 3)
                .input(stick, Aluminium, 2)
                .inputs(ELECTRIC_MOTOR_MV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_MV.getStackForm())
                .input(circuit, Tier.MV)
                .outputs(ROBOT_ARM_MV.getStackForm())
                .duration(100).EUt(VA[MV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtSingle, Gold, 3)
                .input(stick, StainlessSteel, 2)
                .inputs(ELECTRIC_MOTOR_HV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_HV.getStackForm())
                .input(circuit, Tier.HV)
                .outputs(ROBOT_ARM_HV.getStackForm())
                .duration(100).EUt(VA[HV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtSingle, Aluminium, 3)
                .input(stick, Titanium, 2)
                .inputs(ELECTRIC_MOTOR_EV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_EV.getStackForm())
                .input(circuit, Tier.EV)
                .outputs(ROBOT_ARM_EV.getStackForm())
                .duration(100).EUt(VA[EV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(cableGtSingle, Tungsten, 3)
                .input(stick, TungstenSteel, 2)
                .inputs(ELECTRIC_MOTOR_IV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_IV.getStackForm())
                .input(circuit, Tier.IV)
                .outputs(ROBOT_ARM_IV.getStackForm())
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(stickLong, HSSS, 4)
                .input(gear, HSSS)
                .input(gearSmall, HSSS, 3)
                .input(ELECTRIC_MOTOR_LuV, 2)
                .input(ELECTRIC_PISTON_LUV)
                .input(circuit, Tier.LuV)
                .input(circuit, Tier.IV, 2)
                .input(circuit, Tier.EV, 4)
                .input(cableGtSingle, NiobiumTitanium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(250))
                .output(ROBOT_ARM_LuV)
                .duration(600).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(stickLong, Osmiridium, 4)
                .input(gear, Osmiridium)
                .input(gearSmall, Osmiridium, 3)
                .input(ELECTRIC_MOTOR_ZPM, 2)
                .input(ELECTRIC_PISTON_ZPM)
                .input(circuit, Tier.ZPM)
                .input(circuit, Tier.LuV, 2)
                .input(circuit, Tier.IV, 4)
                .input(cableGtSingle, VanadiumGallium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(Lubricant.getFluid(500))
                .output(ROBOT_ARM_ZPM)
                .duration(600).EUt(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(stickLong, Tritanium, 4)
                .input(gear, Tritanium)
                .input(gearSmall, Tritanium, 3)
                .input(ELECTRIC_MOTOR_UV, 2)
                .input(ELECTRIC_PISTON_UV)
                .input(circuit, Tier.UV)
                .input(circuit, Tier.ZPM, 2)
                .input(circuit, Tier.LuV, 4)
                .input(cableGtSingle, YttriumBariumCuprate, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 12))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .output(ROBOT_ARM_UV)
                .duration(600).EUt(100000).buildAndRegister();



        //Field Generators Start ---------------------------------------------------------------------------------------

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(gem, EnderPearl)
                .input(plate, Steel, 2)
                .input(circuit, Tier.LV, 2)
                .input(wireGtQuadruple, ManganesePhosphide, 4)
                .outputs(FIELD_GENERATOR_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(gem, EnderEye)
                .input(plate, Aluminium, 2)
                .input(circuit, Tier.MV, 2)
                .input(wireGtQuadruple, MagnesiumDiboride, 4)
                .outputs(FIELD_GENERATOR_MV.getStackForm())
                .duration(100).EUt(VA[MV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(QUANTUM_EYE)
                .input(plate, StainlessSteel, 2)
                .input(circuit, Tier.HV, 2)
                .input(wireGtQuadruple, MercuryBariumCalciumCuprate, 4)
                .outputs(FIELD_GENERATOR_HV.getStackForm())
                .duration(100).EUt(VA[HV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(gem, NetherStar)
                .input(plateDouble, Titanium, 2)
                .input(circuit, Tier.EV, 2)
                .input(wireGtQuadruple, UraniumTriplatinum, 4)
                .outputs(FIELD_GENERATOR_EV.getStackForm())
                .duration(100).EUt(VA[EV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(QUANTUM_STAR)
                .input(plateDouble, TungstenSteel, 2)
                .input(circuit, Tier.IV, 2)
                .input(wireGtQuadruple, SamariumIronArsenicOxide, 4)
                .outputs(FIELD_GENERATOR_IV.getStackForm())
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, HSSS)
                .input(plate, HSSS, 6)
                .input(QUANTUM_STAR)
                .input(EMITTER_LuV, 2)
                .input(circuit, Tier.LuV, 2)
                .input(wireFine, IndiumTinBariumTitaniumCuprate, 64)
                .input(wireFine, IndiumTinBariumTitaniumCuprate, 64)
                .input(cableGtSingle, NiobiumTitanium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .output(FIELD_GENERATOR_LuV)
                .duration(600).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, NaquadahAlloy)
                .input(plate, NaquadahAlloy, 6)
                .input(QUANTUM_STAR)
                .input(EMITTER_ZPM, 2)
                .input(circuit, Tier.ZPM, 2)
                .input(wireFine, UraniumRhodiumDinaquadide, 64)
                .input(wireFine, UraniumRhodiumDinaquadide, 64)
                .input(cableGtSingle, VanadiumGallium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .output(FIELD_GENERATOR_ZPM)
                .duration(600).EUt(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, Tritanium)
                .input(plate, Tritanium, 6)
                .input(GRAVI_STAR)
                .input(EMITTER_UV, 2)
                .input(circuit, Tier.UV, 2)
                .input(wireFine, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .input(wireFine, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .input(cableGtSingle, YttriumBariumCuprate, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 12))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .output(FIELD_GENERATOR_UV)
                .duration(600).EUt(100000).buildAndRegister();



        //Sensors Start-------------------------------------------------------------------------------------------------

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Brass)
                .input(plate, Steel, 4)
                .input(circuit, Tier.LV)
                .input(gem, Quartzite)
                .outputs(SENSOR_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Electrum)
                .input(plate, Aluminium, 4)
                .input(circuit, Tier.MV)
                .input(gemFlawless, Emerald)
                .outputs(SENSOR_MV.getStackForm())
                .duration(100).EUt(VA[MV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Chrome)
                .input(plate, StainlessSteel, 4)
                .input(circuit, Tier.HV)
                .input(gem, EnderEye)
                .outputs(SENSOR_HV.getStackForm())
                .duration(100).EUt(VA[HV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Platinum)
                .input(plate, Titanium, 4)
                .input(circuit, Tier.EV)
                .input(QUANTUM_EYE)
                .outputs(SENSOR_EV.getStackForm())
                .duration(100).EUt(VA[EV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Iridium)
                .input(plate, TungstenSteel, 4)
                .input(circuit, Tier.IV)
                .input(QUANTUM_STAR)
                .outputs(SENSOR_IV.getStackForm())
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, HSSS)
                .input(ELECTRIC_MOTOR_LuV)
                .input(plate, Ruridit, 4)
                .input(QUANTUM_STAR)
                .input(circuit, Tier.LuV, 2)
                .input(foil, Palladium, 64)
                .input(foil, Palladium, 32)
                .input(cableGtSingle, NiobiumTitanium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .output(SENSOR_LuV)
                .duration(600).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, NaquadahAlloy)
                .input(ELECTRIC_MOTOR_ZPM)
                .input(plate, Osmiridium, 4)
                .input(QUANTUM_STAR, 2)
                .input(circuit, Tier.ZPM, 2)
                .input(foil, Trinium, 64)
                .input(foil, Trinium, 32)
                .input(cableGtSingle, VanadiumGallium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .output(SENSOR_ZPM)
                .duration(600).EUt(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, Tritanium)
                .input(ELECTRIC_MOTOR_UV)
                .input(plate, Tritanium, 4)
                .input(GRAVI_STAR)
                .input(circuit, Tier.UV, 2)
                .input(foil, Naquadria, 64)
                .input(foil, Naquadria, 32)
                .input(cableGtSingle, YttriumBariumCuprate, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .output(SENSOR_UV)
                .duration(600).EUt(100000).buildAndRegister();


        //Emitters Start------------------------------------------------------------------------------------------------

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Brass, 4)
                .input(cableGtSingle, Tin, 2)
                .input(circuit, Tier.LV, 2)
                .input(gem, Quartzite)
                .circuitMeta(1)
                .outputs(EMITTER_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Electrum, 4)
                .input(cableGtSingle, Copper, 2)
                .input(circuit, Tier.MV, 2)
                .input(gemFlawless, Emerald)
                .circuitMeta(1)
                .outputs(EMITTER_MV.getStackForm())
                .duration(100).EUt(VA[MV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Chrome, 4)
                .input(cableGtSingle, Gold, 2)
                .input(circuit, Tier.HV, 2)
                .input(gem, EnderEye)
                .circuitMeta(1)
                .outputs(EMITTER_HV.getStackForm())
                .duration(100).EUt(VA[HV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Platinum, 4)
                .input(cableGtSingle, Aluminium, 2)
                .input(circuit, Tier.EV, 2)
                .input(QUANTUM_EYE)
                .circuitMeta(1)
                .outputs(EMITTER_EV.getStackForm())
                .duration(100).EUt(VA[EV]).buildAndRegister();

        COMPONENT_ASSEMBLER.recipeBuilder()
                .input(stick, Iridium, 4)
                .input(cableGtSingle, Tungsten, 2)
                .input(circuit, Tier.IV, 2)
                .input(QUANTUM_STAR)
                .circuitMeta(1)
                .outputs(EMITTER_IV.getStackForm())
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, HSSS)
                .input(ELECTRIC_MOTOR_LuV)
                .input(stickLong, Ruridit, 4)
                .input(QUANTUM_STAR)
                .input(circuit, Tier.LuV, 2)
                .input(foil, Palladium, 64)
                .input(foil, Palladium, 32)
                .input(cableGtSingle, NiobiumTitanium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .output(EMITTER_LuV)
                .duration(600).EUt(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, NaquadahAlloy)
                .input(ELECTRIC_MOTOR_ZPM)
                .input(stickLong, Osmiridium, 4)
                .input(QUANTUM_STAR, 2)
                .input(circuit, Tier.ZPM, 2)
                .input(foil, Trinium, 64)
                .input(foil, Trinium, 32)
                .input(cableGtSingle, VanadiumGallium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .output(EMITTER_ZPM)
                .duration(600).EUt(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, Tritanium)
                .input(ELECTRIC_MOTOR_UV)
                .input(stickLong, Tritanium, 4)
                .input(GRAVI_STAR)
                .input(circuit, Tier.UV, 2)
                .input(foil, Naquadria, 64)
                .input(foil, Naquadria, 32)
                .input(cableGtSingle, YttriumBariumCuprate, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .output(EMITTER_UV)
                .duration(600).EUt(100000).buildAndRegister();
    }
}
