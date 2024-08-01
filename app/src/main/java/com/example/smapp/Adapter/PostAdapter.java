package com.example.smapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smapp.CommentActivity;
import com.example.smapp.Model.Notification;
import com.example.smapp.Model.Post;
import com.example.smapp.Model.User;
import com.example.smapp.Model.UserStories;
import com.example.smapp.R;
import com.example.smapp.databinding.DashboardRvSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.internal.ParcelableSparseIntArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    ArrayList<Post> list;
    Context context;
    int c, lCount;
    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    boolean changed, value, clicked;

    public PostAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_rv_sample, parent, false);
        c = 1;
        clicked = false;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post model = list.get(position);

        FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .child(model.getPostId())
                .child("likes")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists() && snapshot.getValue().equals(true)) {
                            holder.binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart2, 0, 0, 0);
                        }else
                            holder.binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.like, 0, 0, 0);

                        holder.binding.like.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!clicked) {
                                    clicked = true;

                                    value = false;
                                    if (snapshot.exists() && snapshot.getValue().equals(true)) value = true;

                                    if (value) {

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("posts")
                                                .child(model.getPostId())
                                                .child("likes")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(view.getContext(), "Disliked", Toast.LENGTH_SHORT).show();
                                                        holder.binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
                                                    }
                                                });

                                    } else {

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("posts")
                                                .child(model.getPostId())
                                                .child("likes")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        Toast.makeText(view.getContext(), "Liked", Toast.LENGTH_SHORT).show();
                                                        holder.binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart2, 0, 0, 0);

                                                    }
                                                });

                                        if (!FirebaseAuth.getInstance().getUid().equals(model.getPostBy())) {

                                            Notification notification = new Notification();
                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                            notification.setNotificationAt(new Date().getTime());
                                            notification.setPostBy(model.getPostBy());
                                            notification.setPostId(model.getPostId());
                                            notification.setType("like");
                                            notification.setDelivered(false);
                                            notification.setCheckOpen(false);
                                            notification.setNotificationId(FirebaseDatabase.getInstance().getReference().child("notification")
                                                    .child(model.getPostBy())
                                                    .push().getKey());

                                            FirebaseDatabase.getInstance().getReference().child("notification")
                                                    .child(model.getPostBy())
                                                    .child(notification.getNotificationId()).setValue(notification);
                                        }

                                    }

                                    lCount = 0;
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts")
                                            .child(model.getPostId())
                                            .child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                        if (snapshot1.getValue().equals(true)) lCount++;
                                                    }

                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("posts")
                                                            .child(model.getPostId())
                                                            .child("postLike")
                                                            .setValue(lCount);
                                                    holder.binding.like.setText(lCount + "");
                                                    clicked = false;

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });


                                }
                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (c > 0 || changed) {
            Picasso.get()
                    .load(model.getPostImg())
                    .placeholder(R.drawable.bg_load)
                    .into(holder.binding.postImg);
            FirebaseDatabase.getInstance().getReference().child("Users").child(model.getPostBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    Picasso.get().load(user.getProfile())
                            .placeholder(R.drawable.bg_load)
                            .into(holder.binding.profile);
                    holder.binding.userName.setText(user.getName());
                    holder.binding.about.setText(user.getProfession());
                    holder.binding.like.setText(model.getPostLike() + "");
                    holder.binding.comment.setText(model.getCommentCount()+"");
                    String description = model.getPostDescription();
                    if (description.equals("")) {
                        holder.binding.postDescription.setVisibility(View.GONE);
                    } else {
                        holder.binding.postDescription.setText(model.getPostDescription());
                        holder.binding.postDescription.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            c--;
            changed = false;
        }
        else {

            FirebaseDatabase.getInstance().getReference().child("posts").child(model.getPostId())
                    .child("postLike").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists())
                                holder.binding.like.setText(snapshot.getValue(Integer.class) + "");
                            else
                                holder.binding.like.setText("0");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

            FirebaseDatabase.getInstance().getReference().child("posts").child(model.getPostId())
                    .child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists())
                                holder.binding.comment.setText(snapshot.getValue(Integer.class) + "");
                            else
                                holder.binding.comment.setText("12");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

        }

        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("saved")
                        .child(model.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())holder.binding.save.setImageResource(R.drawable.saved);
                        else holder.binding.save.setImageResource(R.drawable.save);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(FirebaseAuth.getInstance().getUid()).child("saved").child(model.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Toast.makeText(context, "Unsaved", Toast.LENGTH_SHORT).show();
                                    holder.binding.save.setImageResource(R.drawable.save);
                                    FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child(FirebaseAuth.getInstance().getUid()).child("saved").child(model.getPostId()).removeValue();

                                }
                                else{
                                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                                    holder.binding.save.setImageResource(R.drawable.saved);
                                    FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child(FirebaseAuth.getInstance().getUid()).child("saved").child(model.getPostId()).setValue(true);
                                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                                    holder.binding.save.setImageResource(R.drawable.saved);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

        holder.binding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseDatabase.getInstance().getReference().child("Users")
                                .child(model.getPostBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                ArrayList<MyStory> myStories = new ArrayList<>();

                                FirebaseDatabase.getInstance().getReference()
                                        .child("stories").child(model.getPostBy()).child("userStories").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot snapshot1 : snapshot.getChildren()){

                                                    UserStories userStories = snapshot1.getValue(UserStories.class);
                                                    myStories.add(new MyStory(userStories.getImage()));

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

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



            }
        });


        holder.binding.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", model.getPostId());
                intent.putExtra("postBy", model.getPostBy());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        DashboardRvSampleBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = DashboardRvSampleBinding.bind(itemView);


        }
    }


}
