package com.example.xkfeng.asyncdownload.DownloadPackage;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedByInterruptException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by initializing on 2018/6/11.
 *
 */


public class DownloadTask extends AsyncTask<String , Integer , Integer> {


    public static final int TYPE_SUCCESS = 0 ;
    public static final int TYPE_FAILED = 1 ;
    public static final int TYPE_PAUSED = 2 ;
    public static final int TYPE_CANCELED = 3 ;

    private DownloadListener downloadListener ;

    private boolean isCancled = false ;
    private boolean isPaused = false ;

    private int lastProgress  = 0 ;


    public DownloadTask(DownloadListener downloadListener )
    {
        this.downloadListener = downloadListener ;
    }

    /*
    1   下载前的准备工作，比如界面的初始化
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /*
    2   后台下载工作
    （1） 下载工作的实际完成
    （2） 下载进度的实时反馈
    （3） 下载结束的返回

     */
    @Override
    protected Integer doInBackground(String... strings) {

        InputStream is = null ;
        RandomAccessFile savedFile = null ;
        File file = null ;
        try{
            long downloadedLength = 0 ; //已经下载好的文件长度
            String downloadUrl = strings[0] ;
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")) ;
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() ;
            file = new File(directory + fileName) ;
            if (file.exists())
            {
                downloadedLength = file.length() ;
            }
            long contentLength = getContentLength(downloadUrl) ;
            if (contentLength == 0){
                return TYPE_FAILED ;
            }else if (contentLength == downloadedLength)
            {
                return TYPE_SUCCESS ;
            }

            OkHttpClient client = new OkHttpClient() ;
            Request request = new Request.Builder()
                    .addHeader("RANGE" , "bytes="+downloadedLength+"-")
                    .url(downloadUrl)
                    .build() ;
            Response response = client.newCall(request).execute() ;
            if (response != null)
            {
                is = response.body().byteStream() ;
                savedFile = new RandomAccessFile(file , "rw") ;
                savedFile.seek(downloadedLength);
                byte [] b = new byte[1024] ;
                int total = 0 ;
                int len ;
                while((len = is.read(b)) != -1)
                {
                    if (isCancled)
                    {
                        return TYPE_CANCELED ;
                    }
                    else if (isPaused)
                    {
                        return TYPE_PAUSED ;
                    }
                    else {
                        total += len ;
                        savedFile.write(b,0,len);
                        //计算已经下载的百分比
                        int progress = (int)((total + downloadedLength)*100 / contentLength) ;
                        //调用默认的更新方法
                        publishProgress(progress);
                    }

                }
                response.body().close();
                return TYPE_SUCCESS ;
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {

            try{

                if (is != null)
                {
                    is.close();
                }
                if (savedFile != null)
                {
                    savedFile.close();
                }
                if (isCancled && file != null)
                {
                    file.delete() ;
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return TYPE_FAILED;
    }

    /*
    3   下载进度的更新
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0] ;
        if (progress > lastProgress)
        {
            downloadListener.onProgress(progress);
            lastProgress = progress ;
        }
    }

    /*
    4 下载完成后调用
     */
    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        switch ( integer)
        {
            case TYPE_SUCCESS :
                downloadListener.onSuccess();
                break ;

            case TYPE_FAILED :
                downloadListener.onFailed();
                break ;

            case TYPE_CANCELED :
                downloadListener.onCancled();
                break ;

            case TYPE_PAUSED :
                downloadListener.onPause();
                break ;

            default:
                break ;
        }
    }


    public void pauseDownload(){
        isPaused = true ;
    }

    public void cancelDownload(){
        isCancled = true ;
    }


    private long getContentLength(String downloadUrl) throws Exception
    {
        OkHttpClient client = new OkHttpClient() ;
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build() ;
        Response response = client.newCall(request).execute() ;
        if (response!=null && response.isSuccessful())
        {
            long contentLength = response.body().contentLength() ;
            response.body().close();
            return contentLength ;
        }
        return 0 ;
    }
}
