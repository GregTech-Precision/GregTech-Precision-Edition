package gregtech.api.net.packets;

import io.netty.buffer.Unpooled;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class PacketProspecting {
    public int chunkX;
    public int chunkZ;
    public int posX;
    public int posZ;
    public int mode;
    public HashMap<Byte, String> map;
    public Set<String> ores;

    public PacketProspecting(int chunkX, int chunkZ, int posX, int posZ, int mode) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.posX = posX;
        this.posZ = posZ;
        this.mode = mode;
        this.map = new HashMap<>();

        ores = new HashSet<>();
    }

    public static PacketProspecting readPacketData(PacketBuffer buffer) {
        PacketProspecting packet = new PacketProspecting(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
        int checkOut = 0;
        byte kSize = buffer.readByte();
        packet.map = new HashMap<>();
        for (int k = 0; k < kSize; k++) {
            byte y = buffer.readByte();
            String name = buffer.readString(1000);
            packet.map.put(y, name);
            if (y == 1)
                packet.ores.add(name);
            checkOut++;
        }
        int checkOut2 = buffer.readInt();
        if (checkOut != checkOut2) {
            return null;
        }
        return packet;
    }


    public static PacketProspecting readPacketData(NBTTagCompound nbt) {
        if (nbt.hasKey("buffer")) {
            return PacketProspecting.readPacketData(new PacketBuffer(Unpooled.wrappedBuffer(nbt.getByteArray("buffer"))));
        }
        return null;
    }

    public NBTTagCompound writePacketData() {
        NBTTagCompound nbt = new NBTTagCompound();
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        writePacketData(buffer);
        byte[] bytes = buffer.array();
        nbt.setByteArray("buffer", bytes);
        return nbt;
    }

    public void writePacketData(PacketBuffer buffer) {
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeInt(posX);
        buffer.writeInt(posZ);
        buffer.writeInt(mode);
        int checkOut = 0;
        if (map.isEmpty())
            buffer.writeByte(0);
        else {
            buffer.writeByte(map.keySet().size());
            for (byte key : map.keySet()) {
                buffer.writeByte(key);
                buffer.writeString(map.get(key));
                checkOut++;
            }
        }
        buffer.writeInt(checkOut);
    }

    public void addInfo(int info, String orePrefix) {
            map.put((byte) info, orePrefix);
            if (info == 1) {
                ores.add(orePrefix);
            }
        }
    }

