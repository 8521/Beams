package com.ilaquidain.beams;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import java.lang.reflect.Field;
import static android.content.ContentValues.TAG;

/**
 * Created by ilaquidain on 24/02/2017.
 */

public class BeamListFragment extends Fragment {
    TypedArray BeamTypeImages;
    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;
    String key;
    FragmentManager fm;
    Bundle bundle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.beam_type_list,container,false);

        Button btn1 = (Button)v.findViewById(R.id.boton_prueba);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment dgft = new PruebaDialogFragment();
                dgft.show(ft,"Tag1");
            }
        });

        //Retrieve if is beam_Type or Beam_Load
        key = getArguments().getString("Key1");
        if(key!=null&&!key.equals("A")){
            try{
                Field field;
                Class<R.array> res = R.array.class;
                field = res.getField(key+"_Images");
                BeamTypeImages = getResources().obtainTypedArray(field.getInt(null));
            }catch (Exception e){
                Log.e(TAG, "Error al cargar imagenes");
            }
        }else {
            BeamTypeImages = getResources().obtainTypedArray(R.array.beam_type_images);
        }

        //Set Recycler View
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new Adapter1();
        recyclerView.setAdapter(mAdapter);


        return v;
    }

    private class Adapter1 extends RecyclerView.Adapter<ViewHolder1>{
        private Adapter1() {
            super();
        }

        @Override
        public ViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {
            View v2 = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.beam_type_item,parent,false);
            return new ViewHolder1(v2);
        }

        @Override
        public void onBindViewHolder(ViewHolder1 holder, int position) {
            holder.imageView.setImageDrawable(BeamTypeImages.getDrawable(position));
        }

        @Override
        public int getItemCount() {
            return BeamTypeImages.length();
        }
    }

    private class ViewHolder1 extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;

        private ViewHolder1(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image_beam);
            fm = getFragmentManager();
            bundle = new Bundle();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(key.equals("A")){ //quiere decir que estamos en el primer menu
                Fragment fgmt = new BeamListFragment(); //cargamos de nuevo el primer menu
                bundle.putString("Key1","B_"+String.valueOf(getAdapterPosition())); //Al segundo menu pasamos B_0
                fgmt.setArguments(bundle);
                fm.beginTransaction()
                        .replace(R.id.main_frame,fgmt)
                        .addToBackStack(null)
                        .commit();
            }else{
                Fragment fgmt = new CalculationResultsFragment();
                bundle.putString("Key2",key+"_"+String.valueOf(getAdapterPosition()));//Pasamos valor de viga y load B_0_0
                bundle.putString("Key3",key+"_Images");//B_0_Images
                bundle.putInt("Key4",getAdapterPosition());
                fgmt.setArguments(bundle);
                fm.beginTransaction()
                        .replace(R.id.main_frame,fgmt)
                        .addToBackStack(null)
                        .commit();
            }
        }


    }
}
