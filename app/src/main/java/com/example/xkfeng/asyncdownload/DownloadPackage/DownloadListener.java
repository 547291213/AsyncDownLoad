package com.example.xkfeng.asyncdownload.DownloadPackage;

/**
 * Created by initializing on 2018/6/11.
 */

public interface DownloadListener {
    public void onProgress(int progress) ;

    public void onSuccess() ;

    public void onFailed() ;

    public void onPause() ;

    public void onCancled() ;
}
