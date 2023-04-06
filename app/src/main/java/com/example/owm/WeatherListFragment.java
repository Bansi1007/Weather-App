package com.example.owm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<WeatherItem> mWeatherItems;
    private WeatherListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_list, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new WeatherListAdapter(mWeatherItems);
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    public void setWeatherItems(List<WeatherItem> weatherItems) {
        mWeatherItems = weatherItems;
        if (mAdapter != null) {
            mAdapter.setWeatherItems(mWeatherItems);
            mAdapter.notifyDataSetChanged();
        }

    }

}
