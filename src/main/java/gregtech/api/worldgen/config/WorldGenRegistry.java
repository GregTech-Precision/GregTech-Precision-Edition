package gregtech.api.worldgen.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.util.FileUtility;
import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class WorldGenRegistry {

    public static final WorldGenRegistry INSTANCE = new WorldGenRegistry();

    private static final int FLUID_VEIN_VERSION = 2;
    private static final int ORE_VEIN_VERSION = 2;

    private WorldGenRegistry() {
    }

    private final Map<Integer, String> namedDimensions = new HashMap<>();

    private final List<BedrockFluidDepositDefinition> registeredBedrockVeinDefinitions = new ArrayList<>();
    private final List<BedrockFluidDepositDefinition> addonRegisteredBedrockVeinDefinitions = new ArrayList<>();
    private List<BedrockFluidDepositDefinition> removedBedrockVeinDefinitions = new ArrayList<>();
    private final List<BedrockOreDepositDefinition> registeredBedrockOreVeinDefinitions = new ArrayList<>();
    private final List<BedrockOreDepositDefinition> addonRegisteredBedrockOreVeinDefinitions = new ArrayList<>();
    private List<BedrockOreDepositDefinition> removedBedrockOreVeinDefinitions = new ArrayList<>();

    public void initializeRegistry() {
        GTLog.logger.info("Initializing ore generation registry...");
        try {
            reinitializeRegisteredVeins();
        } catch (IOException | RuntimeException exception) {
            GTLog.logger.fatal("Failed to initialize worldgen registry.", exception);
        }
    }

    /**
     * Handles the setup of ore generation files in the config folder.
     * Either creates the default files and reads them, or reads any modified files made by users
     *
     * After reading all json worldgen files in the folder, they are initialized, creating vein definitions
     *
     * @throws IOException
     */
    public void reinitializeRegisteredVeins() throws IOException {
        GTLog.logger.info("Reloading ore generation files from config...");
        registeredBedrockVeinDefinitions.clear();
        Path configPath = Loader.instance().getConfigDir().toPath().resolve(GTValues.MODID);
        // The Path for the file used to name dimensions for the JEI ore gen page
        Path dimensionsFile = configPath.resolve("dimensions.json");
        // The folder where worldgen definitions are stored
        Path worldgenRootPath = configPath.resolve("worldgen");
        // Lock file used to determine if the worldgen files need to be regenerated
        // This is used to ensure modifications to ore gen in modpacks are not overwritten
        Path jarFileExtractLock = configPath.resolve("worldgen_extracted.json");
        if (!Files.exists(worldgenRootPath))
            Files.createDirectories(worldgenRootPath);

        // Remove the old extract lock file if it exists to remove clutter
        Path jarFileExtractLockOLD = configPath.resolve("worldgen_extracted.txt");
        Files.deleteIfExists(jarFileExtractLockOLD);

        // The folder where all physical veins are stored
        Path veinPath = worldgenRootPath.resolve("vein");
        if (!Files.exists(veinPath))
            Files.createDirectories(veinPath);

        // The folder where all bedrock fluid veins are stored
        Path bedrockVeinPath = worldgenRootPath.resolve("fluid");
        if (!Files.exists(bedrockVeinPath))
            Files.createDirectories(bedrockVeinPath);

        // Checks if the dimension file exists. If not, creates the file and extracts the defaults from the mod jar
        if (!Files.exists(dimensionsFile)) {
            Files.createFile(dimensionsFile);
            extractJarVeinDefinitions(configPath, dimensionsFile);
        }

        if (Files.exists(jarFileExtractLock)) {
            JsonObject extractLock = FileUtility.tryExtractFromFile(jarFileExtractLock);
            if (extractLock != null) {
                boolean needsUpdate = false;
                if (extractLock.get("fluidVersion").getAsInt() != FLUID_VEIN_VERSION) {
                    extractJarVeinDefinitions(configPath, bedrockVeinPath);
                    needsUpdate = true;
                }
                if (extractLock.get("veinVersion").getAsInt() != ORE_VEIN_VERSION) {
                    extractJarVeinDefinitions(configPath, veinPath);
                    needsUpdate = true;
                }
                // bump the version(s) on the lock file if needed
                if (needsUpdate) {
                    extractJarVeinDefinitions(configPath, jarFileExtractLock);
                }
            }
        } else {
            // force an override here as needed for updating legacy config blocks
            if (FLUID_VEIN_VERSION > 1) {
                extractJarVeinDefinitions(configPath, bedrockVeinPath);
            }
            if (ORE_VEIN_VERSION > 1) {
                extractJarVeinDefinitions(configPath, veinPath);
            }
            // create extraction lock since it doesn't exist
            Files.createFile(jarFileExtractLock);
            extractJarVeinDefinitions(configPath, jarFileExtractLock);
        }

        //attempt extraction if worldgen root directory is empty
        if (!Files.list(worldgenRootPath.resolve(veinPath)).findFirst().isPresent()) {
            extractJarVeinDefinitions(configPath, veinPath);
        }
        if (!Files.list(worldgenRootPath.resolve(bedrockVeinPath)).findFirst().isPresent()) {
            extractJarVeinDefinitions(configPath, bedrockVeinPath);
        }

        // Read the dimensions name from the dimensions file
        gatherNamedDimensions(dimensionsFile);

        // Will always fail when called from initializeRegistry
        // Placed here to delete the file before being gathered and having its definition initialized
        if(!removedBedrockVeinDefinitions.isEmpty()) {
            removeExistingFiles(bedrockVeinPath, removedBedrockVeinDefinitions);
        }
        if(!removedBedrockOreVeinDefinitions.isEmpty()){
            removeExistingFiles(veinPath, removedBedrockOreVeinDefinitions);
        }

        // Gather the worldgen vein files from the various folders in the config
        List<Path> veinFiles = Files.walk(veinPath)
                .filter(path -> path.toString().endsWith(".json"))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        for (Path worldgenDefinition : veinFiles) {

            // Tries to extract the json worldgen definition from the file
            JsonObject element = FileUtility.tryExtractFromFile(worldgenDefinition);
            if (element == null) {
                break;
            }

            String depositName = veinPath.relativize(worldgenDefinition).toString();

            try {
                BedrockOreDepositDefinition deposit = new BedrockOreDepositDefinition(depositName);

                if (deposit.initializeFromConfig(element)) {
                    registeredBedrockOreVeinDefinitions.add(deposit);
                }
            } catch (RuntimeException exception){
                GTLog.logger.error("Failed to parse worldgen definition {} on path {}", depositName, worldgenDefinition, exception);
            }

        }

        // Gather the worldgen vein files from the various folders in the config
        List<Path> bedrockFluidVeinFiles = Files.walk(bedrockVeinPath)
                .filter(path -> path.toString().endsWith(".json"))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        for (Path worldgenDefinition : bedrockFluidVeinFiles) {

            // Tries to extract the json worldgen definition from the file
            JsonObject element = FileUtility.tryExtractFromFile(worldgenDefinition);
            if (element == null) {
                break;
            }

            // Finds the file name to create the Definition with
            String depositName = bedrockVeinPath.relativize(worldgenDefinition).toString();

            try {
                // Creates the deposit definition and initializes various components based on the json entries in the file
                BedrockFluidDepositDefinition deposit = new BedrockFluidDepositDefinition(depositName);
                // Adds the registered definition to the list of all registered definitions
                if (deposit.initializeFromConfig(element)) {
                    registeredBedrockVeinDefinitions.add(deposit);
                }
            } catch (RuntimeException exception) {
                GTLog.logger.error("Failed to parse worldgen definition {} on path {}", depositName, worldgenDefinition, exception);
            }
        }

        addAddonFiles(worldgenRootPath, addonRegisteredBedrockVeinDefinitions, registeredBedrockVeinDefinitions);

        GTLog.logger.info("Loaded {} bedrock worldgen definitions", registeredBedrockVeinDefinitions.size());
        GTLog.logger.info("Loaded {} bedrock worldgen definitions from addon mods", addonRegisteredBedrockVeinDefinitions.size());
    }

    /**
     * Extracts files from the Gregtech jar and places them in the specified location
     *
     * @param configPath The path of the config root for the Gregtech mod
     * @param targetPath The path of the target location where the files will be initialized
     * @throws IOException
     */
    private static void extractJarVeinDefinitions(Path configPath, Path targetPath) throws IOException {
        // The path of the worldgen folder in the config folder
        Path worldgenRootPath = configPath.resolve("worldgen");
        // The path of the physical vein folder in the config folder
        Path oreVeinRootPath = worldgenRootPath.resolve("vein");
        // The path of the bedrock fluid vein folder in the config folder
        Path bedrockFluidVeinRootPath = worldgenRootPath.resolve("fluid");
        // The path of the named dimensions file in the config folder
        Path dimensionsRootPath = configPath.resolve("dimensions.json");
        // THe path of the lock file in the config folder
        Path extractLockPath = configPath.resolve("worldgen_extracted.json");
        FileSystem zipFileSystem = null;
        try {
            URI sampleUri = WorldGenRegistry.class.getResource("/assets/gregtech/.gtassetsroot").toURI();
            // The Path for representing the worldgen folder in the assets folder in the Gregtech resources folder in the jar
            Path worldgenJarRootPath;
            // The Path for representing the vein folder in the vein folder in the assets folder in the Gregtech resources folder in the jar
            Path oreVeinJarRootPath;
            // The Path for representing the fluid folder in the vein folder in the assets folder in the Gregtech resources folder in the jar
            Path bedrockFluidJarRootPath;
            if (sampleUri.getScheme().equals("jar") || sampleUri.getScheme().equals("zip")) {
                zipFileSystem = FileSystems.newFileSystem(sampleUri, Collections.emptyMap());
                worldgenJarRootPath = zipFileSystem.getPath("/assets/gregtech/worldgen");
                oreVeinJarRootPath = zipFileSystem.getPath("/assets/gregtech/worldgen/vein");
                bedrockFluidJarRootPath = zipFileSystem.getPath("/assets/gregtech/worldgen/fluid");
            } else if (sampleUri.getScheme().equals("file")) {
                worldgenJarRootPath = Paths.get(WorldGenRegistry.class.getResource("/assets/gregtech/worldgen").toURI());
                oreVeinJarRootPath = Paths.get(WorldGenRegistry.class.getResource("/assets/gregtech/worldgen/vein").toURI());
                bedrockFluidJarRootPath = Paths.get(WorldGenRegistry.class.getResource("/assets/gregtech/worldgen/fluid").toURI());
            } else {
                throw new IllegalStateException("Unable to locate absolute path to worldgen root directory: " + sampleUri);
            }

            // Attempts to extract the worldgen definition jsons
            if (targetPath.compareTo(oreVeinRootPath) == 0) {
                GTLog.logger.info("Attempting extraction of standard worldgen definitions from {} to {}",
                        oreVeinJarRootPath, oreVeinRootPath);
                // Find all the default worldgen files in the assets folder
                List<Path> jarFiles = Files.walk(oreVeinJarRootPath)
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());

                // Replaces or creates the default worldgen files
                for (Path jarFile : jarFiles) {
                    Path worldgenPath = oreVeinRootPath.resolve(oreVeinJarRootPath.relativize(jarFile).toString());
                    Files.createDirectories(worldgenPath.getParent());
                    Files.copy(jarFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);
                }
                GTLog.logger.info("Extracted {} builtin worldgen vein definitions into vein folder", jarFiles.size());
            } else
            if (targetPath.compareTo(bedrockFluidVeinRootPath) == 0) {
                GTLog.logger.info("Attempting extraction of standard worldgen definitions from {} to {}",
                        bedrockFluidJarRootPath, bedrockFluidVeinRootPath);
                // Find all the default worldgen files in the assets folder
                List<Path> jarFiles = Files.walk(bedrockFluidJarRootPath)
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());

                // Replaces or creates the default worldgen files
                for (Path jarFile : jarFiles) {
                    Path worldgenPath = bedrockFluidVeinRootPath.resolve(bedrockFluidJarRootPath.relativize(jarFile).toString());
                    Files.createDirectories(worldgenPath.getParent());
                    Files.copy(jarFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);
                }
                GTLog.logger.info("Extracted {} builtin worldgen bedrock fluid definitions into fluid folder", jarFiles.size());
            }
            // Attempts to extract the named dimensions json folder
            else if (targetPath.compareTo(dimensionsRootPath) == 0) {
                GTLog.logger.info("Attempting extraction of standard dimension definitions from {} to {}",
                        worldgenJarRootPath, dimensionsRootPath);

                Path dimensionFile = worldgenJarRootPath.resolve("dimensions.json");

                Path worldgenPath = dimensionsRootPath.resolve(worldgenJarRootPath.relativize(worldgenJarRootPath).toString());
                Files.copy(dimensionFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);

                GTLog.logger.info("Extracted builtin dimension definitions into worldgen folder");
            }
            // Attempts to extract lock txt file
            else if (targetPath.compareTo(extractLockPath) == 0) {
                Path extractLockFile = worldgenJarRootPath.resolve("worldgen_extracted.json");

                Path worldgenPath = extractLockPath.resolve(worldgenJarRootPath.relativize(worldgenJarRootPath).toString());
                Files.copy(extractLockFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);

                GTLog.logger.info("Extracted jar lock file into worldgen folder");
            }

        } catch (URISyntaxException impossible) {
            //this is impossible, since getResource always returns valid URI
            throw new RuntimeException(impossible);
        } finally {
            if (zipFileSystem != null) {
                //close zip file system to avoid issues
                IOUtils.closeQuietly(zipFileSystem);
            }
        }
    }

    private void removeExistingFiles(Path root, @Nonnull List<? extends IWorldgenDefinition> definitions){
        for(IWorldgenDefinition definition : definitions) {
            Path filePath = root.resolve(Paths.get(definition.getDepositName()));

            try {
                if(Files.exists(filePath)) {
                    Files.delete(filePath);
                    GTLog.logger.info("Removed oregen file at {}", definition.getDepositName());
                }
            }
            catch (IOException exception) {
                GTLog.logger.error("Failed to remove oregen file at {}", definition.getDepositName());
            }
        }
    }

    private <T extends IWorldgenDefinition> void addAddonFiles(Path root, @Nonnull List<T> definitions, @Nonnull List<T> registeredDefinitions){
        for(IWorldgenDefinition definition : definitions) {

            JsonObject element = FileUtility.tryExtractFromFile(root.resolve(definition.getDepositName()));

            if(element == null) {
                GTLog.logger.error("Addon mod tried to register bad ore definition at {}", definition.getDepositName());
                definitions.remove(definition);
                continue;
            }

            try {
                definition.initializeFromConfig(element);
                registeredDefinitions.add((T) definition);
            }
            catch (RuntimeException exception) {
                GTLog.logger.error("Failed to parse addon worldgen definition {}", definition.getDepositName(), exception);
            }
        }
    }

    /**
     * Gathers the designated named dimensions from the designated json file
     *
     * @param dimensionsFile The Path to the dimensions.json file
     */
    private void gatherNamedDimensions(Path dimensionsFile) {
        JsonObject element = FileUtility.tryExtractFromFile(dimensionsFile);
        if (element == null) {
            return;
        }

        try {
            JsonArray dims = element.getAsJsonArray("dims");
            for (JsonElement dim : dims) {
                namedDimensions.put(dim.getAsJsonObject().get("dimID").getAsInt(), dim.getAsJsonObject().get("dimName").getAsString());
            }
        } catch (RuntimeException exception) {
            GTLog.logger.error("Failed to parse named dimensions", exception);
        }
    }

    /**
     * Called to remove veins from the list of registered vein definitions
     * Can fail if called on default veins when the veins have been modified by modpack makers
     *
     * After removing all desired veins, call {@link WorldGenRegistry#reinitializeRegisteredVeins()} to delete the existing files
     *
     *
     */
    @SuppressWarnings("unused")
    public void removeVeinDefinitions(IWorldgenDefinition definition) {
        if (definition instanceof BedrockFluidDepositDefinition) {
            if (registeredBedrockVeinDefinitions.contains(definition)) {
                registeredBedrockVeinDefinitions.remove(definition);
                removedBedrockVeinDefinitions.add((BedrockFluidDepositDefinition) definition);
            } else {
                GTLog.logger.error("Failed to remove BedrockFluidDepositDefinition at {}. Deposit was not in list of registered veins.", definition.getDepositName());
            }
        } else if(definition instanceof BedrockOreDepositDefinition) {
            if (registeredBedrockOreVeinDefinitions.contains(definition)) {
                registeredBedrockOreVeinDefinitions.remove(definition);
                removedBedrockOreVeinDefinitions.add((BedrockOreDepositDefinition) definition);
            } else {
                GTLog.logger.error("Failed to remove BedrockOreDepositDefinition at {}. Deposit was not in list of registered veins.", definition.getDepositName());
            }
        }
    }

    /**
     * Adds the provided BedrockFluidDepositDefinition to the list and Map of registered definitions
     * Will not create an entry if a file already exists for the provided definition
     *
     * After adding all veins, call {@link WorldGenRegistry#reinitializeRegisteredVeins()} to initialize the new veins
     * Or, register veins before {@link WorldGenRegistry#initializeRegistry()} is called, and the veins will be loaded with the
     * default veins
     *
     * @param definition The BedrockFluidDepositDefinition to add to the list of registered veins
     */
    @SuppressWarnings("unused")
    public void addVeinDefinitions(BedrockFluidDepositDefinition definition) {
        if(!addonRegisteredBedrockVeinDefinitions.contains(definition)) {
            addonRegisteredBedrockVeinDefinitions.add(definition);
        }
        else {
            GTLog.logger.error("Failed to add bedrock fluid deposit definition at {}. Definition already exists", definition.getDepositName());
        }
    }

    public static List<BedrockFluidDepositDefinition> getBedrockVeinDeposits() {
        return Collections.unmodifiableList(INSTANCE.registeredBedrockVeinDefinitions);
    }

    public static List<BedrockOreDepositDefinition> getBedrockOreVeinDeposit() {
        return Collections.unmodifiableList(INSTANCE.registeredBedrockOreVeinDefinitions);
    }

    public static Map<Integer, String> getNamedDimensions() {
        return INSTANCE.namedDimensions;
    }
}
