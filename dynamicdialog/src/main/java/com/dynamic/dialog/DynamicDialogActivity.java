package com.dynamic.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dynamic.dialog.Resource.PhpLinks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class DynamicDialogActivity extends AppCompatActivity implements View.OnClickListener, Animation.AnimationListener {

    private PhpLinks phpLinks = new PhpLinks();
    private Context context = this;
    public static List<Bitmap> listOfBitmap;
    public static List<String> listOfAppLink;
    private ImageView showImageView;
    private RequestQueue requestQueue, requestCountryQueue;
    private SharedPreferences sharedPreferencesCountry;
    private boolean connectionCheck = false;
    private Animation blinkAnimation;
    private String countryName = "";
    private File bannerDirectory;
    public static int i=0;
    public static int size;
    public static Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_dialog);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        listOfBitmap = new ArrayList<Bitmap>();
        listOfAppLink = new ArrayList<String>();
        blinkAnimation = new AlphaAnimation(1.0f,0.7f);
        blinkAnimation.setDuration(500); // duration - half a second
        blinkAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        blinkAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        blinkAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back i
        showImageView = (ImageView) findViewById(R.id.home_image);
        showImageView.startAnimation(blinkAnimation);
        showImageView.setOnClickListener(this);
        Log.d("WebViewClient", "onPageStarted");
        connectionCheck = isNetworkAvailable();
        i = 0 ;
        timer = new Timer();
        if (connectionCheck) {
            makeCountryReq(context);
            makeMediumBannerStringReq(context, showImageView, blinkAnimation);
            makeSmallBannerStringReq(context, showImageView, blinkAnimation);
        }
    }
    /**
     * Making Medium Banner json object request
     * */
    public void makeMediumBannerStringReq(final Context context1, ImageView imageView, Animation animation) {
        listOfBitmap = new ArrayList<Bitmap>();
        listOfAppLink = new ArrayList<String>();
        context = context1;
        timer = new Timer();
        blinkAnimation = animation ;
        requestQueue = Volley.newRequestQueue(context);
        requestCountryQueue = Volley.newRequestQueue(context);
        makeCountryReq(context);
        showImageView = imageView;

        sharedPreferencesCountry = PreferenceManager.getDefaultSharedPreferences(context);
        countryName = sharedPreferencesCountry.getString("USER_COUNTRY_NAME", "India");

        if (countryName.equals("India")) {
            showImageView.setVisibility(View.VISIBLE);
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    phpLinks.MEDIUMBANNER_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("Res :", response.toString());
                    try {
                        bannerDirectory = new File(context.getFilesDir().getAbsolutePath()+"/BannerDirectory");
                        if(!bannerDirectory.exists()){
                            Log.d("d", "directory does not exists");
                            Log.d("d" , "directory creating "+bannerDirectory.mkdirs());
                        }
                        JSONObject jObject = new JSONObject(response.toString());
                        JSONArray jsonArray = jObject.getJSONArray("result");
                        int length = jsonArray.length();

                        for (int i = 0; i < length; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            final String offerId = jsonObject.getString("id");
                            final String offerLink = jsonObject.getString("link");
                            final String offerImage = jsonObject.getString("image");
                            final File file = new File(bannerDirectory.getAbsolutePath() + "/" +offerId);
                            Log.d("d", "new file path "+file.getAbsolutePath());
                            if(file.exists()){
                                Log.d("d", "file exists");
                                listOfBitmap.add(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                listOfAppLink.add(offerLink);
                                if(i == 0) {
                                    new Thread(){
                                        public void run(){
                                            try{
                                                runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
//                                                        showImageView.setAnimation(blinkAnimation);
                                                        showImageView.setImageBitmap(listOfBitmap.get(0));
                                                        showImageView.setContentDescription(listOfAppLink.get(0));
                                                    }
                                                });
                                            }catch(Exception e){
                                                Log.d("d", "EXception in array index .....");
                                            }
                                        }
                                    }.start();
                                }
                                //Log.d("d", "file deleted "+file.delete());
                            } else {
                                Glide.with(context)
                                        .load(offerImage)
                                        .asBitmap()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                                saveFile(context, resource, file, offerLink);
//                                                imgWebsite.setImageBitmap(resource);

                                            }
                                        });
                            }
                        }
                        size = listOfAppLink.size();

                        if(size > 0 ){
                            int delay = 1000;
                            int period = 8000;
                            timer.scheduleAtFixedRate(new TimerTask() {

                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(i >= size) {
                                                i=0;
                                            }
                                            else {
                                                Log.d("d", "i"+i);
                                                try{
                                                    //appImage.setImageBitmap(null);
//                                                    Toast.makeText(context, "No of Records... : "+listOfAppLink.get(i), Toast.LENGTH_LONG).show();
//                                                    showImageView.setAnimation(blinkAnimation);
                                                    showImageView.setImageBitmap(listOfBitmap.get(i));
                                                    showImageView.setContentDescription(listOfAppLink.get(i));
                                                    i++;
                                                }catch(IndexOutOfBoundsException ee){
                                                    Log.d("d", "Array index exception "+ee.getMessage());
                                                }
                                                catch(Exception e ){
                                                    Log.d("d", "EXCEPTION " +e.getMessage());
                                                }

                                            }
                                        }
                                    });
                                }
                            }, delay, period);
                        }
                    } catch (Exception e) {
                        Log.d("JSON Exception1", e.toString());
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Res :", "Error: " + error.getMessage());
                }
            });
            // Adding request to request queue
            requestQueue.add(strReq);
        } else {
            showImageView.setVisibility(View.GONE);
        }
    }

    public static void saveFile(Context context, Bitmap b, File fileR, String imgLink){
        FileOutputStream fos;
        try {
            File imageFile = new File(fileR.getAbsolutePath());
            if(!imageFile.exists()){
                fos = new FileOutputStream(imageFile);
                b.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                fos.flush();
                listOfBitmap.add(b);
                listOfAppLink.add(imgLink);
            }
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "file not found");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d(TAG, "io exception");
            e.printStackTrace();
        } finally {

        }
    }
    /**
     * Making Medium Banner json object request
     * */
    public void makeSmallBannerStringReq(final Context context1, ImageView imageView, Animation animation) {
        listOfBitmap = new ArrayList<Bitmap>();
        listOfAppLink = new ArrayList<String>();
        context = context1;
        timer = new Timer();
        blinkAnimation = animation ;
        requestQueue = Volley.newRequestQueue(context);
        requestCountryQueue = Volley.newRequestQueue(context);
        makeCountryReq(context);
        showImageView = imageView;
        sharedPreferencesCountry = PreferenceManager.getDefaultSharedPreferences(context);
        countryName = sharedPreferencesCountry.getString("USER_COUNTRY_NAME", "India");

        if (countryName.equals("India")) {
            showImageView.setVisibility(View.VISIBLE);
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    phpLinks.SMALLBANNER_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("Res :", response.toString());
                    try {
                        bannerDirectory = new File(context.getFilesDir().getAbsolutePath()+"/BannerDirectory");
                        if(!bannerDirectory.exists()){
                            Log.d("d", "directory does not exists");
                            Log.d("d" , "directory creating "+bannerDirectory.mkdirs());
                        }
                        JSONObject jObject = new JSONObject(response.toString());
                        JSONArray jsonArray = jObject.getJSONArray("result");
                        int length = jsonArray.length();

                        for (int i = 0; i < length; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            final String offerId = jsonObject.getString("id");
                            final String offerLink = jsonObject.getString("link");
                            final String offerImage = jsonObject.getString("image");
                            final File file = new File(bannerDirectory.getAbsolutePath() + "/" +offerId);
                            Log.d("d", "new file path "+file.getAbsolutePath());
                            if(file.exists()){
                                Log.d("d", "file exists");
                                listOfBitmap.add(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                listOfAppLink.add(offerLink);
                                if(i == 0) {
                                    new Thread(){
                                        public void run(){
                                            try{
                                                runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        showImageView.setAnimation(blinkAnimation);
                                                        showImageView.setImageBitmap(listOfBitmap.get(0));
                                                        showImageView.setContentDescription(listOfAppLink.get(0));
                                                    }
                                                });
                                            }catch(Exception e){
                                                Log.d("d", "EXception in array index .....");
                                            }
                                        }
                                    }.start();
                                }
                                //Log.d("d", "file deleted "+file.delete());
                            } else {
                                Glide.with(context)
                                        .load(offerImage)
                                        .asBitmap()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                                saveFile(context, resource, file, offerLink);
//                                                imgWebsite.setImageBitmap(resource);

                                            }
                                        });
                            }
                        }
                        size = listOfAppLink.size();

                        if(size > 0 ){
                            int delay = 1000;
                            int period = 8000;
                            timer.scheduleAtFixedRate(new TimerTask() {

                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(i >= size) {
                                                i=0;
                                            }
                                            else {
                                                Log.d("d", "i"+i);
                                                try{
                                                    //appImage.setImageBitmap(null);
//                                                    Toast.makeText(context, "No of Records... : "+listOfAppLink.get(i), Toast.LENGTH_LONG).show();
                                                    showImageView.setAnimation(blinkAnimation);
                                                    showImageView.setImageBitmap(listOfBitmap.get(i));
                                                    showImageView.setContentDescription(listOfAppLink.get(i));
                                                    i++;
                                                }catch(IndexOutOfBoundsException ee){
                                                    Log.d("d", "Array index exception "+ee.getMessage());
                                                }
                                                catch(Exception e ){
                                                    Log.d("d", "EXCEPTION " +e.getMessage());
                                                }
                                            }
                                        }
                                    });
                                }
                            }, delay, period);
                        }
                    } catch (Exception e) {
                        Log.d("JSON Exception1", e.toString());
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Res :", "Error: " + error.getMessage());
                }
            });
            // Adding request to request queue
            requestQueue.add(strReq);
        } else {
            showImageView.setVisibility(View.GONE);
        }
    }

    public void makeCountryReq(Context countryContext) {
        StringRequest strReq = new StringRequest(Request.Method.GET,
                phpLinks.COUNTRY_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("Res :", response.toString());
                try {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String countryN = jsonObject.getString("country");
                    SharedPreferences.Editor userCountryN = sharedPreferencesCountry.edit();
                    userCountryN.putString("USER_COUNTRY_NAME", countryN);
                    userCountryN.commit();
                } catch (Exception e) {
                    Log.d("JSON Exception1", e.toString());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Res :", "Error: " + error.getMessage());
            }
        });
        // Adding request to request queue
        requestCountryQueue.add(strReq);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.home_image){
            if(!isNetworkAvailable()){
            }else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse((String)view.getContentDescription()));
                startActivity(browserIntent);
            }
        }
    }
    @Override
    public void onAnimationEnd(Animation arg0) {
    }

    @Override
    public void onAnimationRepeat(Animation arg0) {
    }

    @Override
    public void onAnimationStart(Animation arg0) {
        // TODO Auto-generated method stub
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("d", "inside on stop ");
        timer.cancel();
    }

}
