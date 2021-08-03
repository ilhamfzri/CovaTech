package com.ugm.covatech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataCovidProvinsiActivity extends AppCompatActivity {
    AdapterDataCovidPerProvinsi adapterDataCovidPerProvinsi;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_covid_provinsi);

        loadData();
    }

    public void loadData() {


        final ArrayList<String> arrayProvinsi = new ArrayList<String>();
        final ArrayList<String> arrayPositif = new ArrayList<String>();
        final ArrayList<String> arraySembuh = new ArrayList<String>();
        final ArrayList<String> arrayMeninggal = new ArrayList<String>();

        String urlAPI = "https://api.kawalcorona.com/indonesia/provinsi/";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlAPI,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject jsonObjectCovid19Indo = response.getJSONObject(i);
                                JSONObject jsonObjectsPerProvinsi = jsonObjectCovid19Indo.getJSONObject("attributes");

                                Log.d("Provinsi", jsonObjectsPerProvinsi.getString("Provinsi"));
                                Log.d("Positif", jsonObjectsPerProvinsi.getString("Kasus_Posi"));
                                Log.d("Sembuh", jsonObjectsPerProvinsi.getString("Kasus_Semb"));
                                Log.d("Meninggal", jsonObjectsPerProvinsi.getString("Kasus_Meni"));

                                arrayProvinsi.add(jsonObjectsPerProvinsi.getString("Provinsi"));
                                arrayPositif.add(jsonObjectsPerProvinsi.getString("Kasus_Posi"));
                                arraySembuh.add(jsonObjectsPerProvinsi.getString("Kasus_Semb"));
                                arrayMeninggal.add(jsonObjectsPerProvinsi.getString("Kasus_Meni")
                                );
                            }

                            final String[] stringProvinsi = arrayProvinsi.toArray(new String[0]);
                            final String[] stringPositif = arrayPositif.toArray(new String[0]);
                            final String[] stringSembuh = arraySembuh.toArray(new String[0]);
                            final String[] stringMeninggal = arrayMeninggal.toArray(new String[0]);


                            adapterDataCovidPerProvinsi = new AdapterDataCovidPerProvinsi(DataCovidProvinsiActivity.this, stringProvinsi, stringPositif,
                                    stringSembuh, stringMeninggal, new AdapterDataCovidPerProvinsi.ClickListener() {
                                @Override
                                public void onPositionClicked(int position) {
                                    Log.d("Clicked", "Click");
                                }
                            });
                            recyclerView = findViewById(R.id.recyclerView);
                            recyclerView.setAdapter(adapterDataCovidPerProvinsi);
                            recyclerView.setLayoutManager(new LinearLayoutManager(DataCovidProvinsiActivity.this));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        requestQueue.add(jsonArrayRequest);
    }
}