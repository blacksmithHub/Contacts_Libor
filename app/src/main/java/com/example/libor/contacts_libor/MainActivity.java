package com.example.libor.contacts_libor;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.sqlitelib.DataBaseHelper;
import com.sqlitelib.SQLite;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public EditText name, number;
    public Spinner network;
    public ListView ListViewContacts;
    public ImageButton add, delete;
    public int valueId[] ;
    public ArrayAdapter adapterContact;
    public Integer cntrContact=0;
    public int position, count;
    public boolean update = false;
    public Animation onClick;
    public String split[];
    public String provider[][];
    public String getNetwork, str, cellNum, txtDuplicate;
    public ArrayList<String> arrayList1 = new ArrayList<String>();
    public ArrayAdapter<String> adp;
    public ArrayAdapter adapter;
    public String sql;

    private DataBaseHelper dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbhelper = new DataBaseHelper(MainActivity.this, "ContactsDatabase", 2);

        name = (EditText) findViewById(R.id.name);
        number = (EditText) findViewById(R.id.number);
        network = (Spinner) findViewById(R.id.network);
        add = (ImageButton) findViewById(R.id.add);
        delete = (ImageButton) findViewById(R.id.delete);
        ListViewContacts = (ListView) findViewById(R.id.ListViewContacts);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.network, R.layout.spinner_item);
        network.setAdapter(adapter);

        onClick = AnimationUtils.loadAnimation(this, R.anim.alpha);

//        provider = new String[][] {{"SUN","0922","0923","0925"}, //sun
//                {"GLOBE","0917","0994","0905"}, //globe
//                {"SMART","0918","0947","0998"}, //smart
//                {"TALK N TEXT","0946","0907","0909"}}; //talk n' text

        provider = new String[][] {
                {"0922","0923","0925"}, //sun
                {"0917","0994","0905"}, //globe
                {"0918","0947","0998"}}; //smart

        getNetwork = "SUN";

//        arrayList1.add("ALL NETWORKS");
//        arrayList1.add("SUN");
//        arrayList1.add("GLOBE");
//        arrayList1.add("SMART");
//
//        adp = new ArrayAdapter<String>
//                (MainActivity.this,android.R.layout.simple_spinner_item,arrayList1);
//        network.setAdapter(adp);

        spinNetwork();
        txtName();
        txtNum();
        btnAdd();
        btnDelete();
        longClick();

        refresh();
        reloadDb();
    }

    private void refresh()
    {
        getNetwork = "SUN";
        name.setText("");
        number.setText("");
        number.setHint("Contact number");
        update = false;
        add.setImageResource(R.drawable.add);
        delete.setImageResource(R.drawable.delete);
    }

    private void insert()
    {
        final SQLiteDatabase dbInsert = dbhelper.getWritableDatabase();
        try {
            String sqlStr = "INSERT INTO tblContact (ContactName, ContactNumber, Network) VALUES ('"
                    + name.getText().toString() + "', '" + cellNum + "','"
                    + getNetwork + "')";
            dbInsert.execSQL(sqlStr);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void update()
    {
        final SQLiteDatabase dbUpdate = dbhelper.getWritableDatabase();
        String sqlStr = "UPDATE tblContact SET ContactName = '" + name.getText().toString()
                + "', ContactNumber = '" + cellNum + "', Network = '" + getNetwork
                + "' where id = '" + cntrContact + "'";
        dbUpdate.execSQL(sqlStr);
    }

    private void delete()
    {
        final SQLiteDatabase dbDelete = dbhelper.getWritableDatabase();
        String sqlStr = "DELETE from tblContact where id = '" + cntrContact + "'";
        dbDelete.execSQL(sqlStr);
    }

    private void longClick()
    {
        final SQLiteDatabase db = dbhelper.getWritableDatabase();
        ListViewContacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                try {
                    cntrContact=valueId[i];
                } catch (Exception e) {
                    e.printStackTrace();
                }
                alertDialog.setTitle("Warning!");
                alertDialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setNegativeButton("UPDATE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        update = true;
                        number.setText("");
                        number.setHint("Update contact number");
                        add.setImageResource(R.drawable.save);
                        delete.setImageResource(R.drawable.cancel);
                        //network.setSelection(position+1);
                        reloadDb();

                        try
                        {
                            String query = "Select ContactName FROM tblContact WHERE id = '" + cntrContact + "'";
                            Cursor cursor = db.rawQuery(query, null);

                            if (cursor.moveToFirst()) {
                                cursor.moveToFirst();
                                name.setText(cursor.getString(0));
                            }
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                alertDialog.setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete();
                        refresh();
                        reloadDb();
                    }
                }); alertDialog.show();
                return false;
            }
        });
    }

    private void reloadDb()
    {
        SQLiteDatabase dbContact = dbhelper.getWritableDatabase();
        //get table from sqlite_master
        Cursor cContact = dbContact.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='tblContact'",
                null);
        cContact.moveToNext();
        count = cContact.getCount();

        if (cContact.getCount() == 0) { //check if the database is exisitng
            SQLite.FITCreateTable("ContactsDatabase", this, "tblContact",
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ContactName VARCHAR(90),ContactNumber VARCHAR(90) not null unique,Network VARCHAR(90)"); //create table


        } else {



//            if(getNetwork.equals("ALL NETWORKS"))
//            {
//                sql = "SELECT id, ContactName, ContactNumber,Network  FROM tblContact order by id desc";
//            }
//            else
//            {
//                sql = "SELECT id, ContactName, ContactNumber,Network  FROM tblContact WHERE Network = '"+getNetwork+"' order by id desc";
//            }
            sql = "SELECT id, ContactName, ContactNumber,Network  FROM tblContact WHERE Network = '"+getNetwork+"' order by id desc";
            cContact = dbContact.rawQuery(sql, null);

            String valueContact[] = new String[cContact.getCount()];
            int valueCurrentId[] = new int[cContact.getCount()];


            int ctrl = 0;
            while (cContact.moveToNext()) {
                String strFor = "";

                strFor += "Name: " + cContact.getString(cContact.getColumnIndex("ContactName"));
                strFor += System.lineSeparator() + "Contact #: "
                        + cContact.getString(cContact.getColumnIndex("ContactNumber"));
                strFor += System.lineSeparator() + "Network: " + cContact.getString(cContact.getColumnIndex("Network"));

                valueCurrentId[ctrl]= cContact.getInt(cContact.getColumnIndex("id"));
                valueContact[ctrl] = strFor;
                ctrl++;
            }

            valueId = Arrays.copyOf(valueCurrentId, cContact.getCount());//transfer content array to a public array

            adapterContact = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, valueContact);
            try {
                ListViewContacts.setAdapter(adapterContact);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void btnDelete()
    {
        final SQLiteDatabase dbDelete = dbhelper.getWritableDatabase();
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete.startAnimation(onClick);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                if(update == true)
                {
                    refresh();
                    reloadDb();
                }
                else
                {
                    if (count == 0)
                    {
                        alertDialog.setTitle("No record found!");
                        alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }); alertDialog.show();
                    }
                    else
                    {
                        alertDialog.setTitle("Confirm Delete");
                        alertDialog.setMessage("Are you sure you want delete all records?");
                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {

                                String sqlStr = "DELETE from tblContact";
                                dbDelete.execSQL(sqlStr);
                                refresh();
                                reloadDb();
                            }
                        });
                        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }); alertDialog.show();
                    }
                }

            }
        });
    }

    private void btnAdd()
    {
        //final SQLiteDatabase db = dbhelper.getWritableDatabase();
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add.startAnimation(onClick);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                if(name.length() == 0 && number.length() == 0)
                {
                    alertDialog.setTitle("Missing field!");
                    alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }); alertDialog.show();
                }
                else
                {
//                    if(count != 0)
//                    {
//                        try {
//                            String query = "Select * FROM tblContact WHERE ContactNumber = '" + cellNum + "'";
//                            Cursor cursor = db.rawQuery(query, null);
//
//                            if (cursor.moveToFirst()) {
//                                cursor.moveToFirst();
//                                txtDuplicate = cursor.getString(0);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    else
//                    {
//                        txtDuplicate = "0";
//                    }
//                    if(txtDuplicate == cellNum)
//                    {
//                        alertDialog.setTitle("Contact number already exist!");
//                        alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        }); alertDialog.show();
//                    }
//                    else
//                    {
                    //end end
//                    try {
//                        int a;
//                        for(a = 0; a <= 3; a++)
//                        {
//                            if (position == 3)
//                            {
//                                alertDialog.setTitle("Invalid contact number!");
//                                alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.cancel();
//                                    }
//                                });alertDialog.show();
//                                break;
//                            }
//                            else if (a == 3)
//                            {
//                                position++;
//                                a = 0;
//                            }
//                            else if(split[0].equals(provider[position][a]))
//                            {
//                                getNetwork = provider[position][0];
//
//                                if(update == true)
//                                {
//                                    network.setSelection(position+1);
//                                    update();
//                                    refresh();
//                                    reloadDb();
//                                    add.setImageResource(R.drawable.add);
//                                    delete.setImageResource(R.drawable.delete);
//                                    break;
//                                }
//                                else
//                                {
//                                    if(provider[position][0] == "TALK N TEXT")
//                                    {
//                                        adp.add("TALK N TEXT");
//                                    }
//                                    network.setSelection(position+1);
//                                    insert();
//                                    refresh();
//                                    reloadDb();
//                                    break;
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_SHORT).show();
//                    }

                    for (int x = 0; x <= 2; x++) {

                        if (split[0].equals(provider[position][x])) {

                            if(update == true)
                            {
                                update();
                                    refresh();
                                    reloadDb();
                                break;
                            }
                            else
                            {
                                insert();
                                refresh();
                                reloadDb();
                                break;
                            }

                        } else if (x == 2) {
                            alertDialog.setTitle("Invalid contact number!");
                            alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });alertDialog.show();
                        }
                        }
                }
            }
        });
    }

    private void txtNum()
    {
        number.addTextChangedListener(new TextWatcher() {
            int len=0;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                str = number.getText().toString();
                len = str.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(number.length() == 12) {
                    split = str.split("-");
                    cellNum = split[0]+split[1];
                } else {
                    number.setError("This field cannot be blank!");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                str = number.getText().toString();
                if(str.length() == 4 && len < str.length())
                {
                    number.append("-");
                }
            }
        });
    }

    private void txtName()
    {
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(name.length() != 0) {
                } else {
                    name.setError("This field cannot be blank!");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void spinNetwork()
    {
        network.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        getNetwork = network.getItemAtPosition(i).toString();
                position = i;
//                        if(position > 0)
//                        {
//                            position = position - 1;
//                        }
//                        else if(position < 0)
//                        {
//
//                        }
                Toast.makeText(MainActivity.this,""+i,Toast.LENGTH_SHORT).show();
                reloadDb();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}