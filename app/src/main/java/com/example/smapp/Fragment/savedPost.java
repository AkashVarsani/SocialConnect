package com.example.smapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.service.autofill.FillRequest;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.smapp.Adapter.PostAdapter;
import com.example.smapp.Model.Post;
import com.example.smapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class savedPost extends Fragment {

    ArrayList<Post> list;
    ShimmerRecyclerView savedPostRV;
    ArrayList<String> keys;

    public savedPost() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        list = new ArrayList<>();
        keys = new ArrayList<>();

        View view  = inflater.inflate(R.layout.fragment_saved_post, container, false);
        savedPostRV = view.findViewById(R.id.savedPostRV);

        PostAdapter adapter = new PostAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
       savedPostRV.setLayoutManager(layoutManager);
       savedPostRV.setNestedScrollingEnabled(false);
       savedPostRV.setAdapter(adapter);
       savedPostRV.showShimmerAdapter();

    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
            .child("saved").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        FirebaseDatabase.getInstance().getReference().child("posts").child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Post post = snapshot.getValue(Post.class);
                                post.setPostId(dataSnapshot.getKey());
                                list.add(post);
                                Log.d("posss", post.getPostId());
                                Toast.makeText(getContext(), ""+list.size(), Toast.LENGTH_SHORT).show();
                                savedPostRV.hideShimmerAdapter();
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        return view;
    }
}