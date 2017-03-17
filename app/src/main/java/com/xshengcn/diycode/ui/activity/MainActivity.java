package com.xshengcn.diycode.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.xshengcn.diycode.R;
import com.xshengcn.diycode.model.user.UserDetail;
import com.xshengcn.diycode.ui.ActivityNavigator;
import com.xshengcn.diycode.ui.fragment.NewsFragment;
import com.xshengcn.diycode.ui.fragment.ProjectFragment;
import com.xshengcn.diycode.ui.fragment.TopicFragment;
import com.xshengcn.diycode.ui.iview.IMainView;
import com.xshengcn.diycode.ui.presenter.MainPresenter;
import com.xshengcn.diycode.util.glide.CircleTransform;
import com.xshengcn.diycode.widget.FabDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public class MainActivity extends BaseActivity
    implements IMainView, FabDialog.OnButtonClickListener {

  @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.appbar_layout) AppBarLayout appbarLayout;
  @BindView(R.id.nav_view) NavigationView navView;
  @BindView(R.id.tab_layout) TabLayout tabLayout;
  @BindView(R.id.view_pager) ViewPager viewPager;

  @BindView(R.id.header) @Nullable ImageView header;
  @BindView(R.id.name) @Nullable TextView name;
  @BindView(R.id.email) @Nullable TextView email;

  @Inject MainPresenter presenter;
  @Inject ActivityNavigator navigator;
  @BindView(R.id.fab_menu) FloatingActionButton fabMenu;
  @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;

  private boolean hasNotification;
  private FabDialog fabDialog;

  private CompositeDisposable disposable = new CompositeDisposable();

  private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.SimpleDrawerListener() {
    @Override public void onDrawerStateChanged(int newState) {
      super.onDrawerStateChanged(newState);
      if (newState == DrawerLayout.STATE_DRAGGING) disposable.clear();
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getComponent().inject(this);
    ButterKnife.bind(this);
    setupActionBar();

    viewPager.setOffscreenPageLimit(2);
    viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), this));
    tabLayout.setTabMode(TabLayout.MODE_FIXED);
    tabLayout.setupWithViewPager(viewPager);

    View headerView = navView.getHeaderView(0);
    header = (ImageView) headerView.findViewById(R.id.header);
    header.setOnClickListener(this::clickNavHeader);
    name = (TextView) headerView.findViewById(R.id.name);
    email = (TextView) headerView.findViewById(R.id.email);

    navView.setNavigationItemSelectedListener(menuItem -> onNavigationItemSelected(menuItem));

    fabMenu.setOnClickListener(this::clickFabMenu);
    drawerLayout.addDrawerListener(drawerListener);

    presenter.onAttach(this);
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  private void clickNavHeader(View view) {
    drawerLayout.closeDrawers();
    disposable.clear();
    disposable.add(Observable.just("")
        .subscribeOn(Schedulers.single())
        .delay(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> navigator.showUser()));
  }

  private void clickFabMenu(View view) {
    if (fabDialog == null) {
      fabDialog = new FabDialog(this);
      fabDialog.setOnButtonClickListener(this);
    }
    fabDialog.show();
  }

  @Override protected void onPause() {
    super.onPause();
    if (fabDialog != null) {
      fabDialog.dismiss();
    }
  }

  @Override protected void onDestroy() {
    presenter.onDetach();
    disposable.clear();
    drawerLayout.removeDrawerListener(drawerListener);
    super.onDestroy();
  }

  private boolean onNavigationItemSelected(MenuItem menuItem) {
    drawerLayout.closeDrawers();
    disposable.clear();
    disposable.add(Observable.just(menuItem.getItemId())
        .subscribeOn(Schedulers.single())
        .delay(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::clickNavMenu));
    return true;
  }

  private void clickNavMenu(int id) {
    switch (id) {
      case R.id.nav_topic:
        navigator.showUserTopics();
        break;
      case R.id.nav_favorite:
        navigator.showUserFavorites();
        break;
      case R.id.nav_reply:
        navigator.showUserReplies();
        break;
      case R.id.nav_site:
        navigator.showSite();
        break;
    }
  }

  private void setupActionBar() {
    toolbar.setTitle("");
    toolbar.setLogo(R.drawable.ic_logo);
    setSupportActionBar(toolbar);
    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    if (hasNotification) {
      menu.findItem(R.id.action_notification).setIcon(R.drawable.ic_menu_notification_red);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_search:
        navigator.showSearch();
        break;
      case R.id.action_notification:
        navigator.showNotification();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START);
    } else if (false) {

    } else {
      super.onBackPressed();
    }
  }

  @Override public void setupNavigationView(UserDetail user) {
    Glide.with(this)
        .load(user.avatarUrl.replace("large_avatar", "avatar"))
        .transform(new CircleTransform(this))
        .into(header);
    name.setText(user.name);
    email.setText(user.email);
  }

  @Override public void showNotificationMenuBadge(Boolean showBadge) {
    hasNotification = showBadge;
    invalidateOptionsMenu();
  }

  @Override public void clickNewsButton() {

  }

  @Override public void clickTopicButton() {
    navigator.showCreateTopic();
  }

  public class MainPagerAdapter extends FragmentPagerAdapter {

    private final String[] titles;
    private final List<Fragment> fragments;

    public MainPagerAdapter(FragmentManager fm, Context context) {
      super(fm);
      titles = context.getResources().getStringArray(R.array.main_tabs);
      fragments = new ArrayList<>();
      fragments.add(TopicFragment.newInstance(null));
      fragments.add(NewsFragment.newInstance());
      fragments.add(ProjectFragment.newInstance());
    }

    @Override public Fragment getItem(int position) {
      return fragments.get(position);
    }


    @Override public int getCount() {
      return titles.length;
    }

    @Override public CharSequence getPageTitle(int position) {
      return titles[position];
    }
  }
}