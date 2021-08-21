package com.ugm.covatech;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class AdapterValidasiData extends RecyclerView.Adapter<AdapterValidasiData.MyViewHolder> {
    String data1[], data2[], data3[];
    Context context;
    private final ClickListener listener;

    public AdapterValidasiData(Context ct, String sPlace[], String sAddress[], String sStar[], ClickListener listener) {
        this.listener = listener;
        context = ct;
        data1 = sPlace;
        data2 = sAddress;
        data3 = sStar;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_adapter_validation, parent, false);
        return new MyViewHolder(view);
    }

    public interface ClickListener {
        void onPositionClicked(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.documentID.setText(data1[position]);
        holder.userName.setText(data2[position]);
        holder.tanggalText.setText(data3[position]);
    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tanggalText;
        TextView documentID, userName;
        TextView validationButton;
        private WeakReference<ClickListener> listenerRef;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            listenerRef = new WeakReference<>(listener);
            documentID = itemView.findViewById(R.id.text_document_id);
            userName = itemView.findViewById(R.id.text_user_name);
            validationButton = itemView.findViewById(R.id.button_validasi);
            tanggalText = itemView.findViewById(R.id.text_tanggal);
            validationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listenerRef.get().onPositionClicked(getAdapterPosition());
                }
            });
        }
    }
}
