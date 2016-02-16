package com.rwsw.fantasysurvivor.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rwsw.fantasysurvivor.R;

/**
 * Created by Ryan on 2/15/2016.
 */
public class PicksFragment extends Fragment {
    int fragVal;

    public static PicksFragment init(int val) {
        PicksFragment picksFragment = new PicksFragment();
        Bundle args = new Bundle();
        args.putInt("val", val);
        picksFragment.setArguments(args);
        return picksFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragVal = getArguments() != null ? getArguments().getInt("val") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.fragment_currentpicks, container,
                false);
        View tv = layoutView.findViewById(R.id.text);
        ((TextView) tv).setText("Current Week Picks");
        return layoutView;
    }
}
