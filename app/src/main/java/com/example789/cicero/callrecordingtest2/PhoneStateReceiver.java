package com.example789.cicero.callrecordingtest2;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
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
        m_Context = context;

        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            if (m_MediaRecorder == null) {
                m_MediaRecorder = new MediaRecorder();
            }

            String sState = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            int nState = 0;

            if (sState != null) {
                if (sState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    nState = TelephonyManager.CALL_STATE_IDLE;
                } else if (sState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    nState = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (sState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    m_sNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    nState = TelephonyManager.CALL_STATE_RINGING;
                }
            }

            OnCallStateChanged(context, nState);
            m_nPreviousState = nState;
        }
    }

    public void OnCallStateChanged(Context context, int nState) {
        if (nState != m_nPreviousState) {
            switch (nState) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    ProcessOffHookState();
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

    private void ProcessOffHookState() {
        m_bOffHookState = true;
        m_bIncomingCall = (m_nPreviousState == TelephonyManager.CALL_STATE_RINGING);

        if (m_bIncomingCall) {
            Log.w(TAG, "ProcessOffHookState: Incoming call");
        }

        CreateNotification();

        if (IsRecordingEnabled()) {
            StartRecording();
        }
    }

    private void ProcessIdleState() {
        m_bOffHookState = false;
        if (m_nPreviousState != TelephonyManager.CALL_STATE_RINGING) {
            StopRecording();
            DismissNotification();
        }
    }

    public void CreateNotification() {
        NotificationCompat.Builder builder;

        if (m_Context != null) {
            builder = new NotificationCompat.Builder(m_Context);

            if (builder != null) {
                Resources resources = m_Context.getResources();

                if (resources != null) {
                    builder.setSmallIcon(R.drawable.call_recording)
                            .setContentTitle(resources.getString(R.string.call_recording_title_text))
                            .setContentText(resources.getString(R.string.call_recording_text));
                }

                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(m_Context, MainActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(m_Context);
                // Adds the back stack for the Intent (but not the Intent itself)

                if (stackBuilder != null) {
                    stackBuilder.addParentStack(MainActivity.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (resultPendingIntent != null) {
                        builder.setContentIntent(resultPendingIntent);
                    }

                    builder.setAutoCancel(true);

                    NotificationManager notificationManager = (NotificationManager) m_Context.getSystemService(Context.NOTIFICATION_SERVICE);
                    // mId allows you to update the notification later on.
                    if (notificationManager != null) {
                        notificationManager.notify(NOTIFICATION_ID, builder.build());
                    }
                }
            }
        }
    }

    public void DismissNotification() {
        NotificationManager notificationManager = (NotificationManager) m_Context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    public static void SetActivityRef(Activity activity) {
        m_ActivityRef = new WeakReference<>(activity);
    }

    public static void StartRecording() {

        if (!m_bRecordingStarted && (m_MediaRecorder != null)) {
            m_MediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            m_MediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            m_MediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            Date callTimeStart = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String sDate = dateFormat.format(callTimeStart);
            dateFormat.applyPattern("HH-mm-ss");
            String sTime = dateFormat.format(callTimeStart);

            File recordingsPath = new File(Environment.getExternalStorageDirectory()  + "/call_recordings_test");

            if (!recordingsPath.isDirectory() && recordingsPath.mkdirs()) {
                Log.w(TAG, "Directory for recordings has been created");
            }
            String sPath = recordingsPath.getAbsolutePath() + "/";

            if (!m_bIncomingCall) {
                sPath += "outgoing_";
            } else if (m_sNumber != null) {
                sPath += m_sNumber + " ";
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

            m_bRecordingStarted = true;
        }
    }

    public static void StopRecording() {
        if (m_bRecordingStarted && (m_MediaRecorder != null)) {
            m_MediaRecorder.stop();
            m_MediaRecorder.release();
            m_MediaRecorder = null;

            if (m_Context != null) {
                Toast.makeText(m_Context.getApplicationContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
            }

            m_bRecordingStarted = false;
        }
    }

    public static boolean IsRecordingEnabled() {

        boolean bRecordingEnabled = true;

        if (m_ActivityRef != null) {
            MainActivity mainActivity = (MainActivity) m_ActivityRef.get();

            m_bRecordingDisabled = mainActivity.IsCallRecordingDisabled();

            if ((mainActivity != null) && m_bRecordingDisabled) {
                bRecordingEnabled = false;
            }
        }

        return bRecordingEnabled;
    }

    private static Context m_Context;
    private static int m_nPreviousState = TelephonyManager.CALL_STATE_IDLE;
    private static final String TAG = "PhoneStateReceiver";
    private final int NOTIFICATION_ID = R.drawable.call_recording;
    private static MediaRecorder m_MediaRecorder;
    private static WeakReference<Activity> m_ActivityRef;
    public static boolean m_bRecordingStarted = false;
    public static boolean m_bRecordingDisabled = false;
    private static boolean m_bIncomingCall;
    public static boolean m_bOffHookState;
    private static String m_sNumber;
}