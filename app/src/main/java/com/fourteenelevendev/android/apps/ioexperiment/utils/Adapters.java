package com.fourteenelevendev.android.apps.ioexperiment.utils;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import com.fourteenelevendev.android.apps.ioexperiment.R;
import com.fourteenelevendev.android.apps.ioexperiment.model.Letter;

/**
 * Adapter for RecyclerView
 *
 * Created by kyleparker on 4/6/2016.
 */
public class Adapters {

    /**
     * Adapter to display the users who are participating in the challenge
     */
    public static class LetterAdapter extends RecyclerView.Adapter<LetterAdapter.ViewHolder> {
        private Activity mActivity;
        private ArrayList<Letter> mItems;

        public LetterAdapter(Activity activity) {
            mActivity = activity;
            mItems = new ArrayList<>();
        }

        public void addAll(List<Letter> items) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_letter, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            Letter item = mItems.get(position);

            viewHolder.letter.setImageResource(item.getImageResId());
            viewHolder.letter.setContentDescription(mActivity.getString(item.getContentDescription()));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView letter;

            public ViewHolder(View base) {
                super(base);

                letter = (ImageView) itemView.findViewById(R.id.letter);
            }
        }
    }
}
