package com.example789.cicero.callrecordingtest2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhoneStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        MainActivity mainActivity = (MainActivity) m_ActivityRef.get();

        if ((mainActivity != null) && mainActivity.IsCallRecordingDisabled()) {
            return;
        }

        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

            if (m_MediaRecorder == null) {
                m_MediaRecorder = new MediaRecorder();
            }

            String sState = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String sNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int nState = 0;

            if (sState != null) {
                if (sState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    nState = TelephonyManager.CALL_STATE_IDLE;
                } else if (sState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    nState = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (sState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    nState = TelephonyManager.CALL_STATE_RINGING;
                }
            }

            OnCallStateChanged(context, nState, sNumber);
            m_nPreviousState = nState;
        }
    }

    public void OnCallStateChanged(Context context, int nState, String sNumber) {

        if (nState != m_nPreviousState) {

            m_Context = context;

            switch (nState) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    ProcessOffHookState(sNumber);
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    ProcessIdleState();
                    break;

                default:
                    Log.e(TAG, "Unknown state");
                    break;
            }
        }
    }

    private void ProcessOffHookState(String sNumber) {

        boolean bIncomingCall = (m_nPreviousState == TelephonyManager.CALL_STATE_RINGING);

        if (bIncomingCall) {
            Log.w(TAG, "ProcessOffHookState: Incoming call");
        }

        Date callTimeStart = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String sDate = dateFormat.format(callTimeStart);
        dateFormat.applyPattern("HH-mm-ss");
        String sTime = dateFormat.format(callTimeStart);

        m_MediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        m_MediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        m_MediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        File recordingsPath = new File(Environment.getExternalStorageDirectory()  + "/call_recordings_test");

        if (!recordingsPath.isDirectory() && recordingsPath.mkdirs()) {
            Log.w(TAG, "Directory for recordings has been created");
        }
        String sPath = recordingsPath.getAbsolutePath() + "/";

        if (!bIncomingCall) {
            sPath += "outgoing_";
        } else if (sNumber != null) {
            sPath += sNumber;
        }

        sPath += sDate + "_" + sTime + ".m4a";

        m_MediaRecorder.setOutputFile(sPath);
        try {
            m_MediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        m_MediaRecorder.start();


        if (m_Context != null) {
            Toast.makeText(m_Context.getApplicationContext(), "Recording started", Toast.LENGTH_SHORT).show();
        }
    }

    private void ProcessIdleState() {
        if (m_nPreviousState != TelephonyManager.CALL_STATE_RINGING) {
            m_MediaRecorder.stop();
            m_MediaRecorder.release();
            m_MediaRecorder = null;
            if (m_Context != null) {
                Toast.makeText(m_Context.getApplicationContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void setActivityRef(Activity activity) {
        m_ActivityRef = new WeakReference<>(activity);
    }

    private Context m_Context;
    private static int m_nPreviousState = TelephonyManager.CALL_STATE_IDLE;
    private final String TAG = "TestReceiver";
    private static MediaRecorder m_MediaRecorder;
    private static WeakReference<Activity> m_ActivityRef;
}