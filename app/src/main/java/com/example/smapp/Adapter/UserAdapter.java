package com.example.smapp.Adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smapp.Model.Follow;
import com.example.smapp.Model.Notification;
import com.example.smapp.Model.User;
import com.example.smapp.R;
import com.example.smapp.databinding.UserSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{


    ArrayList<User> list;
    Context context;
    FirebaseDatabase database;

    public UserAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_sample, parent,false);
        database =  FirebaseDatabase.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        Picasso.get()
                .load(user.getProfile())
                .placeholder(R.drawable.bg_load)
                .into(holder.binding.profile);
        holder.binding.name.setText(user.getName());
        holder.binding.profession.setText(user.getProfession());

       FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                                .child(user.getUserId())
                                        .child("followers")
                                                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            holder.binding.btnFollow.setBackground(ContextCompat.getDrawable(context,R.drawable.follow_active_btn));
                            holder.binding.btnFollow.setText("Following");
                            holder.binding.btnFollow.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
                            holder.binding.btnFollow.setTextColor(context.getResources().getColor(R.color.grey));
                            holder.binding.btnFollow.setEnabled(false);
                        }
                        else
                        {
                            holder.binding.btnFollow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Follow follow  = new Follow();
                                    follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                                    follow.setFollowedAt(new Date().getTime());

                                    list.clear();
                                   FirebaseDatabase.getInstance().getReference()
                                            .child("Users")
                                            .child(user.getUserId())
                                            .child("followers")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .setValue(follow)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    list.clear();

                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("Users")
                                                            .child(user.getUserId())
                                                            .child("followersCount")
                                                            .setValue(user.getFollowersCount() +1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    holder.binding.btnFollow.setBackground(ContextCompat.getDrawable(context,R.drawable.follow_active_btn));
                                                                    holder.binding.btnFollow.setText("Following");
                                                                    holder.binding.btnFollow.setTextColor(context.getResources().getColor(R.color.grey));
                                                                    holder.binding.btnFollow.setEnabled(false);

                                                                    Notification notification = new Notification();
                                                                    notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                    notification.setNotificationAt(new Date().getTime());
                                                                    notification.setType("follow");
                                                                    notification.setDelivered(false);
                                                                    notification.setCheckOpen(false);

                                                                    notification.setNotificationId(FirebaseDatabase.getInstance().getReference().child("notification")
                                                                            .child(user.getUserId())
                                                                            .push().getKey());

                                                                    FirebaseDatabase.getInstance().getReference().child("notification")
                                                                            .child(user.getUserId())
                                                                            .child(notification.getNotificationId()).setValue(notification);

                                                                    Toast.makeText(context, "You Followed "+user.getName(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

    UserSampleBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = UserSampleBinding.bind(itemView);

        }
    }

}
