package com.maxlife.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.maxlife.R;
import com.maxlife.fragments.UploadCameraFragment;
import com.maxlife.fragments.UploadGalleryFragment;
import com.maxlife.fragments.UploadTextFragment;
import com.maxlife.fragments.UploadVideoFragment;
import com.maxlife.utils.Constants;

public class UploadActivity extends BaseActivity {

    public RelativeLayout tab1;
    private RelativeLayout tab2;
    public RelativeLayout tab3;
    public RelativeLayout tab4;

    private View tabSelectedView1V;
    private View tabSelectedView2V;
    private View tabSelectedView3V;
    private View tabSelectedView4V;

    private View divider1;
    private View divider2;
    private View divider3;

    private int tab_type;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_upload_new);

        getSupportActionBar().setTitle(getString(R.string.gallery));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tab1 = (RelativeLayout) findViewById(R.id.tab1);
        tab2 = (RelativeLayout) findViewById(R.id.tab2);
        tab3 = (RelativeLayout) findViewById(R.id.tab3);
        tab4 = (RelativeLayout) findViewById(R.id.tab4);

        tabSelectedView1V = findViewById(R.id.tabSelectedView1V);
        tabSelectedView2V = findViewById(R.id.tabSelectedView2V);
        tabSelectedView3V = findViewById(R.id.tabSelectedView3V);
        tabSelectedView4V = findViewById(R.id.tabSelectedView4V);

        divider1 = findViewById(R.id.divider1);
        divider2 = findViewById(R.id.divider2);
        divider3 = findViewById(R.id.divider3);

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.TAB_TYPE)) {
            tab_type = intent.getIntExtra(Constants.TAB_TYPE, 0);
        } else {
            tab_type = Constants.TAB_TYPE_IMAGE;
        }

        tabsInitialize(tab_type);

        tab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportActionBar().setTitle(getString(R.string.gallery));
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.TAB_TYPE, tab_type);
                loadChildFragment(new UploadGalleryFragment(), bundle);
                tabSelectedView1V.setVisibility(View.VISIBLE);
                tabSelectedView2V.setVisibility(View.GONE);
                tabSelectedView3V.setVisibility(View.GONE);
                tabSelectedView4V.setVisibility(View.GONE);
            }
        });

        tab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportActionBar().setTitle(R.string.text);
                tabSelectedView1V.setVisibility(View.GONE);
                tabSelectedView2V.setVisibility(View.VISIBLE);
                tabSelectedView3V.setVisibility(View.GONE);
                tabSelectedView4V.setVisibility(View.GONE);
            }
        });

        tab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportActionBar().setTitle(R.string.camera);
                loadChildFragment(new UploadCameraFragment(), null);
                tabSelectedView1V.setVisibility(View.GONE);
                tabSelectedView2V.setVisibility(View.GONE);
                tabSelectedView3V.setVisibility(View.VISIBLE);
                tabSelectedView4V.setVisibility(View.GONE);
            }
        });

        tab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appData.galleryTabSelected = 4;
                startActivity(getIntent());
                overridePendingTransition(0, 0);
                (UploadActivity.this).finish();
                tabSelectedView1V.setVisibility(View.GONE);
                tabSelectedView2V.setVisibility(View.GONE);
                tabSelectedView3V.setVisibility(View.GONE);
                tabSelectedView4V.setVisibility(View.VISIBLE);

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadChildFragment(Fragment fragment, Bundle bundle) {
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.uploadLL, fragment).commitAllowingStateLoss();
    }

    private void tabsInitialize(int tab_type) {
        if (tab_type == Constants.TAB_TYPE_IMAGE) {
            tab1.setVisibility(View.VISIBLE);
            tab2.setVisibility(View.GONE);
            tab3.setVisibility(View.VISIBLE);
            tab4.setVisibility(View.GONE);

            divider1.setVisibility(View.VISIBLE);
            divider2.setVisibility(View.GONE);
            divider3.setVisibility(View.GONE);

        } else if (tab_type == Constants.TAB_TYPE_VIDEO) {
            tab1.setVisibility(View.VISIBLE);
            tab2.setVisibility(View.GONE);
            tab3.setVisibility(View.GONE);
            tab4.setVisibility(View.VISIBLE);

            divider1.setVisibility(View.VISIBLE);
            divider2.setVisibility(View.GONE);
            divider3.setVisibility(View.GONE);

        } else if (tab_type == Constants.TAB_TYPE_MESSAGE) {
            tab1.setVisibility(View.GONE);
            tab2.setVisibility(View.VISIBLE);
            tab3.setVisibility(View.GONE);
            tab4.setVisibility(View.GONE);

            divider1.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);
            divider3.setVisibility(View.GONE);
        }

        if (appData != null && appData.galleryTabSelected == 4) {

            getSupportActionBar().setTitle(getString(R.string.video));
            loadChildFragment(new UploadVideoFragment(), null);
            tabSelectedView1V.setVisibility(View.GONE);
            tabSelectedView2V.setVisibility(View.GONE);
            tabSelectedView3V.setVisibility(View.GONE);
            tabSelectedView4V.setVisibility(View.VISIBLE);
            appData.galleryTabSelected = 0;

        } else if (tab_type == Constants.TAB_TYPE_VIDEO || tab_type == Constants.TAB_TYPE_IMAGE){

            getSupportActionBar().setTitle(getString(R.string.gallery));
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.TAB_TYPE, tab_type);
            loadChildFragment(new UploadGalleryFragment(), bundle);

        } else if (tab_type == Constants.TAB_TYPE_MESSAGE){

            getSupportActionBar().setTitle(getString(R.string.text));
            loadChildFragment(new UploadTextFragment(), null);
            tabSelectedView2V.setVisibility(View.VISIBLE);

        }

    }

}
