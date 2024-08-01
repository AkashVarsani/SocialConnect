package com.example.smapp.Adapter;

import android.content.Context;
import android.service.autofill.SaveCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smapp.Model.Post;
import com.example.smapp.Model.User;
import com.example.smapp.R;
import com.example.smapp.databinding.SavedRvSampleBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SavedPostAdapter extends RecyclerView.Adapter<SavedPostAdapter.ViewHolder> {

        ArrayList<Post> list;
        Context context;

    public SavedPostAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.saved_rv_sample, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = list.get(position);
        Picasso.get().load(post.getPostImg()).placeholder(R.drawable.bg_load).into(holder.binding.postImg);

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(post.getPostBy()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Picasso.get().load(user.getProfile()).placeholder(R.drawable.bg_load)
                                .into(holder.binding.profile);
                        holder.binding.userName.setText(user.getName());
                        holder.binding.about.setText(user.getProfession());
                        holder.binding.save.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.like.setText(post.getPostLike());
        holder.binding.comment.setText(post.getCommentCount());
        holder.binding.postDescription.setText(post.getPostDescription());



    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        SavedRvSampleBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SavedRvSampleBinding.bind(itemView);

        }
    }
}
