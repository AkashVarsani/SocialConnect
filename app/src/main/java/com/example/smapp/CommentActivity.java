package com.example.smapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.smapp.Adapter.CommentAdapter;
import com.example.smapp.Model.Comment;
import com.example.smapp.Model.Notification;
import com.example.smapp.Model.Post;
import com.example.smapp.Model.User;
import com.example.smapp.databinding.ActivityCommentBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class CommentActivity extends AppCompatActivity {

    ActivityCommentBinding binding;
    String postId;
    String postBy;
    Intent intent;
    boolean createdPost,createdUserDetails,clicked,value;
    int lCount;

    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Comment> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        clicked = false;
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent = getIntent();
        createdPost =false;
        createdUserDetails= false;

        setSupportActionBar(binding.toolbar2);
        CommentActivity.this.setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        postId = intent.getStringExtra("postId");
        postBy = intent.getStringExtra("postBy");

        database.getReference().child("posts")
                .child(postId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                      if(!createdPost)
                      {    createdPost =true;
                          Picasso.get()
                                  .load(post.getPostImg())
                                  .placeholder(R.drawable.bg_load)
                                  .into(binding.postImage);
                          if (!post.getPostDescription().isEmpty())
                              binding.postDescription.setVisibility(View.VISIBLE);
                          binding.postDescription.setText(post.getPostDescription());
                      }
                        binding.like.setText(post.getPostLike()+"");
                        binding.comment.setText(post.getCommentCount()+"");
                        FirebaseDatabase.getInstance().getReference()
                                .child("posts")
                                .child(postId)
                                .child("likes")
                                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.exists() && snapshot.getValue().equals(true)) {
                                            binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart2, 0, 0, 0);
                                        }

                                        binding.like.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (!clicked) {
                                                    clicked = true;

                                                    value = false;
                                                    if (snapshot.exists() && snapshot.getValue().equals(true)) value = true;

                                                    if (value) {

                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("posts")
                                                                .child(postId)
                                                                .child("likes")
                                                                .child(FirebaseAuth.getInstance().getUid())
                                                                .setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(view.getContext(), "Disliked", Toast.LENGTH_SHORT).show();
                                                                        binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
                                                                    }
                                                                });

                                                    } else {
//
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("posts")
                                                                .child(postId)
                                                                .child("likes")
                                                                .child(FirebaseAuth.getInstance().getUid())
                                                                .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {

                                                                        Toast.makeText(view.getContext(), "Liked", Toast.LENGTH_SHORT).show();
                                                                        binding.like.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.heart2, 0, 0, 0);

                                                                    }
                                                                });

                                                        if (!FirebaseAuth.getInstance().getUid().equals(postBy)) {

                                                            Notification notification = new Notification();
                                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                            notification.setNotificationAt(new Date().getTime());
                                                            notification.setPostBy(postBy);
                                                            notification.setPostId(postId);
                                                            notification.setDelivered(false);
                                                            notification.setType("like");
                                                            notification.setCheckOpen(false);
                                                            notification.setNotificationId(FirebaseDatabase.getInstance().getReference().child("notification")
                                                                    .child(postBy)
                                                                    .push().getKey());

                                                            FirebaseDatabase.getInstance().getReference().child("notification")
                                                                    .child(postBy)
                                                                    .child(notification.getNotificationId()).setValue(notification);
                                                        }

                                                    }

                                                   lCount = 0;
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("posts")
                                                            .child(postId)
                                                            .child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                                        if (snapshot1.getValue().equals(true)) lCount++;
                                                                    }

                                                                    FirebaseDatabase.getInstance().getReference()
                                                                            .child("posts")
                                                                            .child(postId)
                                                                            .child("postLike")
                                                                            .setValue(lCount);
                                                                    binding.like.setText(lCount + "");
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

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        database.getReference().child("Users")
                .child(postBy).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if(!createdUserDetails) {
                            createdUserDetails = true;
                            Picasso.get()
                                    .load(user.getProfile())
                                   .placeholder(R.drawable.bg_load)
                                    .into(binding.profile);
                            binding.user.setText(user.getName() + "");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!binding.commentET.getText().toString().isEmpty())
                    {
                Comment comment = new Comment();
                comment.setCommentBody(binding.commentET.getText()+"");
                comment.setCommentAt(new Date().getTime());
                comment.setCommentBy(FirebaseAuth.getInstance().getUid());

                database.getReference().child("posts")
                        .child(postId)
                        .child("comments")
                        .push()
                        .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("posts")
                                        .child(postId)
                                        .child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                int commentCount =0;
                                                if(snapshot.exists()){
                                                    commentCount = (int) snapshot.getChildrenCount();
                                                }
                                                database.getReference().child("posts")
                                                        .child(postId).child("commentCount")
                                                        .setValue(commentCount).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(CommentActivity.this,"Commented",Toast.LENGTH_SHORT).show();
                                                                binding.commentET.setText(null);

                                                               if(!FirebaseAuth.getInstance().getUid().equals(postBy)){
                                                                   Notification notification = new Notification();
                                                                   notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                   notification.setNotificationAt(new Date().getTime());
                                                                   notification.setPostBy(postBy);
                                                                   notification.setPostId(postId);
                                                                   notification.setType("comment");
                                                                   notification.setCheckOpen(false);


                                                                   notification.setNotificationId(FirebaseDatabase.getInstance().getReference().child("notification")
                                                                           .child(postBy)
                                                                           .push().getKey());

                                                                   FirebaseDatabase.getInstance().getReference().child("notification")
                                                                           .child(postBy)
                                                                           .child(notification.getNotificationId()).setValue(notification);
                                                               }

                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        });
            }}
        });

        CommentAdapter adapter = new CommentAdapter(this, list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.commentRV.setLayoutManager(linearLayoutManager);
        binding.commentRV.setAdapter(adapter);

        database.getReference().child("posts")
                .child(postId)
                .child("comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Comment comment = dataSnapshot.getValue(Comment.class);
                            list.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}