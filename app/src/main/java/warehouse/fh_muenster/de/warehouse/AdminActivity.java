package warehouse.fh_muenster.de.warehouse;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import warehouse.fh_muenster.de.warehouse.Server.Config;
import warehouse.fh_muenster.de.warehouse.Server.Server;
import warehouse.fh_muenster.de.warehouse.Server.ServerMockImple;

public class AdminActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;

    /**
     * Liefert das Ergebnis vom Scanner
     * @param requestCode gescannter Code
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                EditText articleCodeTxt = (EditText) findViewById(R.id.admin_article_code);
                String code = data.getExtras().getString("code");
                articleCodeTxt.setText(code);
                Scanner.setRun(0);
            }
            Scanner.run = 0;
        }
        Scanner.run = 0;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        final Button article_save = (Button) findViewById(R.id.admin_article_save);
        final ImageButton scanner = (ImageButton) findViewById(R.id.admin_article_scann);

        article_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Einlesen der Werte
                EditText articleCodeTxt = (EditText) findViewById(R.id.admin_article_code);
                String articleCode = articleCodeTxt.getText().toString();
                EditText articleNameTxt = (EditText) findViewById(R.id.admin_article_name);
                String articleName = articleNameTxt.getText().toString();
                EditText articleLagerortTxt = (EditText) findViewById(R.id.admin_article_lagerort);
                String articleLagerort = articleLagerortTxt.getText().toString();
                // Speichern des Artikels
                SaveArticleTask saveArticleTask = new SaveArticleTask();
                saveArticleTask.execute(articleCode, articleName, articleLagerort);
            }

        });
        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Starten des Scanners
                Intent i = new Intent(getApplicationContext(), Scanner.class);
                startActivityForResult(i, 1);
            }

        });
    }


    private class SaveArticleTask extends AsyncTask<String, Integer, Boolean> {

        /**
         * Speichert einen neuen Artikel auf dem Server
         * @param params 3 Strings, 1: Artikelcode, 2: Artikelname, 3: Lagerort
         * @return Liefert im Erfolgsfall true, sonst false
         */
        @Override
        protected Boolean doInBackground(String... params) {

            if (params.length == 3) {
                String code = params[0];
                String name = params[1];
                String lagerort = params[2];
                boolean result = false;
                WarehouseApplication myApp = (WarehouseApplication) getApplication();
                if (Config.isMock()) {
                    ServerMockImple server = new ServerMockImple();
                    result = server.createArticle(myApp.getEmployee().getSessionId(), code, name, lagerort);
                } else {
                    Server server = new Server();
                    result = server.createArticle(myApp.getEmployee().getSessionId(), code, name, lagerort);
                }
                return result;

            } else {
                return false;
            }
        }

        /**
         * Zeigt einen Toast abhängig vom Result an
         * @param result
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if (result == true) {
                Helper.showToast("Artikle erfolgreich gespeichert!", getApplicationContext());
                removeInput();
            } else {
                Helper.showToast("Fehler Artikle konnte nicht gespeichert werden!", getApplicationContext());
            }
        }

        /**
         * Setzt die Eingabefelder leer
         */
        private void removeInput(){
            EditText articleCodeTxt = (EditText) findViewById(R.id.admin_article_code);
            EditText articleNameTxt = (EditText) findViewById(R.id.admin_article_name);
            EditText articleLagerortTxt = (EditText) findViewById(R.id.admin_article_lagerort);
            articleCodeTxt.setText("");
            articleNameTxt.setText("");
            articleLagerortTxt.setText("");
        }

    }

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

}
