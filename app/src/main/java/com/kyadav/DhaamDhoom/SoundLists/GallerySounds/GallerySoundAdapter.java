package com.kyadav.DhaamDhoom.SoundLists.GallerySounds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kyadav.DhaamDhoom.R;
import com.kyadav.DhaamDhoom.SimpleClasses.Variables;
import com.kyadav.DhaamDhoom.SoundLists.Sounds_GetSet;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/19/2019.
 */


public class GallerySoundAdapter extends RecyclerView.Adapter<GallerySoundAdapter.CustomViewHolder> {
    public Context context;
    public GallerySoundAdapter.OnItemClickListener listener;
    ArrayList<Sounds_GetSet> datalist;

    public GallerySoundAdapter(Context context, ArrayList<Sounds_GetSet> arrayList, GallerySoundAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public GallerySoundAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sound_layout, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(Variables.screen_width - 50, RecyclerView.LayoutParams.WRAP_CONTENT));
        GallerySoundAdapter.CustomViewHolder viewHolder = new GallerySoundAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    @Override
    public void onBindViewHolder(final GallerySoundAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        try {

            holder.sound_name.setText(datalist.get(i).sound_name);
            holder.description_txt.setText(datalist.get(i).description);
            holder.bind(i, datalist.get(i), listener);

           /* if(SoundList_A.running_sound_id.equals(datalist.get(i).id)){
                holder.itemView.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.done).setVisibility(View.VISIBLE);
            }*/

        } catch (Exception e) {

        }

    }


    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Sounds_GetSet item);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageButton done, fav_btn;
        TextView sound_name, description_txt;
        ImageView sound_image;

        public CustomViewHolder(View view) {
            super(view);
            //  image=view.findViewById(R.id.image);
            done = view.findViewById(R.id.done);
            fav_btn = view.findViewById(R.id.fav_btn);
            fav_btn.setVisibility(View.GONE);

            sound_name = view.findViewById(R.id.sound_name);
            description_txt = view.findViewById(R.id.description_txt);
            sound_image = view.findViewById(R.id.sound_image);

        }

        public void bind(final int pos, final Sounds_GetSet item, final GallerySoundAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            fav_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

        }


    }


}

