package com.example.md_app.history;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md_app.Activity_Main;
import com.example.md_app.R;
import com.example.md_app.models.Model_Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Fragment_History extends Fragment {

    Context mContext;

    private List<Model_Appointment> model_appointments = new ArrayList<>();

    private View layout;
    private RecyclerView rel_appointments;
    private Adapter_Appointments adapter_appointments;

    private String st_uid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        st_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_history, container, false);

        rel_appointments = layout.findViewById(R.id.rel_appointments);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        rel_appointments.setLayoutManager(llm);

        init_recyclerView();

        return layout;
    }

    private void init_recyclerView() {
        FirebaseDatabase.getInstance().getReference()
                .child("book_appointments").child(st_uid)
                .orderByChild("obj_timestamp").limitToLast(100)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (model_appointments != null) model_appointments.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Model_Appointment model = snapshot.getValue(Model_Appointment.class);
                    model_appointments.add(model);
                }

                adapter_appointments = new Adapter_Appointments(model_appointments);
                rel_appointments.setAdapter(adapter_appointments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(mContext, "Test", Toast.LENGTH_LONG).show();
            }
        });
    }

}
