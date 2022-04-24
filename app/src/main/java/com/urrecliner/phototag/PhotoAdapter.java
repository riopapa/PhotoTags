package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.SUFFIX_JPG;
import static com.urrecliner.phototag.Vars.buildBitMap;
import static com.urrecliner.phototag.Vars.fabUndo;
import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.mainMenu;
import static com.urrecliner.phototag.Vars.multiMode;
import static com.urrecliner.phototag.Vars.nowPos;
import static com.urrecliner.phototag.Vars.photoAdapter;
import static com.urrecliner.phototag.Vars.photoTags;
import static com.urrecliner.phototag.Vars.spanWidth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private static final int unMarkedTextColor = Color.parseColor("#000000");
    private static final int markedTextColor = Color.parseColor("#AAAAAA");

    @Override
    public int getItemCount() {
        return photoTags.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iVImage;
        TextView tVInfo;

        ViewHolder(final View itemView) {
            super(itemView);
            tVInfo = itemView.findViewById(R.id.info);
            tVInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (multiMode)
                        toggleCheckBox(getBindingAdapterPosition());
                    else
                        loadTagActivity();
                }
            });

            iVImage = itemView.findViewById(R.id.image);
            iVImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (multiMode)
                        toggleCheckBox(getAbsoluteAdapterPosition());
                    else
                        loadTagActivity();
                }
            });

            iVImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (multiMode)
                        loadTagActivity();
                    else
                        toggleCheckBox(getAbsoluteAdapterPosition());
                    return true;
                }
            });
        }

        private void loadTagActivity() {
            nowPos = getAbsoluteAdapterPosition();
//            PhotoTag photoTag = photoTags.get(nowPos);
//            Bitmap photoMap = photoTag.getSumNailMap().copy(Bitmap.Config.ARGB_8888, false);
//            boolean checked = !photoTag.isChecked();
//            iVImage.setImageBitmap(checked ? buildBitMap.makeChecked(photoMap):photoMap);
            Intent intent = new Intent(mContext, TagWithPlace.class);
            mActivity.startActivity(intent);
        }

        private void toggleCheckBox(int position) {

            fabUndo = mActivity.findViewById(R.id.undo_select);
            fabUndo.setVisibility(View.VISIBLE);
            PhotoTag photoTag = photoTags.get(position);
            String shortName = photoTag.getPhotoName();
            Bitmap photoMap = photoTag.getSumNailMap().copy(Bitmap.Config.RGB_565, false);
            boolean checked = !photoTag.isChecked();
            iVImage.setImageBitmap(checked ? buildBitMap.makeChecked(photoMap):photoMap);
            iVImage.setBackgroundColor(checked ? 0x7caee2dc:0xffffffff);
            tVInfo.setTextColor((shortName.endsWith(SUFFIX_JPG))? markedTextColor:unMarkedTextColor);
            tVInfo.setText(shortName);
            photoTag.setChecked(checked);
            photoTags.set(position, photoTag);
            if (!multiMode) {
                multiMode = true;
                int start = position - 12; if (start < 0) start = 0;
                int finish = position + 12; if (finish > photoTags.size()) finish = photoTags.size();
                for (int pos = start; pos < finish; pos++)
                    photoAdapter.notifyItemChanged(pos);
            }
            MenuItem item = mainMenu.findItem(R.id.action_Delete);
            item.setVisible(true);
            item = mainMenu.findItem(R.id.shareMultiPhoto);
            item.setVisible(true);
        }
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        PhotoTag nowPT = photoTags.get(position);
        Bitmap photoMap = nowPT.getSumNailMap();
        if (photoMap == null) {
            nowPT = BuildDB.getPhotoWithMap(nowPT);
            photoMap = nowPT.getSumNailMap();
        }

        String photoName = nowPT.getPhotoName();
        boolean checked = nowPT.isChecked();
        if (checked)
            photoMap = buildBitMap.makeChecked(photoMap.copy(Bitmap.Config.ARGB_8888, false));
        boolean landscape = photoMap.getWidth() > photoMap.getHeight();
        int width = (landscape) ? spanWidth:spanWidth * 6 / 10;
        int height = width*photoMap.getHeight()/photoMap.getWidth();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.iVImage.getLayoutParams();
        params.width = width; params.height = height;
        holder.iVImage.setLayoutParams(params);
        holder.iVImage.setImageBitmap(photoMap);
        holder.iVImage.setBackgroundColor(checked ? 0x7caee2dc:0xffffffff);
        holder.tVInfo.setTextColor((photoName.endsWith(SUFFIX_JPG))? markedTextColor:unMarkedTextColor);
        holder.tVInfo.setText(photoName);
    }
}

/*
The values 1-8 represent the following descriptions (as shown by utilities that support EXIF field decode):

EXIF Orientation Value	Row #0 is:	Column #0 is:
1                   	Top	        Left side
2*	                    Top	        Right side
3	                    Bottom	    Right side
4*	                    Bottom	    Left side
5*	                    Left side	Top
6	                    Right side	Top
7*	                    Right side	Bottom
8	                    Left side	Bottom
NOTE: Values with "*" are uncommon since they represent "flipped" orientations.

 */