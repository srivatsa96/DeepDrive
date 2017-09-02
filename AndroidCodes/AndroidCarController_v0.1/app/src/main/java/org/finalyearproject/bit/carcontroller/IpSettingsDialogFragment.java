package org.finalyearproject.bit.carcontroller;


import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Srivatsa  on 28th, August 2017.
 */

public class IpSettingsDialogFragment extends DialogFragment {

    private String ipString;

    private String portString;

    public interface IpSettingsListener {
        public void onSaveIpClick(String ipString, String portString);
    }

    private IpSettingsListener ipSettingsListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.ip_settings_dialog,null);

        final EditText ip = (EditText)view.findViewById(R.id.ip_address);
        final EditText port =  (EditText)view.findViewById(R.id.port);
        builder.setView(view)
                .setTitle("IP Settings")
                .setMessage("Please enter IP address of on board computer")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ipString = ip.getText().toString();
                        portString = port.getText().toString();
                        ipSettingsListener.onSaveIpClick(ipString, portString);
                    }
                });
        return  builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ipSettingsListener = (IpSettingsListener)activity;
        } catch(ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement IpSettingsListner");
        }
    }
}
