package com.maxlife.fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.activity.BaseActivity;
import com.maxlife.activity.MainActivity;
import com.maxlife.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

/**
 * Created by anshul.mittal on 7/5/16.
 */
public class ProfileFragment extends BaseFragment {

    private TextView firstNameTV;
    private TextView lastNameTV;
    private TextView emailTV;
    private TextView contactNoTV;
    private ImageView profileImageIV;
    private ImageView bannerIV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ((MainActivity) baseActivity).getSupportActionBar().setTitle(R.string.capital_profile);
        ((MainActivity) baseActivity).setTitle(getString(R.string.capital_profile));

        firstNameTV = (TextView) view.findViewById(R.id.firstNameTV);
        lastNameTV = (TextView) view.findViewById(R.id.lastNameTV);
        emailTV = (TextView) view.findViewById(R.id.emailTV);
        contactNoTV = (TextView) view.findViewById(R.id.contactNoTV);
        profileImageIV = (ImageView) view.findViewById(R.id.profileImageIV);
        bannerIV = (ImageView) view.findViewById(R.id.bannerIV);

        setHasOptionsMenu(true);

        if (appData != null && appData.profileData != null) {
            firstNameTV.setText(appData.profileData.first_name);
            lastNameTV.setText(appData.profileData.last_name);
            emailTV.setText(appData.profileData.email);
            if (!appData.profileData.contact_no.equalsIgnoreCase("null") && !appData.profileData.contact_no.equals("")) {
                contactNoTV.setText(appData.profileData.contact_no);
            }
            try {
                new ImageDownloaderTask(appData.profileData.image_file).execute();
                Picasso.with(baseActivity).load(appData.profileData.image_file).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(profileImageIV);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkApi();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile:
                ((MainActivity) baseActivity).loadFragment(new EditProfileFragment());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    class ImageDownloaderTask extends AsyncTask<Void, Void, Bitmap> {
        String imageFile;

        public ImageDownloaderTask(String url) {
            imageFile = url;

        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return baseActivity.downloadBitmap(imageFile);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (bitmap != null) {
                if (isAdded()) {
                    Bitmap blurredBitmap;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        blurredBitmap = BaseActivity.BlurBuilder.blur(baseActivity, bitmap);
                    } else {
                        blurredBitmap = baseActivity.fastblur(bitmap, 0.4f, 3);
                    }
                    bannerIV.setImageBitmap(blurredBitmap);
                }
            }
        }
    }
}
