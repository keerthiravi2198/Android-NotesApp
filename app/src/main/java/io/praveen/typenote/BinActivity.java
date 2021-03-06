package io.praveen.typenote;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.List;

import io.praveen.typenote.SQLite.BinDatabaseHandler;
import io.praveen.typenote.SQLite.ClickListener;
import io.praveen.typenote.SQLite.DatabaseHandler;
import io.praveen.typenote.SQLite.Note;
import io.praveen.typenote.SQLite.NoteAdapter;
import io.praveen.typenote.SQLite.RecyclerTouchListener;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BinActivity extends AppCompatActivity {

    CoordinatorLayout sv;
    NoteAdapter mAdapter;
    InterstitialAd interstitialAd;
    List<Note> l;
    Intent i;

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin);
        interstitialAd = new InterstitialAd(BinActivity.this);
        interstitialAd.setAdUnitId("ca-app-pub-6275597090094912/5536611682");
        interstitialAd.loadAd(new AdRequest.Builder().build());
        i = new Intent(BinActivity.this, MainActivity.class);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/whitney.ttf").setFontAttrId(R.attr.fontPath).build());
        Typeface font2 = Typeface.createFromAsset(getAssets(), "fonts/whitney.ttf");
        SpannableStringBuilder SS = new SpannableStringBuilder("Bin");
        SS.setSpan(new CustomTypefaceSpan("", font2), 0, SS.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(SS);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sv = findViewById(R.id.binView);
        final BinDatabaseHandler db = new BinDatabaseHandler(this);
        l = db.getAllNotes();
        final RecyclerView recyclerView = findViewById(R.id.binRecyclerView);
        final RelativeLayout rl = findViewById(R.id.binPlaceholder);
        if (l.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            rl.setVisibility(View.VISIBLE);
        }
        mAdapter = new NoteAdapter(l);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {

            @Override
            public void onClick(View view, final int position) {
                final Note note = l.get(position);
                final String mNote = note.getNote();
                final int mStar = note.getStar();
                final String mDate = note.getDate();
                new MaterialStyledDialog.Builder(BinActivity.this).setIcon(R.drawable.ic_settings_backup_restore)
                        .setDescription("You may choose to restore your note or delete it permanently!")
                        .setPositiveText("DELETE")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                BinDatabaseHandler db = new BinDatabaseHandler(BinActivity.this);
                                List<Note> l2 = db.getAllNotes();
                                final Note note2 = l2.get(position);
                                db.deleteNote(note2);
                                if(interstitialAd.isLoaded()) {
                                    interstitialAd.show();
                                    interstitialAd.setAdListener(new AdListener(){
                                        @Override
                                        public void onAdClosed() {
                                            startActivity(i);
                                            finish();
                                        }
                                    });
                                } else{
                                    startActivity(i);
                                    finish();
                                }
                            }
                        })
                        .setNegativeText("RESTORE")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                BinDatabaseHandler db = new BinDatabaseHandler(BinActivity.this);
                                DatabaseHandler db2 = new DatabaseHandler(BinActivity.this);
                                db2.addNote(new Note(mNote, mDate, mStar));
                                List<Note> l2 = db.getAllNotes();
                                final Note note2 = l2.get(position);
                                db.deleteNote(note2);
                                i.putExtra("note", true);
                                i.putExtra("restore", true);
                                if(interstitialAd.isLoaded()) {
                                    interstitialAd.show();
                                    interstitialAd.setAdListener(new AdListener(){
                                        @Override
                                        public void onAdClosed() {
                                            startActivity(i);
                                            finish();
                                        }
                                    });
                                } else{
                                    startActivity(i);
                                    finish();
                                }
                            }
                        })
                        .setNeutralText("DISMISS")
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {}
                        })
                        .setHeaderColor(R.color.colorPrimary)
                        .withIconAnimation(false)
                        .withDivider(true)
                        .show();
            }

        }));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        startActivity(i);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(i);
        finish();
        return true;
    }
}
