package com.kyadav.DhaamDhoom.Services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.kyadav.DhaamDhoom.Main_Menu.MainMenuActivity;
import com.kyadav.DhaamDhoom.SimpleClasses.Variables;
import com.kyadav.DhaamDhoom.Video_Recording.AnimatedGifEncoder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

// this the background service which will upload the video into database
public class Upload_Service extends Service {


    static int serverResponseCode = 0;
    private final IBinder mBinder = new LocalBinder();
    boolean mAllowRebind;
    ServiceCallback Callback;
    String result = "";
    Uri uri;
    String video_base64 = "", thumb_base_64 = "", Gif_base_64 = "";
    String description;
    SharedPreferences sharedPreferences;
    String sCurrentLine;

    public Upload_Service() {
        super();
    }

    public Upload_Service(ServiceCallback serviceCallback) {
        Callback = serviceCallback;
    }

    public static int upLoad2Server(String sourceFileUri) {
        String upLoadServerUri = Variables.base_url + "upload.php";

        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String responseFromServer = "";
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            Log.e("Huzza", "Source File Does not exist");
            return 0;
        }
        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data;name=\"fileToUpload\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
            Log.i("Huzza", "Initial .available : " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            ;

            while ((bytesRead = fileInputStream.read(buffer, 0, bufferSize)) > 0) {
                dos.write(buffer, 0, bytesRead);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            Log.d("serverResponseMessage", serverResponseMessage + "");
            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
//this block will give the response of upload link
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                    .getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {

            }
            rd.close();
        } catch (IOException ioex) {

        }
        return serverResponseCode;  // like 200 (Ok)

    } // end upLoad2Server

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    public void setCallbacks(ServiceCallback serviceCallback) {
        Callback = serviceCallback;
    }

    @Override
    public void onCreate() {
        sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction().equals("startservice")) {
                showNotification();

                String uri_string = intent.getStringExtra("uri");
                uri = Uri.parse(uri_string);
                description = intent.getStringExtra("desc");

                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        Bitmap bmThumbnail;
                        bmThumbnail = ThumbnailUtils.createVideoThumbnail(uri.getPath(),
                                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                        Bitmap bmThumbnail_resized = Bitmap.createScaledBitmap(bmThumbnail, (int) (bmThumbnail.getWidth() * 0.4), (int) (bmThumbnail.getHeight() * 0.4), true);

                        thumb_base_64 = Bitmap_to_base64(bmThumbnail_resized);
                        String FilePath = Variables.output_filter_file;
                        //int reponse=upLoad2Server(""+FilePath);
                        File myVideo = new File(uri.getPath());
                        Uri myVideoUri = Uri.parse(myVideo.toString());
                        final MediaMetadataRetriever mmRetriever = new MediaMetadataRetriever();
                        mmRetriever.setDataSource(myVideo.getAbsolutePath());
                        final MediaPlayer mp = MediaPlayer.create(getBaseContext(), myVideoUri);
                        final ArrayList<Bitmap> frames = new ArrayList<Bitmap>();

                        for (int i = 1000000; i < 2000 * 1000; i += 100000) {
                            Bitmap bitmap = mmRetriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                            Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4), true);
                            frames.add(resized);
                        }

                        Gif_base_64 = Base64.encodeToString(generateGIF(frames), Base64.DEFAULT);
                        JSONObject parameters = new JSONObject();
                        try {
                            video_base64 = encodeFileToBase64Binary(Variables.output_filter_file);
                            parameters.put("fb_id", sharedPreferences.getString(Variables.u_id, ""));
                            parameters.put("sound_id", Variables.Selected_sound_id);
                            parameters.put("description", description);
                            JSONObject vidoefiledata = new JSONObject();
                            vidoefiledata.put("file_data", video_base64);
                            parameters.put("videobase64", vidoefiledata);
                            JSONObject imagefiledata = new JSONObject();
                            imagefiledata.put("file_data", thumb_base_64);
                            parameters.put("picbase64", imagefiledata);


                            JSONObject giffiledata = new JSONObject();
                            giffiledata.put("file_data", Gif_base_64);
                            parameters.put("gifbase64", giffiledata);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        generateNoteOnSD("parameters", parameters.toString());
                        RequestQueue rq = Volley.newRequestQueue(Upload_Service.this);
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.POST, Variables.uploadVideo, parameters, new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {
                                        String respo = response.toString();
                                        Log.d("uploadVideo res", respo);
                                        Callback.ShowResponce("Your Video is uploaded Successfully");
                                        stopForeground(true);
                                        stopSelf();


                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("uploadVideo err", error.getMessage());
                                        Callback.ShowResponce("Your Video is upload failed");
                                        stopForeground(true);
                                        stopSelf();

                                    }
                                });

                        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        rq.getCache().clear();
                        rq.add(jsonObjectRequest);


                    }


                }).start();

            } else if (intent.getAction().equals("stopservice")) {
                stopForeground(true);
                stopSelf();
            }
        }
        return Service.START_STICKY;
    }

    // this will show the sticky notification during uploading video
    private void showNotification() {

        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        final String CHANNEL_ID = "default";
        final String CHANNEL_NAME = "Default";
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        androidx.core.app.NotificationCompat.Builder builder = (androidx.core.app.NotificationCompat.Builder) new androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle("Uploading Video")
                .setContentText("Please wait! Video is uploading....")
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        android.R.drawable.stat_sys_upload))
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        startForeground(101, notification);
    }

    // for thumbnail
    public String Bitmap_to_base64(Bitmap imagebitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagebitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] byteArray = baos.toByteArray();
        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }

    // for video base64
    private String encodeFileToBase64Binary(String fileName)
            throws IOException {

        File file = new File(fileName);
        Log.d("FL", file.getName() + " SZ:" + file.length());
        byte[] bytes = loadFile(file);
        String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encodedString;
    }

    //for video gif image
    public byte[] generateGIF(ArrayList<Bitmap> bitmaps) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        for (Bitmap bitmap : bitmaps) {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out);
            Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

            encoder.addFrame(decoded);

        }

        encoder.finish();


        File filePath = new File(Variables.app_folder, "sample.gif");
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(filePath);
            outputStream.write(bos.toByteArray());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return bos.toByteArray();
    }

    public void generateNoteOnSD(String sFileName, String sBody) {
        try {
            File root = new File(Variables.app_folder, "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName + ".txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public Upload_Service getService() {
            return Upload_Service.this;
        }
    }


}