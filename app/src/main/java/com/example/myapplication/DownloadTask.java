package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String ,Integer,Integer> {
    public static final int TYPR_success=0;
    public static final int TYPR_failed=1;
    public static final int TYPR_paused=2;
    public static final int TYPR_canceled=3;

    private DowmloadListener listener;
    private boolean iscanceled=false;
    private boolean ispaused=false;
    private int lastprogress;

    public DownloadTask(DowmloadListener listener){
        this.listener=listener;
    }

    @Override
    protected Integer doInBackground(String... params){
        InputStream is=null;
        RandomAccessFile savedFile=null;
        File file=null;
        try {
            long d_length=0;
            String d_Url=params[0];
            String filename=d_Url.substring(d_Url.lastIndexOf("/"));
            String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file=new File(directory+filename);
            if (file.exists()) d_length=file.length();
            long content_length=getContentLength(d_Url);
            if (content_length==0) return TYPR_failed;
            else if (content_length==d_length) return TYPR_success;

            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder()
                    .addHeader("RANGE","bytes="+d_length+"-").url(d_Url).build();
            Response response=client.newCall(request).execute();
            if (response!=null){
                is=response.body().byteStream();
                savedFile=new RandomAccessFile(file,"rw");
                savedFile.seek(d_length);
                byte[] b=new byte[1024];
                int total=0;
                int len;
                while ((len=is.read(b))!=-1){
                    if (iscanceled) return TYPR_paused;
                    else  if (ispaused) return TYPR_paused;
                    else {
                        total += len;
                        savedFile.write(b,0,len);
                        int progress=(int)((total+d_length)*100/content_length);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPR_success;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is!=null) is.close();
                if (savedFile!=null) savedFile.close();
                if (iscanceled&&file!=null) file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return TYPR_failed;
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            int progress=values[0];
            if (progress>lastprogress){
                listener.onprogress(progress);
                lastprogress=progress;
            }
        }

        @Override
        protected void onPostExecute(Integer status){
            switch (status){
                case TYPR_success:
                    listener.onsuccess();
                    break;
                case TYPR_failed:
                    listener.onfailed();
                    break;
                case TYPR_paused:
                    listener.onpaused();
                    break;
                case TYPR_canceled:
                    listener.oncanceled();
                    break;
                default:
                    break;
            }
        }

        public void pauseDownload(){
        ispaused=true;
        }

        public void cancelDownload(){
        iscanceled=true;
        }


        private long getContentLength(String d_url) throws IOException {
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder().url(d_url).build();
            Response response=client.newCall(request).execute();
            if (response!=null && response.isSuccessful()){
                long content_len=response.body().contentLength();
                response.close();
                return content_len;
            }
            return 0;
        }
}
