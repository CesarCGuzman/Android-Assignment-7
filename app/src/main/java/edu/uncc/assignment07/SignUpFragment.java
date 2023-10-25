package edu.uncc.assignment07;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

import edu.uncc.assignment07.databinding.FragmentSignUpBinding;
import edu.uncc.assignment07.models.AuthResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpFragment extends Fragment {
    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentSignUpBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.login();
            }
        });

        binding.buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.editTextName.getText().toString();
                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();

                if(name.isEmpty()){
                    Toast.makeText(getActivity(), "Enter valid name!", Toast.LENGTH_SHORT).show();
                } else if(email.isEmpty()){
                    Toast.makeText(getActivity(), "Enter valid email!", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()){
                    Toast.makeText(getActivity(), "Enter valid password!", Toast.LENGTH_SHORT).show();
                } else {
                    signUp(name, email, password);
                }
            }
        });
        getActivity().setTitle(R.string.create_account_label);
    }

    private void signUp(String name, String email, String password){
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("name", name)
                .build();

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts/signup")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace(); //Print the error
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
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

                        // Throw that at the main activity
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mListener.authCompleted(authResponse);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
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

    SignUpListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (SignUpListener) context;
    }

    interface SignUpListener {
        void login();
        void authCompleted(AuthResponse authResponse);
    }
}