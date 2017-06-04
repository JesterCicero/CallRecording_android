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

        CheckBox checkBox = (CheckBox) findViewById(R.id.disableCheckBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                m_bCallRecordingDisabled = isChecked;
                PhoneStateReceiver.setActivityRef(MainActivity.this);
            }
        });
    }

    public boolean IsCallRecordingDisabled() {
        return m_bCallRecordingDisabled;
    }

    private boolean m_bCallRecordingDisabled = false;
}
