package org.secuso.privacyfriendlyexample.activities;

import android.content.Context;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.secuso.privacyfriendlyexample.R;

public class element extends android.support.v7.widget.AppCompatButton {
    public int number = 0;
    public int dNumber = 0;
    public int posX = 0;
    public int posY = 0;
    public int dPosX = 0;
    public int dPosY = 0;
    public boolean activated;
    public boolean animateMoving = false;
    public float textSize = 24;
    Context context;
    int backGroundResource;


    public element(Context c)
    {
        super(c);
        context = c;
        setAllCaps(false);
        setTextSize(textSize);
        setBackgroundResource(R.drawable.button_empty);
    }

    public void drawItem() {
        dNumber = number;
        activated = (number!=0);
        if(number== 0) {
            setVisibility(View.INVISIBLE);
            setText("");
        }
        else {
            setText("" + number);
            if(getVisibility() != View.VISIBLE)
            {
                setVisibility(View.VISIBLE);
                Log.i("setting","visible " + number);
            }
        }
        switch (number){
            case 0:
                backGroundResource = R.drawable.button_empty;
                setTextColor((this.getResources().getColor(R.color.black)));
                break;
            case 2:
                backGroundResource = R.drawable.button_2;
                setTextColor((this.getResources().getColor(R.color.black)));
                break;
            case 4:
                backGroundResource = R.drawable.button_4;
                setTextColor((this.getResources().getColor(R.color.black)));
                break;
            case 8:
                backGroundResource = R.drawable.button_8;
                setTextColor((this.getResources().getColor(R.color.white)));
                break;
            case 16:
                backGroundResource = R.drawable.button_16;
                setTextColor((this.getResources().getColor(R.color.white)));
                break;
            case 32:
                backGroundResource = R.drawable.button_32;
                setTextColor((this.getResources().getColor(R.color.white)));
                break;
            case 64:
                backGroundResource = R.drawable.button_64;
                setTextColor((this.getResources().getColor(R.color.white)));
                break;
            case 128:
                backGroundResource = R.drawable.button_128;
                setTextColor((this.getResources().getColor(R.color.white)));
                break;
            case 256:
                backGroundResource = R.drawable.button_256;
                setTextColor((this.getResources().getColor(R.color.white)));
                break;
            case 512:
                backGroundResource = R.drawable.button_512;
                setTextColor((this.getResources().getColor(R.color.white)));
                break;
            case 1024:
                backGroundResource = R.drawable.button_1024;
                break;
            case 2048:
                backGroundResource = R.drawable.button_2048;
                break;
            case 4096:
                backGroundResource = R.drawable.button_4096;
                break;
            case 8192:
                backGroundResource = R.drawable.button_8192;
                break;
            case 16384:
                backGroundResource = R.drawable.button_16384;
                textSize = textSize * 0.8f;
                setTextSize(textSize);
                break;
            case 32768:
                backGroundResource = R.drawable.button_32768;
                textSize = textSize * 0.8f;
                setTextSize(textSize);
                break;
        }
        setBackgroundResource(backGroundResource);
    }
    public String toString()
    {
        return "number: " + number;
    }

    public int getNumber() {
        return number;
    }

    public void setDPosition(int i, int j)
    {
        dPosX = i;
        dPosY = j;
    }
    public void setNumber(int i)
    {
        number = i;
    }
    public int getdPosX(){ return dPosX;}

    public int getdPosY(){ return dPosY;}

    public int getdNumber(){
        return dNumber;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
    public void updateFontSize(){
        //Log.i("FontSize",""+getLayoutParams().width + " " + (float)(getLayoutParams().width/8.0));
        textSize=(float)(getLayoutParams().width/7.0);
        setTextSize(textSize);
    }
    public element copy()
    {
        element temp = new element(context);
        temp.number = number;
        temp.dNumber = dNumber;
        temp.posX = posX;
        temp.posY = posY;
        temp.dPosX = dPosX;
        temp.dPosY = dPosY;
        temp.activated = activated;
        temp.animateMoving = animateMoving;
        temp.textSize = textSize;
        temp.setTextSize(textSize);
        temp.setBackgroundResource(backGroundResource);
        temp.setVisibility(getVisibility());
        temp.setLayoutParams(getLayoutParams());
        return temp;
    }
}
