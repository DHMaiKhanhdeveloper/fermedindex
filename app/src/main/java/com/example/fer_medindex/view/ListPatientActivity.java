package com.example.fer_medindex.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fer_medindex.R;
import com.example.fer_medindex.adapter.AdapterUsersPatient;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class ListPatientActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    AdapterUsersPatient adapterUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_patient_activity);

        recyclerView = findViewById(R.id.recyclerView_list_patient);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        FirebaseRecyclerOptions<ReadWritePatientDetails> options =
                new FirebaseRecyclerOptions.Builder<ReadWritePatientDetails>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Patients"), ReadWritePatientDetails.class)
                        .build();

        adapterUsers = new AdapterUsersPatient(options);
      //  adapterUsers.startListening();
        recyclerView.setAdapter(adapterUsers);
    }
    @Override
    public void onStart() {
        super.onStart();
        adapterUsers.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        adapterUsers.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search,menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                txtSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                txtSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void txtSearch(String str){
        FirebaseRecyclerOptions<ReadWritePatientDetails> options =
                new FirebaseRecyclerOptions.Builder<ReadWritePatientDetails>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Patients").orderByChild("fullname").startAt(str).endAt(str+"~"), ReadWritePatientDetails.class)
                        .build();
        adapterUsers = new AdapterUsersPatient(options);
        adapterUsers.startListening();
        recyclerView.setAdapter(adapterUsers);
    }
}