package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import no.nordicsemi.android.mesh.utils.BitReader;

public class GenericUserPropertiesStatus extends ApplicationStatusMessage implements Parcelable {

    private final int opCode;
    private ArrayList<Short> propertyIds;

    private static final Creator<GenericUserPropertiesStatus> CREATOR = new Parcelable.Creator<GenericUserPropertiesStatus>() {
        @Override
        public GenericUserPropertiesStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new GenericUserPropertiesStatus(message);
        }

        @Override
        public GenericUserPropertiesStatus[] newArray(int size) {
            return new GenericUserPropertiesStatus[size];
        }
    };

    public GenericUserPropertiesStatus(@NonNull final AccessMessage message) {
        super(message);
        this.opCode = message.getOpCode();
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        BitReader bitReader = new BitReader(mParameters);
        propertyIds = new ArrayList<Short>(mParameters.length / 2);
        int numProperties = mParameters.length / 2;
        for (int i = 0; i < numProperties; i++) {
            int firstPropertyByte = bitReader.getBits(8);
            int secondPropertyByte = bitReader.getBits(8);

            propertyIds.add((short) (secondPropertyByte << 8 | firstPropertyByte));
        }
    }

    @Override
    public int getOpCode() {
        return opCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
    }

    public ArrayList<Short> getPropertyIds() {
        return propertyIds;
    }
}