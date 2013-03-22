package com.eit.minimap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Enum tying together Drawable resources with the Id's outputted by iconName in preferences.
 */
public class UserIcons {

    private static final int TEXT_SIZE = 25;
    public static final int[] ICONS = {
            R.drawable.icon_medic,
            R.drawable.icon_sniper,
            R.drawable.icon_rifleman,
            R.drawable.icon_leader
    };

    /**
     * Creates an icon with text on top.
     * @param res Resources needed to fetch images
     * @param icon Icon ID from this class' ICONS var
     * @param text Text to write over the icon
     * @return Bitmap with text.
     */
    public static Bitmap makeIconWithText(Resources res, int icon,String text) {
        final Paint p = new Paint();
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(TEXT_SIZE - 2);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);
        p.setColor(Color.BLACK);
        int newWidth = (int) p.measureText(text)+4;

        Bitmap ourAvatar = BitmapFactory.decodeResource(res, icon);
        Bitmap newAvatar = Bitmap.createBitmap(newWidth,ourAvatar.getHeight()+TEXT_SIZE,ourAvatar.getConfig());
        Canvas c = new Canvas(newAvatar);
        c.drawBitmap(ourAvatar,(newWidth-ourAvatar.getWidth())/2,TEXT_SIZE,p);

        //Draw background black stroke
        c.drawText(text,newAvatar.getWidth()/2,TEXT_SIZE,p);
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        //DRaw foreground white stroke
        c.drawText(text,newAvatar.getWidth()/2,TEXT_SIZE,p);
        return newAvatar;
    }
}
