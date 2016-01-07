package com.sopinet.trazeo.app.helpers;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;



public class RestClient {

    public static String URL_API = "http://beta.trazeo.es/";
    //public static String URL_API = "http://beta.trazeo.es/app_dev.php/";

    public static String URL_API_LOGIN = "api/login";
    public static String URL_API_REGISTER = "api/register";
    public static String URL_API_GROUPS = "api/groups";
    public static String URL_API_RIDE_CREATE = "api/ride/createNew";
    public static String URL_API_RIDE_DATA = "api/ride/data";
    public static String URL_API_SENDPOSITION = "api/ride/sendPosition";

    public static String URL_API_CHILDIN = "api/ride/sendChildInRide";
    public static String URL_API_CHILDOUT = "api/ride/sendChildOutRide";

    public static String URL_API_RIDE_FINISH = "api/ride/finish";

    public static String URL_API_SENDREPORT = "api/ride/report";

    public static String URL_API_LASTPOINT = "api/ride/lastPoint";

    public static String URL_API_WALL = "api/group/timeline/list";
    public static String URL_API_WALL_NEW = "api/group/timeline/new";
    public static String URL_API_TIMESTAMP = "api/timeStamp";

    public static String URL_API_MANAGE_GROUP = "api/manageGroup";
    public static String URL_API_SEARCH_GROUPS = "api/groupsCity";
    public static String URL_API_GET_CITIES = "api/cities";
    public static String URL_API_JOIN_GROUP = "api/joinGroup";
    public static String URL_API_REQUEST_GROUP = "api/requestJoinGroup";
    public static String URL_API_MANAGE_CHILD = "api/manageChild";
    public static String URL_API_GET_CHILDREN = "api/user/childs";
    public static String URL_API_INVITE = "api/group/invite";
    public static String URL_API_GET_LOCALIONS = "api/geo/city/list";
    public static String URL_API_GET_COUNTRIES = "api/geo/countries/list";
    public static String URL_API_DISJOIN = "api/group/disjoin";
    public static String URL_API_REMOVE_CHILD = "api/deleteChild";
    public static String URL_API_REMOVE_GROUP = "api/deleteGroup";
    public static String URL_API_CATALOG_CITIES = "api/catalog/cities";
    public static String URL_API_CATALOG = "api/catalog/city";
    public static String URL_API_MY_POINTS = "api/user/points";
    public static String URL_API_CHANGE_POINTS = "api/catalog/exchange";
    public static String URL_API_PROFILE = "api/user/profile";
    public static String URL_API_CHANGE_PROFILE = "api/user/modify/profile";
    public static String URL_API_CHANGE_PASSWORD = "api/user/change/password";
    public static String URL_API_CHANGE_NOTIFICATIONS = "api/user/notification/modify/settings";
    public static String URL_API_NOTIFICATIONS = "api/user/notification/settings";
    public static String URL_API_POINTS = "api/auto/url";
    public static String URL_API_JOIN_DISJOIN_CHILD = "api/group/setChild";
    public static String URL_API_REGISTER_DEVICE = "api/user/register/device";
    public static String URL_API_CREATE_CHAT = "api/group/create/chat";
    public static String URL_API_CHAT_REPLY = "api/chat/reply";
    public static String URL_API_MEMBERS = "api/group/userList/chat";


    private static AsyncHttpClient client = new AsyncHttpClient();

    private static AsyncHttpClient syncClient = new SyncHttpClient();

    static {

        syncClient.setConnectTimeout(60000);
        client.setConnectTimeout(60000);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    public static void syncGet(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        syncClient.get(url, params, responseHandler);
    }

    public static void syncPost(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        syncClient.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return URL_API + relativeUrl;
    }

}
