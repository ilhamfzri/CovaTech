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

public class AdapterDataCovidPerProvinsi extends RecyclerView.Adapter<AdapterDataCovidPerProvinsi.MyViewHolder> {
    String data1[], data2[], data3[], data4[];
    Context context;
    private final ClickListener listener;

    public AdapterDataCovidPerProvinsi(Context ct, String sProvinsi[], String sPositif[], String sSembuh[], String sMeninggal[], ClickListener listener) {
        this.listener = listener;
        context = ct;
        data1 = sPositif;
        data2 = sSembuh;
        data3 = sMeninggal;
        data4 = sProvinsi;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_adapter_covatrace_covid_per_provinsi, parent, false);
        return new MyViewHolder(view);
    }

    public interface ClickListener {

        void onPositionClicked(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textViewKasusPositif.setText(data1[position]);
        holder.textViewKasusSembuh.setText(data2[position]);
        holder.textViewKasusMeninggal.setText(data3[position]);
        holder.textViewProvinsi.setText(data4[position]);
    }

    @Override
    public int getItemCount() {
        return data1.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewKasusPositif, textViewKasusSembuh, textViewKasusMeninggal, textViewProvinsi;
        LinearLayout layout;
        private WeakReference<ClickListener> listenerRef;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            listenerRef = new WeakReference<>(listener);

            textViewProvinsi = itemView.findViewById(R.id.text_provinsi);
            textViewKasusPositif = itemView.findViewById(R.id.text_kasus_positif);
            textViewKasusSembuh = itemView.findViewById(R.id.text_kasus_sembuh);
            textViewKasusMeninggal = itemView.findViewById(R.id.text_kasus_meninggal);

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
