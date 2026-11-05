package me.rin.redsignalanticheat.event.packetrecive.packettype;

import java.lang.reflect.Field;

public class PacketTypeRecognizer {

    public static PacketType getPacketType(Object nmsPacket) {
        if (nmsPacket == null) {
            return PacketType.OTHER;
        }
        String className = nmsPacket.getClass().getName();
        className = className.split("\\.")[className.split("\\.").length - 1].split("\\$")[0];
        switch (className) {
            case "PacketPlayInFlying":
            case "ServerboundMovePlayerPacket":
                return PacketType.FLYING;
            case "PacketPlayInArmAnimation":
            case "ServerboundSwingPacket":
            case "ServerboundAnimatePacket":
                return PacketType.ARM_ANIMATION;
            case "PacketPlayInBlockDig":
            case "ServerboundPlayerActionPacket":
                return PacketType.BLOCK_DIG;
            case "PacketPlayInSteerVehicle":
            case "ServerboundPlayerInputPacket":
                return PacketType.STEER_VEHICLE;
            case "PacketPlayInSetCreativeSlot":
            case "ServerboundSetCreativeModeSlotPacket":
                return PacketType.SET_CREATIVE_SLOT;
            case "PacketPlayInSettings":
            case "ServerboundClientInformationPacket":
                return PacketType.CLIENT_INFORMATION;
            case "PacketPlayInKeepAlive":
            case "ServerboundKeepAlivePacket":
                return PacketType.ALIVE;
            case "PacketPlayInUseEntity":
            case "ServerboundInteractPacket":
                return PacketType.USE_ENTITY;
            default:
                return PacketType.OTHER;
        }
    }

    public static int getEntityId(Object nmsPacket) {
        if (getPacketType(nmsPacket) != PacketType.USE_ENTITY)
            return 0;
        try {
            Field f = nmsPacket.getClass().getDeclaredField("entityId");
            f.setAccessible(true);
            return f.getInt(nmsPacket);
        } catch (Exception ignored) {
        }
        for (Field field : nmsPacket.getClass().getDeclaredFields()) {
            if (field.getType() == int.class || field.getType() == Integer.class) {
                try {
                    field.setAccessible(true);
                    int value = field.getInt(nmsPacket);
                    if (value != 0) {
                        return value;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return 0;
    }

}
