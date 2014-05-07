package com.sopinet.trazeo.app.helpers;

public class Var {
    //public static String URL_API = "http://dev.trazeo.es/";
    public static String URL_API = "http://dev.trazeo.es/app_dev.php/";
    //ºpublic static String URL_API = "http://192.168.1.131/trazeo-web/web/app_dev.php/";
    //public static String URL_API = "http://192.168.1.149/trazeo-web/web/app_dev.php/";
    public static String URL_API_LOGIN = Var.URL_API + "api/login";
    public static String URL_API_GROUPS = Var.URL_API + "api/groups";
    public static String URL_API_RIDE_CREATE = Var.URL_API + "api/ride/createNew";
    public static String URL_API_RIDE_DATA = Var.URL_API + "api/ride/data";
    public static String URL_API_SENDPOSITION = Var.URL_API + "api/ride/sendPosition";

    public static String URL_API_CHILDIN = Var.URL_API + "api/ride/sendChildInRide";
    public static String URL_API_CHILDOUT = Var.URL_API + "api/ride/sendChildOutRide";

    public static String URL_API_RIDE_FINISH = Var.URL_API + "api/ride/finish";

    public static String URL_API_SENDREPORT = Var.URL_API + "api/ride/report";

    public static String URL_API_LASTPOINT = Var.URL_API + "api/ride/lastPoint";
}