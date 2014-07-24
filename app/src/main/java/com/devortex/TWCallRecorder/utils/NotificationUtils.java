package com.devortex.TWCallRecorder.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.devortex.TWCallRecorder.TWCallRecorderActivity;
import com.devortex.TWCallRecorder.R;
import com.devortex.TWCallRecorder.TWCallRecorderActivity;

import java.util.LinkedList;
import java.util.List;

import static com.devortex.TWCallRecorder.R.string.updated_notification_title;

/**
 * Created by patrick on 7/24/2014.
 */
public class NotificationUtils {
    protected static Context sContext;
    private static NotificationManager sNotificationManager;

    private static final int PENDING_INTENT_SOFT_REBOOT = 2;
    private static final int PENDING_INTENT_REBOOT = 3;

    public NotificationUtils() {
        sContext = TWCallRecorderActivity.getInstance();
    }

    public void notifyRestart() {
        Intent intent = new Intent(sContext, RebootReceiver.class);
        PendingIntent pIntent = PendingIntent.getActivity(sContext, 0, intent, 0);

        String title = sContext.getString(R.string.updated_notification_title);
        String text = sContext.getString(R.string.updated_notification);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(sContext)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        Intent iSoftReboot = new Intent(sContext, RebootReceiver.class);
        iSoftReboot.putExtra(RebootReceiver.EXTRA_SOFT_REBOOT, true);
        PendingIntent pSoftReboot = PendingIntent.getBroadcast(sContext, PENDING_INTENT_SOFT_REBOOT,
                iSoftReboot, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent iReboot = new Intent(sContext, RebootReceiver.class);
        PendingIntent pReboot = PendingIntent.getBroadcast(sContext, PENDING_INTENT_REBOOT,
                iReboot, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(0, sContext.getString(R.string.reboot), pReboot);
        builder.addAction(0, sContext.getString(R.string.soft_reboot), pSoftReboot);

        sNotificationManager =
                (NotificationManager) sContext.getSystemService(sContext.NOTIFICATION_SERVICE);

        sNotificationManager.notify(0, builder.build());
    }

    public static void cancelAll() {
        sNotificationManager.cancelAll();
    }

    public static class RebootReceiver extends BroadcastReceiver {
        public static String EXTRA_SOFT_REBOOT = "soft";
        public static String EXTRA_ACTIVATE_MODULE = "activate_module";

        @Override
        public void onReceive(Context context, Intent intent) {
			/*
			 *  Close the notification bar in order to see the toast
			 *  that module was enabled successfully.
			 *  Furthermore, if SU permissions haven't been granted yet,
			 *  the SU dialog will be prompted behind the expanded notification
			 *  panel and is therefore not visible to the user.
			 */
            sContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            cancelAll();

            RootUtil rootUtil = new RootUtil();
            if (!rootUtil.startShell()) {
                Log.e(TWCallRecorderActivity.TAG, "Could not start root shell");
                return;
            }

            List<String> messages = new LinkedList<String>();
            boolean isSoftReboot = intent.getBooleanExtra(EXTRA_SOFT_REBOOT, false);
            int returnCode = isSoftReboot ?
                    rootUtil.execute("setprop ctl.restart surfaceflinger; setprop ctl.restart zygote", messages)
                    : rootUtil.execute("reboot", messages);

            if (returnCode != 0) {
                Log.e(TWCallRecorderActivity.TAG, "Could not reboot:");
                for (String line : messages) {
                    Log.e(TWCallRecorderActivity.TAG, line);
                }
            }

            rootUtil.dispose();
        }
    }
}
