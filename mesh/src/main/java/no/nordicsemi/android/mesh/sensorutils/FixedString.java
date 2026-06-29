package no.nordicsemi.android.mesh.sensorutils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * The Fixed String characteristic represents an 8, 16, 24, 36 or a 64-octet UTF-8 string.
 */
public class FixedString extends DevicePropertyCharacteristic<String> {
    final int length;

    @SuppressWarnings("CharsetObjectCanBeUsed")
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public FixedString(@NonNull final byte[] data, final int offset, final int length) {
        // Calling super would throw in case of length mismatch, so we do not call it.
        // Instead we just log a warning and continue since this usually still works.
        if (length > data.length) {
            Log.w("FixedString", "Reported length=" + length + " but actual length=" + data.length + " - trying to parse anyways.");
            this.length = data.length;
        } else {
            this.length = length;
        }

        value = new String(data, offset, this.length, Charset.forName("UTF-8"));
    }

    /**
     * FixedString characteristic.
     *
     * @param fixedString String value
     */
    public FixedString(@NonNull final String fixedString) {
        length = parseLength(fixedString);
        value = fixedString;
    }

    private int parseLength(final String fixedString) {
        final int length = fixedString.getBytes().length;
        if (length > 0 && length <= 8) {
            return 8;
        } else if (length > 8 && length <= 16) {
            return 16;
        } else if (length > 16 && length <= 24) {
            return 24;
        } else if (length > 24 && length <= 36) {
            return 36;
        } else if (length > 36 && length <= 64) {
            return 64;
        } else {
            throw new IllegalArgumentException("Invalid length");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public byte[] getBytes() {
        return ByteBuffer.allocate(parseLength(value)).put(value.getBytes()).array();
    }

}
