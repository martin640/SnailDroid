package sk.martin64.snaildroid.view;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public final class Utils {

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void applyStatusBarPadding(ViewGroup viewGroup) {
        int result = 0;
        int resourceId = viewGroup.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = viewGroup.getResources().getDimensionPixelSize(resourceId);
        }

        viewGroup.setPadding(viewGroup.getPaddingLeft(),
                result,
                viewGroup.getPaddingRight(),
                viewGroup.getPaddingBottom());
    }

    public static String humanReadableByteCountSI(long bytes) {
        return humanReadableByteCountSI(bytes, 1);
    }
    public static String humanReadableByteCountSI(long bytes, int precision) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%." + precision + "f %cB", bytes / 1000.0, ci.current());
    }
    public static String humanReadableBitsCount(long bytes, int precision) {
        bytes = bytes * 8;
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " b";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%." + precision + "f %cb", bytes / 1000.0, ci.current());
    }
    public static String shortNumber(long num, int precision) {
        if (-1000 < num && num < 1000) {
            return String.valueOf(num);
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (num <= -999_950 || num >= 999_950) {
            num /= 1000;
            ci.next();
        }
        return String.format("%." + precision + "f%c", num / 1000.0, ci.current());
    }
}