package com.example.smapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smapp.CommentActivity;
import com.example.smapp.Fragment.NotificationFragment;
import com.example.smapp.Fragment.ProfileFragment;
import com.example.smapp.Model.Notification;
import com.example.smapp.Model.User;
import com.example.smapp.R;
import com.example.smapp.databinding.FragmentNotificationBinding;
import com.example.smapp.databinding.NotificationRvSampleBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHoder> {

    public NotificationAdapter(ArrayList<Notification> list, Context context) {
        this.list = list;
        this.context = context;
    }

    ArrayList<Notification> list;
    Context context;

    @NonNull
    @Override
    public ViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = LayoutInflater.from(context).inflate(R.layout.notification_rv_sample, parent, false);

        return new ViewHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoder holder, int position) {

      Notification notification = list.get(list.size()-position-1);

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(notification.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Picasso.get().load(user.getProfile())
                                .into(holder.binding.profile);

                        switch (notification.getType()){

                            case "comment":
                                holder.binding.notification.setText(Html.fromHtml("<b>"+ user.getName()+"</b>"+"  "+"commented on your post."));break;
                            case "like" :
                                holder.binding.notification.setText(Html.fromHtml("<b>"+ user.getName()+"</b>"+"  "+"liked on your post"));break;
                            case "follow":
                                holder.binding.notification.setText(Html.fromHtml("<b>"+ user.getName()+"</b>"+"  "+"started following you."));break;
                        }
                        holder.binding.time.setText(TimeAgo.using(notification.getNotificationAt()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            if(notification.isCheckOpen()) holder.binding.notificationView.setBackgroundTintList(context.getResources().getColorStateList(R.color.white));
            else holder.binding.notificationView.setBackgroundTintList(context.getResources().getColorStateList(R.color.lightBlue));

            holder.binding.notificationView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.binding.notificationView.setBackgroundTintList(context.getResources().getColorStateList(R.color.white));

                    FirebaseDatabase.getInstance().getReference()
                            .child("notification").child(FirebaseAuth.getInstance().getUid())
                            .child(notification.getNotificationId()).child("checkOpen").setValue(true);


                    if(!notification.getType().equals("follow")){
                        Intent intent = new Intent(context, CommentActivity.class);
                        intent.putExtra("postId",notification.getPostId());
                        intent.putExtra("postBy",notification.getPostBy());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(context, ProfileFragment.class);
                        context.startActivity(intent);
                    }
                }
            });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHoder extends RecyclerView.ViewHolder {

        NotificationRvSampleBinding binding;
        public ViewHoder(@NonNull View itemView) {
            super(itemView);

            binding = NotificationRvSampleBinding.bind(itemView);
        }
    }

}
