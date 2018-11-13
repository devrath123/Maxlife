package com.maxlife.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.adapters.NavigationAdapter;
import com.maxlife.adapters.PlanAdapter;
import com.maxlife.data.NavItemsData;
import com.maxlife.data.PlanData;
import com.maxlife.fragments.AboutUsFragment;
import com.maxlife.fragments.ChangePasswordFragment;
import com.maxlife.fragments.EditProfileFragment;
import com.maxlife.fragments.FullScreenImageFragment;
import com.maxlife.fragments.HomeFragment;
import com.maxlife.fragments.ProfileFragment;
import com.maxlife.fragments.SettingsFragment;
import com.maxlife.fragments.UploadConfirmFragment;
import com.maxlife.utils.Constants;
import com.maxlife.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;
import com.toxsl.volley.toolbox.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements BaseActivity.PermCallback {

    public DrawerLayout mDrawerLayout;
    public LinearLayout mLeftDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList, planLV;
    NavItemsData navData;

    private EditText buyPlanET;
    private Button continueBT;
    private LinearLayout profileLL;
    public TextView userNameTV, planTV, viewProfileTV;
    public ImageView drawerProfileImageIV, planEditIV;
    private boolean isFirst;
    private ArrayList<PlanData> plan_list = new ArrayList<>();
    private PlanAdapter listAdapter;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        isFirst = true;

        setPermCallback(this);

        initDrawer();
        viewProfileTV = (TextView) findViewById(R.id.viewProfileTV);
        profileLL = (LinearLayout) findViewById(R.id.profileLL);
        userNameTV = (TextView) findViewById(R.id.userNameTV);
        planTV = (TextView) findViewById(R.id.planTV);
        drawerProfileImageIV = (ImageView) findViewById(R.id.drawerProfileImageIV);
        planEditIV = (ImageView) findViewById(R.id.planEditIV);
        planLV = (ListView) findViewById(R.id.planLV);
        listAdapter = new PlanAdapter(MainActivity.this, plan_list, this);


        if (appData.profileData != null) {
            userNameTV.setText(appData.profileData.full_name);
            try {
                if (appData.profileData.image_file != null && !appData.profileData.image_file.equals("")) {
                    Picasso.with(this).load(appData.profileData.image_file).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(drawerProfileImageIV);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        viewProfileTV.setTypeface(font, Typeface.NORMAL);

        profileLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new ProfileFragment());
            }
        });


        planTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (planLV.getVisibility() == View.VISIBLE) {
                    planLV.setVisibility(View.GONE);
                } else {
                    planLV.setVisibility(View.VISIBLE);
                }
            }
        });


        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("isPush")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(getString(R.string.are_you_alive));
            builder.setNeutralButton(getString(R.string.yes_i_am_alive), new AliveDialogInterface(bundle) {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (bundle.containsKey("id") && !bundle.getString("id").isEmpty()) {
                        aliveResponseApi(bundle.getString("id"));
                    } else {
                        showToast(getString(R.string.there_is_some_problem));
                    }
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }

    }

    private void getPlans() {
        syncManager.sendToServer("api/user/plan-list", null, this);
    }


    private void aliveResponseApi(String notificationId) {
        RequestParams params = new RequestParams();
        params.put("Notification[id]", notificationId);
        syncManager.sendToServer("api/user/response", params, this);
        startDialog();
    }

    public void editPlanTitle( int pos) {
        dialog = getDialog();
        buyPlanET = (EditText) dialog.findViewById(R.id.buyPlanET);
        continueBT = (Button) dialog.findViewById(R.id.continueBT);
        continueBT.setOnClickListener(new MyClickListener(pos) {
            @Override
            public void onClick(View view) {
                if (buyPlanET.getText().toString().isEmpty()) {
                    showToast("Please Enter Plan Name");
                } else {
                    mDrawerLayout.closeDrawer(mLeftDrawer);
                    RequestParams params = new RequestParams();
                    params.put("Membership[title]", buyPlanET.getText().toString());
                    syncManager.sendToServer("api/user/membership-update?id=" + plan_list.get(position).plan_id, params, MainActivity.this);
                    dialog.dismiss();
                }

            }
        });
    }

    public void setPlan(int pos) {
        planTV.setText(plan_list.get(pos).name);
        prefStore.saveString("plan_id", plan_list.get(pos).plan_id);
        syncManager.sendToServer("api/post/active-plan?id=" + plan_list.get(pos).plan_id, null, this);
    }


    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);

        ArrayList<NavItemsData> navDrawerItems = new ArrayList<>();

        String[] navigationMenu = getResources().getStringArray(R.array.NavigationMenu);
        int[] navDrawerImage = {
                R.mipmap.ic_drawer_home,
                R.mipmap.ic_drawer_settings,
                R.mipmap.ic_about_us,
                R.mipmap.ic_drawer_logout,
                R.drawable.ic_dollar
        };
        for (int i = 0; i < navigationMenu.length; i++) {
            navData = new NavItemsData();
            navData.title = navigationMenu[i];
            navData.icon = navDrawerImage[i];
            navDrawerItems.add(navData);
        }

        NavigationAdapter navigationAdapter = new NavigationAdapter(this, navDrawerItems);
        mDrawerList.setAdapter(navigationAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
                hideKeyboard();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
                hideKeyboard();
            }
        };

        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        planLV.setAdapter(listAdapter);
        getPlans();
        Intent intent = getIntent();
        if (intent.hasExtra("transaction_id")) {
            saveRecordOnServer(intent.getStringExtra("transaction_id"), intent.getStringExtra("plan_id"));
        }
        if (intent.hasExtra("newConfirmType") && appData != null && appData.mode != "edit") {
            if (intent.getIntExtra("newConfirmType", 0) == Constants.TYPE_MESSAGE) {
                UploadConfirmFragment fragment = new UploadConfirmFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", Constants.TYPE_MESSAGE);
                bundle.putString("text", intent.getStringExtra("text"));
                fragment.setArguments(bundle);
                loadFragment(fragment);
                isFirst = false;
            } else {
                UploadConfirmFragment fragment = new UploadConfirmFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", intent.getIntExtra("newConfirmType", Constants.TYPE_IMAGE));
                fragment.setArguments(bundle);
                loadFragment(fragment);
                isFirst = false;
            }
            getIntent().removeExtra("newConfirmType");
        } else if (isFirst) {
            loadFragment(new HomeFragment());
            isFirst = false;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        getIntent().removeExtra("newConfirmType");
    }

    private void saveRecordOnServer(String transaction_id, String plan_id) {
        String action = "api/user/membership";
        RequestParams params = new RequestParams();
        params.put("Membership[plan_id]", plan_id);
        params.put("Membership[transaction_id]", transaction_id);
        params.put("Membership[title]", prefStore.getString("plan_buy_for"));
        syncManager.sendToServer(action, params, this);
        startDialog();
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).addToBackStack(null).commitAllowingStateLoss();
        mDrawerLayout.closeDrawer(mLeftDrawer);
    }

    @Override
    public void permGranted() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (f instanceof EditProfileFragment) {
            ((EditProfileFragment) f).openGallery();
        } else {
            Intent intent = new Intent(this, UploadActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void permDenied() {

    }

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

        public void selectItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new HomeFragment();
                    break;
                case 1:
                    fragment = new SettingsFragment();
                    break;
                case 2:
                    fragment = new AboutUsFragment();
                    break;
                case 3:
                    logoutApi();
                    break;
                case 4:
                    Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).addToBackStack(null).commitAllowingStateLoss();
            }

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mLeftDrawer);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (fragment instanceof ProfileFragment) {
            menuInflater.inflate(R.menu.menu_profile, menu);
        }

        if (fragment instanceof ChangePasswordFragment
                || fragment instanceof EditProfileFragment
                || fragment instanceof FullScreenImageFragment) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            planLV.setVisibility(View.GONE);
            return true;
        }
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof HomeFragment) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.are_you_sure_you_want_to_exit))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            exitFromApp();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                loadFragment(new HomeFragment());
            } else {
                super.onBackPressed();
            }
        }
        hideKeyboard();
        mDrawerLayout.closeDrawer(mLeftDrawer);
    }

    @Override
    public void onSyncSuccess(String controller, String action, boolean status, JSONObject jsonObject) {
        super.onSyncSuccess(controller, action, status, jsonObject);
        if (controller.equals("user") && action.equals("membership")) {
            if (status) {
                getPlans();
                showToast(getString(R.string.pay));
                if (appData.profileData.is_membership == 0) {
                    appData.profileData.is_membership = 1;
                    isMessagePost = false;
                    Intent intent = new Intent(this, MainActivity.class);
                    loadFragment(new HomeFragment());
                    startActivity(intent);
                }
                getIntent().removeExtra("transaction_id");
            } else {
                String errors = "Unexpected Error Occured";
                if (jsonObject.has("error")) {
                    errors = jsonObject.optString("error");
                }
                showToast(errors);
            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("response")) {
            if (status) {

            } else {

            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("membership-update")) {
            if (status) {
                getPlans();
                showToast(getString(R.string.plan_name_changed));
            } else {
                showToast(getString(R.string.already_taken));
            }
        } else if (controller.equalsIgnoreCase("post") && action.equalsIgnoreCase("active-plan")) {
            if (status) {
                getPlans();
                planLV.setVisibility(View.GONE);
                showToast("Plan Activated !");
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        } else if (controller.equalsIgnoreCase("user") && action.equalsIgnoreCase("plan-list")) {
            if (status) {
                plan_list.clear();
                JSONArray array = null;
                try {
                    array = jsonObject.getJSONArray("detail");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int n = 0; n < array.length(); n++) {
                    try {
                        JSONObject object = array.getJSONObject(n);
                        if (object.getString("state_id").equals("1")) {
                            planTV.setText(object.getString("title"));
                            prefStore.saveString("plan_id", object.getString("id"));
                        }
                        PlanData planData = new PlanData();
                        planData.name = object.getString("title");
                        planData.plan_id = object.getString("id");

                        plan_list.add(planData);
                        listAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public abstract class AliveDialogInterface implements DialogInterface.OnClickListener {

        Bundle bundle;

        public AliveDialogInterface(Bundle bundle) {
            this.bundle = bundle;
        }

    }
    public class MyClickListener implements  View.OnClickListener {


        int position;

        public MyClickListener( int position) {
            this.position = position;

        }

        @Override
        public void onClick(View view) {

        }
    }

}
