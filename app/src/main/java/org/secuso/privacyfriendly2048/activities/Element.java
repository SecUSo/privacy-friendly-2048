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

import static org.secuso.privacyfriendly2048.helpers.ThemeResolverKt.GetColorInt;
import static org.secuso.privacyfriendly2048.helpers.ThemeResolverKt.GetColorRes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.TypedValue;
import android.view.View;

import org.secuso.privacyfriendly2048.R;

/**
 * This class extends the android.support.v7.widget.AppCompatButton class and represents a box on the game field.
 * This element contains different features like the presented value (number) and the position of this value in the playing field (posX, posY).
 * Because of the animation a distinction is made between the calculated value and the displayed value.
 * Furthermore there are more features like the color of the box and the font size of the value.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */

public class Element extends androidx.appcompat.widget.AppCompatButton {
    public int number = 0;
    public int dNumber = 0;
    public int posX = 0;
    public int posY = 0;
    public int dPosX = 0;
    public int dPosY = 0;
    public boolean activated;
    public boolean animateMoving = false;
    Context context;
    int color;


    @SuppressLint("RestrictedApi")
    public Element(Context c) {
        super(c);
        context = c;
        setAllCaps(false);
        setBackgroundResource(R.drawable.game_brick);
        setColor(GetColorRes(context, R.attr.buttonEmpty));
        setMaxLines(1);
        setAutoSizeTextTypeUniformWithConfiguration(1,
                100,
                1,
                TypedValue.COMPLEX_UNIT_SP);
    }

    public void drawItem() {
        dNumber = number;
        activated = (number != 0);
        if (number == 0) {
            setVisibility(View.INVISIBLE);
            setText("");
        } else {
            setText("" + number);
            if (getVisibility() != View.VISIBLE)
                setVisibility(View.VISIBLE);
        }

        switch (number) {
            case 0:
                setColor(GetColorRes(context, R.attr.buttonEmpty));
                setTextColor(GetColorInt(context, R.attr.buttonEmptyText));
                break;
            case 2:
                setColor(GetColorRes(context, R.attr.button2));
                setTextColor(GetColorInt(context, R.attr.button2Text));
                break;
            case 4:
                setColor(GetColorRes(context, R.attr.button4));
                setTextColor(GetColorInt(context, R.attr.button4Text));
                break;
            case 8:
                setColor(GetColorRes(context, R.attr.button8));
                setTextColor(GetColorInt(context, R.attr.button8Text));
                break;
            case 16:
                setColor(GetColorRes(context, R.attr.button16));
                setTextColor(GetColorInt(context, R.attr.button16Text));
                break;
            case 32:
                setColor(GetColorRes(context, R.attr.button32));
                setTextColor(GetColorInt(context, R.attr.button32Text));
                break;
            case 64:
                setColor(GetColorRes(context, R.attr.button64));
                setTextColor(GetColorInt(context, R.attr.button64Text));
                break;
            case 128:
                setColor(GetColorRes(context, R.attr.button128));
                setTextColor(GetColorInt(context, R.attr.button128Text));
                break;
            case 256:
                setColor(GetColorRes(context, R.attr.button256));
                setTextColor(GetColorInt(context, R.attr.button256Text));
                break;
            case 512:
                setColor(GetColorRes(context, R.attr.button512));
                setTextColor(GetColorInt(context, R.attr.button512Text));
                break;
            case 1024:
                setColor(GetColorRes(context, R.attr.button1024));
                setTextColor(GetColorInt(context, R.attr.button1024Text));
                break;
            case 2048:
                setColor(GetColorRes(context, R.attr.button2048));
                setTextColor(GetColorInt(context, R.attr.button2048Text));
                break;
            case 4096:
                setColor(GetColorRes(context, R.attr.button4096));
                setTextColor(GetColorInt(context, R.attr.button4096Text));
                break;
            case 8192:
                setColor(GetColorRes(context, R.attr.button8192));
                setTextColor(GetColorInt(context, R.attr.button8192Text));
                break;
            case 16384:
                setColor(GetColorRes(context, R.attr.button16384));
                setTextColor(GetColorInt(context, R.attr.button16384Text));
                break;
            case 32768:
                setColor(GetColorRes(context, R.attr.button32768));
                setTextColor(GetColorInt(context, R.attr.button32768Text));
                break;
        }
    }

    private void setColor(int c) {
        color = c;
        Drawable background = getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(c);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(c);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(c);
        }
    }

    public String toString() {
        return "number: " + number;
    }

    public int getNumber() {
        return number;
    }

    public void setDPosition(int i, int j) {
        dPosX = i;
        dPosY = j;
    }

    public void setNumber(int i) {
        number = i;
    }

    public int getdPosX() {
        return dPosX;
    }

    public int getdPosY() {
        return dPosY;
    }

    public int getdNumber() {
        return dNumber;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public Element copy() {
        Element temp = new Element(context);
        temp.number = number;
        temp.dNumber = dNumber;
        temp.posX = posX;
        temp.posY = posY;
        temp.dPosX = dPosX;
        temp.dPosY = dPosY;
        temp.activated = activated;
        temp.animateMoving = animateMoving;
        temp.color = color;
        //temp.setBackgroundResource(backGroundResource);
        temp.setColor(color);
        temp.setVisibility(getVisibility());
        temp.setLayoutParams(getLayoutParams());
        return temp;
    }
}
