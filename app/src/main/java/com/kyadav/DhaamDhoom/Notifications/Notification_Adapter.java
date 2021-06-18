package com.kyadav.DhaamDhoom.Notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.kyadav.DhaamDhoom.R;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class Notification_Adapter extends RecyclerView.Adapter<Notification_Adapter.CustomViewHolder> {
    public Context context;
    public Notification_Adapter.OnItemClickListener listener;
    ArrayList<Notification_Get_Set> datalist;

    public Notification_Adapter(Context context, ArrayList<Notification_Get_Set> arrayList, Notification_Adapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public Notification_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        Notification_Adapter.CustomViewHolder viewHolder = new Notification_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    @Override
    public void onBindViewHolder(final Notification_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        holder.bind(i, datalist.get(i), listener);

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Notification_Get_Set item);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageButton done;

        public CustomViewHolder(View view) {
            super(view);
            //  image=view.findViewById(R.id.image);
            done = view.findViewById(R.id.done);

        }

        public void bind(final int pos, final Notification_Get_Set item, final Notification_Adapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });


        }


    }

}