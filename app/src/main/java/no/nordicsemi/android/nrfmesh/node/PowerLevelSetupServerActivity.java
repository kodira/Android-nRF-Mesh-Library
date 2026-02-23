package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.models.GenericPowerLevelSetupServer;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericPowerDefaultGet;
import no.nordicsemi.android.mesh.transport.GenericPowerDefaultSet;
import no.nordicsemi.android.mesh.transport.GenericPowerDefaultStatus;
import no.nordicsemi.android.mesh.transport.GenericPowerRangeGet;
import no.nordicsemi.android.mesh.transport.GenericPowerRangeSet;
import no.nordicsemi.android.mesh.transport.GenericPowerRangeStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutPowerLevelSetupServerBinding;

@AndroidEntryPoint
public class PowerLevelSetupServerActivity extends ModelConfigurationActivity {

    private static final String TAG = PowerLevelSetupServerActivity.class.getSimpleName();

    private TextView powerDefaultValue;
    private TextView powerRangeMinValue;
    private TextView powerRangeMaxValue;
    private Slider mPowerDefaultSlider;
    private RangeSlider mPowerRangeSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof GenericPowerLevelSetupServer) {
            final LayoutPowerLevelSetupServerBinding nodeControlsContainer = LayoutPowerLevelSetupServerBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);

            powerDefaultValue = nodeControlsContainer.powerDefaultValue;
            powerRangeMinValue = nodeControlsContainer.powerRangeMinValue;
            powerRangeMaxValue = nodeControlsContainer.powerRangeMaxValue;

            mPowerDefaultSlider = nodeControlsContainer.powerDefaultSlider;
            mPowerDefaultSlider.setValueFrom(0);
            mPowerDefaultSlider.setValueTo(65535);
            mPowerDefaultSlider.setValue(0);
            mPowerDefaultSlider.setStepSize(1);

            mPowerRangeSlider = nodeControlsContainer.powerRangeSlider;
            mPowerRangeSlider.setValueFrom(0);
            mPowerRangeSlider.setValueTo(65535);
            mPowerRangeSlider.setValues(0.0f, 65535.0f);
            mPowerRangeSlider.setStepSize(1);

            mActionRead = nodeControlsContainer.actionRead;
            mActionRead.setOnClickListener(v -> {
                sendGenericPowerDefaultGet();
                sendGenericPowerRangeGet();
            });

            mPowerDefaultSlider.addOnChangeListener((slider, value, fromUser) ->
                    powerDefaultValue.setText(String.valueOf((int) value)));

            mPowerDefaultSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull final Slider slider) {

                }

                @Override
                public void onStopTrackingTouch(@NonNull final Slider slider) {
                    final int defaultPowerLevel = (int) slider.getValue();
                    sendGenericPowerDefaultSet(defaultPowerLevel);
                }
            });

            mPowerRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
                final List<Float> values = slider.getValues();
                if (values.size() >= 2) {
                    powerRangeMinValue.setText(String.valueOf(values.get(0).intValue()));
                    powerRangeMaxValue.setText(String.valueOf(values.get(1).intValue()));
                }
            });

            mPowerRangeSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull RangeSlider slider) {

                }

                @Override
                public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                    final List<Float> values = slider.getValues();
                    if (values.size() >= 2) {
                        final int minPowerLevel = values.get(0).intValue();
                        final int maxPowerLevel = values.get(1).intValue();
                        sendGenericPowerRangeSet(minPowerLevel, maxPowerLevel);
                    }
                }
            });

            mViewModel.getSelectedModel().observe(this, meshModel -> {
                if (meshModel != null) {
                    updateAppStatusUi(meshModel);
                    updatePublicationUi(meshModel);
                    updateSubscriptionUi(meshModel);
                }
            });
        }
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        mPowerDefaultSlider.setEnabled(true);
        mPowerRangeSlider.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mPowerDefaultSlider.setEnabled(false);
        mPowerRangeSlider.setEnabled(false);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        mSwipe.setOnRefreshListener(this);

        if (meshMessage instanceof GenericPowerDefaultStatus) {
            final GenericPowerDefaultStatus status = (GenericPowerDefaultStatus) meshMessage;
            final int defaultPower = status.getPowerDefault();
            powerDefaultValue.setText(String.valueOf(defaultPower));
            mPowerDefaultSlider.setValue(defaultPower);
        } else if (meshMessage instanceof GenericPowerRangeStatus) {
            final GenericPowerRangeStatus status = (GenericPowerRangeStatus) meshMessage;
            final int minPower = status.getRangeMin();
            final int maxPower = status.getRangeMax();
            powerRangeMinValue.setText(String.valueOf(minPower));
            powerRangeMaxValue.setText(String.valueOf(maxPower));
            mPowerRangeSlider.setValues((float) minPower, (float) maxPower);
        }
        hideProgressBar();
    }

    /**
     * Send generic power default get to mesh node
     */
    public void sendGenericPowerDefaultGet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending message to element's unicast address: " + MeshAddress.formatAddress(address, true));
                    final GenericPowerDefaultGet genericPowerDefaultGet = new GenericPowerDefaultGet(appKey);
                    sendAcknowledgedMessage(address, genericPowerDefaultGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Send generic power range get to mesh node
     */
    public void sendGenericPowerRangeGet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    Log.v(TAG, "Sending message to element's unicast address: " + MeshAddress.formatAddress(address, true));
                    final GenericPowerRangeGet genericPowerRangeGet = new GenericPowerRangeGet(appKey);
                    sendAcknowledgedMessage(address, genericPowerRangeGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Send generic power default set to mesh node
     *
     * @param defaultPower default power level
     */
    public void sendGenericPowerDefaultSet(final int defaultPower) {
        if (!checkConnectivity(mContainer)) return;
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    if (!model.getBoundAppKeyIndexes().isEmpty()) {
                        final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                        final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);
                        final int address = element.getElementAddress();
                        final GenericPowerDefaultSet genericPowerDefaultSet = new GenericPowerDefaultSet(appKey, defaultPower);
                        sendAcknowledgedMessage(address, genericPowerDefaultSet);
                    } else {
                        mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                    }
                }
            }
        }
    }

    /**
     * Send generic power range set to mesh node
     *
     * @param minPower minimum power level
     * @param maxPower maximum power level
     */
    public void sendGenericPowerRangeSet(final int minPower, final int maxPower) {
        if (!checkConnectivity(mContainer)) return;
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    if (!model.getBoundAppKeyIndexes().isEmpty()) {
                        final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                        final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);
                        final int address = element.getElementAddress();
                        final GenericPowerRangeSet genericPowerRangeSet = new GenericPowerRangeSet(appKey, minPower, maxPower);
                        sendAcknowledgedMessage(address, genericPowerRangeSet);
                    } else {
                        mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                    }
                }
            }
        }
    }
}