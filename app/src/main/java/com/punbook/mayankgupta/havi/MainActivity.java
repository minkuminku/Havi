package com.punbook.mayankgupta.havi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.appevents.internal.Constants;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.punbook.mayankgupta.havi.dummy.DummyContent;
import com.punbook.mayankgupta.havi.dummy.Status;
import com.punbook.mayankgupta.havi.dummy.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ItemFragment.OnListFragmentInteractionListener,
        TaskFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 123;
    public static final String DB_ROOT = "users";
    public static final String SEPERATOR = "/";
    public static final String TASKS = "tasks";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mTaskDatabaseReference;
    private ChildEventListener mTaskEventListener;
    private ChildEventListener mChildEventListener2;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String uniqueUserId;

    private List<Task> tasks = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        //mFirebaseStorage = FirebaseStorage.getInstance();

       // mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("users");

  //      mUserDatabaseReference = mFirebaseDatabase.getReference().child("users/user1");


        // [START handle_data_extras]

        // [END handle_data_extras]

        if (getIntent().getExtras() != null) {

            Task task = new Task();

            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
                switch (key){

                    case "SUMMARY" :    task.setSummary(value.toString());
                    task.setStartDate(6372637236l); task.setExpiryDate(62372637236l); task.setStatus(Status.parse("active"));
                    //   mUserDatabaseReference.push().setValue(task);
                        break;


                }
            }



        }



        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //attachDatabaseReadListener();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Toast.makeText(getApplicationContext(),"Signed in in activity",Toast.LENGTH_SHORT).show();
                    uniqueUserId = user.getUid();

                    final String userTablePath = DB_ROOT+SEPERATOR+uniqueUserId;
                    final String userTableTaskPath = DB_ROOT+SEPERATOR+uniqueUserId + SEPERATOR + TASKS;
                    mUserDatabaseReference = mFirebaseDatabase.getReference().child(userTablePath);
                    mTaskDatabaseReference = mFirebaseDatabase.getReference().child(userTableTaskPath);
                    mUserDatabaseReference.child("username").setValue(user.getEmail());
                   // mUserDatabaseReference.child("name").setValue(user.getEmail());
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // User is signed out
                    onSignedOutCleanup();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void onSignedInInitialize(String username) {
       // mUsername = username;
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
       // mUsername = ANONYMOUS;
        DummyContent.ITEMS.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mTaskEventListener == null) {
            mTaskEventListener = new ChildEventListener() {
                private int counter = 1;
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                        Task friendlyMessage = dataSnapshot.getValue(Task.class);
                        friendlyMessage.setPostKey(dataSnapshot.getKey());
                        friendlyMessage.setId("" + counter++);

                        System.out.println("friendlyMessage  = " + friendlyMessage);
                        //  DummyContent.DummyItem dummyItem = new DummyContent.DummyItem("" + DummyContent.ITEMS.size()+1,friendlyMessage.getStatus(),friendlyMessage.getSummary());
                        //  dummyItem.setPostKey(friendlyMessage.getId());

                        //DummyContent.ITEMS.add(dummyItem);
                        tasks.add(friendlyMessage);


                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            //mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
           //  mUserDatabaseReference.addChildEventListener(mTaskEventListener); // remove it
             mTaskDatabaseReference.addChildEventListener(mTaskEventListener); // remove it
        }
    }

    private void detachDatabaseReadListener() {
        if (mTaskEventListener != null) {
           // mUserDatabaseReference.removeEventListener(mTaskEventListener);
            mTaskDatabaseReference.removeEventListener(mTaskEventListener);
            mTaskEventListener = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        //mMessageAdapter.clear();
        DummyContent.ITEMS.clear();
        detachDatabaseReadListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    //default one
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

   /* @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            AuthUI.getInstance().signOut(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

         if (id == R.id.nav_gallery) {
            GalleryFragment galleryFragment = GalleryFragment.newInstance("jjjjjjjjjj","hhhhhhhshjswsjwswswss");
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Toast.makeText(this,galleryFragment.getTag(),Toast.LENGTH_SHORT).show();
            Log.i("MYTAG","" +galleryFragment.getId());
            Log.i("MYTAG","end" +galleryFragment.getTag());

           fragmentTransaction.replace(R.id.content_main,galleryFragment,TAG);
           fragmentTransaction.addToBackStack(null);
                   fragmentTransaction.commit();



        } else if (id == R.id.nav_manage) {

            ItemFragment itemFragment = ItemFragment.newInstance(0, tasks);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.content_main,itemFragment,itemFragment.getTag());
           // fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();


        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onListFragmentInteraction(Task item) {
        Toast.makeText(this,item.getStatus().toString(),Toast.LENGTH_SHORT).show();
       /* FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GalleryFragment galleryFragment = (GalleryFragment) fragmentManager.findFragmentByTag(TAG);
        fragmentTransaction.replace(R.id.content_main,galleryFragment);
       // fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();*/

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TaskFragment taskFragment = TaskFragment.newInstance(item.getStatus().toString(),item.getSummary(), item.getPostKey(), item.getComments());
        fragmentTransaction.replace(R.id.content_main,taskFragment,"FRAG_TAG");
        // fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();



    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Toast.makeText(this,"TASK FRAGMENT CLICKED",Toast.LENGTH_SHORT).show();
    }

    //TODO : make DAO for handling database reference



}

