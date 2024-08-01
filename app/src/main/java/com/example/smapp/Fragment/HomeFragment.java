package com.example.smapp.Fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.example.smapp.Adapter.PostAdapter;
import com.example.smapp.Adapter.StoryAdapter;
import com.example.smapp.Model.Post;
import com.example.smapp.Model.Story;
import com.example.smapp.Model.UserStories;
import com.example.smapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
public class HomeFragment extends Fragment {

    RecyclerView storyRV;
    ShimmerRecyclerView dashboardRV;
    ArrayList<Story> list;
    ArrayList<Post> postList;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    ImageView addStoryImage;
    RoundedImageView storyImg;
    ProgressDialog dialog;
    ActivityResultLauncher<String> gallaryLaunncher;
    int postCount;
    boolean created;
    String lastPostId, lastPostId2;
    FragmentTransaction transaction;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(getContext());

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        created=false;
        addStoryImage = view.findViewById(R.id.addStory);
        storyImg = view.findViewById(R.id.storyImg);
        list = new ArrayList<>();
        storage = FirebaseStorage.getInstance();
        storyRV = view.findViewById(R.id.storyRV);

        dashboardRV = view.findViewById(R.id.savedPostRV);


        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Story Uploading...");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        StoryAdapter storyAdapter = new StoryAdapter(list,getContext());
        storyRV.setLayoutManager(linearLayoutManager);
        storyRV.setNestedScrollingEnabled(false);
        storyRV.setAdapter(storyAdapter);

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    list.clear();
                    View view1 = LayoutInflater.from(getContext()).inflate(R.layout.story_rv_design, null, false);
                    ViewGroup.LayoutParams p1 = view1.findViewById(R.id.postImg).getLayoutParams();
                    ViewGroup.LayoutParams params = storyRV.getLayoutParams();
                    params.width+=20;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Story story = new Story();
                        story.setStoryBy(dataSnapshot.getKey());
                        story.setStoryAt(dataSnapshot.child("postedAt").getValue(Long.class));

                        params.width = params.width + p1.width + 9;

                        ArrayList<UserStories> stories = new ArrayList<>();
                        for (DataSnapshot snapshot1 : dataSnapshot.child("userStories").getChildren()){
                            UserStories userStories = snapshot1.getValue(UserStories.class);
                            stories.add(userStories);
                            }
                        story.setUserStoriesList(stories);
                        list.add(story);


                    }
                    storyRV.setLayoutParams(params);
                    storyAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postList = new ArrayList<>();

        PostAdapter postAdapter = new PostAdapter(postList, getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        dashboardRV.setLayoutManager(linearLayoutManager1);
        dashboardRV.setNestedScrollingEnabled(false);
        dashboardRV.setAdapter(postAdapter);
        dashboardRV.showShimmerAdapter();

       database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(postCount!=snapshot.getChildrenCount())postAdapter.setChanged(true);
                    postList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                        Post post = dataSnapshot.getValue(Post.class);
                        post.setPostId(dataSnapshot.getKey());
                        postList.add(post);
                        lastPostId = post.getPostId();
                        if(!postAdapter.isChanged()){
                            if(lastPostId2==lastPostId)postAdapter.setChanged(false);
                            else  postAdapter.setChanged(true);
                        }
                    }
                    lastPostId2=lastPostId;
                    postCount = postList.size();
                   if(!created) {dashboardRV.hideShimmerAdapter();created=true;}
                    postAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



        addStoryImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dialog.show();
                gallaryLaunncher.launch("image/*");
            }
        });

        gallaryLaunncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Bitmap bitmap=null;

                try {
                    bitmap= MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), result);
                } catch (IOException e) {}

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
                String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, getString(R.string.app_name), null);

                Uri uuu = Uri.parse(path);

                final StorageReference reference = storage.getReference().child("stories")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child(new Date().getTime()+"");
                reference.putFile(uuu).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                    Story story = new Story();
                                    story.setStoryAt(new Date().getTime());

                                    database.getReference().child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child("postedAt").setValue(story.getStoryAt()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    UserStories stories = new UserStories(uri.toString(), story.getStoryAt());
                                                    database.getReference().child("stories")
                                                            .child(FirebaseAuth.getInstance().getUid())
                                                            .child("userStories")
                                                            .push().setValue(stories).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(getContext(),"Story Uploaded...",Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                            }
                        });
                    }
                });



            }
        });

        view.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.menu_message, popupMenu.getMenu());
                popupMenu.show();

                transaction = getFragmentManager().beginTransaction();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){

                            case R.id.savedPost:
                                transaction.replace(R.id.container, new savedPost());
                                break;

                            case R.id.message: break;

                        }
                        transaction.commit();

                        return false;
                    }
                });

            }
        });

        return view;
    }
}