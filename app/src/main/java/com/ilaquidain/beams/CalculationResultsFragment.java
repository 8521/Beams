package com.ilaquidain.beams;

//Imported packages
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;



public class CalculationResultsFragment extends Fragment implements View.OnClickListener {
    Double RA;
    int inputsize;                                  //number of input variables (length and udl)
    ArrayList<Double> InVal = new ArrayList<>();    //value of the input variables
    String scenario;
    Double moment;
    Double shear;
    Double deflection;
    Double multipliermoment;
    Double multipliershear;
    Double multiplierdeflection;
    GraphView graphmoment;
    GraphView graphshear;
    GraphView graphdeflection;
    ArrayList<Double> MomentPoint = new ArrayList<>(); //puntos de la grafica de momento en SI
    ArrayList<Double> ShearPoint = new ArrayList<>();//puntos de la grafica de cortante en SI
    ArrayList<Double> DeflectionPoint = new ArrayList<>();
    ArrayList<Double> XValues = new ArrayList<>();     //puntos del eje de abcisas
    ArrayList<Double> MomentPointShown = new ArrayList<>(); //puntos de la grafica de moment en la unidad seleccionada
    ArrayList<Double> ShearPointShown = new ArrayList<>();  //puntos de la grafica de cortante en la unidad seleccionada
    ArrayList<Double> DeflectionPointShown = new ArrayList<>();
    ArrayList<String> MomentUnits = new ArrayList<>();      //distintas unidades disponibles para la grafica de momento
    ArrayList<String> ShearUnits = new ArrayList<>();       //distintas unidades disponibles para la grafica de cortante
    ArrayList<String> DeflectionUnits = new ArrayList<>();
    ArrayList<ReactionObject> ROList = new ArrayList<>(); //Matriz que contiene las reacciones
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series1;
    LineGraphSeries<DataPoint> series2;
    int[] unitselected = new int[3]; // unit selected for each graph
    TextView unitmoment;
    TextView unitshear;
    TextView unitdeflection;

    RecyclerView recylcerview;
    RecyclerView.Adapter mAdapter3;

    Boolean partialinput = true;
    Boolean totalinput = true;

    ArrayList<Integer> ROUnitSelec = new ArrayList<>();
    private final static String TAG = "ERROR Typedarray";
    private final static int pts = 20;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.beam_type_calculation_results,container,false);
        scenario = getArguments().getString("Key2");
        Log.i(TAG, scenario);
        cargarimagen(v);
        createrecyclerviewitems(scenario); //metemos en el recyclerview y las reacciones
        recylcerview = (RecyclerView)v.findViewById(R.id.recyclerview_results);
        recylcerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter3 = new Adapter3();
        recylcerview.setAdapter(mAdapter3);

        try{
            Field field;
            Class<R.array> res = R.array.class;
            field = res.getField("moment");
            TypedArray ta1 = getResources().obtainTypedArray(field.getInt(null));
            for(int i=0;i<ta1.length();i++){MomentUnits.add(ta1.getString(i));}
            field = res.getField("pointload");
            ta1 = getResources().obtainTypedArray(field.getInt(null));
            for(int i =0;i<ta1.length();i++){ShearUnits.add(ta1.getString(i));}
            field = res.getField("length");
            ta1 = getResources().obtainTypedArray(field.getInt(null));
            for(int i=0;i<ta1.length();i++){DeflectionUnits.add(ta1.getString(i));}
            ta1.recycle();
        }catch (Exception e){
            Log.e(TAG, "Moment/Shear/Deflection-Units");
        }

        //Create Moment Diagragm
        graphmoment = (GraphView) v.findViewById(R.id.graph_moment);
        graphshear = (GraphView) v.findViewById(R.id.graph_shear);
        graphdeflection = (GraphView) v.findViewById(R.id.graph_deflection);
        series = new LineGraphSeries<>();
        series1 = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();

        //Units of Graphs
        unitmoment = (TextView)v.findViewById(R.id.momentdiagram_unit);
        unitshear = (TextView)v.findViewById(R.id.sheardiagram_unit);
        unitdeflection = (TextView)v.findViewById(R.id.defleciondiagram_unit);
        for(int i =0;i<3;i++){unitselected[i]=0;}
        unitmoment.setText(MomentUnits.get(unitselected[0]));
        unitshear.setText(ShearUnits.get(unitselected[1]));
        unitdeflection.setText(DeflectionUnits.get(unitselected[2]));
        unitmoment.setOnClickListener(this);
        unitshear.setOnClickListener(this);
        unitdeflection.setOnClickListener(this);

        return v;
    }

    private void cargarimagen(View v) {
        TypedArray typedarrayimagenesreacciones;
        ImageView image1 = (ImageView)v.findViewById(R.id.reactions_graph);
        try{
            String scenario2 = getArguments().getString("Key3");
            int position = getArguments().getInt("Key4");
            Field field;
            Class<R.array> res = R.array.class;
            field = res.getField(scenario2);
            typedarrayimagenesreacciones = getResources().obtainTypedArray(field.getInt(null));
            image1.setImageDrawable(typedarrayimagenesreacciones.getDrawable(position));
                        typedarrayimagenesreacciones.recycle();
        }catch (Exception e){
            Log.e("Cargar Imagen Reaccion", "cargarimagen: error al cargar imagen de reacciones");
        }

    }

    //Este metodo crea el grafico
    private void populategraph() {
        series.resetData(generateData1());
        series1.resetData(generateData2());
        graphmoment.getViewport().setXAxisBoundsManual(true);
        graphmoment.getViewport().setMinX(0.00);
        graphmoment.getViewport().setMaxX(XValues.get(pts));
        graphshear.getViewport().setXAxisBoundsManual(true);
        graphshear.getViewport().setMinX(0.00);
        graphshear.getViewport().setMaxX(XValues.get(pts));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        nf.setMinimumIntegerDigits(1);
        graphmoment.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
        graphshear.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
        graphmoment.addSeries(series);
        graphshear.addSeries(series1);
    }

    //Estos dos generan la lista de puntos para la grafica
    private DataPoint[] generateData1() {
        DataPoint[] pointsmoment = new DataPoint[pts+1];
        for(int i=0;i<pts+1;i++){
            DataPoint momentpoint = new DataPoint(XValues.get(i),MomentPointShown.get(i));
            pointsmoment[i] = momentpoint;
        }
        return pointsmoment;
    }
    private DataPoint[] generateData2() {
        DataPoint[] pointsshear = new DataPoint[pts+1];
        for(int i=0;i<pts+1;i++){
            DataPoint shearpoint = new DataPoint(XValues.get(i),ShearPointShown.get(i));
            pointsshear[i] = shearpoint;
        }
        return pointsshear;
    }
    private DataPoint[] generateData3() {
        DataPoint[] pointsdefelection = new DataPoint[pts+1];
        for(int i=0;i<pts+1;i++){
            DataPoint deflectionpoint = new DataPoint(XValues.get(i),DeflectionPointShown.get(i));
            pointsdefelection[i] = deflectionpoint;
        }
        return pointsdefelection;
    }
    //Este metodo cambia la unidad de los puntos
    private void scalepoints() {
        MomentPointShown.clear();
        ShearPointShown.clear();
        for(int i=0;i<pts+1;i++){
            MomentPointShown.add(i,MomentPoint.get(i)*multipliermoment);
            ShearPointShown.add(i,ShearPoint.get(i)*multipliershear);
        }
    }
    //Este metodo calcula la proporcion entre distintas unidades para el grafico
    private void calculatemultiplier() {
        Field field;
        Class<R.array> res = R.array.class;
        TypedArray ta67;
        try{
            field = res.getField("moment_conv");
            ta67 = getResources().obtainTypedArray(field.getInt(null));
            multipliermoment = Double.parseDouble(ta67.getString(unitselected[0]));
            field= res.getField("pointload_conv");
            ta67 = getResources().obtainTypedArray(field.getInt(null));
            multipliershear = Double.parseDouble(ta67.getString(unitselected[1]));
            ta67.recycle();
        }catch (Exception e){
            Log.e(TAG, "calculatemultiplier");
        }
    }
    //Este metodo calcula el valor de los puntos
    private void calculatepoints(){
        Double l, w, x;
        MomentPoint.clear();
        ShearPoint.clear();
        XValues.clear();
        switch (scenario) {
            //Cases: B_0_0;B_0_1;B_0_2:B_0_3:
            //Cases: B_1_0;B_1_1;B_1_2:B_1_3;
            //Cases: B_2_0;B_2_1;B_2_2;B_2_3;
            //Cases: B_3_0;B_3_1;B_3_2:B_3_3;
            case "B_0_0":
                for (int i = 0; i < pts+1; i++) {
                    l = InVal.get(0); //d1 = length
                    w = InVal.get(1); //d2 = udl
                    x = l/pts*i;
                    moment = l * w / 2 * x - w * Math.pow(x,2) * 1 / 2;
                    shear = l * w / 2  - w * x;
                    MomentPoint.add(moment);
                    ShearPoint.add(shear);
                    XValues.add(x);
                }
                break;
            case "B_0_1":
                for (int i = 0; i < pts+1; i++) {
                    l = InVal.get(0); //d1 = length
                    w = InVal.get(1); //d2 = udl
                    x = l/pts*i;
                    moment = w*x/(6*l)*(Math.pow(l,2)-Math.pow(x,2));
                    shear =  w/(6*l)*(3*Math.pow(x,2)-Math.pow(l,2));
                    MomentPoint.add(moment);
                    ShearPoint.add(shear);
                    XValues.add(x);
                }
                break;
            case "B_0_2":
                for (int i=0; i<pts+1;i++){
                    l = InVal.get(0);
                    w = InVal.get(1);
                    x = l/pts*i;
                    if(x<=l/2){
                        moment = w*x/(12*l)*(3*Math.pow(l,2)-4*Math.pow(x,2));
                        shear = w/(4*l)*(Math.pow(l,2)-4*Math.pow(x,2));
                    }else if(x>l/2&&x<=l){
                        moment = w*(l-x)/(12*l)*(3*Math.pow(l,2)-4*Math.pow((l-x),2));
                        shear = -w/(4*l)*(Math.pow(l,2)-4*Math.pow((l-x),2));
                    }else {
                        Log.e(TAG,"ERROR DE CALCULO");
                    }
                    MomentPoint.add(moment);
                    ShearPoint.add(shear);
                    XValues.add(x);
                }
            case "B_0_3":
                for (int i=0; i<pts+1;i++){
                    l = InVal.get(0);
                    w = InVal.get(1);
                    x = l/pts*i;
                    if(x<=l/2){
                        moment = w*x/(12*l)*(3*Math.pow(l,2)-6*l*x+4*Math.pow(x,2));
                        shear = w/(4*l)*Math.pow((l-2*x),2);
                    }else if(x>l/2&&x<=l){
                        moment = w*(l-x)/(12*l)*(Math.pow(l,2)-2*l*x+4*Math.pow(x,2));
                        shear = -w/(4*l)*Math.pow((2*x-l),2);
                    }else {
                        Log.e(TAG,"ERROR DE CALCULO");
                    }
                    MomentPoint.add(moment);
                    ShearPoint.add(shear);
                    XValues.add(x);
                }
        }
    }

    //Mediante este metodo creamos la matriz que contiene las reacciones para mostrar en el recyclerview
    //Creamos las reacciones y despues el recylcer view
    private void createrecyclerviewitems(String scenario) {
        try {
            //Añadimos variables de entrada
            Field field5;
            Class<R.array> res = R.array.class;
            field5 = res.getField(scenario);
            TypedArray ta1 = getResources().obtainTypedArray(field5.getInt(null));
            field5 = res.getField(scenario + "_1");
            TypedArray ta2 = getResources().obtainTypedArray(field5.getInt(null));
            for (int i = 0; i < ta1.length(); i++) {
                ReactionObject RO = new ReactionObject();
                RO.setReactionName(ta1.getString(i));
                RO.setReactionUnit(ta2.getString(i));
                ROList.add(RO);
            }
            inputsize = ta1.length(); // Numero de variables sin inercia ni modulo de elasticidad
            for(int i = 0; i< ROList.size(); i++){InVal.add(null);}

            //Ahora añadimos las reacciones
            field5 = res.getField(scenario + "_2");
            ta1 = getResources().obtainTypedArray(field5.getInt(null));
            field5 = res.getField(scenario + "_3");
            ta2 = getResources().obtainTypedArray(field5.getInt(null));
            for (int i = 0; i < ta1.length(); i++) {
                ReactionObject R03 = new ReactionObject();
                R03.setReactionName(ta1.getString(i));
                R03.setReactionUnit(ta2.getString(i));
                ROList.add(R03);
            }
            ta1.recycle();
            ta2.recycle();

            //las unidades seleccionadas son inicialmente en sistema internacional
            for(int i = 0; i< ROList.size(); i++){
                ROUnitSelec.add(0);
            }

        } catch (Exception e) {
            Log.e("Error Variables Entrada", "onCreateView: Error cargar variables entrada");
        }
    }



    private String MetodoParaObtenerUnidadDeMedida(String s1, int i) {
        String v1 = "ERROR";
        TypedArray ta1;
        if(s1!=null){ //pasamos "pressure" o "length" y luego es este typed array buscamos el tipo de unidad
            try{
                Field field1;
                Class<R.array> res = R.array.class;
                field1 = res.getField(s1);
                ta1 = getResources().obtainTypedArray(field1.getInt(null));
                v1 = ta1.getString(ROUnitSelec.get(i)); //Unidad seleccionada
                ta1.recycle();
            }catch (Exception e){
                Log.e("Error Unidad Reaction", "MetodoParaObtenerUnidadDeMedida: Error cargar unidad");
            }
        }
        return v1;
    }

    //Click en las unidades de los graficos
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.momentdiagram_unit:
                generateunitdialogwindow("Moment");
                break;
            case R.id.sheardiagram_unit:
                generateunitdialogwindow("Shear");
                break;
            case R.id.defleciondiagram_unit:
                generateunitdialogwindow("Deflection");
                break;
        }
    }

    private void generateunitdialogwindow(final String typeofgraph) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LinearLayout linearlayout = new LinearLayout(getActivity());
        RadioGroup radiogroup = new RadioGroup(getActivity());
        if(typeofgraph.equals("Moment")){
            for(int i = 0;i<MomentUnits.size();i++) {
                RadioButton radiobutton = new RadioButton(getActivity());
                radiobutton.setText(MomentUnits.get(i));
                radiogroup.addView(radiobutton);
                if(i==unitselected[0]){radiogroup.check(radiobutton.getId());}
            }
        }
        if(typeofgraph.equals("Shear")){
            for(int i = 0;i<ShearUnits.size();i++) {
                RadioButton radiobutton = new RadioButton(getActivity());
                radiobutton.setText(ShearUnits.get(i));
                radiogroup.addView(radiobutton);
                if(i==unitselected[1]){radiogroup.check(radiobutton.getId());}
            }
        }
        if(typeofgraph.equals("Deflection")){
            for(int i =0;i<DeflectionUnits.size();i++){
                RadioButton radiobutton = new RadioButton(getActivity());
                radiobutton.setText(DeflectionUnits.get(i));
                radiogroup.addView(radiobutton);
                if(i==unitselected[2]){radiogroup.check(radiobutton.getId());}
            }
        }
        linearlayout.addView(radiogroup);
        builder.setView(linearlayout);
        builder.show();
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                View radioButton2 = group.findViewById(checkedId);
                int radioId = group.indexOfChild(radioButton2);
                switch (typeofgraph){
                    case "Moment":
                        unitselected[0]=radioId;
                        unitmoment.setText(MomentUnits.get(unitselected[0]));
                        calculatemultiplier();
                        scalepoints();
                        populategraph();
                        break;
                    case "Shear":
                        unitselected[1]=radioId;
                        unitshear.setText(ShearUnits.get(unitselected[1]));
                        calculatemultiplier();
                        scalepoints();
                        populategraph();
                        break;
                    case "Deflection":
                        unitselected[2]=radioId;
                        unitdeflection.setText(DeflectionUnits.get(unitselected[2]));
                        calculatedeflectionmultiplier();
                        scaledeflectionpoints();
                        populatedeflectiongraph();
                        break;
                }
            }
        });
    }

    private String ajustardecimales(double d){
        DecimalFormat df1 = new DecimalFormat("#.####");
        DecimalFormat df2 = new DecimalFormat("#.####E0");
        String d2;
        if(d==0.00){
            d2 = df1.format(d);
        } else if(d>99999.99 || (d<0.001&&d>-0.001)||d<-99999.99){
            d2 = df2.format(d);
        }else {
            d2 = df1.format(d);
        }
        return d2;
    }

    private class Adapter3 extends RecyclerView.Adapter<ViewHolder3>{
        private Adapter3() {
            super();
        }

        @Override
        public ViewHolder3 onCreateViewHolder(ViewGroup parent, int viewType) {
            View v3 = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.beam_type_calculation_restuts_item, parent, false);
            return new ViewHolder3(v3);
        }

        @Override
        public void onBindViewHolder(ViewHolder3 holder, int position) {
            if(position<inputsize){
                holder.txtviewvalue.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark));
                holder.txtviewvalue.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
                holder.txtviewvalue.setClickable(true);
                holder.txtviewquestion.setVisibility(View.INVISIBLE);
                holder.txtviewquestion.setClickable(false);
            }else{
                holder.txtviewvalue.setClickable(false);
            }
            holder.txtviewname.setText(ROList.get(position).getReactionName());
            holder.txtviewunit.setText(MetodoParaObtenerUnidadDeMedida(ROList.get(position).getReactionUnit(),position));
            if(position==ROList.size()-1&&ROList.get(position).getReactionValue()!=null&&
                    ROList.get(position).getReactionValue().equals("0.00")){
                    holder.txtviewvalue.setText("\u2245 0");
            }else{
            String S2 = SItoX(ROList.get(position).getReactionValue(),ROList.get(position).getReactionUnit(),ROUnitSelec.get(position));
            holder.txtviewvalue.setText(S2);}

        }

        @Override
        public int getItemCount() {
            return ROList.size();
        }
    }

    private class ViewHolder3 extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView txtviewname;
        TextView txtviewunit;
        TextView txtviewvalue;
        TextView txtviewquestion;

        private ViewHolder3(View itemView) {
            super(itemView);

            txtviewname = (TextView)itemView.findViewById(R.id.reactiontextviewname);
            txtviewquestion = (TextView)itemView.findViewById(R.id.questionmark);
            txtviewquestion.setOnClickListener(this);
            txtviewunit = (TextView)itemView.findViewById(R.id.reactiontextviewunit);
            txtviewunit.setOnClickListener(this);
            txtviewvalue= (TextView) itemView.findViewById(R.id.reactiontextviewresult);
            txtviewvalue.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.reactiontextviewunit:
                    String typeofunit = ROList.get(getAdapterPosition()).getReactionUnit();
                    TypedArray ta2;
                    try {
                        Field field;
                        Class<R.array> res = R.array.class;
                        field = res.getField(typeofunit);
                        ta2 = getResources().obtainTypedArray(field.getInt(null));
                        createdialogwindow(ta2);
                        ta2.recycle();
                    } catch (Exception e) {
                        Log.e(TAG, "onClick: Error loading units");
                    }
                    break;
                case R.id.reactiontextviewresult:
                    if (getAdapterPosition() < inputsize) {
                        inputdialogwindow(ROList.get(getAdapterPosition()).getReactionUnit());
                    }
                    break;
                case R.id.questionmark:
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag("webview");
                    if(prev!=null){
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment dfgmt = new WebViewDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("scenario",scenario);
                    int position2 = getAdapterPosition()-inputsize;
                    bundle.putInt("position",position2);
                    dfgmt.setArguments(bundle);
                    dfgmt.show(ft,"webview");


            }
        }

        private void inputdialogwindow(final String Unidad) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final AlertDialog dialog;
            LinearLayout lnlayout = new LinearLayout(getActivity());
            lnlayout.setOrientation(LinearLayout.VERTICAL);
            final EditText etxt = new EditText(getActivity());
            if (!txtviewvalue.getText().toString().equals("")){
                etxt.setText(txtviewvalue.getText().toString());
            }
            etxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            lnlayout.addView(etxt);
            //mostrar modulos de elasticidad para hormigon y acero
            if(getAdapterPosition()==inputsize-2){
                RadioGroup rg = new RadioGroup(getActivity());
                RadioButton rb1 = new RadioButton(getActivity());
                rb1.setText(R.string.EConcrete);
                RadioButton rb2 = new RadioButton(getActivity());
                rb2.setText(R.string.ESteel);
                rg.addView(rb1);
                rg.addView(rb2);
                rb1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        etxt.setText(SItoX(String.valueOf(33000000000.00),"elasticitymodulus",ROUnitSelec.get(getAdapterPosition())));
                    }
                });
                rb2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        etxt.setText(SItoX(String.valueOf(210000000000.00),"elasticitymodulus",ROUnitSelec.get(getAdapterPosition())));
                    }
                });
                lnlayout.addView(rg);
            }
            builder.setView(lnlayout);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if(!etxt.getText().toString().equals("")) {
                        Double inputvalue = Double.parseDouble(etxt.getText().toString());
                        txtviewvalue.setText(ajustardecimales(inputvalue));
                        String SIVal = XtoSI(etxt.getText().toString(),Unidad,ROUnitSelec.get(getAdapterPosition()));
                        ROList.get(getAdapterPosition()).setReactionValue(SIVal); //lo guardamos en las reacciones
                        InVal.set(getAdapterPosition(), Double.parseDouble(SIVal));//lo guardamos en las variables de entrada
                        calculatereactions(getAdapterPosition());
                        mAdapter3.notifyDataSetChanged();
                  }
                }
            });
            dialog = builder.create();
            Window window = dialog.getWindow();
            if(window!=null){
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);}
            dialog.show();
            etxt.setSelection(etxt.getText().length());

            etxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i== EditorInfo.IME_ACTION_DONE){
                        if(!etxt.getText().toString().equals("")){
                            dialog.dismiss();
                            return true;
                        }else {
                            return false;
                        }
                    }
                    return false;
                }
            });
        }

        //create dialog window with options of units
        private void createdialogwindow(TypedArray ta2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LinearLayout ln = new LinearLayout(getActivity());
            RadioGroup rg = new RadioGroup(getActivity());
            for(int i =0; i<ta2.length();i++){
                RadioButton rb = new RadioButton(getActivity());
                rb.setText(ta2.getString(i));
                rg.addView(rb);
                if(ROUnitSelec.get(getAdapterPosition())==i){
                    rg.check(rb.getId());
                }
            }
            ln.addView(rg);
            builder.setView(ln);
            builder.show();
            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                    View rb2 = radioGroup.findViewById(i);
                    int j = radioGroup.indexOfChild(rb2);
                    RadioButton rb3 = (RadioButton)radioGroup.getChildAt(j);

                    String newvalue = SItoX(ROList.get(getAdapterPosition()).getReactionValue(),
                            ROList.get(getAdapterPosition()).getReactionUnit(),j);

                    txtviewvalue.setText(newvalue);
                    txtviewunit.setText(rb3.getText());
                    ROUnitSelec.set(getAdapterPosition(),j);
                }
            });
        }
    }

    private String XtoSI(String Value, String unit, int position){
        String result = null;
        if(Value!=null) {
            try {
                Field field;
                Class<R.array> res = R.array.class;
                field = res.getField(unit + "_conv");
                TypedArray ta1 = getResources().obtainTypedArray(field.getInt(null));
                String convf = ta1.getString(position);
                Double result1 = Double.parseDouble(Value) * 1 / Double.parseDouble(convf);
                result = String.valueOf(ajustardecimales(result1));
                ta1.recycle();
            } catch (Exception e) {
                Log.e(TAG, "XtoSI");
            }
        }
        return result;
    }

    private String SItoX (String Value, String unit, int position) {
        String result = null;
        if (Value != null) {
            try {
                Field field;
                Class<R.array> res = R.array.class;
                field = res.getField(unit + "_conv");
                TypedArray ta1 = getResources().obtainTypedArray(field.getInt(null));
                String convf = ta1.getString(position);
                Double result1 = Double.parseDouble(Value) * Double.parseDouble(convf);
                result = String.valueOf(ajustardecimales(result1));
                ta1.recycle();
            } catch (Exception e) {
                Log.e(TAG, "SItoX");
            }
        }
        return result;
    }

    private void calculatereactions(int position) {
        partialinput = true;
        totalinput = true;

        for(int i=0;i<inputsize-2;i++){
            if(InVal.get(i)==null){
                partialinput = false;
            }
        }
        for(int i=0;i<inputsize;i++){
            if(InVal.get(i)==null){
                totalinput=false;
            }
        }
        if(partialinput&&totalinput&&position>=inputsize-2){
            CalculateWithEI();
        }else if(partialinput&&totalinput&&position<inputsize-2){
            CalculateWithoutEI();
            CalculateWithEI();
        }else if(partialinput){
            CalculateWithoutEI();
        }
    }

    private void CalculateWithoutEI() {
        double l ,w;
        switch (scenario){
            case "B_0_0":
                RA = InVal.get(0)*InVal.get(1)/2;
                ROList.get(4).setReactionValue(ajustardecimales(RA));
                ROList.get(5).setReactionValue(ajustardecimales(RA));
                ROList.get(7).setReactionValue(ajustardecimales(RA));
                RA = Math.pow(InVal.get(0),2)*InVal.get(1)*1/8;
                ROList.get(6).setReactionValue(ajustardecimales(RA));
                break;
            case "B_0_1":
                l = InVal.get(0);
                w = InVal.get(1);
                RA = w*l/6;
                ROList.get(4).setReactionValue(ajustardecimales(RA)); //RA
                RA = w*l/3;
                ROList.get(5).setReactionValue(ajustardecimales(RA)); //RB
                ROList.get(7).setReactionValue(ajustardecimales(RA)); //Vmax
                double xmax = l/Math.sqrt(3);
                RA = w*xmax/(6*l)*(Math.pow(l,2)-Math.pow(xmax,2));
                ROList.get(6).setReactionValue(ajustardecimales(RA)); //Mmax
                break;
            case "B_0_2":
                l = InVal.get(0);
                w = InVal.get(1);
                RA = w*l/4;
                ROList.get(4).setReactionValue(ajustardecimales(RA));
                ROList.get(5).setReactionValue(ajustardecimales(RA));
                ROList.get(7).setReactionValue(ajustardecimales(RA));
                RA = w*Math.pow(l,2)/12;
                ROList.get(6).setReactionValue(ajustardecimales(RA));
                break;
            case "B_0_3":
                l = InVal.get(0);
                w = InVal.get(1);
                RA = w*l/4;
                ROList.get(4).setReactionValue(ajustardecimales(RA));
                ROList.get(5).setReactionValue(ajustardecimales(RA));
                ROList.get(7).setReactionValue(ajustardecimales(RA));
                RA = w*Math.pow(l,2)/24;
                ROList.get(6).setReactionValue(ajustardecimales(RA));
                break;
        }
        calculatepoints();
        calculatemultiplier();
        scalepoints();
        populategraph();
    }
    private void CalculateWithEI() {
        Double EI = InVal.get(inputsize-2)*InVal.get(inputsize-1);
        double l, w;
        switch (scenario) {
            case "B_0_0":
                RA = (Math.pow(InVal.get(0), 4) * 5 * InVal.get(1)) / (EI * 384);
                if(RA<0.0001){
                    ROList.get(10).setReactionValue("0.00");
                }
                else{
                    ROList.get(10).setReactionValue(ajustardecimales(RA));
                    calculatedeflectionpoints();
                    calculatedeflectionmultiplier();
                    scaledeflectionpoints();
                    populatedeflectiongraph();
                }
                RA = (Math.pow(InVal.get(0),3)*InVal.get(1))/(24*EI);
                ROList.get(8).setReactionValue(ajustardecimales(RA));
                RA = -RA;
                ROList.get(9).setReactionValue(ajustardecimales(RA));
                break;
            case "B_0_1":
                l = InVal.get(0);
                w = InVal.get(1);
                double xmax  = l*Math.sqrt(1-4/Math.sqrt(30));
                RA = 7*w*Math.pow(l,3)/(360*EI);
                ROList.get(8).setReactionValue(ajustardecimales(RA)); //theta A
                RA = -8*w*Math.pow(l,3)/(360*EI);
                ROList.get(9).setReactionValue(ajustardecimales(RA)); //theta B
                RA = w*xmax/(360*l*EI)*(7*Math.pow(l,4)+3*Math.pow(xmax,4)-10*Math.pow(l,2)*Math.pow(xmax,2)); //Deflection
                if(RA<0.0001){
                    ROList.get(10).setReactionValue("0.00");
                }
                else{
                    ROList.get(10).setReactionValue(ajustardecimales(RA));
                    calculatedeflectionpoints();
                    calculatedeflectionmultiplier();
                    scaledeflectionpoints();
                    populatedeflectiongraph();
                }
                break;
            case "B_0_2":
                l = InVal.get(0);
                w = InVal.get(1);
                RA = 5*w*Math.pow(l,3)/(192*EI);
                ROList.get(8).setReactionValue(ajustardecimales(RA)); //theta A
                ROList.get(9).setReactionValue(ajustardecimales(-RA)); //theta B
                RA = w*Math.pow(l,4)/(120*EI); //Deflection
                if(RA<0.0001){
                    ROList.get(10).setReactionValue("0.00");
                }
                else{
                    ROList.get(10).setReactionValue(ajustardecimales(RA));
                    calculatedeflectionpoints();
                    calculatedeflectionmultiplier();
                    scaledeflectionpoints();
                    populatedeflectiongraph();
                }
                break;
            case "B_0_3":
                l = InVal.get(0);
                w = InVal.get(1);
                RA = w*Math.pow(l,3)/(64*EI);
                ROList.get(8).setReactionValue(ajustardecimales(RA)); //theta A
                ROList.get(9).setReactionValue(ajustardecimales(-RA)); //theta B
                RA = 3*w*Math.pow(l,4)/(640*EI); //Deflection
                if(RA<0.0001){
                    ROList.get(10).setReactionValue("0.00");
                }
                else{
                    ROList.get(10).setReactionValue(ajustardecimales(RA));
                    calculatedeflectionpoints();
                    calculatedeflectionmultiplier();
                    scaledeflectionpoints();
                    populatedeflectiongraph();
                }
                break;
        }
    }

    private void populatedeflectiongraph() {
        series2.resetData(generateData3());
        graphdeflection.getViewport().setXAxisBoundsManual(true);
        graphdeflection.getViewport().setMinX(0.00);
        graphdeflection.getViewport().setMaxX(XValues.get(pts));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        nf.setMinimumIntegerDigits(1);
        graphdeflection.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
        graphdeflection.addSeries(series2);
    }

    private void scaledeflectionpoints() {
        DeflectionPointShown.clear();
        for(int i=0;i<pts+1;i++){
            DeflectionPointShown.add(i,DeflectionPoint.get(i)*multiplierdeflection);}
    }


    private void calculatedeflectionmultiplier() {
        try {
            Field field;
            Class<R.array> res = R.array.class;
            field = res.getField("length_conv");
            TypedArray ta68 = getResources().obtainTypedArray(field.getInt(null));
            multiplierdeflection = Double.parseDouble(ta68.getString(unitselected[2]));
            ta68.recycle();
        }catch (Exception e){
            Log.e(TAG, "DeflectionMultiplierCalculation");
        }
    }

    private void calculatedeflectionpoints() {
        double l, w, w1, w2, a, b, c, EI, x;
        EI = InVal.get(inputsize-2)*InVal.get(inputsize-1);
        DeflectionPoint.clear();
        switch (scenario){
            case "B_0_0":
                l = InVal.get(0);
                w = InVal.get(1);
                for(int i=0;i<pts+1;i++) {
                    x = l/pts*i;
                    deflection = -w * x / (24 * EI) * (Math.pow(l, 3) - 2 * l * Math.pow(x, 2) + Math.pow(x, 3));
                    DeflectionPoint.add(deflection);
                }
                break;
            case "B_0_1":
                l = InVal.get(0);
                w = InVal.get(1);
                for(int i=0;i<pts+1;i++) {
                    x = l/pts*i;
                    deflection = -w*x/(360*l*EI)*(7*Math.pow(l,4)+3*Math.pow(x,4)-10*Math.pow(l,2)*Math.pow(x,2));
                    DeflectionPoint.add(deflection);
                }
                break;
            case "B_0_2":
                l = InVal.get(0);
                w = InVal.get(1);
                for(int i=0;i<pts+1;i++) {
                    x = l/pts*i;
                    if(x<=l/2){
                        deflection = -w*x/(960*EI*l)*Math.pow((5*Math.pow(l,2)-4*Math.pow(x,2)),2);
                    }else if(x>l/2&&x<=l){
                        deflection = -w*(l-x)/(960*EI*l)*Math.pow((5*Math.pow(l,2)-4*Math.pow((l-x),2)),2);
                    }
                    DeflectionPoint.add(deflection);
                }
                break;
            case "B_0_3":
                l = InVal.get(0);
                w = InVal.get(1);
                for(int i=0;i<pts+1;i++) {
                    x = l/pts*i;
                    if(x<=l/2){
                        deflection = -w*x/(960*EI*l)*(15*Math.pow(l,4)-40*Math.pow(l,2)*Math.pow(x,2)+40*l*Math.pow(x,3)-16*Math.pow(x,4));
                    }else if(x>l/2&&x<=l){
                        deflection = -w/(960*EI*l)*(16*Math.pow(x,5)-40*Math.pow(x,4)*l+40*Math.pow(l,2)*Math.pow(x,3)
                                -40*Math.pow(l,3)*Math.pow(x,2)+25*x*Math.pow(l,4)-Math.pow(l,5));
                    }
                    DeflectionPoint.add(deflection);
                }
                break;

        }
    }
}


