package com.sopinet.trazeo.app.helpers;

import org.androidannotations.annotations.sharedpreferences.SharedPref;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;

/**
 * Created by hidabe on 21/04/14.
 */
@SharedPref
public interface MyPrefs {
    @DefaultString("")
    String email();

    // The field age will have default value 42
    @DefaultString("")
    String password();

    // The field lastUpdated will have default value 0
    long lastUpdated();
}
