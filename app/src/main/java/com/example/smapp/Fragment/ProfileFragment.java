package com.example.smapp.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.smapp.Adapter.FollowersAdapter;
import com.example.smapp.LogInActivity;
import com.example.smapp.Model.Follow;
import com.example.smapp.Model.User;
import com.example.smapp.R;
import com.example.smapp.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {

    ArrayList<Follow> list;
    FragmentProfileBinding binding;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;


    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user = snapshot.getValue(User.class);
                            Picasso.get()
                                    .load(user.getCoverPhoto())
                                    .placeholder(R.drawable.bg_rectangle)
                                    .into(binding.coverPhoto);

                            Picasso.get()
                                    .load(user.getProfile())
                                    .placeholder(R.drawable.bg_load)
                                    .into(binding.profile);

                            binding.userName.setText(user.getName());
                            binding.profession.setText(user.getProfession());
                            binding.followers.setText(user.getFollowersCount()+"");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        list = new ArrayList<>();

        FollowersAdapter followersAdapter = new FollowersAdapter(list, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.friendRV.setLayoutManager(linearLayoutManager);
        binding.friendRV.setAdapter(followersAdapter);

        database.getReference().child("Users")
                        .child(auth.getUid())
                                .child("followers").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Follow follow = dataSnapshot.getValue(Follow.class);
                            list.add(follow);
                        }
                        followersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(getContext(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_toolbar, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {

                            case R.id.changeProfile: {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent, 22);
                                break;
                            }
                            case R.id.changeCover: {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent, 11);
                                break;
                            }
                            case R.id.signOut: {
                                auth.signOut();
                                Intent intent = new Intent(getContext(), LogInActivity.class);
                                startActivity(intent);
                                break;
                            }
                            case R.id.changeName:{

                                builder.setTitle("Change User Name");
                                View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_layout, (ViewGroup) getView(), false);
                                EditText editText = view.findViewById(R.id.editText);
                                builder.setView(view);

                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        binding.userName.setText(editText.getText().toString());
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Users").child(FirebaseAuth.getInstance().getUid())
                                                .child("name").setValue(editText.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(getContext(),"User Name Changed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                                builder.show();

                                break;
                            }

                            case R.id.changeProfession:{

                                builder.setTitle("Change User Profession");
                                View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_layout, (ViewGroup) getView(), false);
                                EditText editText = view.findViewById(R.id.editText);
                                builder.setView(view);

                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        binding.profession.setText(editText.getText().toString());
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Users").child(FirebaseAuth.getInstance().getUid())
                                                .child("profession").setValue(editText.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(getContext(),"User Profession Changed", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                                builder.show();

                                break;
                            }

                        }

                        return false;
                    }
                });

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
      if(requestCode==11){
          if (data.getData() != null) {
              Uri uri = data.getData();
              binding.coverPhoto.setImageURI(uri);

              final StorageReference reference = storage.getReference().child("cover_photo").child(FirebaseAuth.getInstance().getUid());
              reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                  @Override
                  public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                      Toast.makeText(getContext(),"Cover Photo Saved", Toast.LENGTH_SHORT).show();

                      reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                          @Override
                          public void onSuccess(Uri uri) {
                              database.getReference().child("Users").child(auth.getUid()).child("coverPhoto").setValue(uri.toString());
                          }
                      });

                  }
              });


          }
      }
      else {
          if (data.getData() != null) {
              Uri uri = data.getData();
              binding.profile.setImageURI(uri);

              final StorageReference reference = storage.getReference().child("profile_photo").child(FirebaseAuth.getInstance().getUid());
              reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                  @Override
                  public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                      Toast.makeText(getContext(),"Profile Photo Saved", Toast.LENGTH_SHORT).show();

                      reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                          @Override
                          public void onSuccess(Uri uri) {
                              database.getReference().child("Users").child(auth.getUid()).child("profile").setValue(uri.toString());
                          }
                      });

                  }

              });


          }
      }
    }
}