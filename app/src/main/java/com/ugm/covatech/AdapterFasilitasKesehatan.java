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

public class AdapterFasilitasKesehatan extends RecyclerView.Adapter<AdapterFasilitasKesehatan.MyViewHolder> {
    String data1[],data2[];
    Context context;
    private final ClickListener listener;
    public AdapterFasilitasKesehatan(Context ct, String sName[], String sDistance[], ClickListener listener){
        this.listener=listener;
        context=ct;
        data1=sName;
        data2=sDistance;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_adapter_fasilitas_kesehatan,parent,false);
        return new MyViewHolder(view);
    }

    public interface ClickListener {

        void onPositionClicked(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.locationName.setText(data1[position]);
        holder.locationDistance.setText(data2[position]+" km");
    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder {
        TextView locationName,locationDistance;
        LinearLayout layout;
        private WeakReference<ClickListener> listenerRef;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            locationName=itemView.findViewById(R.id.location);
            locationDistance=itemView.findViewById(R.id.distance);
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
