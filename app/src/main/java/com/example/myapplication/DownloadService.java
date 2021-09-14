package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;

public class DownloadService extends Service {
    
    private String d_url;
    private DownloadTask downloadTask;
    private DowmloadListener listener=new DowmloadListener() {
        @Override
        public void onprogress(int progress) {
            getNotificationManager().notify(1,getNotification("Download...",progress));
        }

        @Override
        public void onsuccess() {
            downloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Success",-1));
            Log.d("DownloadService:","Download Success");
        }

        @Override
        public void onfailed() {
            downloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed",-1));
            Log.d("DownloadService:","Download Failed");

        }

        @Override
        public void onpaused() {
            downloadTask=null;
            Log.d("DownloadService:","Download Paused");

        }

        @Override
        public void oncanceled() {
            downloadTask=null;
            Log.d("DownloadService:","Download Canceled");

        }
    };

    private DownloadBinder binder=new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent){
        return  binder;
    }

    class DownloadBinder extends Binder{
        public void startDownload(String url){
            if (downloadTask==null){
                d_url=url;
                downloadTask=new DownloadTask(listener);
                downloadTask.execute(d_url);

                startForeground(1,getNotification("Downloading...",0));
                Log.d("DownloadService:","Downloading...");
            }
        }

        public void pauseDownload(){
            if (downloadTask!=null){
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload(){
            if (downloadTask!=null){
                downloadTask.cancelDownload();
            }else {
                if (d_url!=null){
                    String filename=d_url.substring(d_url.lastIndexOf("/"));
                    String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file=new File(directory+filename);
                    if (file.exists()) file.delete();
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Log.d("DownloadService:","Download canceled");

                }
            }

        }
    }

    private Notification getNotification(String s, int progress) {
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(s);
        if (progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE) ;
    }

}