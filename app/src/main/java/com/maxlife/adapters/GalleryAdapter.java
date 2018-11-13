package com.maxlife.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.maxlife.R;
import com.maxlife.activity.BaseActivity;
import com.maxlife.data.GalleryData;
import com.maxlife.data.UploadImgData;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by anshul.mittal on 10/5/16.
 */
public class GalleryAdapter extends BaseAdapter {

    private BaseActivity context;
    private ArrayList<GalleryData> galleryList;
    private int width = 0;

    public GalleryAdapter(BaseActivity context, ArrayList<GalleryData> galleryList) {
        this.context = context;
        this.galleryList = galleryList;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        width = display.getWidth();
    }

    @Override
    public int getCount() {
        return galleryList.size();
    }

    @Override
    public Object getItem(int position) {
        return galleryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = context.inflater.inflate(R.layout.adapter_gallery, parent, false);

            holder.imageIV = (ImageView) convertView.findViewById(R.id.imageIV);
            holder.iconVideoIV = (ImageView) convertView.findViewById(R.id.iconVideoIV);
            holder.checkCB = (CheckBox) convertView.findViewById(R.id.checkCB);

            holder.imageIV.setLayoutParams(new RelativeLayout.LayoutParams((width/3 - 10), (width/3 - 10)));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        GalleryData galleryData = galleryList.get(position);

        if (galleryData.isImage) {
            if (galleryData.imageData.isSelected) {
                holder.checkCB.setChecked(true);
            } else {
                holder.checkCB.setChecked(false);
            }
            holder.iconVideoIV.setVisibility(View.GONE);
            Picasso.with(context).load(new File(galleryData.imageData.path)).resize(300,300).placeholder(R.color.black).into(holder.imageIV);

            holder.imageIV.setOnClickListener(new ImageClickListener(position, galleryData) {
                @Override
                public void onClick(View v) {
                    if (context.appData != null) {

                        if (galleryData.imageData.isSelected) {
                            for (int i = 0; i < context.appData.uploadImagesList.size(); i++) {
                                if (galleryData.imageData.path.equals(context.appData.uploadImagesList.get(i).uploadImng.getPath())) {
                                    context.appData.uploadImagesList.remove(i);
                                    galleryData.imageData.isSelected = false;
                                    notifyDataSetChanged();
                                    break;
                                }
                            }
                        } else {
                            if (context.isImageValid(galleryList.get(position).imageData.path)) {
                                if (context.appData.uploadImagesList.size() < 3) {
                                    UploadImgData data= new UploadImgData();
                                    data.uploadImng= Uri.fromFile(new File(galleryList.get(position).imageData.path));
                                    context.appData.uploadImagesList.add(data);
                                    galleryList.get(position).imageData.isSelected = true;
                                    notifyDataSetChanged();
                                } else {
                                    context.showToast(context.getString(R.string.only_3_images_are_allowed));
                                }
                            }
                        }
                    }
                }
            });

        } else {
            if (galleryData.videoData.isSelected) {
                holder.checkCB.setChecked(true);
            } else {
                holder.checkCB.setChecked(false);
            }
            holder.iconVideoIV.setVisibility(View.VISIBLE);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            holder.imageIV.setImageBitmap(MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), galleryData.videoData.video_id, MediaStore.Video.Thumbnails.MICRO_KIND, options));

            holder.imageIV.setOnClickListener(new ImageClickListener(position, galleryData) {
                @Override
                public void onClick(View v) {
                    if (context.appData != null) {

                        if (galleryData.videoData.isSelected) {
                            if (galleryData.videoData.path.equals(context.appData.videoToUpload)) {
                                context.appData.videoToUpload = "";
                                galleryData.videoData.isSelected = false;
                                notifyDataSetChanged();
                            }
                        } else {
                            if (galleryData.videoData.duration > 0) {
                                if (galleryData.videoData.duration <= (3*60*1000) + 999) {
                                    for (int i = 0; i < galleryList.size(); i++) {
                                        if (galleryList.get(i).isImage == false) {
                                            galleryList.get(i).videoData.isSelected = false;
                                        }
                                    }
                                    context.appData.videoToUpload = galleryData.videoData.path;

                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = 1;
                                    context.appData.videoToUploadBitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), galleryData.videoData.video_id, MediaStore.Video.Thumbnails.MINI_KIND, options);
                                    galleryData.videoData.isSelected = true;
                                    notifyDataSetChanged();
                                } else {
                                    context.showToast(context.getString(R.string.image_duration_should_be_less_than_3_minutes));
                                }
                            } else {
                                context.showToast(context.getString(R.string.not_valid_video));
                            }
                        }
                    }
                }
            });

        }

        return convertView;
    }

    private class ViewHolder{

        private ImageView imageIV;
        private CheckBox checkCB;
        private ImageView iconVideoIV;

    }

    private abstract class ImageClickListener implements View.OnClickListener{

        int position;
        GalleryData galleryData;

        public ImageClickListener(int position, GalleryData galleryData) {
            this.position = position;
            this.galleryData = galleryData;
        }

    }
}
