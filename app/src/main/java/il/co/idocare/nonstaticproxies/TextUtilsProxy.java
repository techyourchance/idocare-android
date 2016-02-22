package il.co.idocare.nonstaticproxies;

import android.text.TextUtils;

/**
 * Non-static proxy for TextUtils
 */
public class TextUtilsProxy {


    public boolean isEmpty(CharSequence str) {
        return TextUtils.isEmpty(str);
    }
}
