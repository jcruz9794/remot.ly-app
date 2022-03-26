package com.example.cosc195cst107finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
/**
 *
 * This parent activity is what holds both the PhotoListFragment and PhotoDetailsFragment together.
 *
 * @author Jonathan Cruz CST107
 * @version 1
 */
public class Stats extends AppCompatActivity
{
    // MAIN
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Get Fragment Manager
        FragmentManager manager = getSupportFragmentManager();

        // Get refs to PhotoListFragment and PhotoDetailsFragment
        PhotoListFragment listFragment = (PhotoListFragment) manager.findFragmentById(R.id.listFragment);
        PhotoDetailsFragment detailsFragment = (PhotoDetailsFragment) manager.findFragmentById(R.id.detailsFragment);

        // Set PhotoDetailsFragment as the listener
        listFragment.setPhotoSelectedListener(detailsFragment);
    }
}