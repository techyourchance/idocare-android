package il.co.idocare.testdoubles.utils;

import il.co.idocare.utils.Logger;

/**
 * No op logger for testing
 */
public class NullLogger extends Logger {

    @Override
    public void e(String tag, String message) {

    }

    @Override
    public void w(String tag, String message) {

    }

    @Override
    public void v(String tag, String message) {

    }

    @Override
    public void d(String tag, String message) {

    }
}
