package com.example.cosc195cst107finalproject;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
/**
 *
 * This class generates the bottom 1/4th of the Stats Screen where the user's response will show.
 *
 * @author Jonathan Cruz CST107
 * @version 1
 */
public class PhotoDetailsFragment extends Fragment implements PhotoSelectedListener
{
    // MAIN
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_photo_details, container, false);
    }

    @Override public void setSelectedPhoto(Photo photo)
    {
        // Get the View which represents this Fragment
        View detailsView = this.getView();

        // Get ref to tvResponse
        TextView response = (TextView)detailsView.findViewById(R.id.tvResponse);

        // Set the text of each TextView to match the selected Photo
        response.setText("\"" + photo.correctResponse + "\"");
    }
}