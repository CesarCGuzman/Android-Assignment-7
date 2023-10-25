package edu.uncc.assignment07;

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

import java.io.IOException;

import edu.uncc.assignment07.databinding.FragmentCreatePostBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreatePostFragment extends Fragment {
    public CreatePostFragment() {
        // Required empty public constructor
    }


    FragmentCreatePostBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goBackToPosts();
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postText = binding.editTextPostText.getText().toString();
                if(postText.isEmpty()){
                    Toast.makeText(getActivity(), "Enter valid post !!", Toast.LENGTH_SHORT).show();
                } else {
                    // Create a post request using okhttp to create the post
                    sendPost(postText);
                }
            }
        });

        getActivity().setTitle(R.string.create_post_label);
    }

    private void sendPost(String postText){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("post_text", postText.toString())
                .build();

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts/create")
                .header("Authorization", "BEARER " + token)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();    //Print the error
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.goBackToPosts();
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Error creating post!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    CreatePostListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreatePostListener) context;
    }

    interface CreatePostListener {
        void goBackToPosts();
    }
}