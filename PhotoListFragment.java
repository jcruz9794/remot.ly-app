package com.example.cosc195cst107finalproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
/**
 *
 * This class generates the top 3/4 of the Stats Screen, which displays the user's stats,
 * save option, and a list of Photos that the user got correct.
 *
 * @author Jonathan Cruz CST107
 * @version 1
 */
public class PhotoListFragment extends Fragment
{
    // Database properties
    private SessionDbHelper db;
    private Cursor cursor;

    // Imported properties (eg. to fetch stats, image resource IDs, and sound fx /sfx)
    protected static Exercise parentActivity;
    private static MediaPlayer mediaPlayer;

    // Stats properties
    public static String name;
    private PhotoSelectedListener listener;
    public static ArrayList<Photo> correctPhotos;
    public static int[] mysteryPhotos = new int[10];
    public final double MINIMUM_SCORE = 70;//must score 70% or higher

    // XML element properties
    private TextView tvCaption, tvAccuracy, tvAllTime;
    private Button btnSave, btnAlt;
    private ImageView ivCorrect1, ivCorrect2, ivCorrect3, ivCorrect4, ivCorrect5, ivCorrect6, ivCorrect7, ivCorrect8, ivCorrect9, ivCorrect10;

    // MAIN
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate Fragment, setup SQLite db, get Media Player (for sfx)
        View photoListFragment = inflater.inflate(R.layout.fragment_photo_list, container, false);
        try
        {
            db = new SessionDbHelper(getActivity());
        }
        catch(SQLException e)
        {
            Toast.makeText(getActivity(), "The SQL database has encountered a problem.", Toast.LENGTH_LONG).show();
            btnSave.setEnabled(false);
        }
        catch(Exception exp) {
            Toast.makeText(getActivity(), "An unknown error occurred. You cannot save this session.", Toast.LENGTH_LONG).show();
            btnSave.setEnabled(false);
        }
        mediaPlayer = parentActivity.getMediaPlayer();

        // Header UI stuff
        tvCaption = photoListFragment.findViewById(R.id.tvCaption);
        tvAccuracy = photoListFragment.findViewById(R.id.tvAccuracy);
        tvAllTime = photoListFragment.findViewById(R.id.tvAllTime);
        loadAndCalculateStats();

        // Middle UI stuff
        btnAlt = photoListFragment.findViewById(R.id.btnAlt);
        btnAlt.setBackgroundColor(Color.rgb(100,110,140));
        assignAltHandler();

        btnSave = photoListFragment.findViewById(R.id.btnSave);
        btnSave.setBackgroundColor(Color.rgb(100,110,140));
        assignSaveHandler();

        // Footer UI stuff
        ivCorrect1 = photoListFragment.findViewById(R.id.ivCorrect1);
        ivCorrect2 = photoListFragment.findViewById(R.id.ivCorrect2);
        ivCorrect3 = photoListFragment.findViewById(R.id.ivCorrect3);
        ivCorrect4 = photoListFragment.findViewById(R.id.ivCorrect4);
        ivCorrect5 = photoListFragment.findViewById(R.id.ivCorrect5);
        ivCorrect6 = photoListFragment.findViewById(R.id.ivCorrect6);
        ivCorrect7 = photoListFragment.findViewById(R.id.ivCorrect7);
        ivCorrect8 = photoListFragment.findViewById(R.id.ivCorrect8);
        ivCorrect9 = photoListFragment.findViewById(R.id.ivCorrect9);
        ivCorrect10 = photoListFragment.findViewById(R.id.ivCorrect10);
        ivCorrect1.setVisibility(View.INVISIBLE);
        ivCorrect2.setVisibility(View.INVISIBLE);
        ivCorrect3.setVisibility(View.INVISIBLE);
        ivCorrect4.setVisibility(View.INVISIBLE);
        ivCorrect5.setVisibility(View.INVISIBLE);
        ivCorrect6.setVisibility(View.INVISIBLE);
        ivCorrect7.setVisibility(View.INVISIBLE);
        ivCorrect8.setVisibility(View.INVISIBLE);
        ivCorrect9.setVisibility(View.INVISIBLE);
        ivCorrect10.setVisibility(View.INVISIBLE);
        populatePhotos();

        return photoListFragment;
    }


    /**
     * This procedure assigns functionality to the Save button.
     * It will try to save the current Session (stats) to the database.
     * On success, it will send a success Toast and disable the button.
     * On failure, it will send a failure Toast.
     */
    private void assignSaveHandler()
    {
        btnSave.setOnClickListener(e -> {
            try
            {
                // Save the session
                db.open();
                Session session = new Session(name, correctPhotos.size());
                db.createSession(session);
                db.close();

                // GUI reactions
                Toast.makeText(getActivity(), "Session saved", Toast.LENGTH_LONG).show();
                btnSave.setText("Saved");
                btnSave.setEnabled(false);

                // If the user saved the session, btnAlt text becomes "Go Again"
                btnAlt.setText(btnAlt.getText().toString().equals("Pretend This Never Happened") ?
                                "Go Again" : "Unlock More Exercises!");
            }
            catch(SQLException e2)
            {
                Toast.makeText(getActivity(), "Could not save the session.", Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * This procedure assigns functionality to the Alt button.
     * This button has two alternate functions depending on the stats achieved:
     *
     *      - If below 70%, user can "Pretend This Never Happened" and play again without saving.
     *      - If 70% or above, user can "Unlock More Exercises", which is the end of this release.
     */
    private void assignAltHandler()
    {
        // If below 70%
        if((correctPhotos.size() * 10) < MINIMUM_SCORE)
        {
            btnAlt.setText("Pretend This Never Happened");

            btnAlt.setOnClickListener(e -> {
                parentActivity.sfxSaveProceedNoise();
                Intent goAgain = new Intent(getActivity(), Exercise.class);
                goAgain.putExtra("realname", name);
                this.startActivity(goAgain);
            });
        }
        else // If 70% or above
        {
            btnAlt.setText("Unlock More Exercises!");

            btnAlt.setOnClickListener(e -> {
                sfxStatsGoodNoise();
                Toast.makeText(getActivity(), "Congratulations! You've reached the end of this app.", Toast.LENGTH_LONG).show();
            });
        }
    }


    /**
     * This procedure fills the tvAccuracy and tvAllTime stats fields.
     * To do this, it must calculate the number of correctPhotos + those already saved in the db.
     *
     * Additionally, this procedure fills the tvTopCaption with a message based on the user meeting
     * the MINIMUM_SCORE requirement.
     */
    private void loadAndCalculateStats()
    {
        // Get stats and photo res IDs from Exercise screen
        correctPhotos = parentActivity.correctPhotos;
        mysteryPhotos = parentActivity.MYSTERY_PHOTOS;

        // Calculate current stats
        double accuracy = Math.floor(correctPhotos.size() * 10);
        double allTimeCorrect = correctPhotos.size();
        double allTimeRounds = 10;
        double allTimeAccuracy;

        try // Include previous stats from database
        {
            db.open();
            cursor = db.getAllSessionsBySQL();
            if(cursor.moveToFirst()) {
                do{
                    // Add stats from this user's records
                    if(cursor.getString(1).equals(name))
                    {
                        allTimeCorrect += (double)cursor.getLong(2);
                        allTimeRounds += 10;
                    }
                } while(cursor.moveToNext());
            }
            db.close();
        }
        catch(SQLException e3)
        {
            Toast.makeText(getActivity(), "Could not retrieve records.", Toast.LENGTH_LONG).show();
        }

        // Calculate all-time accuracy
        allTimeAccuracy = (allTimeCorrect / allTimeRounds) * 100;

        // Set TextViews
        setStatsText(accuracy, allTimeAccuracy);

        // Play appropriate sound fx /sfx
        playStatsResultNoise();
    }

    /**
     * This procedure sets the text for the Stats.
     * @param accuracy - the calculated accuracy (eg. 20.0)
     * @param allTimeAccuracy - the calculated all time accuracy
     */
    private void setStatsText(double accuracy, double allTimeAccuracy)
    {
        tvAccuracy.setText(Math.round(accuracy) + "%");
        tvAllTime.setText(Math.round(allTimeAccuracy) + "%");
        tvCaption.setText(accuracy < MINIMUM_SCORE ? "(Ouch.)" : "You're psychic, " + name + "!");
    }


    /**
     * This procedure shows all the correct photos and assigns click handlers to them.
     */
    private void populatePhotos()
    {
        if (correctPhotos.size() > 0) {
            ivCorrect1.setImageResource( mysteryPhotos[correctPhotos.get(0).round -1] );
            ivCorrect1.setOnClickListener(e -> changePhoto(correctPhotos.get(0)));
            ivCorrect1.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 1) {
            ivCorrect2.setImageResource( mysteryPhotos[correctPhotos.get(1).round -1] );
            ivCorrect2.setOnClickListener(e -> changePhoto(correctPhotos.get(1)));
            ivCorrect2.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 2) {
            ivCorrect3.setImageResource( mysteryPhotos[correctPhotos.get(2).round -1] );
            ivCorrect3.setOnClickListener(e -> changePhoto(correctPhotos.get(2)));
            ivCorrect3.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 3) {
            ivCorrect4.setImageResource( mysteryPhotos[correctPhotos.get(3).round -1] );
            ivCorrect4.setOnClickListener(e -> changePhoto(correctPhotos.get(3)));
            ivCorrect4.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 4) {
            ivCorrect5.setImageResource( mysteryPhotos[correctPhotos.get(4).round -1] );
            ivCorrect5.setOnClickListener(e -> changePhoto(correctPhotos.get(4)));
            ivCorrect5.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 5) {
            ivCorrect6.setImageResource( mysteryPhotos[correctPhotos.get(5).round -1] );
            ivCorrect6.setOnClickListener(e -> changePhoto(correctPhotos.get(5)));
            ivCorrect6.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 6) {
            ivCorrect7.setImageResource( mysteryPhotos[correctPhotos.get(6).round -1] );
            ivCorrect7.setOnClickListener(e -> changePhoto(correctPhotos.get(6)));
            ivCorrect7.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 7) {
            ivCorrect8.setImageResource( mysteryPhotos[correctPhotos.get(7).round -1] );
            ivCorrect8.setOnClickListener(e -> changePhoto(correctPhotos.get(7)));
            ivCorrect8.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 8) {
            ivCorrect9.setImageResource( mysteryPhotos[correctPhotos.get(8).round -1] );
            ivCorrect9.setOnClickListener(e -> changePhoto(correctPhotos.get(8)));
            ivCorrect9.setVisibility(View.VISIBLE);
        }
        if (correctPhotos.size() > 9) {
            ivCorrect10.setImageResource(mysteryPhotos[correctPhotos.get(9).round -1] );
            ivCorrect10.setOnClickListener(e -> changePhoto(correctPhotos.get(9)));
            ivCorrect10.setVisibility(View.VISIBLE);
        }
    }


    /**
     * This method is part of a design pattern, which allows the selected photo from this
     * fragment to trigger an event response in the listener fragment.
     *
     * @param listener - the detailsFragment
     */
    public void setPhotoSelectedListener(PhotoSelectedListener listener)
    {
        this.listener = listener;
    }


    /**
     * This procedure is used when the photos are clicked. It tells the detailsFragment
     * which Photo object to retrieve information from.
     *
     * @param photo - the selected Photo object to retrieve information from.
     */
    public void changePhoto(Photo photo)
    {
        this.listener.setSelectedPhoto(photo);
    }


    /*
        AUDIO HANDLERS
     */
    /**
     * This procedure handles the global Media Player instance.
     * It plays a noise after the 'Sketching' phase countdown.
     */
    protected void sfxStatsNotGoodNoise()
    {
        // Release previous audio
        mediaPlayer.release();

        // Play noise
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.stats_notgood);
        mediaPlayer.start();
    }

    /**
     * This procedure handles the global Media Player instance.
     * It plays a noise after the 'Sketching' phase countdown.
     */
    protected void sfxStatsGoodNoise()
    {
        // Release previous audio
        mediaPlayer.release();

        // Play noise
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.stats_good);
        mediaPlayer.start();
    }

    /**
     * This procedure plays the appropriate sfx noise based on the user's score.
     */
    private void playStatsResultNoise()
    {
        if((correctPhotos.size() * 10) < MINIMUM_SCORE)
        {
            sfxStatsNotGoodNoise();
            return;
        }
        sfxStatsGoodNoise();
    }
}