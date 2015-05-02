package com.okihita.glutracker.ViewSplash;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.okihita.glutracker.R;
import com.okihita.glutracker.ViewBase.BaseActivity;
import com.okihita.glutracker.util.Config;

public class SplashActivity extends ActionBarActivity {

    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* If a user has logged in, go to app homepage, without calling onCreateView(). */
        if (PreferenceManager.getDefaultSharedPreferences(this).getInt(Config.LOGGED_IN_USER_ID, 0) != 0)
            startActivity(new Intent(this, BaseActivity.class));

        /* Set content view and fragment positioning. */
        setContentView(R.layout.single_fragment_fullscreen_activity);
        mFragmentManager = getFragmentManager();
        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragmentContainer);

        /* If there's no fragment already, add SplashFragment. */
        if (fragment == null) {
            fragment = new SplashFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }

        /* Setup the Toolbar. */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.splash_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        switch (item.getItemId()) {

            case R.id.action_login:

                Fragment loginFragment = new LoginFragment();
                mFragmentTransaction.replace(R.id.fragmentContainer, loginFragment).commit();
                return true;

            case R.id.action_signup:
                Fragment signupFragment = new SignupFragment();
                mFragmentTransaction.replace(R.id.fragmentContainer, signupFragment).commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
