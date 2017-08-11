package com.ilaquidain.beams;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ilaquidain on 06/04/2017.
 */

public class PruebaDialogFragment extends DialogFragment {
        String equation;

        @Override
        public void onStart()
        {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null)
            {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(width, height);
            }
        }
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.webview_dialogfragment_prubea,container,false);
            return v;
        }
    }

