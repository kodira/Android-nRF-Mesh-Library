package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

/**
 * Generic User Property Status message implementation.
 * 
 * This message is sent as a response to Generic User Property Get and Generic User Property Set messages.
 */
public class GenericUserPropertyStatus extends GenericPropertyStatus {

    /**
     * Constructs a GenericUserPropertyStatus message.
     *
     * @param message AccessMessage containing the parsed message
     */
    public GenericUserPropertyStatus(@NonNull final AccessMessage message) {
        super(message);
    }

    @Override
    void parseStatusParameters() {
        if (mParameters.length < 2) {
            throw new IllegalArgumentException("Invalid Generic User Property Status message length: " + mParameters.length);
        }

        try {
            // Parse User Property ID (2 bytes, little-endian) 
            // Use inherited field 'propertyId'
            int firstPropertyByte = mParameters[0] & 0xFF;
            int secondPropertyByte = mParameters[1] & 0xFF;
            propertyId = (short) (secondPropertyByte << 8 | firstPropertyByte);

            // Parse optional fields
            if (mParameters.length > 2) {
                // User Access field is present (1 byte)
                // Use inherited field 'userAccess'
                userAccess = mParameters[2];
                
                // User Property Value field (variable length, remaining bytes)
                // Use inherited field 'propertyValue'
                if (mParameters.length > 3) {
                    propertyValue = new byte[mParameters.length - 3];
                    System.arraycopy(mParameters, 3, propertyValue, 0, mParameters.length - 3);
                }
            } else {
                // Optional fields not present, set to default/null values
                userAccess = 0;  // Default access
                propertyValue = null;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Generic User Property Status message", e);
        }
    }

    // Inherits getPropertyId(), getUserAccess(), getPropertyValue(), and Parcelable methods from parent class
}