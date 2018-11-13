package com.maxlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.maxlife.R;
import com.maxlife.utils.Constants;
import com.maxlife.utils.ZoomImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by anshul.mittal on 18/12/15.
 */
public class FullScreenImageFragment extends BaseFragment {

    private View view;
    private ImageButton closeFullScreenBT;
    private ZoomImageView fullScreenImageZIV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_full_screen_image, container, false);
        setHasOptionsMenu(true);

        fullScreenImageZIV = (ZoomImageView) view.findViewById(R.id.fullScreenImageZIV);
        closeFullScreenBT = (ImageButton) view.findViewById(R.id.closeFullScreenBT);

        closeFullScreenBT.setVisibility(View.GONE);
        displayImage();
        closeFullScreenBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = getArguments();
                if (bundle != null && bundle.containsKey("imagePosition")) {
                    appData.uploadImagesList.get(bundle.getInt("imagePosition")).uploadImng=null;
                    appData.uploadImagesList.get(bundle.getInt("imagePosition")).caption="";
                }
                baseActivity.onBackPressed();
            }
        });

        return view;
    }

    private void displayImage() {
        Bundle bundle = getArguments();
        if (bundle != null){

            if (bundle.getInt("isLocal") == Constants.LOCAL_IMAGE) {
                Picasso.with(baseActivity).load(new File(bundle.getString("imageUrl"))).into(fullScreenImageZIV, new Callback() {
                    @Override
                    public void onSuccess() {
                        closeFullScreenBT.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        baseActivity.showToast(baseActivity.getString(R.string.no_image_found));
                        closeFullScreenBT.setVisibility(View.GONE);
                    }
                });
            }else if (bundle.getInt("isLocal") == Constants.REMOTE_IMAGE){
                Picasso.with(baseActivity).load(bundle.getString("imageUrl")).placeholder(R.mipmap.profile_camera).into(fullScreenImageZIV, new Callback() {
                    @Override
                    public void onSuccess() {
                        closeFullScreenBT.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        baseActivity.showToast(baseActivity.getString(R.string.no_image_found));
                    }
                });
            }
        }
    }



}
