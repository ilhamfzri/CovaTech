package com.ugm.covatech;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class AdapterCovaTribute extends RecyclerView.Adapter<AdapterCovaTribute.MyViewHolder> {
    String data1[],data2[];
    Float data3[];
    Context context;
    private final ClickListener listener;
    public AdapterCovaTribute(Context ct, String sPlace[], String sAddress[], Float sStar[], ClickListener listener){
        this.listener=listener;
        context=ct;
        data1=sPlace;
        data2=sAddress;
        data3=sStar;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_adapter_covatribute,parent,false);
        return new MyViewHolder(view);
    }

    public interface ClickListener {
        void onPositionClicked(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.locationName.setText(data1[position]);
        holder.locationAddress.setText(data2[position]+" km");
        holder.ratingBar.setRating(data3[position]);
    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder {
        MaterialRatingBar ratingBar;
        TextView locationName,locationAddress;
        TextView reviewButton;
        private WeakReference<ClickListener> listenerRef;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            locationName=itemView.findViewById(R.id.textPlace);
            locationAddress=itemView.findViewById(R.id.textAddress);
            reviewButton=itemView.findViewById(R.id.buttonReview);
            ratingBar=itemView.findViewById(R.id.starBar);
            reviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listenerRef.get().onPositionClicked(getAdapterPosition());
                }
            });
        }
    }
}
