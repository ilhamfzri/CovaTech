package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FasilitasKesehatan extends AppCompatActivity {
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fasilitas_kesehatan);

        listView = findViewById(R.id.listView);
        String mLocation[] = {"Rumah Sakit Bayangkara", "Rumah Sakit UGM"};
        String mAddress[] = {"Jarak 4km", "Jarak 5km"};

        MyAdapter adapter = new MyAdapter(this, mLocation, mAddress);
        listView.setAdapter(adapter);
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String rLocation[];
        String rAddress[];

        MyAdapter(Context c, String location[], String address[]){
            super(c, R.layout.row_fasilitas_kesehatan, location);
            this.context = c;
            this.rLocation = location;
            this.rAddress = address;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row_fasilitas_kesehatan, parent, false);
            TextView location = row.findViewById(R.id.location);
            TextView distance = row.findViewById(R.id.distance);

            Typeface poppinsSemiBold = ResourcesCompat.getFont(context, R.font.poppins_semibold);
            Typeface poppins = ResourcesCompat.getFont(context, R.font.poppins);

            location.setTypeface(poppinsSemiBold);
            location.setText(rLocation[position]);

            distance.setTypeface(poppins);
            distance.setText(rAddress[position]);


            return row;
        }
    }
}