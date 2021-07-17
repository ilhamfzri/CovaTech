package com.ugm.covatech;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

public class AdapterCovaTrace extends RecyclerView.Adapter<AdapterCovaTrace.MyViewHolder> {
    String data1[],data2[];
    Context context;
    private final ClickListener listener;
    public AdapterCovaTrace(Context ct, String sName[], String sTime[], ClickListener listener){
        this.listener=listener;
        context=ct;
        data1=sName;
        data2=sTime;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_adapter_covatrace,parent,false);
        return new MyViewHolder(view);
    }

    public interface ClickListener {

        void onPositionClicked(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.placeName.setText(data1[position]);
        holder.placeTime.setText(data2[position]);
    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder {
        TextView placeName,placeTime;
        LinearLayout layout;
        private WeakReference<ClickListener> listenerRef;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            placeName = itemView.findViewById(R.id.namePlace);
            placeTime =itemView.findViewById(R.id.timeRange);
            layout=itemView.findViewById(R.id.layout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listenerRef.get().onPositionClicked(getAdapterPosition());
                }
            });
        }
    }
}
