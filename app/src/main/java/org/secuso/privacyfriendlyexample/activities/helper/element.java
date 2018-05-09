package org.secuso.privacyfriendlyexample.activities.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import org.secuso.privacyfriendlyexample.R;

public class element extends Button {
    public int number = 0;
    public int posX = 0;
    public int posY = 0;
    public boolean activated;
    public element(Context c)
    {
        super(c);
        setAllCaps(false);
        setTextSize(24);
    }

    public void setNumber(int number) {
        this.number = number;
        activated = (number!=0);
        if(number== 0)
            setText("");
        else
            setText(""+number);
        switch (number){
            case 0:
                setBackgroundResource(R.drawable.button_empty);
                break;
            case 2:
                setBackgroundResource(R.drawable.button_2);
                break;
            case 4:
                setBackgroundResource(R.drawable.button_4);
                break;
            case 8:
                setBackgroundResource(R.drawable.button_8);
                break;
        }
    }
    public String toString()
    {
        return "number: " + number;
    }

    public int getNumber() {
        return number;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public void updateFontSize(){
        Log.i("FontSize",""+getLayoutParams().width + " " + (float)(getLayoutParams().width/8.0));
        setTextSize((float)(getLayoutParams().width/8.0));
    }
}
