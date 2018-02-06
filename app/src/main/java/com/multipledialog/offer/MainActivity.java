package com.multipledialog.offer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.dynamic.dialog.DynamicDialogActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Animation.AnimationListener{

    private Context context = this;
    private ImageView smallImage;
    private ImageView bannerImage;
    Animation blinkAnimation;
    DynamicDialogActivity dynamicDialogActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smallImage = (ImageView) findViewById(R.id.small_image);
        bannerImage = (ImageView) findViewById(R.id.banner_image);
        blinkAnimation = new AlphaAnimation(1.0f, 0.7f);
        blinkAnimation.setDuration(500);
        blinkAnimation.setInterpolator(new LinearInterpolator());
        blinkAnimation.setRepeatCount(Animation.INFINITE);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        smallImage.startAnimation(blinkAnimation);
//        bannerImage.startAnimation(blinkAnimation);
        smallImage.setOnClickListener(this);
        bannerImage.setOnClickListener(this);

        DynamicDialogActivity.i = 0 ;
        dynamicDialogActivity = new DynamicDialogActivity();
        try {
//            dynamicDialogActivity.makeMediumBannerStringReq(context, bannerImage, blinkAnimation);
            dynamicDialogActivity.makeSmallBannerStringReq(context, smallImage, blinkAnimation);

        } catch (Exception e) {
            Log.d("d", "Exception occured " + e.getMessage());
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.small_image){
            if(!isNetworkAvailable()){
            }else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String)view.getContentDescription()));
                startActivity(browserIntent);
            }
        } else if(id == R.id.banner_image){
            if(!isNetworkAvailable()){
            }else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String)view.getContentDescription()));
                startActivity(browserIntent);
            }
        }
    }
    @Override
    protected void onDestroy() {
        Log.d("d", "Inside on destroy main activity ");
        DynamicDialogActivity.timer.cancel();
        super.onDestroy();
    }
}
