package com.ilaquidain.beams;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private TypedArray typedArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Open List with beam types when starting app
        FragmentManager fm = getFragmentManager();
        Fragment fgmt = new BeamListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Key1","A");
        fgmt.setArguments(bundle);
        fm.beginTransaction()
                .add(R.id.main_frame,fgmt)
                .commit();
    }

}

