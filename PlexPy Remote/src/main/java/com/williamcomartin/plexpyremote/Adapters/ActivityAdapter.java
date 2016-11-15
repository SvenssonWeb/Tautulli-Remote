package com.williamcomartin.plexpyremote.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.williamcomartin.plexpyremote.ApplicationController;
import com.williamcomartin.plexpyremote.Helpers.UrlHelpers;
import com.williamcomartin.plexpyremote.Models.ActivityModels.Activity;
import com.williamcomartin.plexpyremote.R;

import java.util.List;
import java.util.Map;

/**
 * Created by wcomartin on 2015-11-25.
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    View itemView;
    private SharedPreferences SP;

    public ActivityAdapter() {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {


        protected TextView vSeasonEpisode;
        protected TextView vTitle;

        protected TextView vUser;
        protected TextView vStatus;
        protected TextView vPlayer;

        protected TextView vStream;
        protected TextView vVideo;
        protected TextView vAudio;

        protected TextView vStreamLabel;
        protected TextView vVideoLabel;
        protected TextView vAudioLabel;

        protected ProgressBar vprogress;

        protected NetworkImageView vImage;

        public ViewHolder(View itemView) {
            super(itemView);

            vTitle = (TextView) itemView.findViewById(R.id.activity_card_title);
            vSeasonEpisode = (TextView) itemView.findViewById(R.id.activity_card_season_episode);

            vUser = (TextView) itemView.findViewById(R.id.activity_card_user);
            vStatus = (TextView) itemView.findViewById(R.id.activity_card_status);
            vPlayer = (TextView) itemView.findViewById(R.id.activity_card_player);

            vStream = (TextView) itemView.findViewById(R.id.activity_card_stream);
            vVideo = (TextView) itemView.findViewById(R.id.activity_card_video);
            vAudio = (TextView) itemView.findViewById(R.id.activity_card_audio);

            vStreamLabel = (TextView) itemView.findViewById(R.id.activity_card_stream_label);
            vVideoLabel = (TextView) itemView.findViewById(R.id.activity_card_video_label);
            vAudioLabel = (TextView) itemView.findViewById(R.id.activity_card_audio_label);

            vprogress = (ProgressBar) itemView.findViewById(R.id.progressbar);

            vImage = (NetworkImageView) itemView.findViewById(R.id.activity_card_image);

        }
    }

    private List<Activity> activities;

    // Pass in the contact array into the constructor
    public ActivityAdapter(List<Activity> activities) {
        this.activities = activities;
    }

    @Override
    public ActivityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        itemView = inflater.inflate(R.layout.item_activity, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ActivityAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Activity activity = activities.get(position);

        ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar);

        progressBar.setProgress(50);

        // Set item views based on the data model
        if(activity.media_type.equals("episode")) {
            viewHolder.vTitle.setText(activity.grandparent_title + " - " + activity.title);
            viewHolder.vSeasonEpisode.setText("S" + String.format("%02d", Integer.parseInt(activity.parent_media_index)) + "E" + String.format("%02d", Integer.parseInt(activity.media_index)));
        } else {
            viewHolder.vTitle.setText(activity.title);

            if (activity.media_type.equals("track")){
                viewHolder.vSeasonEpisode.setText(activity.grandparent_title + " - " + activity.parent_title);
            }

        }

        viewHolder.vUser.setText(activity.friendly_name);
        viewHolder.vStatus.setText(activity.state);
        viewHolder.vPlayer.setText(activity.platform + " - " + activity.player);

        String stream = buildStreamString(activity);
        String video = buildVideoString(activity);
        String audio = buildAudioString(activity);

        viewHolder.vStream.setText(stream);
        viewHolder.vVideo.setText(video);
        viewHolder.vAudio.setText(audio);

        if(activity.media_type.equals("track")){
            viewHolder.vVideo.setVisibility(View.GONE);
            viewHolder.vVideoLabel.setVisibility(View.GONE);
        }

        SP = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getInstance().getApplicationContext());

        viewHolder.vprogress.setProgress(Integer.parseInt(activity.progress_percent));
        String imageUrl;
        if(!activity.art.equals("")) {
            imageUrl = UrlHelpers.getImageUrl(activity.art, "500", "280", "art");
        } else {
            imageUrl = UrlHelpers.getImageUrl(activity.thumb, "500", "280", "art");
        }
        viewHolder.vImage.setImageUrl(imageUrl, ApplicationController.getInstance().getImageLoader());


    }

    private String buildStreamString(Activity activity) {
        String stream = "";

        if(activity.media_type.equals("track")){
            if(activity.audio_decision.equals("direct play")){
                stream += "Direct Play";
            } else if (activity.audio_decision.equals("copy")){
                stream += "Direct Stream";
            } else {
                stream += "Transcoding";
                stream += " (" + activity.transcode_speed + ")";
                if(activity.throttled.equals("1")){
                    stream += " (Throttled)";
                }
            }
        } else {
            if(activity.audio_decision.equals("direct play") && activity.video_decision.equals("direct play")){
                stream += "Direct Play";
            } else if (activity.audio_decision.equals("copy") && activity.video_decision.equals("copy")){
                stream += "Direct Stream";
            } else {
                stream += "Transcoding";
                stream += " (" + activity.transcode_speed + ")";
                if(activity.throttled.equals("1")){
                    stream += " (Throttled)";
                }
            }
        }

        return stream;
    }

    private String buildVideoString(Activity activity) {
        String video = "";

        if(activity.video_decision.equals("direct play")){
            video += "Direct Play";
            video += " (" + activity.video_codec + ")";
            video += " (" + activity.width + "x" + activity.height + ")";
        } else if (activity.video_decision.equals("copy")){
            video += "Direct Stream";
            video += " (" + activity.transcode_video_codec + ")";
            video += " (" + activity.width + "x" + activity.height + ")";
        } else {
            video += "Transcoding";
            video += " (" + activity.transcode_video_codec + ")";
            video += " (" + activity.transcode_width + "x" + activity.transcode_height + ")";
        }

        return video;
    }

    private String buildAudioString(Activity activity) {
        String audio = "";

        if(activity.audio_decision.equals("direct play")){
            audio += "Direct Play";
            audio += " (" + activity.audio_codec + ")";
            audio += " (" + activity.audio_channels + " ch)";
        } else if (activity.audio_decision.equals("copy")){
            audio += "Direct Stream";
            audio += " (" + activity.transcode_audio_codec + ")";
            audio += " (" + activity.transcode_audio_channels + " ch)";
        } else {
            audio += "Transcoding";
            audio += " (" + activity.transcode_audio_codec + ")";
            audio += " (" + activity.transcode_audio_channels + " ch)";
        }

        return audio;
    }

    @Override
    public int getItemCount() {
        if(activities == null){
            return 0;
        }
        return activities.size();
    }
}
