package com.mhassanetimadi.CRIDA_e_teamwork;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by M-Qasim on 2/17/2017.
 */

public class PicHelper {

    public static void setImage(final Context context, final String url, final ImageView iv) {

        Picasso.with(context).load(url).networkPolicy(NetworkPolicy.OFFLINE).into(iv, new Callback() {
                    @Override
                    public void onSuccess() {
                        // do nothing
                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(url).into(iv);
                    }
                });
    }
}
