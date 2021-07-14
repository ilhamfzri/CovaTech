package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FasilitasKesehatan extends AppCompatActivity {
    final ArrayList<String> arrayName = new ArrayList<String>();
    final ArrayList<String> arrayAddress = new ArrayList<String>();
    final ArrayList<String> arrayDistance = new ArrayList<String>();
    final ArrayList<String> arrayContact = new ArrayList<String>();
    final ArrayList<String> arrayDirection = new ArrayList<String>();
    RecyclerView recyclerView;
    AdapterFasilitasKesehatan adapterFasilitasKesehatan;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fasilitas_kesehatan);
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        loadData();

    }

    public void loadData(){
        db.collection("fasilitas_kesehatan").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){
                        arrayName.add(document.getString("Name"));
                        arrayAddress.add(document.getString("Address"));
                        arrayContact.add(document.getString("Contact"));
                        arrayDirection.add(document.getString("Direction"));

                    }

                    final String [] stringArrayName = arrayName.toArray(new String[0]);
                    final String [] stringArrayDistance = arrayAddress.toArray(new String[0]);
                    final String [] stringArrayAddress = arrayAddress.toArray(new String[0]);
                    final String [] stringArrayContact = arrayContact.toArray(new String[0]);
                    final String [] stringArrayDirection = arrayDirection.toArray(new String[0]);

                    adapterFasilitasKesehatan =new AdapterFasilitasKesehatan(FasilitasKesehatan.this, stringArrayName, stringArrayDistance, new AdapterFasilitasKesehatan.ClickListener() {
                        @Override
                        public void onPositionClicked(int position) {
                            showBottomSheet(stringArrayName[position],stringArrayAddress[position], 3, stringArrayContact[position], stringArrayDirection[position]);
                        }
                    });
                    recyclerView.setAdapter(adapterFasilitasKesehatan);
                    recyclerView.setLayoutManager(new LinearLayoutManager(FasilitasKesehatan.this));
                }
            }
        });
    }

    public void showBottomSheet(String sName, String sAddress, Integer sDistance, final String sContact, final String sDirection){
        TextView namePlace, addressPlace;
        Button buttonDial;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FasilitasKesehatan.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_sheet_fasilitas_kesehatan, (LinearLayout)findViewById(R.id.bottomSheetContainer));

        bottomSheetView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                String number=sContact.replaceAll("[\\D]", "");
                dialIntent.setData(Uri.parse("tel:"+number));
                startActivity(dialIntent);
            }
        });

        bottomSheetView.findViewById(R.id.directionPlaces).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+sDirection);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        namePlace = bottomSheetView.findViewById(R.id.namePlace);
        namePlace.setText(sName);
        addressPlace = bottomSheetView.findViewById(R.id.addressPlace);
        addressPlace.setText(sAddress);
        buttonDial = bottomSheetView.findViewById(R.id.button);
        buttonDial.setText(sContact);


        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }
}