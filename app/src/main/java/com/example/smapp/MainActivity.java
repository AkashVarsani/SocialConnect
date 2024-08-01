package com.example.smapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import com.example.smapp.Fragment.AddFragment;
import com.example.smapp.Fragment.HomeFragment;
import com.example.smapp.Fragment.Notification2Fragment;
import com.example.smapp.Fragment.ProfileFragment;
import com.example.smapp.Fragment.SearchFragment;
import com.example.smapp.Fragment.savedPost;
import com.example.smapp.Model.Notification;
import com.example.smapp.Model.User;
import com.example.smapp.databinding.ActivityMainBinding;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    int ii = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new savedPost());
        transaction.commit();




        binding.navCons.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (position) {
                    case 0:
                        transaction.replace(R.id.container, new HomeFragment());
                        break;
                    case 1:
                        transaction.replace(R.id.container, new Notification2Fragment());
                        break;
                    case 2:
                        transaction.replace(R.id.container, new AddFragment());
                        break;
                    case 3:
                        transaction.replace(R.id.container, new SearchFragment());
                        break;
                    case 4:
                        transaction.replace(R.id.container, new ProfileFragment());
                        break;

                }

                transaction.commit();

            }
        });

        FirebaseDatabase.getInstance().getReference().child("notification")
                .child(auth.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                            Notification notification = snapshot1.getValue(Notification.class);
                            if (!notification.isDelivered()) {

                                FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child(notification.getNotificationBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);

                                                String text = "";
                                                switch (notification.getType()) {
                                                    case "like":
                                                        text = Html.fromHtml("<b>" + user.getName() + "</b>" + " liked your post").toString();
                                                        break;
                                                    case "comment":
                                                        text = Html.fromHtml("<b>" + user.getName() + "</b>" + " commented on your post").toString();
                                                        break;
                                                    case "follow":
                                                        text = Html.fromHtml("<b>" + user.getName() + "</b>" + " started following you").toString();
                                                        break;
                                                }

                                                createNotificationChannel();
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ch1")
                                                        .setSmallIcon(R.drawable.bg_ring)
                                                        .setContentTitle(getString(R.string.app_name))
                                                        .setContentText(text)
                                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                                notificationManager.notify(ii, builder.build());
                                                ii++;

                                                FirebaseDatabase.getInstance().getReference().child("notification").child(FirebaseAuth.getInstance().getUid())
                                                        .child(notification.getNotificationId()).child("delivered").setValue(true);

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ch1", "ch1", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("ch1");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}