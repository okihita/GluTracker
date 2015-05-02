package com.okihita.glutracker.ViewBase;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.okihita.glutracker.R;
import com.okihita.glutracker.ViewSplash.SplashActivity;
import com.okihita.glutracker.util.Config;

public class BaseActivity extends ActionBarActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mMenuTitles = new String[]{"Measure", "Profile", "Logbook", "How To", "About", "Logout"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Set content view and fragment positioning. */
        setContentView(R.layout.single_fragment_drawer_activity);

        /* Setup the Toolbar. */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        /* Setup the navigation drawer. */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_drawer, mMenuTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectItem(position);
            }
        });

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState(); // to properly show burger-arrow as per Android documentation

        // set first screen on launch to Profile
        if (savedInstanceState == null)
            selectItem(1);
    }

    @Override
    public View onCreateView(String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }


    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void selectItem(int position) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

        /* Correspond with mMenuTitles string. */
        switch (position) {
            case 0: /* Measure. */
                ft.replace(R.id.fragmentContainer, new MeasureFragment())
                        .addToBackStack("measure").commit();
                break;

            case 1: /* Profile. */
                ft.replace(R.id.fragmentContainer, new ProfileFragment())
                        .addToBackStack("profile").commit();
                break;

            case 2: /* Logbook. */
                ft.replace(R.id.fragmentContainer, new LogbookFragment())
                        .addToBackStack("logbook").commit();
                break;

            case 3: /* How To. */
                ft.replace(R.id.fragmentContainer, new HowToFragment())
                        .addToBackStack("howto").commit();
                break;

            case 4: /* About. */
                ft.replace(R.id.fragmentContainer, new AboutFragment())
                        .addToBackStack("about").commit();
                break;

            case 5: /* Logout. */
                PreferenceManager.getDefaultSharedPreferences(
                        this.getApplicationContext()).edit()
                        .putInt(Config.LOGGED_IN_USER_ID, 0).commit();
                startActivity(new Intent(BaseActivity.this, SplashActivity.class));
                break;
        }

        /* Close the drawer after selection. */
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}
