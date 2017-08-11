package com.ilaquidain.beams;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

import io.github.kexanie.library.MathView;

import static android.content.ContentValues.TAG;

/**
 * Created by ilaquidain on 20/03/2017.
 */

public class WebViewDialogFragment extends DialogFragment {
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
        View v = inflater.inflate(R.layout.webview_dialogfragment,container,false);
        MathView mahtview = (MathView)v.findViewById(R.id.MathView);
        String scenario = getArguments().getString("scenario");
        int position = getArguments().getInt("position");
        if(scenario!=null){
            try {
                Field field;
                Class<R.array> res = R.array.class;
                field = res.getField(scenario+"_4");
                TypedArray ta1 = getResources().obtainTypedArray(field.getInt(null));
                equation = ta1.getString(position);
                mahtview.setText(equation);
                ta1.recycle();
            }catch (Exception e){
                Log.e(TAG, "Error loading equation to webview");
            }
        }
        return v;
    }
}
