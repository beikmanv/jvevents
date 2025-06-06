package com.northcoders.jvevents.ui.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.ui.fragments.userpage.UserPageFragment;
import com.northcoders.jvevents.ui.fragments.eventpage.EventPageFragment;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    NavigationBarView bottomNavBar;

    // Use one instance of each fragment to avoid recreation
    private final EventPageFragment eventPageFragment = new EventPageFragment();
    private final UserPageFragment userPageFragment = new UserPageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavBar = findViewById(R.id.bottomnavbar);
        bottomNavBar.setOnItemSelectedListener(this);

        // Restore state or load default fragment
        if (savedInstanceState == null) {
            bottomNavBar.setSelectedItemId(R.id.eventButton);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.baseFragment, eventPageFragment)
                    .commit();
        }

        // Optionally navigate from intent
        else if (getIntent().hasExtra("navigateTo")) {
            String destination = getIntent().getStringExtra("navigateTo");
            if ("CalendarPageFragment".equals(destination)) {
                bottomNavBar.setSelectedItemId(R.id.profileButton);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.baseFragment, userPageFragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.eventButton) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.baseFragment, eventPageFragment)
                    .commit();
            return true;
        }

        if (itemId == R.id.profileButton) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.baseFragment, userPageFragment)
                    .commit();
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (userPageFragment.isAdded()) {
            userPageFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
