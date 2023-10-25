package edu.uncc.assignment07;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import edu.uncc.assignment07.databinding.FragmentPostsBinding;
import edu.uncc.assignment07.databinding.PagingRowItemBinding;
import edu.uncc.assignment07.databinding.PostRowItemBinding;
import edu.uncc.assignment07.models.Post;
import edu.uncc.assignment07.models.PostResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostsFragment extends Fragment {
    public PostsFragment() {
        // Required empty public constructor
    }

    FragmentPostsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.createPost();
            }
        });

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.logout();
            }
        });

        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        postsAdapter = new PostsAdapter();
        binding.recyclerViewPosts.setAdapter(postsAdapter);
        getPosts(1);

        binding.recyclerViewPaging.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        pagingAdapter = new PagingAdapter();
        binding.recyclerViewPaging.setAdapter(pagingAdapter);
        binding.textViewPaging.setText("Loading...");


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        binding.textViewTitle.setText("Hey " + sharedPreferences.getString("userFullName", null) + "!");

        getActivity().setTitle(R.string.posts_label);
    }

    PostsAdapter postsAdapter;
    PagingAdapter pagingAdapter;
    ArrayList<Post> mPosts = new ArrayList<>();
    ArrayList<String> mPages = new ArrayList<>();

    private final OkHttpClient client = new OkHttpClient();

    class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder> {
        @NonNull
        @Override
        public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PostRowItemBinding binding = PostRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new PostsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull PostsViewHolder holder, int position) {
            Post post = mPosts.get(position);
            holder.setupUI(post);
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        class PostsViewHolder extends RecyclerView.ViewHolder {
            PostRowItemBinding mBinding;
            Post mPost;
            public PostsViewHolder(PostRowItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Post post){
                mPost = post;
                mBinding.textViewPost.setText(post.getPost_text());
                mBinding.textViewCreatedBy.setText(post.getCreated_by_name());
                mBinding.textViewCreatedAt.setText(post.getCreated_at());

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", null);
                String userId = sharedPreferences.getString("userId", null);

                if (Integer.parseInt(userId) != Integer.parseInt(post.getCreated_by_uid())){
                    mBinding.imageViewDelete.setVisibility(View.GONE);
                } else {
                    mBinding.imageViewDelete.setVisibility(View.VISIBLE);
                }

                mBinding.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletePost(post);
                    }
                });
            }
        }

    }
    private void deletePost(Post post){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        String userId = sharedPreferences.getString("userId", null);

        if (Integer.parseInt(userId) != Integer.parseInt(post.getCreated_by_uid())){
            Toast.makeText(getActivity(), "You can only delete your own posts", Toast.LENGTH_LONG).show();
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("post_id", post.getPost_id())
                .build();

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts/delete")
                .header("Authorization", "BEARER " + token)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    getActivity().runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Post Deleted", Toast.LENGTH_SHORT).show();
                            getPosts(1);
                        }
                    });
                } else {
                    Log.d("demo", "onResponse: Not Successful");
                    Log.d("demo", "onResponse: " + response.code());
                }
            }
        });
    }

    private void getPosts(int page){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts?page=" + page)
                .header("Authorization", "BEARER " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    String body = response.body().string();
                    mPosts.clear();
                    mPages.clear();

                    Gson gson = new Gson();
                    PostResponse postResponse = gson.fromJson(body, PostResponse.class);
                    mPosts.addAll(postResponse.getPosts());
                    for (int i = 0; i < Integer.parseInt(postResponse.getTotalCount()); i++) {
                        // Add a number for every 10 posts
                        if (i % 10 == 0) {
                            mPages.add(String.valueOf(i / 10 + 1));
                        }
                    }

                    getActivity().runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            postsAdapter.notifyDataSetChanged();
                            pagingAdapter.notifyDataSetChanged();
                            binding.textViewPaging.setText("Showing page "+ page  + " of " + mPages.size() + " pages");
                        }
                    });
                } else {
                    Log.d("demo", "onResponse: Not Successful");
                    Log.d("demo", "onResponse: " + response.code());
                }
            }
        });
    }

    class PagingAdapter extends RecyclerView.Adapter<PagingAdapter.PagingViewHolder> {
        @NonNull
        @Override
        public PagingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PagingRowItemBinding binding = PagingRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new PagingViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull PagingViewHolder holder, int position) {
            String page = mPages.get(position);
            holder.setupUI(page);
        }

        @Override
        public int getItemCount() {
            return mPages.size();
        }

        class PagingViewHolder extends RecyclerView.ViewHolder {
            PagingRowItemBinding mBinding;
            String mPage;
            public PagingViewHolder(PagingRowItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(String page){
                mPage = page;
                mBinding.textViewPageNumber.setText(page);
                mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getPosts(Integer.parseInt(page));
                    }
                });
            }
        }

    }

    PostsListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (PostsListener) context;
    }

    interface PostsListener{
        void logout();
        void createPost();
    }
}