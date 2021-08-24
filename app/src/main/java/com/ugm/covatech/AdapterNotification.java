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

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.MyViewHolder> {
    String data1[], data2[], data3[];
    Integer icon[];
    Context context;
    private final ClickListener listener;

    public AdapterNotification(Context ct, String sTitle[], String sBody[], String sDate[], Integer sIcon[], ClickListener listener) {
        this.listener = listener;
        context = ct;
        data1 = sTitle;
        data2 = sBody;
        data3 = sDate;
        icon = sIcon;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_adapter_notification, parent, false);
        return new MyViewHolder(view);
    }

    public interface ClickListener {

        void onPositionClicked(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.notificationTitle.setText(data1[position]);
        holder.notificationBody.setText(data2[position]);
        holder.notificationIcon.setBackgroundResource(icon[position]);
        holder.notificationDate.setText(data3[position]);
    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView notificationIcon;
        TextView notificationTitle, notificationBody, notificationDate;
        LinearLayout layout;
        private WeakReference<ClickListener> listenerRef;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            notificationTitle = itemView.findViewById(R.id.text_judul_notifikasi);
            notificationBody = itemView.findViewById(R.id.text_isi_notifikasi);
            notificationIcon = itemView.findViewById(R.id.image_notification);
            notificationDate = itemView.findViewById(R.id.text_tanggal_notification);

            layout = itemView.findViewById(R.id.layout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listenerRef.get().onPositionClicked(getAdapterPosition());
                }
            });
        }
    }
}
