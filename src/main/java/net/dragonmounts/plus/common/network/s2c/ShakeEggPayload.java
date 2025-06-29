package net.dragonmounts.plus.common.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static net.dragonmounts.plus.common.DragonMountsShared.makeId;

public record ShakeEggPayload(int id, int amplitude, int axis, int flag) implements CustomPacketPayload {
    public static final Type<ShakeEggPayload> TYPE = new Type<>(makeId("shake_egg"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShakeEggPayload> CODEC =
            CustomPacketPayload.codec(ShakeEggPayload::encode, ShakeEggPayload::decode);

    public static ShakeEggPayload decode(FriendlyByteBuf buffer) {
        return new ShakeEggPayload(buffer.readVarInt(), buffer.readByte(), Byte.toUnsignedInt(buffer.readByte()), buffer.readByte());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeByte(this.amplitude).writeByte(this.axis).writeByte(this.flag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
