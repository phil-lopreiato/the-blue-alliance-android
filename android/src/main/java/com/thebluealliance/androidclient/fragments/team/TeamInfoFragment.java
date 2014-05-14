package com.thebluealliance.androidclient.fragments.team;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.thebluealliance.androidclient.R;
import com.thebluealliance.androidclient.activities.ViewTeamActivity;
import com.thebluealliance.androidclient.background.PopulateTeamInfo;
import com.thebluealliance.androidclient.interfaces.RefreshableActivityListener;

import java.util.List;

public class TeamInfoFragment extends Fragment implements View.OnClickListener, RefreshableActivityListener {

    private String mTeamKey;

    private PopulateTeamInfo task;

    public TeamInfoFragment() {
        // Empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTeamKey = getArguments().getString(ViewTeamActivity.TEAM_KEY);
        if (mTeamKey == null) {
            throw new IllegalArgumentException("TeamInfoFragment must be created with a team key!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_team_info, container, false);
        // Register this fragment as the callback for all clickable views
        v.findViewById(R.id.team_location_container).setOnClickListener(this);
        v.findViewById(R.id.team_twitter_button).setOnClickListener(this);
        v.findViewById(R.id.team_cd_button).setOnClickListener(this);
        v.findViewById(R.id.team_youtube_button).setOnClickListener(this);
        v.findViewById(R.id.team_website_button).setOnClickListener(this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        task = new PopulateTeamInfo(this);
        task.execute(mTeamKey);
    }

    @Override
    public void onClick(View view) {
        PackageManager manager = getActivity().getPackageManager();
        if (view.getTag() != null) {

            String uri = view.getTag().toString();
            Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
            List<ResolveInfo> handlers = manager.queryIntentActivities(i, 0);
            if (handlers.size() > 0) {
                // There is an application to handle this intent intent
                startActivity(i);
            } else {
                // No application can handle this intent
                Toast.makeText(getActivity(), "No app can handle that request", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onRefreshStart() {
        task = new PopulateTeamInfo(this);
        task.execute(mTeamKey);
    }

    @Override
    public void onRefreshStop() {
        task.cancel(false);
    }
}
