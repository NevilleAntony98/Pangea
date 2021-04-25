package com.nevilleantony.prototype.downloadmanager;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/*
https://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
https://stackoverflow.com/questions/22273045/java-getting-download-progress
*/

public class FileDownload {
    final private String USER_AGENT = "";
    private String file_url;
    private long min_range;
    private long max_range;
    private String location;
    HttpURLConnection urlconnection = null;
    FileOutputStream file_output = null;

    public void setFileUrl(String file_url){
        this.file_url = file_url;
    }

    public void setDownloadRange(long min_range, long max_range){
        this.min_range = min_range;
        this.max_range = max_range;
    }

    public void setFileLocation(String location){
        this.location = location;
    }

    public void startDownload(){
        //Better file download method to keep track of progress
        String range = String.format("bytes=%d-%d", min_range, max_range);
        try{
            URL url = new URL(file_url);
            urlconnection = (HttpURLConnection) url.openConnection();
            urlconnection.setRequestProperty("Range", range);
            urlconnection.connect();
            System.out.println("Response code: " + urlconnection.getResponseCode());
            ReadableByteChannel rbc = Channels.newChannel(urlconnection.getInputStream());

            file_output = new FileOutputStream(location, true);
            file_output.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            file_output.close();
            urlconnection.disconnect();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        catch(Exception e){
            System.out.println("Downloading failed due to: " + e);
        }
    }

    public void pauseDownload(){
        /*
            1.Closing the connection.
            2.Call procedures to store the file info at that point.
        */
        try{
            file_output.close();
            urlconnection.disconnect();
            System.out.println("Download paused");
            System.out.println("File size downloaded: " + getFileSize());
        }
        catch(Exception e){
            System.out.println("Pausing download failed due to: " + e);
        }

    }
    public void resumeDownload(){
        /*
            1. Calls getFileSize()
            2. sets new range to DownloadRange
            3. call startDownad()
        */
        long current_file_size;
        try{
            current_file_size = getFileSize();
            if(current_file_size <= max_range){
                setDownloadRange(current_file_size, max_range);
                System.out.println("Resuming download: " + min_range + "-" + max_range);
                startDownload();
            }
            else{
                System.out.println(String.format("Current file size %d exceeds %d", current_file_size, max_range));
            }

        }
        catch(Exception e){
            System.out.println("Resuming download failed due to: " + e);
        }
    }

    public long getDownloadSize(){
        try{
            long dwnld_size = 0;
            URL url = new URL(file_url);
            urlconnection = (HttpURLConnection) url.openConnection();
            urlconnection.setRequestMethod("HEAD");
            urlconnection.connect();
            dwnld_size = urlconnection.getContentLength();
            urlconnection.disconnect();
            return dwnld_size;
        }
        catch(Exception e){
            System.out.println("Failed to retrive download size due to: " + e);
            return -1;
        }
    }

    public double getDwnldProgress(){
        return 0.0;
    }
    public long getFileSize(){return 0;}
}