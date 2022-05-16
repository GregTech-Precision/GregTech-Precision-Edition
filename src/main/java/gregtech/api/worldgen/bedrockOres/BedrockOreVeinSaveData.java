package gregtech.api.worldgen.bedrockOres;

import com.google.common.collect.Table;
import gregtech.api.GTValues;
import gregtech.api.worldgen.bedrockFluids.ChunkPosDimension;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

public class BedrockOreVeinSaveData extends WorldSavedData {

    private static BedrockOreVeinSaveData INSTANCE;
    public static final String dataName = GTValues.MODID + ".bedrockOreVeinData";

    public BedrockOreVeinSaveData(String s) {
        super(s);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList veinList = nbt.getTagList("oreVeinInfo", 10);
        BedrockOreVeinHandler.veinCache.clear();
        for (int i = 0; i < veinList.tagCount(); i++) {
            NBTTagCompound tag = veinList.getCompoundTagAt(i);
            ChunkPosDimension coords = ChunkPosDimension.readFromNBT(tag);
            int layer = tag.getInteger("layer");
            if (coords != null) {
                BedrockOreVeinHandler.OreVeinWorldEntry info = BedrockOreVeinHandler.OreVeinWorldEntry.readFromNBT(tag.getCompoundTag("oreInfo"));
                BedrockOreVeinHandler.veinCache.put(coords, layer, info);
            }
        }
    }

    @Override
    public @Nonnull
    NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList veinList = new NBTTagList();
        for (Table.Cell<ChunkPosDimension, Integer, BedrockOreVeinHandler.OreVeinWorldEntry> e : BedrockOreVeinHandler.veinCache.cellSet()) {
            if (e.getRowKey() != null && e.getColumnKey() != null && e.getValue() != null) {
                NBTTagCompound tag = e.getRowKey().writeToNBT();
                tag.setTag("oreInfo", e.getValue().writeToNBT());
                tag.setInteger("layer", e.getColumnKey());
                veinList.appendTag(tag);
            }
        }
        nbt.setTag("oreVeinInfo", veinList);

        return nbt;
    }


    public static void setDirty() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && INSTANCE != null)
            INSTANCE.markDirty();
    }

    public static void setInstance(BedrockOreVeinSaveData in) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
            INSTANCE = in;
    }
}
