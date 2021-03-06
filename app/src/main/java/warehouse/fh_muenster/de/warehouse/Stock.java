package warehouse.fh_muenster.de.warehouse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import warehouse.fh_muenster.de.warehouse.Server.Config;
import warehouse.fh_muenster.de.warehouse.Server.Server;
import warehouse.fh_muenster.de.warehouse.Server.ServerMockImple;

public class Stock extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;
    private HashMap<String, Article> hm;
    private ListView mListLayout;
    private ArrayAdapter<String> mAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    private boolean finishActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        WarehouseApplication myApp = (WarehouseApplication) getApplication();
        StockAllItemsTask stockAllItemsTask = new StockAllItemsTask();
        stockAllItemsTask.execute(myApp.getEmployee().getSessionId());

        //Drawer Menu
        mListLayout = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        addDrawerItems();
        setupDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * AsyncTask
     * Holt sich alle Lagerbestände vom Server.
     */
    private class StockAllItemsTask extends AsyncTask<Integer, Integer, HashMap<String, Article>> {
        ProgressDialog dialog;
        HashMap<String, Article> hm2;

        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(Stock.this, getResources().getString(R.string.dialog_wait),
                    getResources().getString(R.string.dialog_article), true);
        }

        @Override
        protected HashMap<String, Article> doInBackground(Integer... params) {
            if (params.length != 1) {
                return null;
            }

            int id = params[0];
            if (Config.isMock()) {
                ServerMockImple server = new ServerMockImple();
                hm2 = server.getArticles(id);
            } else {
                Server server = new Server();
                hm2 = server.getArticles(id);
            }

            return hm2;
        }

        @Override
        protected void onPostExecute(HashMap<String, Article> result) {
            if (result != null) {
                hm = result;
                WarehouseApplication myApp = (WarehouseApplication) getApplication();
                myApp.setArticleMap(hm);
                dialog.dismiss();
                printTable();
            } else {

            }
        }
    }

    /**
     * Erstellt eine Tabelle mit allen Lagerbeständen.
     */
    private void printTable() {
        TableLayout table = (TableLayout) findViewById(R.id.stock_table_layout);

        int i = 0;
        for (Map.Entry<String, Article> entry : hm.entrySet()) {
            String articleNr = entry.getKey();

            Article article = entry.getValue();

            TableRow row = new TableRow(this);

            // Erzeugen der Spalten
            TextView lagerSpalte = createTextView(String.valueOf(articleNr));
            TextView artikelSpalte = createTextView(String.valueOf(article.getQuantityOnStock()));
            Button annehmen_btn = createButton(articleNr);

            //Hinzufügen der Spalten
            row.addView(lagerSpalte);
            row.addView(artikelSpalte);
            row.addView(annehmen_btn);
            row = designRow(i, row);
            i++;
            table.addView(row);
        }
    }

    private TextView createTextView(String text) {
        TextView spalte = new TextView(this);
        spalte.setText(text);
        return spalte;
    }

    // Färbt die Zeilen der Tabelle ein
    private TableRow designRow(int i, TableRow row) {
        if (i % 2 == 0) {
            row.setBackgroundColor(0x50CCCCCC);
        } else {
            row.setBackgroundColor(0xAACCCCCC);
        }
        return row;
    }

    /**
     * Erstellt je einen Button pro Artikel zum Ändern der Menge.
     * @param i
     * @return
     */
    private Button createButton(final String i) {
        final Button aendernbutton = new Button(this);

        aendernbutton.setText(getResources().getString(R.string.stock_table_head_alter));
        aendernbutton.setBackgroundColor(Color.TRANSPARENT);

        if (i == "90000") {
            aendernbutton.setText("Test");
        }

        aendernbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                WarehouseApplication myApp = (WarehouseApplication) getApplication();
                Context context = view.getContext();
                Intent intent = new Intent(context, StockAmendment.class);
                intent.putExtra("id", i);
                intent.putExtra("storageLocation", myApp.getArticleMap().get(i).getStorageLocation().toString());
                intent.putExtra("quantity", String.valueOf(myApp.getArticleMap().get(i).getQuantityOnStock()));
                startActivity(intent);
                finish();
            }
        });
        return aendernbutton;
    }

    /**
     * Bei Doppelklick auf die Zurücktaste wird der Nutzer ausgelogt.
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();

            WarehouseApplication myApp = (WarehouseApplication) getApplication();

            LogoutTask logoutTask = new LogoutTask();
            logoutTask.execute(myApp.getEmployee().getSessionId());

            myApp.setOpenCommissionsMap(null);
            myApp.setPickerCommissionsMap(null);
            myApp.setEmployee(null);

            Helper.showToast(getResources().getString(R.string.toast_logout), getApplicationContext());
            return;
        }
        this.doubleBackToExitPressedOnce = true;

        Helper.showToast(getResources().getString(R.string.toast_exit), getApplicationContext());

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    /**
     * Erstellt das Menü zur Navigation für den Lageristen.
     */
    private void addDrawerItems() {
        final WarehouseApplication myApp = (WarehouseApplication) getApplication();

        String[] menuArray = {getResources().getString(R.string.drawer_commission), getResources().getString(R.string.drawer_commission_overview),
                getResources().getString(R.string.drawer_stock), getResources().getString(R.string.drawer_logout)};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuArray) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLUE);
                if (text.getText().toString().equals(getResources().getString(R.string.drawer_stock))) {
                    text.setTextColor(Color.parseColor("#BDBDBD"));
                }
                return view;
            }
        };

        mListLayout = (ListView) findViewById(R.id.navList);
        mListLayout.setAdapter(mAdapter);

        mListLayout.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                Intent newActivity0 = new Intent(getApplicationContext(), CommissioningOverview.class);
                                newActivity0.putExtra("screen", "myCommission");
                                startActivity(newActivity0);
                                finishActivity = true;
                                break;
                            case 1:
                                Intent newActivity1 = new Intent(getApplicationContext(), CommissioningOverview.class);
                                newActivity1.putExtra("screen", "commissionOverview");
                                startActivity(newActivity1);
                                finishActivity = true;
                                break;
                            case 2:
                                finishActivity = false;
                                break;
                            case 3:
                                LogoutTask logoutTask = new LogoutTask();
                                logoutTask.execute(myApp.getEmployee().getSessionId());

                                myApp.setOpenCommissionsMap(null);
                                myApp.setPickerCommissionsMap(null);
                                myApp.setEmployee(null);

                                Helper.showToast(getResources().getString(R.string.toast_logout), getApplicationContext());

                                Intent newActivity3 = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(newActivity3);
                                finishActivity = true;
                                break;
                        }
                        if (finishActivity) {
                            finish();
                        }
                    }
                }
        );
    }

    //Drawer Menu
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.drawer_title);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    //Drawer Menu
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    //Drawer Menu
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //Drawer Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }
}
