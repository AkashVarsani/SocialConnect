package com.example.smapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.smapp.Adapter.NotificationAdapter;
import com.example.smapp.CommentActivity;
import com.example.smapp.Model.Notification;
import com.example.smapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Notification2Fragment extends Fragment {

    ShimmerRecyclerView recyclerView;
    ArrayList<Notification> list;
    boolean created;

    public Notification2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification2, container, false);

        created = false;
        recyclerView = view.findViewById(R.id.notification2RV);
        list = new ArrayList<>();
        NotificationAdapter notificationAdapter = new NotificationAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(notificationAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.showShimmerAdapter();

//        list.add(new Notification(R.drawable.profilepic, "<b>Ritesh Girase</b> Liked your Photo.", "just now"));

        FirebaseDatabase.getInstance().getReference().child("notification")
                .child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            Notification notification = dataSnapshot.getValue(Notification.class);
                            list.add(notification);
                        }
                        if(!created) {recyclerView.hideShimmerAdapter();created=true;}
                            notificationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });





        return view;
    }
}