package com.example.xkfeng.asyncdownload;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.xkfeng.asyncdownload.DownloadPackage.DownloadService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity" ;
    private static final int NET_STATE = 1 ;
    private static final int WIFI_STATE = 2 ;
    private static final int NO_NET_STATE = 3 ;

    private Button startBtn , pauseBtn , cancelBtn ;
    private DownloadService.downloadBinder binder ;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            binder = (DownloadService.downloadBinder)service ;
            Log.i(TAG , "ON SERVICE CONNECTED") ;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.i(TAG , "ON SERVICE DISCONNECTED") ;
            binder = null ;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button)findViewById(R.id.startDownloadBtn) ;
        pauseBtn = (Button)findViewById(R.id.pauseDownloadBtn) ;
        cancelBtn = (Button)findViewById(R.id.cancelDonwloadBtn) ;

        startBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        Intent intent = new Intent(MainActivity.this ,DownloadService.class) ;
        startService(intent) ;
        bindService(intent ,connection ,BIND_AUTO_CREATE) ;

        if (ContextCompat.checkSelfPermission(this , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} , 1);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 1 :
                if (grantResults.length>0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this , "拒绝权限，将使程序无法使用",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break ;

            default:

                break ;
        }
    }

    @Override
    public void onClick(View v) {

        Log.i(TAG , "onClick") ;
        if (binder == null)
        {
            return ;
        }
        if (v.getId() == R.id.startDownloadBtn)
        {

            Log.i(TAG , "onClick Start") ;
            final String url = "https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe" ;
            int STATE = isWifi(MainActivity.this);
            switch (STATE)
            {
                case WIFI_STATE:
                    binder.startDownload(url);
                    break ;
                case NET_STATE :
                    AlertDialog alertDialog =  new AlertDialog.Builder(this)
                            .setMessage("提示")
                            .setIcon(R.mipmap.ic_launcher)
                            .setPositiveButton("确定下载", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    binder.startDownload(url);
                                }
                            })
                            .setNeutralButton("取消", null)
                            .show() ;
                    break ;

                case NO_NET_STATE :

                    break ;
            }



        }else if (v.getId() == R.id.pauseDownloadBtn)
        {
            binder.pauseDownload();

            Log.i(TAG , "onClick Pause") ;


        }else if (v.getId() == R.id.cancelDonwloadBtn)
        {
            binder.cancelDownload();
            Log.i(TAG , "onClick Cancel") ;


        }
    }
    private static int isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info= connectivityManager.getActiveNetworkInfo();
        if (info==null) //如果没有联网
        {
            Toast.makeText(mContext , "没有联网" ,Toast.LENGTH_SHORT).show();
            return NO_NET_STATE ;
        }

        if (info!= null
                && info.getType() == ConnectivityManager.TYPE_WIFI) {

            Toast.makeText(mContext , "当前处于wifi状态，直接下载" ,Toast.LENGTH_SHORT).show();
            return WIFI_STATE;
        }


        Toast.makeText(mContext , "当前处于移动流量状态" ,Toast.LENGTH_SHORT).show();
        return NET_STATE;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }


}
