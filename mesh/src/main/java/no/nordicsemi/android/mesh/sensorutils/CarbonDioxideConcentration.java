package no.nordicsemi.android.mesh.sensorutils;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.unsignedBytesToInt;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * The CO2 Concentration characteristic is used to represent a measure of carbon dioxide concentration.
 */
public class CarbonDioxideConcentration extends DevicePropertyCharacteristic<Integer> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public CarbonDioxideConcentration(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        int bits = unsignedBytesToInt(data[offset], data[offset + 1]);
        if (bits == 0xFFFF) this.value = null;
        else this.value = bits;
    }

    /**
     * CO2 Concentration characteristic
     *
     * @param concentration CO2 Concentration in PPM.
     */
    public CarbonDioxideConcentration(final int concentration) {
        this.value = concentration;
    }

    @NonNull
    @Override
    public String toString() {
        return this.value + " PPM";
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xFF};
        if (this.value != null) {
            int bits = this.value;
            for (int n = 0; n < 2; n++) {
                bytes[n] = (byte) ((bits >> (n * 8)) & 0xFF);
            }
        }
        return bytes;
    }
}