package com.example.cosc195cst107finalproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.ArrayList;

/**
 *
 * This class manages the "Exercise" the user goes through after logging in.
 * This includes the "waiting", "drawing", and "response" phases.
 *
 * @author Jonathan Cruz CST107
 * @version 1
 */
public class Exercise extends AppCompatActivity implements SensorEventListener
{
    // Sensor properties
    private boolean compatible;
    private SensorManager sm;

    // Audio property (for sound fx /sfx)
    private MediaPlayer mediaPlayer;

    // 'Waiting' phase properties (reused in other phases also)
    private TextView tvTopCaption;
    private TextView tvLogo2;

    // 'Sketching' phase properties
    private int currentRound;
    private boolean countingDown;
    private TextView tvInstructions;
    private ImageView ivMysteryPhoto;
    protected final int[] MYSTERY_PHOTOS = { R.drawable.round1, R.drawable.round2, R.drawable.round3, R.drawable.round4, R.drawable.round5, R.drawable.round6, R.drawable.round7, R.drawable.round8, R.drawable.round9, R.drawable.round10 };

    // 'Response' phase properties
    private RadioButton rdoOption1, rdoOption2, rdoOptionNone;
    private RadioGroup rdoGroup;
    private Button btnNext;
    private boolean responsePhase;

    // Temp stats properties
    private String name;
    protected ArrayList<Photo> correctPhotos; // list of Photos that were correct (eg. from round 3, from round 7)

    // Round logic properties
    private String[] answerKey;
    private String[] fakeAnswerKey;


    /**
     * Main.
     * @param savedInstanceState - any bundled state data
     */
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        initProperties();// initialize with default values, handlers, etc.
        loadWaitingPhase();// begin with "Waiting" phase (waits for user to set phone down)
    }


    /**
     * This procedure loads the "Waiting" phase of the exercise. It begins with the instructions:
     * "Place your phone down to load photo [currentRound]/10".
     */
    public void loadWaitingPhase()
    {
        // show instructions for this round
        tvInstructions.setText("Place your phone face down to load photo " + currentRound + "/10");
        tvInstructions.setVisibility(View.VISIBLE);
        tvLogo2.setVisibility(View.VISIBLE);

        // hide "Sketching" phase (IF USER PEEKING)
        ivMysteryPhoto.setVisibility(View.INVISIBLE);

        // hide "Response" phase (IF ROUND 2 OR HIGHER)
        if ( currentRound >= 2 )
        {
            responsePhase = false;
            tvTopCaption.setText("Welcome back, " + name + ".");
            rdoOption1.setVisibility(View.INVISIBLE);
            rdoOption2.setVisibility(View.INVISIBLE);
            rdoOptionNone.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * This procedure loads the "Sketching" phase of the exercise. It shows the mystery photo,
     * and begins a countdown.
     *
     * (NOTE: only one countdown starts, even with multiple method calls).
     */
    private void loadSketchingPhase()
    {
        //show mystery photo
        ivMysteryPhoto.setImageResource(MYSTERY_PHOTOS[currentRound - 1]);
        ivMysteryPhoto.setVisibility(View.VISIBLE);

        //start countdown (if not started already)
        startCountdown();

        //hide "Waiting" phase
        tvTopCaption.setVisibility(View.INVISIBLE);
        tvInstructions.setVisibility(View.INVISIBLE);
        tvLogo2.setVisibility(View.INVISIBLE);
    }


    /**
     * This procedure loads the "Response" phase of the exercise. It shows the radio buttons
     * with response options, and provides a button that either goes to the next photo or to
     * the Stats screen (if it's the last round).
     *
     * It also sets the boolean flag (this.countingDown) to false, indicating that the 5s
     * countdown has completed.
     */
    public void loadResponsePhase()
    {
        //show "Response" phase
        sfxFaceUpNoise();
        responsePhase = true;
        tvLogo2.setVisibility(View.VISIBLE);
        tvTopCaption.setVisibility(View.VISIBLE);
        tvTopCaption.setText("Hi " + name + ", you drew:");
        loadRadioResponses();
        rdoOption1.setVisibility(View.VISIBLE);
        rdoOption2.setVisibility(View.VISIBLE);
        rdoOptionNone.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
            btnNext.setEnabled(false);// wait for radio button selection
            rdoOption1.setOnClickListener(e -> {
                sfxOptionSelectNoise();
                btnNext.setEnabled(true);
            });
            rdoOption2.setOnClickListener(e -> {
                sfxOptionSelectNoise();
                btnNext.setEnabled(true);
            });
            rdoOptionNone.setOnClickListener(e -> {
                sfxOptionSelectNoise();
                btnNext.setEnabled(true);
            });

        // change Next button (IF LAST ROUND)
        if ( currentRound == 10 )
        {
            btnNext.setText("VIEW YOUR RESULTS");
            btnNext.setOnClickListener(e -> {

                saveResponse();

                // move to Stats activity, passing the account's real name into the Intent
                Intent viewStats = new Intent(this, Stats.class);
                this.startActivity(viewStats);

                // pass stats and photo res IDs to the Stats PhotoListFragment
                PhotoListFragment.name = this.name;
                PhotoListFragment.parentActivity = this;
            });
        }

        //hide "Sketching" phase
        countingDown = false;
        ivMysteryPhoto.setVisibility(View.INVISIBLE);
        tvInstructions.setVisibility(View.INVISIBLE);
    }


    /**
     * This procedure loads the next set of responses for the radio buttons.
     * It randomly determines where to put the correct answer.
     */
    private void loadRadioResponses()
    {
        int random = (int)(Math.random() * 2) + 1;

        String correct = this.answerKey[currentRound-1];
        String wrong = this.fakeAnswerKey[(int)(Math.random() * 20)];

        // Randomly decide whether Option 1 or Option 2 gets the correct answer.
        if (random == 1)
        {
            rdoOption1.setText(correct);
            rdoOption2.setText(wrong);
        }
        else if (random == 2)
        {
            rdoOption1.setText(wrong);
            rdoOption2.setText(correct);
        }
    }


    /**
     * This helper method is used by loadSketchingPhase() to start the 5s countdown
     * before loading the "Response" phase.
     *
     * It uses a boolean flag (this.countingDown) to prevent multiple countdowns from starting.
     */
    private void startCountdown()
    {
        if (countingDown == false)
        {
            // flag count down
            countingDown = true;

            // loadResponsePhase in 5 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> loadResponsePhase(), 5000);
        }
    }


    /**
     * This helper method is used by loadResponsePhase() to track the user's correct responses.
     */
    private void saveResponse()
    {
        // Get correct response
        String correctResponse = answerKey[ currentRound -1 ];

        // Get selected response
        String selectedResponse = ( (RadioButton)findViewById(rdoGroup.getCheckedRadioButtonId()) ).getText().toString();

        // Compare
        if (selectedResponse.equals(correctResponse))
        {
            // store correct photo, store selected answer
            correctPhotos.add(new Photo(currentRound, answerKey[currentRound-1]));
        }
        // clear selection
        rdoGroup.clearCheck();
    }


    /**
     * This method determines if the device has an accelerometer sensor.
     * @return - true if there is an accelerometer, else false.
     */
    private boolean isCompatible()
    {
        if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null )
        {
            return true;
        }
        return false;
    }


    /*
        AUDIO HANDLERS
     */
    /**
     * This procedure handles the global Media Player instance.
     * It plays a noise for btnSave.
     */
    protected void sfxSaveProceedNoise()
    {
        // Release previous audio
        mediaPlayer.release();

        // Play noise
        mediaPlayer = MediaPlayer.create(this, R.raw.save_proceed);
        mediaPlayer.start();
    }

    /**
     * This procedure handles the global Media Player instance.
     * It plays a noise after the 'Sketching' phase countdown.
     */
    protected void sfxFaceUpNoise()
    {
        // Release previous audio
        mediaPlayer.release();

        // Play noise
        mediaPlayer = MediaPlayer.create(this, R.raw.face_up);
        mediaPlayer.start();
    }

    /**
     * This procedure handles the global Media Player instance.
     * It plays a noise after clicking a response option.
     */
    protected void sfxOptionSelectNoise()
    {
        // Release previous audio
        mediaPlayer.release();

        // Play noise
        mediaPlayer = MediaPlayer.create(this, R.raw.response_select);
        mediaPlayer.start();
    }

    /**
     * Getter.
     * @return - this.mediaPlayer (reused in Stats screen)
     */
    protected MediaPlayer getMediaPlayer()
    {
        return this.mediaPlayer;
    }


    /*
        REQUIRED METHODS AND OVERRIDES (for Sensors and Listener registration)
     */
    /**
     * This procedure registers this class as the accelerometer/magnetometer listener
     * and sets the rate sensor reporting rate when the app resumes.
     */
    @Override protected void onResume()
    {
        super.onResume();

        Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // Register the listener if this device is compatible
        if(compatible)
        {
            sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }


    /**
     * This procedure unregisters this class as the accelerometer/magnetometer listener.
     * (This helps to save battery while the app is paused)
     */
    @Override protected void onPause()
    {
        super.onPause();

        sm.unregisterListener(this);
    }


    /**
     * This declaration is required by the accelerometer, but is unneeded in this app.
     */
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    /**
     * This procedure reads the Z-axis value from the accelerometer's SensorEvent.
     * It toggles the mystery photo on or off based on this value, where a z-value
     * of -9.81 means the phone is parallel to the floor (screen face down).
     *
     * NOTE: I added a +/- 0.10 margin of error surrounding the target z-value.
     * @param event - the SensorEvent that triggered this method call
     */
    @Override public void onSensorChanged(SensorEvent event)
    {
        if ( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )
        {
            //read the Z-axis value (-9.81 is exactly flat face down)
            float z = event.values[2];

            //Toggle show photo
            if ( z < -9.71 && z > -9.91 )
            {
                if ( !responsePhase ) { loadSketchingPhase(); }
            }
            //Toggle hide photo
            else
            {
                if ( !responsePhase ) { loadWaitingPhase(); }
            }
        }
    }

    /*
        ANSWER BANK (answerKey, fakeAnswerKey)
     */
    /**
     * This method imports the answer key into the app.
     * @return - String[] answers
     */
    private String[] importAnswerKey()
    {
        String[] answers = new String[10];
        answers[0] = "A series of diamond shapes.";
        answers[1] = "A pentagonal shape.";
        answers[2] = "A round shape inside a square shape.";
        answers[3] = "Twin stripes inside a circle.";
        answers[4] = "A series of wiggly lines.";
        answers[5] = "One or more circles against a pole.";
        answers[6] = "A crystal or great pyramid.";
        answers[7] = "A small humanoid by a large humanoid.";
        answers[8] = "Squares within squares.";
        answers[9] = "An eye that watches a dwelling.";
        return answers;
    }


    /**
     * This method imports the fake answer key into the app.
     * @return - String[] fake answers
     */
    private String[] importFakeAnswerKey()
    {
        String[] fakeAnswers = new String[20];
        fakeAnswers[0] = "A series of pretzel shapes.";
        fakeAnswers[1] = "Two triangular shapes.";
        fakeAnswers[2] = "A donut shape beside a square.";
        fakeAnswers[3] = "Dots. Dots everywhere.";
        fakeAnswers[4] = "An extremely tiny Z or N.";
        fakeAnswers[5] = "A creature with odd number of legs.";
        fakeAnswers[6] = "'Fake news' in graffiti letters.";
        fakeAnswers[7] = "A cartoon face inside a pear shape.";
        fakeAnswers[8] = "A 3-dimensional cube.";
        fakeAnswers[9] = "Something phallic.";
        fakeAnswers[10] = "A superman symbol.";
        fakeAnswers[11] = "A chain-link or paperclip.";
        fakeAnswers[12] = "A mountain range.";
        fakeAnswers[13] = "A vehicle in a tunnel.";
        fakeAnswers[14] = "Domino shapes.";
        fakeAnswers[15] = "Several hands or fingers.";
        fakeAnswers[16] = "Lightning.";
        fakeAnswers[17] = "A severe weather event.";
        fakeAnswers[18] = "A horseshoe shape.";
        fakeAnswers[19] = "A cross made with banana shapes.";
        return fakeAnswers;
    }

    /*
        GLOBAL PROPERTY INITIALIZATION
     */
    /**
     * This procedure finds all views by Id and initializes with default values, including:
     *      - this device's compatibility
     *      - values passed from previous activity
     *      - event handlers
     *      - default TextView strings
     *      - default visibility
     */
    private void initProperties()
    {
        //get SensorManager and determine compatibility
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        compatible = isCompatible();

        //set Media Player with a face-up noise
        this.mediaPlayer = MediaPlayer.create(this, R.raw.face_up);

        //set first round
        currentRound = 1;
        countingDown = false;

        //get images, correct log, answer key
        correctPhotos = new ArrayList<Photo>();
        answerKey = importAnswerKey();
        fakeAnswerKey = importFakeAnswerKey();

        //get name & logo
        name = getIntent().getExtras().getString("realname");
        tvLogo2 = findViewById(R.id.tvLogo2);

        //get & set top caption
        tvTopCaption = findViewById(R.id.tvTopCaption);
        //tvTopCaption.setText(compatible ? "Welcome back, " + name + "." : "Your device does not support accelerometer use.");
        tvTopCaption.setText("Your device does not support accelerometer use.");

        //get & hide instructions
        tvInstructions = findViewById(R.id.tvPromptUser);
        tvInstructions.setVisibility(View.INVISIBLE);

        //get & hide photo
        ivMysteryPhoto = findViewById(R.id.ivMysteryPhoto);
        ivMysteryPhoto.setVisibility(View.INVISIBLE);

        //get & hide response options
        responsePhase = false;
        rdoGroup = findViewById(R.id.rdoGroup);
        rdoOption1 = findViewById(R.id.rdoOption1);
        rdoOption1.setVisibility(View.INVISIBLE);
        rdoOption2 = findViewById(R.id.rdoOption2);
        rdoOption2.setVisibility(View.INVISIBLE);
        rdoOptionNone = findViewById(R.id.rdoOptionNone);
        rdoOptionNone.setText("None of the above");
        rdoOptionNone.setVisibility(View.INVISIBLE);

        //get, set, & hide "next" button
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(e -> {
            saveResponse();
            sfxSaveProceedNoise();// uses media player
            currentRound++;
            loadWaitingPhase();
        });
        btnNext.setVisibility(View.INVISIBLE);
        btnNext.setBackgroundColor(Color.rgb(100,110,140));
    }
}