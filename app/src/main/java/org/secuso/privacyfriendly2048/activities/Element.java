/*
 This file is part of Privacy Friendly 2048. This app implements the functions of the
 game 2048 in a privacy friendly version.

 Privacy Friendly 2048 is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */


package org.secuso.privacyfriendly2048.activities;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;

import org.secuso.privacyfriendly2048.R;

/**
 * This class is a gameElement and contains the position and number of a game element
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180807
 */

public class Element extends android.support.v7.widget.AppCompatButton {
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


    public Element(Context c)
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
                    setColor(ContextCompat.getColor(context, R.color.button_empty));
                    setTextColor(ContextCompat.getColor(context,R.color.black));
                    break;
                case 2:
                    setColor(ContextCompat.getColor(context,R.color.button2));
                    setTextColor(ContextCompat.getColor(context,R.color.black));
                    break;
                case 4:
                    setColor(ContextCompat.getColor(context,R.color.button4));
                    setTextColor(ContextCompat.getColor(context,R.color.black));
                    break;
                case 8:
                    setColor(ContextCompat.getColor(context,R.color.button8));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 16:
                    setColor(ContextCompat.getColor(context,R.color.button16));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 32:
                    setColor(ContextCompat.getColor(context,R.color.button32));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 64:
                    setColor(ContextCompat.getColor(context,R.color.button64));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 128:
                    setColor(ContextCompat.getColor(context,R.color.button128));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 256:
                    setColor(ContextCompat.getColor(context,R.color.button256));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 512:
                    setColor(ContextCompat.getColor(context,R.color.button512));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 1024:
                    setColor(ContextCompat.getColor(context,R.color.button1024));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 2048:
                    setColor(ContextCompat.getColor(context,R.color.button2048));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 4096:
                    setColor(ContextCompat.getColor(context,R.color.button4096));
                    setTextColor(ContextCompat.getColor(context,R.color.black));
                    break;
                case 8192:
                    setColor(ContextCompat.getColor(context,R.color.button8192));
                    setTextColor(ContextCompat.getColor(context,R.color.black));
                    break;
                case 16384:
                    setColor(ContextCompat.getColor(context,R.color.button16384));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    textSize = textSize * 0.8f;
                    setTextSize(textSize);
                    break;
                case 32768:
                    setColor(ContextCompat.getColor(context,R.color.button32768));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
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
                    setTextColor(ContextCompat.getColor(context,R.color.black));
                    break;
                case 2:
                    setColor(ContextCompat.getColor(context,R.color.button2_2));
                    setTextColor(ContextCompat.getColor(context,R.color.black));
                    break;
                case 4:
                    setColor(ContextCompat.getColor(context,R.color.button4_2));
                    setTextColor(ContextCompat.getColor(context,R.color.black));
                    break;
                case 8:
                    setColor(ContextCompat.getColor(context,R.color.button8_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 16:
                    setColor(ContextCompat.getColor(context,R.color.button16_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 32:
                    setColor(ContextCompat.getColor(context,R.color.button32_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 64:
                    setColor(ContextCompat.getColor(context,R.color.button64_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 128:
                    setColor(ContextCompat.getColor(context,R.color.button128_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 256:
                    setColor(ContextCompat.getColor(context,R.color.button256_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 512:
                    setColor(ContextCompat.getColor(context,R.color.button512_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 1024:
                    setColor(ContextCompat.getColor(context,R.color.button1024_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 2048:
                    setColor(ContextCompat.getColor(context,R.color.button2048_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 4096:
                    setColor(ContextCompat.getColor(context,R.color.button4096_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 8192:
                    setColor(ContextCompat.getColor(context,R.color.button8192_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    break;
                case 16384:
                    setColor(ContextCompat.getColor(context,R.color.button16384_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
                    textSize = textSize * 0.8f;
                    setTextSize(textSize);
                    break;
                case 32768:
                    setColor(ContextCompat.getColor(context,R.color.button32768_2));
                    setTextColor(ContextCompat.getColor(context,R.color.white));
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
        textSize=(float)(getLayoutParams().width/7.0);
        setTextSize(textSize);
    }
    public Element copy()
    {
        Element temp = new Element(context);
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
