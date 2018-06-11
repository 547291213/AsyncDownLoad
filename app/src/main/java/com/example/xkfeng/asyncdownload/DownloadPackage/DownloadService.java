package com.example.xkfeng.asyncdownload.DownloadPackage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.service.autofill.FillEventHistory;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telecom.ConnectionService;
import android.util.Log;
import android.widget.Toast;

import com.example.xkfeng.asyncdownload.MainActivity;
import com.example.xkfeng.asyncdownload.R;

import java.io.File;

/**
 * Created by initializing on 2018/6/11.
 */

public class DownloadService extends Service {


    private DownloadTask downloadTask ;

    private String downloadUrl ;

    private downloadBinder myBinder = new downloadBinder() ;

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {

            getNotificationManager().notify(1 , getNotification("Downloading..." , progress));
        }

        @Override
        public void onSuccess() {

            downloadTask = null ;
            //关闭前台服务
            stopForeground(true);
            getNotificationManager().notify(1 , getNotification("Download Success" , -1));
            Toast.makeText(DownloadService.this , "下载成功", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onFailed() {

            downloadTask =null ;
            stopForeground(true);
            getNotificationManager().notify(1 , getNotification("Download Failed" , - 1));
            Toast.makeText(DownloadService.this , "下载失败" , Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {

            downloadTask = null ;
            Toast.makeText(DownloadService.this, "下载暂停" , Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancled() {

            downloadTask = null ;
            stopForeground(true);
            Toast.makeText(DownloadService.this , "下载取消" ,Toast.LENGTH_SHORT).show();
        }
    } ;

    public class downloadBinder extends Binder{
        public void startDownload (String url)
        {
            if (downloadTask == null)
            {
                downloadUrl = url ;
                downloadTask = new DownloadTask(downloadListener) ;
                downloadTask.execute(downloadUrl) ;
                startForeground(1 , getNotification("Downloading..." , 0)); ;
                Toast.makeText(DownloadService.this , "Downloading ......." , Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload(){
            if (downloadTask != null)
            {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload(){
            if (downloadTask != null)
            {
                downloadTask.cancelDownload();
            }
            if (downloadUrl != null){

                //取消下载的时候需要删除文件,并且通知关闭
                String filename =  downloadUrl.substring(downloadUrl.lastIndexOf("/")) ;
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() ;
                File file = new File(directory + filename) ;
                if (file.exists())
                {
                    file.delete() ;
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(DownloadService.this, "CANCELED" ,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("DownloadService" , "ON SERVICE CREATED") ;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private NotificationManager getNotificationManager()
    {
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE) ;
    }
    private Notification getNotification(String title ,int progress)
    {
        Intent intent = new Intent(this , MainActivity.class) ;
        PendingIntent pi = PendingIntent.getActivity(this , 0 , intent , 0) ;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this) ;
        builder.setSmallIcon(R.mipmap.ic_launcher) ;
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)) ;
        builder.setContentIntent(pi) ;
        builder.setContentTitle(title) ;
        if (progress >= 0)
        {
            builder.setContentText(progress + "%") ;
            builder.setProgress(100 , progress , false) ;
        }
        return builder.build() ;
    }

}
