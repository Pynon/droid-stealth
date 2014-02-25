package sharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.stealth.android.R;

/**
 * Created by Alex on 2/25/14.
 */
public class APAppSharingFragment extends DialogFragment {

    public interface AppSharingListener{
        public void dialogCanceled();
        public void networkCreated();
        public void networkCreationFailed();
    }

    private WifiAPManager mWifiAPManager;
    private AppSharingListener mListener;
    private AlertDialog mDialog;

    public APAppSharingFragment(){
        //TODO for now, needs to be cleaner
        mListener = new HttpShare();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mWifiAPManager = new WifiAPManager(getActivity());

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setTitle(R.string.ap_dialog_title);

        mBuilder.setView(createContentView());

        mBuilder.setPositiveButton(R.string.start_ap_app_sharing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean result = mWifiAPManager.setWifiApEnabled(getConfigFromFields(), true);
                if(mListener != null){
                    if(result)
                        mListener.networkCreated();
                    else {
                        //turn wifi back on and continue
                        mWifiAPManager.enableWifi(true);
                        dismiss();

                        //notify listener
                        if(mListener != null)
                            mListener.networkCreationFailed();
                    }
                }
            }
        });
        mBuilder.setNegativeButton(R.string.cancel_ap_app_sharing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();

                //if hotspot is enabled, now's the time to close it!
                if(mWifiAPManager.getWifiApState() == WifiAPManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED){
                    if(mWifiAPManager.setWifiApEnabled(getConfigFromFields(), false))
                        mWifiAPManager.enableWifi(true);
                }
                if(mListener != null)
                    mListener.dialogCanceled();
            }
        });

        mDialog = mBuilder.create();

        return mDialog;
    }

    /**
     * Creates the content view and hooks up any events that are needed
     * @return
     */
    private View createContentView(){
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View contentView = inflater.inflate(R.layout.ap_sharing_dialog, null);

        //disable ssid button
        mSSIDField = (TextView)contentView.findViewById(R.id.SSID_field);
        mSSIDField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                boolean validSSID = SharingUtils.validSSID(editable.toString());
                if(!validSSID)
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                else
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        //Link show password checkbox to TextView
        mPasswordField = (TextView)contentView.findViewById(R.id.password_field);
        ((CheckBox)contentView.findViewById(R.id.show_password_check)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mPasswordField.setInputType(checked ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        return contentView;
    }

    TextView mPasswordField;
    TextView mSSIDField;

    private WifiConfiguration getConfigFromFields(){
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = mSSIDField.getText().toString();

        if(mPasswordField.getText() == null){
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        else {
            configuration.preSharedKey = mPasswordField.getText().toString();
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        }

        configuration.status = WifiConfiguration.Status.ENABLED;

        return configuration;
    }

    private class HttpShare implements AppSharingListener {

        @Override
        public void dialogCanceled() {

        }

        @Override
        public void networkCreated() {

        }

        @Override
        public void networkCreationFailed() {
        }
    }
}

