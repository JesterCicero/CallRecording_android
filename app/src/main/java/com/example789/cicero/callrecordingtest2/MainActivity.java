package com.example789.cicero.callrecordingtest2;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox disableCheckBox = (CheckBox) findViewById(R.id.disableCheckBox);

        if (savedInstanceState != null) {
            m_bCallRecordingDisabled = savedInstanceState.getBoolean(KEY_CALL_RECORDING_DISABLED, false);
            disableCheckBox.setChecked(m_bCallRecordingDisabled);
        } else {
            disableCheckBox.setChecked(PhoneStateReceiver.m_bRecordingDisabled);
        }

        disableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                m_bCallRecordingDisabled = isChecked;

                if (m_bCallRecordingDisabled) {
                    PhoneStateReceiver.StopRecording();
                } else if (PhoneStateReceiver.m_bOffHookState){
                    PhoneStateReceiver.StartRecording();
                }

                PhoneStateReceiver.m_bRecordingDisabled = m_bCallRecordingDisabled;
                PhoneStateReceiver.setActivityRef(MainActivity.this);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
}
