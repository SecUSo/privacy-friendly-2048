package org.secuso.privacyfriendlyexample.activities;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

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
    int color;


    public element(Context c)
    {
        super(c);
        context = c;
        setAllCaps(false);
        setTextSize(textSize);
        setBackgroundResource(R.drawable.game_brick);
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_color","1").equals("1"))
            setColor(context.getResources().getColor(R.color.button_empty));
        else
            setColor(context.getResources().getColor(R.color.button_empty_2));

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
                setVisibility(View.VISIBLE);
        }

        if(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_color","1").equals("1"))
        {
            switch (number){
                case 0:
                    setColor(R.color.button_empty);
                    setTextColor((this.getResources().getColor(R.color.black)));
                    break;
                case 2:
                    setColor(context.getResources().getColor(R.color.button2));
                    setTextColor((this.getResources().getColor(R.color.black)));
                    break;
                case 4:
                    setColor(context.getResources().getColor(R.color.button4));
                    setTextColor((this.getResources().getColor(R.color.black)));
                    break;
                case 8:
                    setColor(context.getResources().getColor(R.color.button8));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 16:
                    setColor(context.getResources().getColor(R.color.button16));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 32:
                    setColor(context.getResources().getColor(R.color.button32));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 64:
                    setColor(context.getResources().getColor(R.color.button64));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 128:
                    setColor(context.getResources().getColor(R.color.button128));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 256:
                    setColor(context.getResources().getColor(R.color.button256));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 512:
                    setColor(context.getResources().getColor(R.color.button512));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 1024:
                    setColor(context.getResources().getColor(R.color.button1024));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 2048:
                    setColor(context.getResources().getColor(R.color.button2048));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 4096:
                    setColor(context.getResources().getColor(R.color.button4096));
                    setTextColor((this.getResources().getColor(R.color.black)));
                    break;
                case 8192:
                    setColor(context.getResources().getColor(R.color.button8192));
                    setTextColor((this.getResources().getColor(R.color.black)));
                    break;
                case 16384:
                    setColor(context.getResources().getColor(R.color.button16384));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    textSize = textSize * 0.8f;
                    setTextSize(textSize);
                    break;
                case 32768:
                    setColor(context.getResources().getColor(R.color.button32768));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    textSize = textSize * 0.8f;
                    setTextSize(textSize);
                    break;
            }
        }
        else
        {
            switch (number){
                case 0:
                    setColor(R.color.button_empty_2);
                    setTextColor((this.getResources().getColor(R.color.black)));
                    break;
                case 2:
                    setColor(context.getResources().getColor(R.color.button2_2));
                    setTextColor((this.getResources().getColor(R.color.black)));
                    break;
                case 4:
                    setColor(context.getResources().getColor(R.color.button4_2));
                    setTextColor((this.getResources().getColor(R.color.black)));
                    break;
                case 8:
                    setColor(context.getResources().getColor(R.color.button8_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 16:
                    setColor(context.getResources().getColor(R.color.button16_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 32:
                    setColor(context.getResources().getColor(R.color.button32_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 64:
                    setColor(context.getResources().getColor(R.color.button64_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 128:
                    setColor(context.getResources().getColor(R.color.button128_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 256:
                    setColor(context.getResources().getColor(R.color.button256_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 512:
                    setColor(context.getResources().getColor(R.color.button512_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 1024:
                    setColor(context.getResources().getColor(R.color.button1024_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 2048:
                    setColor(context.getResources().getColor(R.color.button2048_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 4096:
                    setColor(context.getResources().getColor(R.color.button4096_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 8192:
                    setColor(context.getResources().getColor(R.color.button8192_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    break;
                case 16384:
                    setColor(context.getResources().getColor(R.color.button16384_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    textSize = textSize * 0.8f;
                    setTextSize(textSize);
                    break;
                case 32768:
                    setColor(context.getResources().getColor(R.color.button32768_2));
                    setTextColor((this.getResources().getColor(R.color.white)));
                    textSize = textSize * 0.8f;
                    setTextSize(textSize);
                    break;
            }
        }
    }
    private void setColor(int c)
    {
        color = c;
        Drawable background = getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable)background).getPaint().setColor(c);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable)background).setColor(c);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable)background).setColor(c);
        }
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
        temp.color = color;
        temp.setTextSize(textSize);
        //temp.setBackgroundResource(backGroundResource);
        temp.setColor(color);
        temp.setVisibility(getVisibility());
        temp.setLayoutParams(getLayoutParams());
        return temp;
    }
}
