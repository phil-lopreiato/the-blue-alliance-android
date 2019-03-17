package com.thebluealliance.androidclient.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.thebluealliance.androidclient.NfcUris;
import com.thebluealliance.androidclient.R;
import com.thebluealliance.androidclient.ShareUris;
import com.thebluealliance.androidclient.TBAAndroid;
import com.thebluealliance.androidclient.TbaLogger;
import com.thebluealliance.androidclient.Utilities;
import com.thebluealliance.androidclient.adapters.ViewEventFragmentPagerAdapter;
import com.thebluealliance.androidclient.di.components.DaggerFragmentComponent;
import com.thebluealliance.androidclient.di.components.FragmentComponent;
import com.thebluealliance.androidclient.di.components.HasFragmentComponent;
import com.thebluealliance.androidclient.eventbus.ActionBarTitleEvent;
import com.thebluealliance.androidclient.helpers.ConnectionDetector;
import com.thebluealliance.androidclient.helpers.EventHelper;
import com.thebluealliance.androidclient.listeners.ClickListenerModule;
import com.thebluealliance.androidclient.models.ApiStatus;
import com.thebluealliance.androidclient.subscribers.SubscriberModule;
import com.thebluealliance.androidclient.types.ModelType;
import com.thebluealliance.androidclient.views.SlidingTabs;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ViewEventActivity extends MyTBASettingsActivity
        implements ViewPager.OnPageChangeListener, HasFragmentComponent {

    public static final String EVENTKEY = "eventKey";
    public static final String TAB = "tab";

    private String mEventKey;
    private int mSelectedTab;
    private ViewPager pager;
    private ViewEventFragmentPagerAdapter adapter;
    private boolean isDistrict;
    private CastContext mCastContext;
    private FragmentComponent mComponent;

    /**
     * Will be run in {@code onResume()}; used to perform UI setup that can't happen before the
     * activity is resumed
     */
    private Runnable mOnNewIntentRunnable;

    /**
     * Create new intent for ViewEventActivity
     *
     * @param c        context
     * @param eventKey Key of the event to show
     * @param tab      The tab number from ViewEventFragmentPagerAdapter.
     * @return Intent you can launch
     */
    public static Intent newInstance(Context c, String eventKey, int tab) {
        Intent intent = new Intent(c, ViewEventActivity.class);
        intent.putExtra(EVENTKEY, eventKey);
        intent.putExtra(TAB, tab);
        return intent;
    }

    public static Intent newInstance(Context c, String eventKey) {
        return newInstance(c, eventKey, ViewEventFragmentPagerAdapter.TAB_INFO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras();

        mEventKey = extras.getString(EVENTKEY, "");
        if (!EventHelper.validateEventKey(mEventKey)) {
            throw new IllegalArgumentException("ViewEventActivity must be given a valid event key");
        }

        mSelectedTab = extras.getInt(TAB, ViewEventFragmentPagerAdapter.TAB_INFO);

        setModelKey(mEventKey, ModelType.EVENT);
        setShareEnabled(true);
        setContentView(R.layout.activity_view_event);

        pager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new ViewEventFragmentPagerAdapter(getSupportFragmentManager(), mEventKey);
        pager.setAdapter(adapter);
        // To support refreshing, all pages must be held in memory at once
        // This should be increased if we ever add more pages
        pager.setOffscreenPageLimit(10);
        pager.setPageMargin(Utilities.getPixelsFromDp(this, 16));

        SlidingTabs tabs = (SlidingTabs) findViewById(R.id.tabs);
        tabs.setOnPageChangeListener(this);
        tabs.setViewPager(pager);
        ViewCompat.setElevation(tabs, getResources().getDimension(R.dimen.toolbar_elevation));

        pager.setCurrentItem(mSelectedTab);  // Do this after we set onPageChangeListener, so that FAB gets hidden, if needed

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupActionBar();

        if (!ConnectionDetector.isConnectedToInternet(this)) {
            showWarningMessage(BaseActivity.WARNING_OFFLINE);
        }

        isDistrict = true;

        setSettingsToolbarTitle("Event Settings");
        mCastContext = CastContext.getSharedInstance(this);
        setupCastListener();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        TbaLogger.d("onNewIntent called");
        setIntent(intent);
        String newEventKey;

        Bundle extras = intent.getExtras() == null ? new Bundle() : intent.getExtras();

        newEventKey = extras.getString(EVENTKEY, "");
        if (!EventHelper.validateEventKey(newEventKey)) {
            throw new IllegalArgumentException("ViewEventActivity must be constructed with a key");
        }

        mSelectedTab = extras.getInt(TAB, ViewEventFragmentPagerAdapter.TAB_INFO);

        if (mEventKey != null && newEventKey.equals(mEventKey)) {
            // The event keys are the same; don't recreate anything
            return;
        } else {
            mEventKey = newEventKey;
        }
        setModelKey(mEventKey, ModelType.EVENT);

        mOnNewIntentRunnable = () -> {
            // If the settings panel was open before, close it
            closeSettingsPanel(false);

            // Reset the title; this will be set from the EventInfoFragment
            setActionBarTitle("");

            adapter.removeAllFragments();
            adapter = new ViewEventFragmentPagerAdapter(getSupportFragmentManager(), mEventKey);
            pager.setAdapter(adapter);
            pager.setCurrentItem(mSelectedTab);
        };

        TbaLogger.d("Got new ViewEvent intent with key: " + mEventKey);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBeamUri(String.format(NfcUris.URI_EVENT, mEventKey));
        setShareUri(String.format(ShareUris.URI_EVENT, mEventKey));

        if (mOnNewIntentRunnable != null) {
            mOnNewIntentRunnable.run();
            mOnNewIntentRunnable = null;
        }

        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);
    }

    @Override
    public void onCreateNavigationDrawer() {
        useActionBarToggle(false);
        encourageLearning(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }

    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;

    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {}

            @Override
            public void onSessionEnding(CastSession session) {}

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {}

            @Override
            public void onSessionSuspended(CastSession session, int reason) {}

            private void onApplicationConnected(CastSession castSession) {
                TbaLogger.i("Cast application connected");
                mCastSession = castSession;
                supportInvalidateOptionsMenu();
                loadRemoteMedia();
            }

            private void onApplicationDisconnected() {
                supportInvalidateOptionsMenu();
            }
        };
    }


    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, "nefirst_red");
        movieMetadata.putString(MediaMetadata.KEY_TITLE, "Webcast");

        return new MediaInfo.Builder("https://video-weaver.fra02.hls.ttvnw.net/v1/playlist/CsYDXJiuYA-Iyi4XmjoFA9bycV2AYOmq3I8hjsulTuyClh9k1COnGDw6XYEL_Z7FU94ZTamMkwXIrCwipjO0tihuVxlmjsz9K9taQyycvhheMzM_LfGHGZ_ZThGiIkgPRnRSO0_aEGZwBPoicwEMVw9vk6YdsvQGHwNQEmSA-w6ax-HRO2mhlFCqW7x3TF3756zaV8aYse4yVClVsS7Q99KKzLXb7ciEvONDeMVb5fAi0ZcsV2VsAH7fcYY3Hj32J9Cy3-Q2EVc1d0h4_0FXYbDNtBMWNal7vH0QHa7hhJiLCspLMnqbf8yLaDq4Q_gzwFf6m4exqp69r1jl-hxpdYc79WXm1kaqb9NNQymcq4hyEPjYh4WzbVWGP3JUoX7pTfCWGzGAtoWOzCDozM_GBbIDn8kTvDeTj8i_wNsu3XyquH0zdHBhsP9UZIeTOrxVt3X8DwvhnPRYgVl_pzXOassWvX39Fsi-9JFg7hSDErPCDN9JZKX7f4edQ6PPR_g9tMgUZik73osxXNjjixK9WkOqKEd5ha5qKvdP4ARsFxrCMwzwUiCtvYjX1Em4ZJDwg21g5KtPnr9uHH1mp4r6_QcciBD6w82O7BIQ0C2M8xZ43jjzm7-Hk9pCRBoMh0EzBinWMniwm28a.m3u8")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(movieMetadata)
                .build();
    }

    private void loadRemoteMedia() {
        if (mCastSession == null) {
            TbaLogger.w("Can't load remote media - no session found");
            return;
        }
        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            TbaLogger.w("Can't load remote media - no client found");
            return;
        }
        TbaLogger.i("Loading remote media");
        remoteMediaClient.load(buildMediaInfo(),
                new MediaLoadOptions.Builder()
                        .setAutoplay(true).build());
    }

    private void setupActionBar() {
        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // The title is empty now; the EventInfoFragment will set the appropriate title
        // once it is loaded.
        setActionBarTitle("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (isDrawerOpen()) {
                    closeDrawer();
                    return true;
                }
                // If this tasks exists in the back stack, it will be brought to the front and all other activities
                // will be destroyed. HomeActivity will be delivered this intent via onNewIntent().
                startActivity(HomeActivity.newInstance(this, R.id.nav_item_events).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            case R.id.stats_help:
                Utilities.showHelpDialog(this, R.raw.stats_help, getString(R.string.stats_help_title));
                return true;
            case R.id.points_help:
                Utilities.showHelpDialog(this, R.raw.district_points_help, getString(R.string.district_points_help));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ViewPager getPager() {
        return pager;
    }

    public void scrollToTab(int tab) {
        if (pager != null) {
            pager.setCurrentItem(tab);
        }
    }

    @Override
    protected void onTbaStatusUpdate(ApiStatus newStatus) {
        super.onTbaStatusUpdate(newStatus);
        if (newStatus.getDownEvents().contains(mEventKey)) {
            // This event is down
            showWarningMessage(BaseActivity.WARNING_EVENT_DOWN);
        } else {
            // This event is not down! Hide the message if it was previously displayed
            dismissWarningMessage(BaseActivity.WARNING_EVENT_DOWN);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mSelectedTab = position;

        // hide the FAB if we aren't on the first page
        if (position != ViewEventFragmentPagerAdapter.TAB_INFO) {
            hideFab(true);
        } else {
            syncFabVisibilityWithMyTbaEnabled(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionBarTitleUpdated(ActionBarTitleEvent event) {
        setActionBarTitle(event.getTitle());
        setActionBarSubtitle(event.getSubtitle());
    }

    public FragmentComponent getComponent() {
        if (mComponent == null) {
            TBAAndroid application = ((TBAAndroid) getApplication());
            mComponent = DaggerFragmentComponent.builder()
                    .applicationComponent(application.getComponent())
                    .datafeedModule(application.getDatafeedModule())
                    .binderModule(application.getBinderModule())
                    .databaseWriterModule(application.getDatabaseWriterModule())
                    .gceModule(application.getGceModule())
                    .authModule(application.getAuthModule())
                    .subscriberModule(new SubscriberModule(this))
                    .clickListenerModule(new ClickListenerModule(this))
                    .build();
        }
        return mComponent;
    }

    @Override
    public void inject() {
        getComponent().inject(this);
    }
}