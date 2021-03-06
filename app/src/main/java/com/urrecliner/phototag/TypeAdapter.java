package com.urrecliner.phototag;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static com.urrecliner.phototag.Vars.placeActivity;
import static com.urrecliner.phototag.Vars.typeIcons;
import static com.urrecliner.phototag.Vars.typeNames;
import static com.urrecliner.phototag.Vars.typeNumber;
import static com.urrecliner.phototag.Vars.placeType;
import static com.urrecliner.phototag.Vars.typeAdapter;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeHolder> {

    public TypeAdapter(ArrayList<TypeInfo> typeInfos) {
    }

    static class TypeHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        ImageView ivIcon;
        View viewLine;

        TypeHolder(View view) {
            super(view);
            this.viewLine = itemView.findViewById(R.id.type_layout);
            this.tvName = itemView.findViewById(R.id.typeName);
            this.ivIcon = itemView.findViewById(R.id.typeIcon);
            this.viewLine.setOnClickListener(view1 -> {
                int oldType = typeNumber;
                typeNumber = getAbsoluteAdapterPosition();
                typeAdapter.notifyItemChanged(oldType);
                placeType = typeNames[typeNumber];
                typeAdapter.notifyItemChanged(typeNumber);
                ImageView iv = placeActivity.findViewById(R.id.queryLocs);
                iv.setImageResource(typeIcons[typeNumber]);
//                iv.setImageBitmap(utils.maskedIcon(typeIcons[typeNumber]));
            });
        }
    }

    @NonNull
    @Override
    public TypeHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.type_item, viewGroup, false);
        return new TypeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeHolder viewHolder, int position) {

        viewHolder.tvName.setText(typeNames[position]);
        viewHolder.ivIcon.setImageResource(typeIcons[position]);
        if (typeNumber == position)
            viewHolder.tvName.setBackgroundColor(Color.LTGRAY);
        else
            viewHolder.tvName.setBackgroundColor(0x00000000);

        viewHolder.ivIcon.setTag(""+position);
    }

    @Override
    public int getItemCount() {
        return (typeNames.length);
    }

}