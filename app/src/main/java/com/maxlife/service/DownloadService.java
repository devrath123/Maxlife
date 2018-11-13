package com.maxlife.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.os.ResultReceiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anshul.mittal on 7/6/16.
 */
public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra("url");
        int messgageIdToUpdate = intent.getIntExtra("messageId", 0);

        String video_name = intent.getStringExtra("video_name");
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        File mediaFile = null;
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();


            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MaxLife");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {

                }
            }

            mediaFile = new File(mediaStorageDir, ""+ video_name);

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(mediaFile);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                Bundle resultData = new Bundle();
                resultData.putInt("progress", (int) (total * 100 / fileLength));
                resultData.putString("file_path", mediaFile.getAbsolutePath());
                resultData.putInt("fileLength", fileLength);
                receiver.send(UPDATE_PROGRESS, resultData);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Bundle resultData = new Bundle();
            resultData.putInt("progress", 100);
            resultData.putString("path", mediaFile.getPath());
            resultData.putInt("messageId", messgageIdToUpdate);
            receiver.send(UPDATE_PROGRESS, resultData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}