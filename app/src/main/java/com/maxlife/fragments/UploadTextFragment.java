package com.maxlife.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.activity.MainActivity;
import com.maxlife.utils.Constants;
import com.maxlife.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UploadTextFragment extends BaseFragment {

    private EditText textET;
    private TextView countTV;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_upload_text, container, false );

        ImageView profileImageIV = (ImageView) view.findViewById(R.id.profileImageIV);
        TextView nameTV = (TextView) view.findViewById(R.id.nameTV);
        TextView dateTV = (TextView) view.findViewById(R.id.dateTV);

        textET = (EditText) view.findViewById( R.id.textET );
        Button postBT = (Button) view.findViewById(R.id.postBT);
        ImageView clearMsgIV = (ImageView) view.findViewById(R.id.clearMsgIV);
        countTV = (TextView) view.findViewById(R.id.countTV);

        dateTV.setText(getCurrentFormattedDate().replace(".", ""));
        if (appData != null && appData.profileData != null) {
            nameTV.setText(appData.profileData.full_name);
            if (appData.profileData.image_file != null && !appData.profileData.image_file.equals("")) {
                Picasso.with(baseActivity).load(appData.profileData.image_file).transform(new RoundedTransformation(100, 0)).resize(200, 200).into(profileImageIV);
            }
        }

        postBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textET.getText().toString().trim().equals( "" )) {
                    baseActivity.showToast( getString( R.string.please_enter_message_to_post ) );
                } else if (textET.getText().toString().length() > 50000 && appData.profileData.is_membership ==1) {
                    baseActivity.showToast( getString( R.string.maximum_50000_characters_allowed ) );
                }
                else if (textET.getText().toString().length() > 10000 && appData.profileData.is_membership ==0) {
                    baseActivity.showToast( getString( R.string.maximum_10000_characters_allowed ) );
                } else {

                    if (appData != null && appData.mode.equalsIgnoreCase( "edit" )) {
                        if (appData.modePostType == Constants.TYPE_IMAGE) {
                            baseActivity.showToast( baseActivity.getString( R.string.editing_image_post ) );
                        } else if (appData.modePostType == Constants.TYPE_VIDEO) {
                            baseActivity.showToast( baseActivity.getString( R.string.editing_video_post_ ) );
                        }
                    } else {
                        Intent intent = new Intent( baseActivity, MainActivity.class );
                        intent.putExtra( "newConfirmType", Constants.TYPE_MESSAGE );
                        intent.putExtra( "text", textET.getText().toString() );
                        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
                        baseActivity.startActivity( intent );
                        baseActivity.finish();
                    }

                }
            }
        } );

        clearMsgIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textET.setText("");
            }
        });

        textET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {

                countTV.setText("" + textET.getText().length() + "/50000");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    public String getCurrentFormattedDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdfOutput = new SimpleDateFormat("d/MM/yyyy, hh:mm a", Locale.getDefault());

        return sdfOutput.format(c.getTime());
    }
    @Override
    public void onResume() {
        super.onResume();
    }
}
