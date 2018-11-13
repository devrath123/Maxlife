package com.maxlife.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.support.v4.os.ResultReceiver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maxlife.R;
import com.maxlife.activity.BaseActivity;
import com.maxlife.activity.MainActivity;
import com.maxlife.data.PostData;
import com.maxlife.fragments.UpdateConfirmFragment;
import com.maxlife.service.DownloadService;
import com.maxlife.utils.Constants;
import com.squareup.picasso.Picasso;
import com.toxsl.imageview.CircularImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anshul.mittal on 18/5/16.
 */
public class PostsAdapter extends ArrayAdapter<PostData> {

    private BaseActivity baseActivity;
    private ProgressDialog mProgressDialog;

    public PostsAdapter(Context context, int resource, List<PostData> objects) {
        super(context, resource, objects);

        this.baseActivity = (BaseActivity) context;

        createProgressDialog();
        baseActivity.setPermCallback(new PremissionGetResult());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = ((BaseActivity) getContext()).inflater.inflate(R.layout.adapter_home, parent, false);

            convertView.setTag(position);
            holder = new ViewHolder();
            holder.profileImageCIV = (CircularImageView) convertView.findViewById(R.id.profileImageCIV);
            holder.nameTV = (TextView) convertView.findViewById(R.id.nameTV);
            holder.dateTV = (TextView) convertView.findViewById(R.id.dateTV);
            holder.editIV = (ImageView) convertView.findViewById(R.id.editIV);

            holder.messageLL = (LinearLayout) convertView.findViewById(R.id.messageLL);
            holder.imagesLL = (LinearLayout) convertView.findViewById(R.id.imagesLL);

            holder.messageTV = (TextView) convertView.findViewById(R.id.messageTV);
            holder.image1IV = (ImageView) convertView.findViewById(R.id.image1IV);
            holder.caption1TV = (TextView) convertView.findViewById(R.id.caption1TV);
            holder.image2IV = (ImageView) convertView.findViewById(R.id.image2IV);
            holder.caption2TV = (TextView) convertView.findViewById(R.id.caption2TV);
            holder.image3IV = (ImageView) convertView.findViewById(R.id.image3IV);
            holder.caption3TV = (TextView) convertView.findViewById(R.id.caption3TV);

            holder.videoFL = (FrameLayout) convertView.findViewById(R.id.videoFL);
            holder.videoThumbIV = (ImageView) convertView.findViewById(R.id.videoThumbIV);
            holder.captionVideoTV = (TextView) convertView.findViewById(R.id.captionVideoTV);

            if (baseActivity.appData != null && baseActivity.appData.profileData != null) {
                holder.nameTV.setText(baseActivity.appData.profileData.full_name);
                if (baseActivity.appData.profileData.image_file != null && !baseActivity.appData.profileData.image_file.equals("")) {
                    Picasso.with(baseActivity).load(baseActivity.appData.profileData.image_file).into(holder.profileImageCIV);
                }
            }

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.data = getItem(position);

        holder.dateTV.setText(baseActivity.getDateFormatted(holder.data.create_time).replace(".", ""));

        setDataInView(holder);

        return convertView;
    }

    private void setDataInView(ViewHolder holder) {
        if (holder.data.type_id == Constants.TYPE_MESSAGE) {
            holder.messageLL.setVisibility(View.VISIBLE);
            holder.imagesLL.setVisibility(View.GONE);
            holder.videoFL.setVisibility(View.GONE);
            holder.messageTV.setText(holder.data.content);
            holder.captionVideoTV.setVisibility(View.GONE);
        }
        if (holder.data.type_id == Constants.TYPE_IMAGE && holder.data.image_file_list.size() > 0) {
            holder.messageLL.setVisibility(View.GONE);
            holder.imagesLL.setVisibility(View.VISIBLE);
            holder.videoFL.setVisibility(View.GONE);

            if (holder.data.image_file_list != null && holder.data.image_file_list.size() > 0) {
                if (holder.data.image_file_list.size() == 1) {
                    try {
                        Picasso.with(baseActivity).load(holder.data.image_file_list.get(0).image_file).placeholder(R.mipmap.placeholder).into(holder.image1IV);
                        Picasso.with(baseActivity).load("no image").into(holder.image2IV);
                        Picasso.with(baseActivity).load("no image").into(holder.image3IV);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    holder.image1IV.setVisibility(View.VISIBLE);
                    holder.caption1TV.setText(holder.data.image_file_list.get(0).caption);
                    holder.image2IV.setVisibility(View.GONE);
                    holder.image3IV.setVisibility(View.GONE);


                } else if (holder.data.image_file_list.size() == 2) {
                    try {
                        Picasso.with(baseActivity).load("no image").into(holder.image1IV);
                        Picasso.with(baseActivity).load(holder.data.image_file_list.get(0).image_file).placeholder(R.mipmap.placeholder).into(holder.image2IV);
                        if (holder.data.image_file_list.get(1).image_file != null && !holder.data.image_file_list.get(1).image_file.isEmpty()) {
                            Picasso.with(baseActivity).load(holder.data.image_file_list.get(1).image_file).placeholder(R.mipmap.placeholder).into(holder.image3IV);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    holder.image1IV.setVisibility(View.GONE);
                    holder.image2IV.setVisibility(View.VISIBLE);
                    holder.caption2TV.setText(holder.data.image_file_list.get(0).caption);
                    holder.image3IV.setVisibility(View.VISIBLE);
                    holder.caption3TV.setText(holder.data.image_file_list.get(1).caption);


                } else if (holder.data.image_file_list.size() >= 3) {
                    try {
                        if (!holder.data.image_file_list.get(0).image_file.isEmpty()) {
                            Picasso.with(baseActivity).load(holder.data.image_file_list.get(0).image_file).placeholder(R.mipmap.placeholder).into(holder.image1IV);
                        }
                        if (!holder.data.image_file_list.get(1).image_file.isEmpty()) {
                            Picasso.with(baseActivity).load(holder.data.image_file_list.get(1).image_file).placeholder(R.mipmap.placeholder).into(holder.image2IV);
                        }
                        if (!holder.data.image_file_list.get(2).image_file.isEmpty()) {
                            Picasso.with(baseActivity).load(holder.data.image_file_list.get(2).image_file).placeholder(R.mipmap.placeholder).into(holder.image3IV);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    holder.image1IV.setVisibility(View.VISIBLE);
                    holder.caption1TV.setText(holder.data.image_file_list.get(0).caption);
                    holder.image2IV.setVisibility(View.VISIBLE);
                    holder.caption2TV.setText(holder.data.image_file_list.get(1).caption);
                    holder.image3IV.setVisibility(View.VISIBLE);
                    holder.caption3TV.setText(holder.data.image_file_list.get(2).caption);


                }
            }
        }
        if (holder.data.type_id == Constants.TYPE_VIDEO) {

            holder.imagesLL.setVisibility(View.GONE);
            holder.messageLL.setVisibility(View.GONE);
            holder.videoFL.setVisibility(View.VISIBLE);
            holder.captionVideoTV.setVisibility(View.VISIBLE);

            if (holder.data.videoData != null) {
                String thumbnail = holder.data.videoData.video_thumbnail;
                if (thumbnail != null && !thumbnail.equalsIgnoreCase("")) {
                    try {
                        Picasso.with(baseActivity).load(holder.data.videoData.video_thumbnail).placeholder(R.mipmap.placeholder).into(holder.videoThumbIV);
                        holder.captionVideoTV.setText(holder.data.videoData.caption);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Picasso.with(baseActivity).load("no image").into(holder.videoThumbIV);
                }
            } else {
                Picasso.with(baseActivity).load("no image").into(holder.videoThumbIV);
            }
        }

        holder.editIV.setOnClickListener(new EditPostOnClickListener(holder.data) {
            @Override
            public void onClick(View v) {
                UpdateConfirmFragment fragment = new UpdateConfirmFragment();
                Bundle bundle = new Bundle();
                PostData postData = new PostData(Parcel.obtain());
                postData.id = data.id;
                postData.type_id = data.type_id;
                postData.create_time = data.create_time;
                postData.tagged_email = data.tagged_email;
                postData.content = data.content;
                postData.image_file_list = new ArrayList<>();
                postData.image_file_list.addAll(data.image_file_list);
                postData.videoData = data.videoData;
                bundle.putParcelable("postData", postData);
                fragment.setArguments(bundle);
                ((MainActivity) baseActivity).loadFragment(fragment);
                baseActivity.appData.uploadImagesList.clear();
                baseActivity.appData.videoToUpload = "";
                baseActivity.appData.videoToUploadBitmap = null;
            }
        });

        holder.videoThumbIV.setOnClickListener(new OpenDialogOnClickListener(holder.data) {
            @Override
            public void onClick(View v) {
                if (data.type_id == Constants.TYPE_VIDEO) {
                    if (data.videoData != null && data.videoData.video_file != null && !data.videoData.video_file.equalsIgnoreCase("")) {


                        File dir = new File(Environment.getExternalStorageDirectory(), "MaxLife");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File file = new File(dir, "" + data.videoData.video_name);
                        if (file.exists()
                                && file.getAbsolutePath().toString().equalsIgnoreCase(baseActivity.prefStore.getString("downloaded_video_path"))
                                && file.length() == baseActivity.prefStore.getInt("downloaded_video_size", 0)) {
                            data.videoData.video_file = file.getAbsolutePath().toString();

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(data.videoData.video_file), "video/*");
                            if (intent.resolveActivity(baseActivity.getPackageManager()) != null) {
                                baseActivity.startActivity(intent);
                            } else {
                                baseActivity.showToast("No application to open video");
                            }


                        } else {
                            if (baseActivity.checkPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                                showDownloadingProgressDialog();
                                Intent intent = new Intent(baseActivity, DownloadService.class);
                                intent.putExtra("url", data.videoData.video_file);
                                intent.putExtra("video_name", data.videoData.video_name);
                                intent.putExtra("receiver", new DownloadReceiver(new Handler()));
                                baseActivity.startService(intent);
                            }
                        }


                    }
                }
            }
        });

        holder.image1IV.setOnClickListener(new OpenImageDialogOnClickListener(holder.data, 1));
        holder.image2IV.setOnClickListener(new OpenImageDialogOnClickListener(holder.data, 2));
        holder.image3IV.setOnClickListener(new OpenImageDialogOnClickListener(holder.data, 3));
    }

    private void createProgressDialog() {
        mProgressDialog = new ProgressDialog(baseActivity);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgress(0);
    }

    private void showDownloadingProgressDialog() {
        if (!baseActivity.isFinishing()) {
            mProgressDialog.setMessage(baseActivity.getString(R.string.downloading));
            mProgressDialog.show();
            mProgressDialog.setProgress(0);
        }
    }

    private class ViewHolder {
        private CircularImageView profileImageCIV;
        private TextView nameTV;
        private TextView dateTV;
        private ImageView editIV;

        private LinearLayout messageLL;
        private LinearLayout imagesLL;

        private TextView messageTV;
        private ImageView image1IV;
        private TextView caption1TV;
        private ImageView image2IV;
        private TextView caption2TV;
        private ImageView image3IV;
        private TextView caption3TV;

        private FrameLayout videoFL;
        private ImageView videoThumbIV;
        private TextView captionVideoTV;

        private PostData data;
    }

    private abstract class EditPostOnClickListener implements View.OnClickListener {

        public PostData data;

        public EditPostOnClickListener(PostData postData) {
            this.data = postData;
        }
    }

    private abstract class OpenDialogOnClickListener implements View.OnClickListener {

        public PostData data;

        public OpenDialogOnClickListener(PostData postData) {
            this.data = postData;
        }
    }

    private class OpenImageDialogOnClickListener implements View.OnClickListener {

        public PostData data;
        public int imagePosition;

        public OpenImageDialogOnClickListener(PostData postData, int imagePosition) {
            this.data = postData;
            this.imagePosition = imagePosition;
        }

        @Override
        public void onClick(View v) {

            View dialogView = baseActivity.inflater.inflate(R.layout.dialog_image, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(baseActivity);
            builder.setView(dialogView);

            ImageView fullScreenImageZIV = (ImageView) dialogView.findViewById(R.id.fullScreenImageZIV);

            if (imagePosition == 1) {
                if (data.image_file_list.size() > 0) {
                    Picasso.with(baseActivity).load(data.image_file_list.get(0).image_file).placeholder(R.mipmap.placeholder).into(fullScreenImageZIV);
                }
            } else if (imagePosition == 2) {
                if (data.image_file_list.size() == 2) {
                    Picasso.with(baseActivity).load(data.image_file_list.get(0).image_file).placeholder(R.mipmap.placeholder).into(fullScreenImageZIV);
                } else if (data.image_file_list.size() == 3) {
                    Picasso.with(baseActivity).load(data.image_file_list.get(1).image_file).placeholder(R.mipmap.placeholder).into(fullScreenImageZIV);
                }
            } else if (imagePosition == 3) {
                if (data.image_file_list.size() == 2) {
                    Picasso.with(baseActivity).load(data.image_file_list.get(1).image_file).placeholder(R.mipmap.placeholder).into(fullScreenImageZIV);
                } else if (data.image_file_list.size() == 3) {
                    Picasso.with(baseActivity).load(data.image_file_list.get(2).image_file).placeholder(R.mipmap.placeholder).into(fullScreenImageZIV);
                }
            }

            builder.show();
        }
    }


    @SuppressLint("ParcelCreator")
    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                int progress = resultData.getInt("progress");
                if (progress < 50) {
                    baseActivity.prefStore.setString("downloaded_video_path", resultData.getString("file_path"));
                    baseActivity.prefStore.setInt("downloaded_video_size", resultData.getInt("fileLength"));
                }
                mProgressDialog.setProgress(progress);
                if (progress == 100) {
                    mProgressDialog.dismiss();
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resultData.getString("file_path")));
                        intent.setDataAndType(Uri.parse(resultData.getString("file_path")), "video/mp4");
                        baseActivity.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class PremissionGetResult implements BaseActivity.PermCallback {

        @Override
        public void permGranted() {

        }

        @Override
        public void permDenied() {

        }
    }

}
