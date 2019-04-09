package com.example.monakk.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.MobileAds;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;
import com.example.monakk.R;
import com.example.monakk.fragments.BlankFragment;
import com.example.monakk.fragments.MainFragment;
import com.example.monakk.model.Niche;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.cache.ConnectionBuddyCache;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    BlankFragment.OnFragmentInteractionListener,
    MainFragment.OnFragmentInteractionListener,
    ConnectivityChangeListener {

  @BindView(R.id.nav_view)
  NavigationView mNavigationView;
  @BindView(R.id.toolbar)
  Toolbar toolbar;

  DrawerLayout mDrawer;
  private Realm mRealm;
  ActionBarDrawerToggle mToggle;

  private final Handler mHandler = new Handler();
  android.support.v4.app.FragmentTransaction fragmentTransaction;
  ProgressDialog progressDialog;
  MainFragment mainFragment = new MainFragment();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
        .cordinatorlayout);
    if (savedInstanceState != null) {
      ConnectionBuddyCache.clearLastNetworkState(this);
    }

    ButterKnife.bind(this);
    MobileAds.initialize(this, "ca-app-pub-4083824684818051~6248546920");

    ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);
    Realm.init(getApplicationContext());

    setRealm();
    setProgressDialog();

    Animation rotation = AnimationUtils.loadAnimation(this, R.anim.flipy);
    rotation.setFillAfter(true);

    progressDialog.show();

    setSupportActionBar(toolbar);

    setupDrawer();

    mToggle.syncState();
    mNavigationView.setNavigationItemSelectedListener(this);

    if(!isOnline()){
      Snackbar snackbar = Snackbar
          .make(coordinatorLayout, "No Internet !!", Snackbar.LENGTH_INDEFINITE)
          .setAction("Settings", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
          });

      snackbar.show();

      progressDialog.dismiss();

    }

    getFromFirebase();

    checkProgressState();

    showHomeFragment();
  }

  public void setProgressDialog() {
    progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Fetching");
    progressDialog.setIndeterminate(true);
    progressDialog.setCancelable(false);
    progressDialog.show();
  }
  private void checkProgressState(){
    if (mRealm.where(Niche.class).findAll().size() > 0) {

      progressDialog.dismiss();
    }
  }





  private void setupDrawer(){
    mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
     mToggle = new ActionBarDrawerToggle(
        this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawer.addDrawerListener(mToggle);

  }
  private void setRealm(){
    RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().name(Realm.DEFAULT_REALM_NAME)
        .schemaVersion(0)
        .deleteRealmIfMigrationNeeded()
        .build();
    mRealm = Realm.getInstance(realmConfiguration);
  }
  public boolean checkIfExists(String id) {

    RealmQuery<Niche> query = mRealm.where(Niche.class)
        .equalTo("topic", id);

    return query.count() != 0;
  }

  public void getFromFirebase() {

    Firebase myFirebaseRef = new Firebase("https://knowfeed.firebaseio.com/");

    myFirebaseRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot snapshot) {

        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
          Log.wtf("onDataChange: ", postSnapshot.getKey());
          HashMap<String, String> dummy = new HashMap<>();

          for (DataSnapshot topicSnapshot : postSnapshot.getChildren()) {
            dummy.put(topicSnapshot.getKey(), (String) topicSnapshot.getValue());
          }

          mRealm.beginTransaction();
          mRealm.copyToRealmOrUpdate(new Niche(mRealm, postSnapshot.getKey(), dummy));
          mRealm.commitTransaction();
        }
        if (mainFragment.isVisible()) {
          progressDialog.dismiss();
          mainFragment.reloadList();
        }
      }

      @Override
      public void onCancelled(FirebaseError error) {

      }
    });
  }

  public boolean isOnline() {
    ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = null;
    if (connMgr != null) {
      networkInfo = connMgr.getActiveNetworkInfo();
    }
    return (networkInfo != null && networkInfo.isConnected());
  }

  @Override
  public void onBackPressed() {
    if (mDrawer.isDrawerOpen(GravityCompat.START)) {
      mDrawer.closeDrawer(GravityCompat.START);
    } else {
      if (mainFragment.isVisible()) {
        this.finish();
      } else {
        showHomeFragment();
      }
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {

    int id = item.getItemId();

    if (id == R.id.nav_camera) {

      showHomeFragment();
    }

    mDrawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onFragmentInteraction(Uri uri) {

  }

  public void showFragment(Fragment fragmentToSet) {
    fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.frame, fragmentToSet);
    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
        android.R.anim.fade_in, android.R.anim.fade_out);
    fragmentTransaction.commit();
  }

  public void setUrl(String mUrl) {
    showFragment(BlankFragment.newInstance(mUrl)
    );
  }

  public void showHomeFragment() {
    if (mainFragment == null) {
      mainFragment = new MainFragment();
    }
    toolbar.setTitle(R.string.app_title);
    fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.frame, mainFragment).addToBackStack("mainFragnment");
    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
        android.R.anim.fade_in, android.R.anim.fade_out);
    fragmentTransaction.commitAllowingStateLoss();
  }

  @Override
  public void onConnectionChange(ConnectivityEvent event) {
    if (event.getState() == ConnectivityState.CONNECTED) {


    } else {

      final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
          .cordinatorlayout);
      Snackbar snackbar = Snackbar
          .make(coordinatorLayout, "No Internet", Snackbar.LENGTH_INDEFINITE)
          .setAction("Settings", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
          });

      snackbar.show();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mRealm.close();
  }
}
