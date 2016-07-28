package com.example.qserver.intentservicetutorial;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;

    ArrayList<String> getimagedevice = new ArrayList<>();

    int id = 1;
    String urlsToDownload[] = { "http://1.bp.blogspot.com/--M8WrSToFoo/VTVRut6u-2I/AAAAAAAAB8o/dVHTtpXitSs/s1600/URL.png", "http://1.bp.blogspot.com/-s48gpkCu-Q0/UBaFOO7wZqI/AAAAAAAAAPo/4MroCME1FMY/s1600/vanity+url.jpg",
            "http://www.themelab.com/wp-content/uploads/fbcustomurl-600x245.png", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSd_cxbmyyBWSklELbuDYyCHXVj8LZ7le1kJMcxbdpiaGHirHJg",
            "https://2.bp.blogspot.com/-DBFmr6FGhpM/V126PCALKWI/AAAAAAAABow/AJV8fScKjmE6HPoZyVFFYMljqTN4TEVtgCLcB/s1600/Url%2BEncode%2BDecode%2BOnline%2B%2527.png" };
    int counter = 0;
    private NotificationReceiver nReceiver;
    ArrayList<AsyncTask<String, String, Void>> arr;

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getExtras().getString(DownloadService.NOT_EVENT_KEY);
            Log.i("NotificationReceiver", "NotificationReceiver onReceive : " + event);
            if (event.trim().contentEquals(DownloadService.NOT_REMOVED)) {
                killTasks();
            }
        }
    }

    private void killTasks() {
        if (null != arr & arr.size() > 0) {
            for (AsyncTask<String, String, Void> a : arr) {
                if (a != null) {
                    Log.i("NotificationReceiver", "Killing download thread");
                    a.cancel(true);
                }
            }
            mNotifyManager.cancelAll();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_progress);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Downloading images...").setContentText("Download in progress").setSmallIcon(R.mipmap.ic_launcher);
        // Start a lengthy operation in a background thread
        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(id, mBuilder.build());
       // mBuilder.setAutoCancel(true);

        arr = new ArrayList<AsyncTask<String, String, Void>>();
        int incr;
        for (incr = 0; incr < urlsToDownload.length; incr++) {
            ImageDownloader imageDownloader = new ImageDownloader();
            imageDownloader.execute(urlsToDownload[incr]);
            arr.add(imageDownloader);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killTasks();
        unregisterReceiver(nReceiver);
    }

    private void downloadImagesToSdCard(String downloadUrl, String imageName) {
        FileOutputStream fos;
        InputStream inputStream = null;

        try {
            URL url = new URL(downloadUrl);
            /* making a directory in sdcard */
            String sdCard = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(sdCard, "DemoDownload");

            /* if specified not exist create new */
            if (!myDir.exists()) {
                myDir.mkdir();
                Log.v("", "inside mkdir");
            }

            /* checks the file and if it already exist delete */
            String fname = imageName;
            File file = new File(myDir, fname);
            Log.d("file===========path", "" + file);
            if (file.exists())
                file.delete();

            /* Open a connection */
            URLConnection ucon = url.openConnection();

            HttpURLConnection httpConn = (HttpURLConnection) ucon;
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            inputStream = httpConn.getInputStream();

            /*
             * if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
             * inputStream = httpConn.getInputStream(); }
             */

            fos = new FileOutputStream(file);
            // int totalSize = httpConn.getContentLength();
            // int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, bufferLength);
                // downloadedSize += bufferLength;
                // Log.i("Progress:", "downloadedSize:" + downloadedSize +
                // "totalSize:" + totalSize);
            }
            inputStream.close();
            fos.close();
            Log.d("test", "Image Saved in sdcard..");
        } catch (IOException io) {
            inputStream = null;
            fos = null;
            io.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }

    private class ImageDownloader extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... param) {
            downloadImagesToSdCard(param[0], "Image" + counter + ".png");
            return null;
        }

        protected void onProgressUpdate(String... values) {
        }

        @Override
        protected void onPreExecute() {
            Log.i("Async-Example", "onPreExecute Called");
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i("Async-Example", "onPostExecute Called");

            float len = urlsToDownload.length;
            // When the loop is finished, updates the notification
            if (counter >= len - 1) {
                mBuilder.setContentTitle("Done.");
                mBuilder.setContentText("Download complete")
                        // Removes the progress bar
                        .setProgress(0, 0, false);
                mNotifyManager.notify(id, mBuilder.build());
            } else {
                int per = (int) (((counter + 1) / len) * 100f);
                Log.i("Counter", "Counter : " + counter + ", per : " + per);
                mBuilder.setContentText("Downloaded (" + per + "/100");
                mBuilder.setProgress(100, per, false);
                // Displays the progress bar for the first time.
                mNotifyManager.notify(id, mBuilder.build());
            }
            counter++;

            getimagedevice.add(Environment.getExternalStorageDirectory().toString()+"/DemoDownload"+"/Image0.png");
            String s =  getimagedevice.get(0);

            getimagedevice.add(Environment.getExternalStorageDirectory()+"/DemoDownload"+"/Image1.png");
            String s2 =  getimagedevice.get(1);
            getimagedevice.add(Environment.getExternalStorageDirectory()+"/DemoDownload"+"/Image2.png");
            getimagedevice.add(Environment.getExternalStorageDirectory()+"DemoDownload"+"/Image2.png");



        }

    }
}
