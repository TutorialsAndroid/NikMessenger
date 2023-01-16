package com.messenger.nik.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.messenger.nik.R;
import com.messenger.nik.fragment.ChatFragment;
import com.messenger.nik.customInterface.ChatUserData;
import com.messenger.nik.models.GifModel;

import java.util.ArrayList;

public class GifAdapter extends RecyclerView.Adapter<GifAdapter.GifViewHolder> {

    //CONSTANTS
    private static final String TAG = GifAdapter.class.getSimpleName();

    private final ArrayList<GifModel> imageUrls;
    private final Context context;
    private final ChatUserData cd;

    public GifAdapter(Context context, ArrayList<GifModel> gifModels, ChatUserData chatUserData) {
        this.context = context;
        this.imageUrls = gifModels;
        this.cd = chatUserData;
    }

    @NonNull
    @Override
    public GifViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gif_view, viewGroup, false);
        return new GifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GifViewHolder gifViewHolder, int i) {
        Glide.with(context).load(imageUrls.get(i).getImageUrl()).into(gifViewHolder.img);

        gifViewHolder.img.setOnClickListener(v -> {
            cd.data(null,imageUrls.get(i).getImageUrl(),null,null);
            ChatFragment.get().sendGif( ChatFragment.get().sendGifModel() );
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public static class GifViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        public GifViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.gif_image_view);
        }
    }
}
