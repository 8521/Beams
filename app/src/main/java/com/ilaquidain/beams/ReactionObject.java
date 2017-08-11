package com.ilaquidain.beams;

/**
 * Created by ilaquidain on 11/03/2017.
 */

public class ReactionObject {
    private String ReactionName;
    private String ReactionValue;
    private String ReactionValueSI;
    private String ReactionUnit;
    private String ReactionEquation;
    private int UnitSelected;

    public void setReactionName(String mReactionName){ReactionName = mReactionName;}
    public String getReactionName(){return ReactionName;}

    public void setReactionValue(String mReactionValue){ReactionValue = mReactionValue;}
    public String getReactionValue(){return ReactionValue;}

    public void setReactionUnit(String mReactionUnit){ReactionUnit = mReactionUnit;}
    public String getReactionUnit(){return ReactionUnit;}

    public void setReactionEquation(String mReactionEquation){ReactionEquation = mReactionEquation;}
    public String getReactionEquation() {return ReactionEquation;}

    public void setReactionValueSI(String mReactionValueSI){ReactionValue = mReactionValueSI;}
    public String getReactionValueSI(){return ReactionValueSI;}

    public void setUnitSelected(int mUnitSelected){UnitSelected = mUnitSelected;}
    public int getUnitSelected(){return UnitSelected;}
}
