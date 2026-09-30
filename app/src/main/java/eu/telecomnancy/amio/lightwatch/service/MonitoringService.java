package eu.telecomnancy.amio.lightwatch.service;

/*
import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import eu.telecomnancy.amio.lightwatch.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MonitoringService extends Service {
    public static final String ACTION_READINGS = "eu.telecomnancy.amio.lightwatch.ACTION_READINGS";
    public static final String EXTRA_LIGHTS_ON = "lights_on";
    private ScheduledExecutorService scheduler;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID_ONGOING, buildOngoingNotification());
        scheduler.scheduleWithFixedDelay(this::poll, 0, intervalSeconds, TimeUnit.SECONDS);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        scheduler.shutdownNow(); // impératif : sinon le thread survit au service
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
}

 */