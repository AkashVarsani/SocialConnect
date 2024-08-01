package com.example.smapp.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smapp.Model.Story;
import com.example.smapp.Model.User;
import com.example.smapp.Model.UserStories;
import com.example.smapp.R;
import com.example.smapp.databinding.StoryRvDesignBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder>{

    public StoryAdapter(ArrayList<Story> list, Context context) {
        this.list = list;
        this.context = context;
    }

    ArrayList<Story> list;
    Context context;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.story_rv_design, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Story model = list.get(position);
        if(model.getUserStoriesList().size()>0) {
            UserStories lastStory = model.getUserStoriesList().get(model.getUserStoriesList().size() - 1);

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(model.getStoryBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            Picasso.get().load(user.getProfile())
                                    .placeholder(R.drawable.bg_load).into(holder.binding.profile);
                            holder.binding.name.setText(user.getName());
                            holder.binding.statusCircle.setPortionsCount(model.getUserStoriesList().size());

                            holder.binding.statusCircle.setVisibility(View.GONE);
                            holder.binding.statusCircle.setVisibility(View.VISIBLE);
                            Picasso.get().load(lastStory.getImage())
                                    .placeholder(R.drawable.bg_load).resize(340,300).into(holder.binding.postImg);

                            holder.binding.postImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ArrayList<MyStory> myStories = new ArrayList<>();

                                    for(UserStories stories: model.getUserStoriesList()){
                                        myStories.add(new MyStory(
                                                stories.getImage()
                                        ));
                                    }

                                    new StoryView.Builder(((AppCompatActivity)context).getSupportFragmentManager())
                                            .setStoriesList(myStories) // Required
                                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                            .setTitleText(user.getName()) // Default is Hidden
                                            .setSubtitleText(user.getProfession()) // Default is Hidden
                                            .setTitleLogoUrl(user.getProfile()) // Default is Hidden
                                            .setStoryClickListeners(new StoryClickListeners() {
                                                @Override
                                                public void onDescriptionClickListener(int position) {
                                                    //your action
                                                }

                                                @Override
                                                public void onTitleIconClickListener(int position) {
                                                    //your action
                                                }
                                            }) // Optional Listeners
                                            .build() // Must be called before calling show method
                                            .show();


                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        StoryRvDesignBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = StoryRvDesignBinding.bind(itemView);
        }
    }


}
