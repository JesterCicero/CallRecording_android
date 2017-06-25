package com.example789.cicero.callrecordingtest2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            m_bCallRecordingDisabled = savedInstanceState.getBoolean(KEY_CALL_RECORDING_DISABLED, false);

            if (m_DisableCheckBox != null) {
                m_DisableCheckBox.setChecked(m_bCallRecordingDisabled);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        m_DisableCheckBox = (CheckBox) view.findViewById(R.id.disableCheckBox);

        if (m_DisableCheckBox != null) {
            m_DisableCheckBox.setChecked(PhoneStateReceiver.m_bRecordingDisabled);

            m_DisableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    m_bCallRecordingDisabled = isChecked;

                    if (m_bCallRecordingDisabled) {
                        PhoneStateReceiver.StopRecording();
                    } else if (PhoneStateReceiver.m_bOffHookState){
                        PhoneStateReceiver.StartRecording();
                    }

                    PhoneStateReceiver.m_bRecordingDisabled = m_bCallRecordingDisabled;
                    PhoneStateReceiver.SetFragmentRef(MainFragment.this);
                }
            });
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_CALL_RECORDING_DISABLED, m_bCallRecordingDisabled);
    }

    public boolean IsCallRecordingDisabled() {
        return m_bCallRecordingDisabled;
    }

    // This boolean is for using when saveInstanceState is applied
    // the actual enabling/disabling of call recording can be fetched from
    // broadcast receiver class
    private boolean m_bCallRecordingDisabled = false;
    private static final String KEY_CALL_RECORDING_DISABLED = "Call_Recording_Disabled";
    CheckBox m_DisableCheckBox;
}
