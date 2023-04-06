package com.example.owm;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.ViewHolder> {
    private List<WeatherItem> mWeatherItems;

    public WeatherListAdapter(List<WeatherItem> weatherItems) {
        mWeatherItems = weatherItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherItem item = mWeatherItems.get(position);

        // Calculate the timestamp for 5 days from now
        long fiveDaysFromNow = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5);

        // Parse the date and time from the dt_txt field
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dateTime;
        try {
            dateTime = dateFormat.parse(item.getDtTxt());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date and time", e);
            return;
        }

        // Filter out items with timestamps outside of the next 5 days
        if (dateTime.getTime() > fiveDaysFromNow) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // Check if the timestamp is at an interval of 3 hours
        long timestamp = dateTime.getTime() / 1000;
        if (timestamp % TimeUnit.HOURS.toSeconds(3) != 0) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // Set the timestamp
        SimpleDateFormat format = new SimpleDateFormat("MMM d, HH:mm");
        String timestampString = format.format(dateTime);
        holder.timestampTextView.setText(timestampString);

        // Set the temperature
        holder.temperatureTextView.setText(String.format(Locale.getDefault(), "%.1f K", item.getTemperature()));

        // Set the pressure
        holder.pressureTextView.setText(String.format(Locale.getDefault(), "%.0f hPa", item.getPressure()));

        // Set the humidity
        holder.humidityTextView.setText(String.format(Locale.getDefault(), "%.0f%%", item.getHumidity()));


    }


    public void setWeatherItems(List<WeatherItem> weatherItems) {
        mWeatherItems = weatherItems;
        notifyDataSetChanged();
    }



    @Override
    public int getItemCount() {
        return mWeatherItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timestampTextView;
        public TextView temperatureTextView;
        public TextView pressureTextView;
        public TextView humidityTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            temperatureTextView = itemView.findViewById(R.id.temperature_text_view);
            pressureTextView = itemView.findViewById(R.id.pressure_text_view);
            humidityTextView = itemView.findViewById(R.id.humidity_text_view);
        }
    }
}
