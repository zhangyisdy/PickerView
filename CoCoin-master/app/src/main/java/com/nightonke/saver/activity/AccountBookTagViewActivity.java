package com.nightonke.saver.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nightonke.saver.adapter.DrawerTagChooseGridViewAdapter;
import com.nightonke.saver.model.Logo;
import com.nightonke.saver.model.SettingManager;
import com.nightonke.saver.model.User;
import com.nightonke.saver.ui.MyGridView;
import com.nightonke.saver.R;
import com.nightonke.saver.model.RecordManager;
import com.nightonke.saver.adapter.TagViewFragmentAdapter;
import com.nightonke.saver.util.CoCoinUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class AccountBookTagViewActivity extends AppCompatActivity {

    private MaterialViewPager mViewPager;

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    private TagViewFragmentAdapter tagModeAdapter = null;

    private Context mContext;

    private MyGridView myGridView;
    private DrawerTagChooseGridViewAdapter drawerTagChooseAdapter;

    private TextView userName;
    private TextView userEmail;

    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_account_book_tag_view);

        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);

        View view = mViewPager.getRootView();
        TextView title = (TextView)view.findViewById(R.id.logo_white);
        title.setTypeface(CoCoinUtil.typefaceLatoLight);
        title.setText(SettingManager.getInstance().getAccountBookName());

        mViewPager.getPagerTitleStrip().setTypeface(CoCoinUtil.typefaceLatoLight, Typeface.NORMAL);

        setTitle("");

        toolbar = mViewPager.getToolbar();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        userName = (TextView)findViewById(R.id.user_name);
        userEmail = (TextView)findViewById(R.id.user_email);
        userName.setTypeface(CoCoinUtil.typefaceLatoRegular);
        userEmail.setTypeface(CoCoinUtil.typefaceLatoLight);

        User user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User.class);
        if (user != null) {
            userName.setText(user.getUsername());
            userEmail.setText(user.getEmail());
        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(true);
            }
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, 0, 0);
        mDrawer.setDrawerListener(mDrawerToggle);

        View logo = findViewById(R.id.logo_white);
        if (logo != null) {
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.notifyHeaderChanged();
                }
            });
        }

        tagModeAdapter = new TagViewFragmentAdapter(getSupportFragmentManager());
        mViewPager.getViewPager().setOffscreenPageLimit(tagModeAdapter.getCount());
        mViewPager.getViewPager().setAdapter(tagModeAdapter);
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

        mViewPager.clearAnimation();
        if (SettingManager.getInstance().getShowPicture()) {
            mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
                @Override
                public HeaderDesign getHeaderDesign(int page) {
                    return HeaderDesign.fromColorAndDrawable(
                            CoCoinUtil.GetTagColor(RecordManager.TAGS.get(page).getId()),
                            CoCoinUtil.GetTagDrawable(RecordManager.TAGS.get(page).getId()));
                }
            });
        } else {
            mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
                @Override
                public HeaderDesign getHeaderDesign(int page) {
                    return HeaderDesign.fromColorAndDrawable(
                            CoCoinUtil.GetTagColor(RecordManager.TAGS.get(page).getId()),
                            CoCoinUtil.GetTagDrawable(-3));
                }
            });
        }

        myGridView = (MyGridView)mDrawer.findViewById(R.id.grid_view);
        drawerTagChooseAdapter = new DrawerTagChooseGridViewAdapter(mContext);
        myGridView.setAdapter(drawerTagChooseAdapter);

        myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                YoYo.with(Techniques.Bounce).delay(0).duration(700).playOn(view);
                mViewPager.getViewPager().setCurrentItem(position);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawer.closeDrawers();
                    }
                }, 700);
            }
        });

        profileImage= (CircleImageView)mDrawer.findViewById(R.id.profile_image);

        loadLogo();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MaterialViewPagerHelper.unregister(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    private void loadLogo() {
        User user = BmobUser.getCurrentUser(CoCoinApplication.getAppContext(), User.class);
        if (user != null) {
            try {
                File logoFile = new File(CoCoinApplication.getAppContext().getFilesDir() + CoCoinUtil.LOGO_NAME);
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(logoFile));
                if (b == null) {
                    // the local logo file is missed
                    // try to get from the server
                    BmobQuery<Logo> bmobQuery = new BmobQuery();
                    bmobQuery.addWhereEqualTo("objectId", user.getLogoObjectId());
                    bmobQuery.findObjects(CoCoinApplication.getAppContext()
                            , new FindListener<Logo>() {
                        @Override
                        public void onSuccess(List<Logo> object) {
                            // there has been an old logo in the server/////////////////////////////////////////////////////////
                            Log.d("Saver", "There is an old logo");
                            String url = object.get(0).getFile().getUrl();
                            Ion.with(CoCoinApplication.getAppContext()).load(url)
                                    .write(new File(CoCoinApplication.getAppContext().getFilesDir()
                                            + CoCoinUtil.LOGO_NAME))
                                    .setCallback(new FutureCallback<File>() {
                                        @Override
                                        public void onCompleted(Exception e, File file) {
                                            profileImage.setImageBitmap(BitmapFactory.decodeFile(
                                                    CoCoinApplication.getAppContext().getFilesDir()
                                                            + CoCoinUtil.LOGO_NAME));
                                        }
                                    });
                        }
                        @Override
                        public void onError(int code, String msg) {
                            // the picture is lost
                            Log.d("Saver", "Can't find the old logo in server.");
                        }
                    });
                } else {
                    // the user logo is in the storage
                    profileImage.setImageBitmap(b);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            // use the default logo
            profileImage.setImageResource(R.drawable.default_user_logo);
        }
    }

}
