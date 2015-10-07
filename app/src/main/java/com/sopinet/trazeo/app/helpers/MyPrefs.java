package com.sopinet.trazeo.app.helpers;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;
import org.androidannotations.annotations.sharedpreferences.DefaultString;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface MyPrefs {
    @DefaultString("")
    String user_id();

    @DefaultString("")
    String email();

    @DefaultString("")
    String pass();

    @DefaultString("")
    String notifications();

    @DefaultString("")
    String civiclubNotifications();

    @DefaultString("")
    String notificationsId();

    @DefaultString("")
    String civiclubNotificationsId();

    @DefaultString("")
    String myPoints();

    @DefaultBoolean(false)
    boolean isMonitor();

    @DefaultBoolean(false)
    boolean rideTutorial();

    @DefaultInt(-1)
    int new_user();

    @DefaultBoolean(true)
    boolean childInOutNotification();

    @DefaultBoolean(true)
    boolean RaidInOutNotification();

    @DefaultBoolean(true)
    boolean chatsNotification();

    @DefaultString("")
    String device_id();

    @DefaultString("")
    String res_id();
}
