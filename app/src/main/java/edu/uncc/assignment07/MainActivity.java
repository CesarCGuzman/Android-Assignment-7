package edu.uncc.assignment07;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import edu.uncc.assignment07.models.AuthResponse;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener,
        SignUpFragment.SignUpListener, PostsFragment.PostsListener, CreatePostFragment.CreatePostListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Clear the token and user information from SharedPreferences if the user closes the app
        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Check if the user is authenticated
        boolean isAuthenticated = isAuthenticated();

        // If you are authenticated, navigate to the Posts List Fragment
        if(isAuthenticated){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerView, new PostsFragment())
                    .commit();
        } else { // Otherwise, navigate to the Login Fragment so you can get authenticated or create an account
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerView, new LoginFragment())
                    .commit();
        }
    }

    // This method checks if the user is authenticated by checking if the token is stored in SharedPreferences
    private boolean isAuthenticated() {
        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("token", null);
        return authToken != null;
    }

    @Override
    public void createNewAccount() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new SignUpFragment())
                .commit();
    }

    // This method is called when the authentication process is completed
    @Override
    public void authCompleted(AuthResponse authResponse) {
        if(authResponse != null){
            // Handles authentication completion - stores the token and user information in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token", authResponse.getToken());
            editor.putString("userId", authResponse.getUser_id());
            editor.putString("userFullName", authResponse.getUser_fullname());
            editor.apply();

            // Navigate to the Posts List Fragment or perform other necessary actions
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerView, new PostsFragment())
                    .commit();
        } else {
            // Handle authentication failure
            Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void login() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new LoginFragment())
                .commit();
    }

    // This method removes the token and user information from SharedPreferences and navigates to the Login Fragment
    @Override
    public void logout() {
        // Clear the token and user information from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Return to the Login Fragment or perform any other necessary actions for the logout process
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void createPost() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new CreatePostFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goBackToPosts() {
        getSupportFragmentManager().popBackStack();
    }
}