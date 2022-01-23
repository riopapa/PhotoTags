package com.urrecliner.phototag;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static com.urrecliner.phototag.Vars.buildDB;
import static com.urrecliner.phototag.Vars.dirActivity;
import static com.urrecliner.phototag.Vars.dirNotReady;
import static com.urrecliner.phototag.Vars.dirFolders;
import static com.urrecliner.phototag.Vars.fullFolder;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.multiMode;
import static com.urrecliner.phototag.Vars.sharedPref;
import static com.urrecliner.phototag.Vars.short1Folder;
import static com.urrecliner.phototag.Vars.short2Folder;
import static com.urrecliner.phototag.Vars.squeezeDB;
import static com.urrecliner.phototag.Vars.utils;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    @Override
    public int getItemCount() {
        return dirFolders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iVImage;
        TextView tVInfo;

        ViewHolder(final View itemView) {
            super(itemView);
            tVInfo = itemView.findViewById(R.id.dirName);
            iVImage = itemView.findViewById(R.id.dirImage);
            itemView.setOnClickListener(view -> {
                int pos = getBindingAdapterPosition();
                SharedPreferences.Editor editor = sharedPref.edit();
                fullFolder = dirFolders.get(pos).getLongFolder();
                utils.setShortFolderNames(fullFolder);
                editor.putString("fullFolder", fullFolder);
                editor.apply();
                dirActivity.finish();
                buildDB.cancel();
                multiMode = false;
                dirNotReady = false;
                Intent intent = new Intent(mContext, MainActivity.class);
                dirActivity.startActivity(intent);
            });
        }
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DirectoryFolder df = dirFolders.get(position);
        String folderName = df.getLongFolder();
        utils.setShortFolderNames(folderName);
        String s = short1Folder.equals("0") ? short2Folder: short1Folder + "\n" + short2Folder;
        s +=  "("+df.getNumberOfPics()+")";
        holder.tVInfo.setText(s);
        holder.iVImage.setImageBitmap(df.getImageBitmap());
    }
}