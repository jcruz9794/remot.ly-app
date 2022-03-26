package com.example.cosc195cst107finalproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
/**
 *
 * This class manages the Login Screen the user sees when first launching the app.
 *
 * @author Jonathan Cruz CST107
 * @version 1
 */
public class MainActivity extends AppCompatActivity
{
    // 'Login screen' properties
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private SharedPreferences loginPrefs;
    private String authUser, authPass, authName;
    private MediaPlayer sfxBadLoginNoise;

    // MAIN
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load dummy account into SharedPreferences (if it isn't already)
        loadDummyAccount();

        // assign views, and event handler
        sfxBadLoginNoise = MediaPlayer.create(this, R.raw.login_notgood);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(e -> loginHandler());
        btnLogin.setBackgroundColor(Color.rgb(100,110,140));
    }


    /**
     * This procedure compares the EditText strings against the stored SharedPrefs info.
     * If valid, it goes to the Exercise activity, and passes the account's real name.
     */
    private void loginHandler()
    {
        refreshPrefs();

        if(etUsername.getText().toString().equals(authUser) && etPassword.getText().toString().equals(authPass))
        {
            // move to Exercise activity, passing the account's real name into the Intent
            Intent authenticated = new Intent(this, Exercise.class);
            authenticated.putExtra("realname", authName);
            this.startActivity(authenticated);
        }
        else
        {
            sfxBadLoginNoise.start();
            Toast.makeText(this, "Invalid username and/or password.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * This procedure checks if user has dummy login stored in SharedPrefs.
     * If not, this adds it. (username, password, real name)
     */
    private void loadDummyAccount()
    {
        refreshPrefs();

        if(authUser.equals("not found") && authPass.equals("not found") && authName.equals("not found"))
        {
            SharedPreferences.Editor prefsEditor = loginPrefs.edit();
            prefsEditor.putString("username", "Jeb123");
            prefsEditor.putString("password", "ilovesquirrels");
            prefsEditor.putString("realname", "Jeb");
            prefsEditor.commit();
        }
    }


    /**
     * This procedure refreshes the global properties: authUser, authPass, & authName.
     * (Retrieves them from SharedPreferences)
     */
    private void refreshPrefs()
    {
        loginPrefs = this.getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        authUser = loginPrefs.getString("username", "not found");
        authPass = loginPrefs.getString("password", "not found");
        authName = loginPrefs.getString("realname", "not found");
    }
}