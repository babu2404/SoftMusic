package com.android.softmusic;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ViewPager2 viewPager2;

    //give db reference here...
    DatabaseReference nref;

    TextView SongName,SongArtist;
    MediaPlayer mediaPlayer;

    // make new flag for play
    boolean play = true;
    ImageView Play,Pause,Prev,Next;

    //make new variable to store current song index
    Integer currentSongIndex=0;

    SeekBar seekBar;

    //for time pass and due time
    TextView Pass,Due;

    //we need handler to handle text on ui

    Handler handler;
    //we need two string to store text
    String out,out2;
    //need integer to store total time
    Integer totalTime;

    ImageView Heart,Repeat;

    // add one listener(Repeat)
    boolean RepeatSong = false;


    //create a new list to store image url that we will retrieve from db..

    ArrayList<String> imageurl = new ArrayList<>();
    ArrayList<String> songnames = new ArrayList<>();
    ArrayList<String> songartists = new ArrayList<>();
    ArrayList<String> songurls = new ArrayList<>();


    List<SliderItem> sliderItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager2 = findViewById(R.id.viewpagerimageslider);

        nref = FirebaseDatabase.getInstance().getReference();

        SongName = (TextView)findViewById(R.id.songname);
        SongArtist = (TextView)findViewById(R.id.songartist);

        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        Pass = (TextView)findViewById(R.id.tv_pass);
        Due = (TextView)findViewById(R.id.tv_due);

        handler = new Handler();

        Play = (ImageView) findViewById(R.id.play);
        Pause = (ImageView)findViewById(R.id.pause);
        Prev = (ImageView)findViewById(R.id.prev);
        Next = (ImageView)findViewById(R.id.next);

        Heart = (ImageView)findViewById(R.id.heart);
        Repeat = (ImageView)findViewById(R.id.repeat);

        // now i will make list of type slider items and then add images to this list and then finally we will set this list to our adapter.

        nref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //now iterator over snapshot child..
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    imageurl.add(ds.child("imageurl").getValue(String.class));

                    //now add all names and artist  in our list..
                    //add child in firebase of songname and song artist
                    songnames.add(ds.child("songname").getValue(String.class));
                    songartists.add(ds.child("songartist").getValue(String.class));

                    //add song url
                    songurls.add(ds.child("songurl").getValue(String.class));

                }

                for (int i = 0; i< imageurl.size(); i++) {
                    // now add urls in slideritems...
                    sliderItems.add(new SliderItem(imageurl.get(i)));
                }
                viewPager2.setAdapter(new SliderAdapter(sliderItems));

                // now set clip to padding and clip to children false...
                viewPager2.setClipToPadding(false);
                viewPager2.setClipChildren(false);

                //now set offscreen pages limit

                viewPager2.setOffscreenPageLimit(3);
                viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

                //now make composite page transformer to set page margin and page y scale.....

                CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
                compositePageTransformer.addTransformer(new MarginPageTransformer(40));
                compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
                    @Override
                    public void transformPage(@NonNull View page, float position) {
                        page.setScaleY(1);
                    }
                });
                viewPager2.setPageTransformer(compositePageTransformer);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                //make new function
                init(viewPager2.getCurrentItem());
                //store value of index here
                currentSongIndex = viewPager2.getCurrentItem();
            }
        });


        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //to play next song after click on next button  we have to increase song index value to play next song....
                currentSongIndex = currentSongIndex +1;
                //now we will set current item in view pager by this index value....i.e(next value)
                viewPager2.setCurrentItem(currentSongIndex);

                //finally call our limit function to play song and set all value...
                init(currentSongIndex);

            }
        });
        Prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here decrease index by 1
                currentSongIndex = currentSongIndex -1;
                viewPager2.setCurrentItem(currentSongIndex);
                init(currentSongIndex);
            }
        });

        Heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // now we will check like value...
                nref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String like = snapshot.child(String.valueOf(currentSongIndex+1)).child("like").getValue(String.class);
                        if(like.equals("0"))
                        {
                            //if on click over heart button if value will be zero then we set value 1 now to db and set image to imageview..
                            Heart.setImageResource(R.drawable.heart);
                            nref.child(String.valueOf(currentSongIndex+1)).child("like").setValue("1");
                        }
                        else {
                            //if value will be 1...then we will set value to 0..to unlike song
                            Heart.setImageResource(R.drawable.heart2);
                            nref.child(String.valueOf(currentSongIndex+1)).child("like").setValue("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        //similarly we will do for repeat button
        Repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String repeat = snapshot.child(String.valueOf(currentSongIndex + 1)).child("repeat").getValue(String.class);
                        if (repeat.equals("0")) {
                            Repeat.setImageResource(R.drawable.ic_baseline_repeat_24);
                            nref.child(String.valueOf(currentSongIndex + 1)).child("repeat").setValue("1");
                            RepeatSong = true;
                            RepeatSongs();
                        } else {
                            Repeat.setImageResource(R.drawable.repeat2);
                            nref.child(String.valueOf(currentSongIndex + 1)).child("repeat").setValue("0");
                            RepeatSong = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        //now set on seek change listener on our seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b)
                {
                    mediaPlayer.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /*
         sliderItems.add(new SliderItem(R.drawable.song1));
        sliderItems.add(new SliderItem(R.drawable.song2));
        sliderItems.add(new SliderItem(R.drawable.song3));
        sliderItems.add(new SliderItem(R.drawable.song4));
        sliderItems.add(new SliderItem(R.drawable.song5));
        */
    }

    private void init(int currentItem) {
        //we check here is mediaplayer will loaded with and other source then we will reset it first...otherwise two song will play at one time.
        try {
            if (mediaPlayer.isPlaying())
                mediaPlayer.reset();
        } catch (Exception e) { }
        //now after changing song we have to make pause button visible and play button invisible and also we need to set our flag true
        Pause.setVisibility(View.VISIBLE);
        Play.setVisibility(View.INVISIBLE);
        play = true;

        //new settext to our textview with the help of array list and currentitem value..
        SongName.setText(songnames.get(currentItem));
        SongArtist.setText(songartists.get(currentItem));

        nref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //now we will check condition here..
                String like = snapshot.child(String.valueOf(currentSongIndex+1)).child("like").getValue(String.class);
                String repeat = snapshot.child(String.valueOf(currentSongIndex+1)).child("repeat").getValue(String.class);

                if(like.equals("0"))
                {
                    Heart.setImageResource(R.drawable.heart2);
                }
                else
                {
                    Heart.setImageResource(R.drawable.heart);
                }
                if(repeat.equals("0"))
                {
                    Repeat.setImageResource(R.drawable.repeat2);
                    RepeatSong = false;
                }
                else {
                    Repeat.setImageResource(R.drawable.ic_baseline_repeat_24);
                    RepeatSong = true;

                    //now make separate function to check mediaplayer is complete
                    RepeatSongs();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(songurls.get(currentItem));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    //call initialize seek bar function here.....
                    initializeSeekbar();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void RepeatSongs() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(RepeatSong)
                {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void initializeSeekbar() {
        seekBar.setMax(mediaPlayer.getDuration()/1000);//set max limit of song
        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
        seekBar.setProgress(mCurrentPosition);

        MainActivity.this.runOnUiThread(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                if(mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);

                    out = String.format("%02d:%02d",seekBar.getProgress()/60,seekBar.getProgress()%60);

                    //now set this to pass text view

                    Pass.setText(out);
                }
                handler.postDelayed(this,1000);
            }

        });
        totalTime = mediaPlayer.getDuration()/1000;
        out2 = String.format("%02d:%02d",totalTime/60,totalTime%60);
        Due.setText(out2);
    }

    public void playpausebutton(View v)
    {
        //after click on play pause button we will check our flag first...if it will be true....then its mean we need to pause song
        if (play)
        {
            play = false;
            Pause.setVisibility(View.INVISIBLE);
            Play.setVisibility(View.VISIBLE);
            mediaPlayer.pause();

            //so if any song will we playing then its flag will be true...and hence we check if flag true then we will pause this song after click over button
        }
        else
        {
            //this is the condition when song will be paused
            play = true;
            Pause.setVisibility(View.VISIBLE);
            Play.setVisibility(View.INVISIBLE);
            mediaPlayer.start();
        }

    }
}