package com.ali.example;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ali.animatingimageview.AnimatingImageView;

import java.util.Random;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private AnimatingImageView mAnimatingImageView;
        private Random mRandom;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_main, container, false);
            if (view != null) {
                mAnimatingImageView = (AnimatingImageView)view.findViewById(R.id.animating_image_view);
            }
            setHasOptionsMenu(true);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (mAnimatingImageView != null) {
                mAnimatingImageView.setDrawableResourceIds(new int[]{R.drawable.image_1, R.drawable.image_2, R.drawable.image_3, R.drawable.image_4});
                mAnimatingImageView.startAnimation();
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (mAnimatingImageView == null) {
                return super.onOptionsItemSelected(item);
            }
            switch (item.getItemId()) {
                case R.id.is_animating:
                    Toast.makeText(getActivity(), getString(R.string.is_animating) + ":" + mAnimatingImageView.isAnimating(), Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.start_animation:
                    mAnimatingImageView.startAnimation();
                    return true;
                case R.id.stop_animation:
                    mAnimatingImageView.stopAnimation();
                    return true;
                case R.id.set_start_delay:
                    /**
                     * create a random number which can be zero
                     */
                    if (mRandom == null) {
                        mRandom = new Random();
                    }
                    int startDelay = Math.abs(mRandom.nextInt() % 1000);
                    mAnimatingImageView.setStartDelay(startDelay);
                    Toast.makeText(getActivity(), "Start Delay:" + startDelay, Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.reset_animation:
                    mAnimatingImageView.resetAnimation();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }
}