package com.example.md_app.Doctors;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md_app.Activity_Add_Doctor;
import com.example.md_app.Activity_Main;
import com.example.md_app.R;
import com.example.md_app.models.Model_Doctor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Doctors extends Fragment {

    private List<Model_Doctor> model_doctors = new ArrayList<>();

    private View view;
    private Button btn_add_doctor;
    private RecyclerView rel_doctors;
    private Adapter_Doctors adapter_doctors;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_doctors, container, false);

        init_viewItem();

        event_handling();

        return view;
    }

    private void init_viewItem(){
        btn_add_doctor = view.findViewById(R.id.fragDoctors_btn_add);

        rel_doctors = view.findViewById(R.id.fragDoctors_rel_doctors);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rel_doctors.setLayoutManager(llm);

        init_rel_doctors();
    }

    private void init_rel_doctors(){
        FirebaseDatabase.getInstance()
                .getReference().child("doctors")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (model_doctors != null) model_doctors.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            String st = snapshot.getKey();
                            Model_Doctor doctor = snapshot.getValue(Model_Doctor.class);
                            model_doctors.add(doctor);
                        }
                        adapter_doctors = new Adapter_Doctors(model_doctors);
                        rel_doctors.setAdapter(adapter_doctors);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void event_handling(){
        btn_add_doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Activity_Add_Doctor.class);
                startActivity(intent);
            }
        });
    }
}
