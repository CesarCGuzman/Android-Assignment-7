package edu.uncc.assignment07;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.uncc.assignment07.databinding.FragmentLoginBinding;
import edu.uncc.assignment07.models.AuthResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFragment extends Fragment {
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentLoginBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();
                if(email.isEmpty()){
                    Toast.makeText(getActivity(), "Enter valid email!", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()){
                    Toast.makeText(getActivity(), "Enter valid password!", Toast.LENGTH_SHORT).show();
                } else {
                    // Login with the email and pass we got from the user
                    login(email, password);
                }
            }
        });

        binding.buttonCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.createNewAccount();
            }
        });

        getActivity().setTitle(R.string.login_label);
    }

    private void login(String email, String password){
        OkHttpClient client = new OkHttpClient();
        // Create a request body with the email and password in it
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        // Create a request with the url and the request body
        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts/login")
                .post(formBody)
                .build();

        // Throw that at the api
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace(); //Print the error
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                // If the response is successful (bro exists in the api) then we can get the token and user id
                if (response.isSuccessful()){
                    String responseString = response.body().string();
                    Log.d("demo", "onResponse: " + responseString);

                    try {
                        // Get the stuffs and throw it in a AuthResponse object
                        JSONObject jsonObject = new JSONObject(responseString);
                        String token = jsonObject.getString("token");
                        String userId = jsonObject.getString("user_id");
                        String user_fullname = jsonObject.getString("user_fullname");

                        // Create a new AuthResponse object
                        AuthResponse authResponse = new AuthResponse();

                        // Set the stuffs
                        authResponse.setToken(token);
                        authResponse.setUser_id(userId);
                        authResponse.setUser_fullname(user_fullname);

                        // Throw it to the main activity
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mListener.authCompleted(authResponse);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else { // If the response is not successful (bro doesn't exist in the api) then we tell them
                    String responseString = response.body().string();
                    JSONObject jsonObject;
                    String message ="";
                    try {
                        jsonObject = new JSONObject(responseString);
                        message = jsonObject.getString("message");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String finalMessage = message;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Error")
                                    .setMessage(finalMessage)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    });
                }
            }
        });
    }

    LoginListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (LoginListener) context;
    }

    interface LoginListener {
        void createNewAccount();
        void authCompleted(AuthResponse authResponse);
    }
}