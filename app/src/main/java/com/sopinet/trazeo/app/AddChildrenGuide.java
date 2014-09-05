package com.sopinet.trazeo.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sopinet.android.nethelper.NetHelper;
import com.sopinet.android.nethelper.SimpleContent;
import com.sopinet.trazeo.app.gson.Children;
import com.sopinet.trazeo.app.gson.EChild;
import com.sopinet.trazeo.app.gson.EditGroup;
import com.sopinet.trazeo.app.helpers.MyPrefs_;
import com.sopinet.trazeo.app.helpers.NewChildAdapter;
import com.sopinet.trazeo.app.helpers.Var;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.lang.reflect.Type;
import java.util.ArrayList;

@EActivity(R.layout.add_children_guide)
public class AddChildrenGuide extends ActionBarActivity {

    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    ListView childrenList;

    @ViewById
    LinearLayout newChildForm;

    @ViewById
    Button backBtn;

    @ViewById
    Button saveBtn;

    @ViewById
    EditText childName;

    @ViewById
    RadioButton boy;

    ProgressDialog pdialog;

    NewChildAdapter adapter;

    Children children;

    ArrayList<EChild> newChildren;

    @AfterViews
    void init() {
        showDialog("Cargando...");
        newChildren = new ArrayList<EChild>();
        getUserChildren();
    }

    @Background
    void getUserChildren() {
        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email=" + myPrefs.email().get();
        data += "&pass=" + myPrefs.pass().get();
        String result = "";
        try {
            result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_GET_CHILDREN, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        final Type objectCPD = new TypeToken<Children>() {
        }.getType();
        this.children = new Gson().fromJson(result, objectCPD);
        showUserChildren();
    }

    @UiThread
    void showUserChildren() {
        this.pdialog.dismiss();
        this.adapter = new NewChildAdapter(this, R.layout.new_child_list_item, this.children.data);
        this.childrenList.setAdapter(adapter);
    }

    @Click(R.id.addBtn)
    public void addChild() {
        if(childName.getText().toString().equals("")) {
            Toast.makeText(this, "Debes escribir el nombre del niño", Toast.LENGTH_LONG).show();
        } else {
            String gender;
            EChild child;
            if(this.boy.isChecked())
                gender = "boy";
            else
                gender = "girl";

            child = new EChild(childName.getText().toString(), gender);
            this.children.data.add(child);
            this.adapter = new NewChildAdapter(this, R.layout.new_child_list_item, this.children.data);
            this.childrenList.setAdapter(this.adapter);

            this.newChildren.add(child);

            childName.setText("");
        }
    }

    @Click(R.id.saveBtn)
    public void saveBtnClick() {
        if(NetHelper.isOnline(this)) {
            showDialog("Estamos registrando tus niños...");
            sendNewChildren();
        } else {
            Toast.makeText(this, "No hay conexión", Toast.LENGTH_LONG).show();
        }
    }

    @Background
    void sendNewChildren() {
        for (EChild child : this.newChildren) {
            SimpleContent sc = new SimpleContent(this, "trazeo", 0);
            String data = "email=" + myPrefs.email().get();
            data += "&pass=" + myPrefs.pass().get();
            data += "&name=" + child.nick;
            data += "&gender=" + child.gender;
            String result = "";
            try {
                result = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_MANAGE_CHILD, data);
            } catch (SimpleContent.ApiException e) {
                e.printStackTrace();
            }
            Log.d("NEW CHILD", "NEW CHILD: " + result);
        }

        String[] groupNameArray = myPrefs.email().get().split("@");
        String groupName = groupNameArray[0];
        Log.d("GROUPNAME", "GROUPNAME: " + groupName);

        SimpleContent sc = new SimpleContent(this, "trazeo", 0);
        String data = "email=" + myPrefs.email().get();
        data += "&pass=" + myPrefs.pass().get();
        data += "&name=" + "El grupo de " + groupName;
        data += "&visibility=1";
        String groupResult = "";
        try {
            groupResult = sc.postUrlContent(myPrefs.url_api().get() + Var.URL_API_MANAGE_GROUP, data);
        } catch (SimpleContent.ApiException e) {
            e.printStackTrace();
        }

        Log.d("NEWGROUP", "NEW GROUP: " + groupResult);

        final Type objectCPD = new TypeToken<EditGroup>() {
        }.getType();
        EditGroup new_group = new Gson().fromJson(groupResult, objectCPD);

        showSaveResult(new_group);
    }

    @UiThread
    void showSaveResult(EditGroup new_group) {
        this.pdialog.dismiss();
        if(this.newChildren.size() > 0)
            Toast.makeText(this, "El registro se ha hecho con éxito", Toast.LENGTH_LONG).show();

        if(new_group.state.equals("1")) {
            Intent i = new Intent(this, SelectGroupActivity_.class);
            i.putExtra("firstGroup", true);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(this, "Ha habido un problema al crear tu primer grupo, inténtalo de nuevo", Toast.LENGTH_LONG).show();
        }
    }

    @Click(R.id.backBtn)
    public void backBtnClick() {
        finish();
        Toast.makeText(this, "No se registrará ningún niño para que camine al colegio", Toast.LENGTH_LONG).show();
    }

    private void showDialog(String msg) {
        this.pdialog = new ProgressDialog(this);
        this.pdialog.setCancelable(false);
        this.pdialog.setMessage(msg);
        this.pdialog.show();
    }
}
