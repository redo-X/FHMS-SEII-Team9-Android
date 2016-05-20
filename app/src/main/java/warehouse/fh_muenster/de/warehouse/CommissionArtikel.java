package warehouse.fh_muenster.de.warehouse;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CommissionArtikel extends AppCompatActivity {
    int artikelZaehler = 1;
    int artikelGesamt = 8;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private Article article;

    private Commission commission = new Commission();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commission_artikel);

        int id = getIntent().getExtras().getInt("id");
        final Button weiter_btn = (Button) findViewById(R.id.weiter_btn);
        final Button fehlmenge_btn = (Button) findViewById(R.id.commission_artikel_fehlmengeMelden);

        //Erstelle DrawerMenu
        mDrawerList = (ListView) findViewById(R.id.navList);
        addDrawerItems();

        TextView ueberschrift = (TextView) findViewById(R.id.commission_id_label);
        TextView artikelanzahlLabel = (TextView) findViewById(R.id.commission_artikelAnzahl_label);


        WarehouseApplication myApp = (WarehouseApplication) getApplication();
        this.commission = myApp.getPickerCommissionById(id);
        artikelGesamt = commission.getArticleHashMap().size();
        //artikelGesamt = commission.getArticleArray().length;



        ueberschrift.setText("Kommission mit der Nummer: " + String.valueOf(id) + " ausgewählt\n");
        setTableRows();
        //artikelanzahlLabel.setText("Artikel " + artikelZaehler +  " von " + artikelGesamt);


        weiter_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Letzer Artikel, beenden der Activity und anzeigen von erfollgs meldung
                if(artikelZaehler > artikelGesamt){
                    showToast("Kommission erfolgreich abgeschlossen");
                    finish();
                }
                else {
                    // Noch mehere Artikel zu Kommissionieren
                    // Einlesen der zu Kommissionierten Menge
                    EditText kommissionierteMenge_txt = (EditText) findViewById(R.id.commission_artikel_artikel_commession_edit);
                    int kommissionierteMenge = 0;
                    try{
                        //Wenn gültige Menge eingegben kann nächster Artikel aufgerufen werden
                        kommissionierteMenge = Integer.valueOf(kommissionierteMenge_txt.getText().toString());
                        if(kommissionierteMenge != article.getQuantityOnCommit() ){
                            throw new Exception();
                        }
                            artikelZaehler++;
                            setTableRows();
                            kommissionierteMenge_txt.setText("");
                        //@TODO Prüfen ob kommissionierteMenge < zu kommissionierteMenge
                    }
                    catch (Exception e){
                        // Keine Eingabe oder zu große Eingabe
                        showToast("Eingegebene Menge ist nicht gültig");
                    }
                    // Wenn Letzter Artikel Button beschriftung ändern
                    if(artikelZaehler >= artikelGesamt){
                        weiter_btn.setText("Kommession abschließen");
                        artikelZaehler++;
                    }
                }
            }
        });

        fehlmenge_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Context context = view.getContext();
                Intent i = new Intent(context, StockOut.class);
                i.putExtra("id", article.getCode());
                startActivity(i);
            }
        });

    }



    /**
     * Hilfsmethode für DrawerMenu
     */
    private void addDrawerItems() {
        String[] MenuArray = {"Meine Kommissionen", "Offene Kommissionen", "LogOut"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, MenuArray);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String MenuArray = String.valueOf(parent.getItemAtPosition(position));
                        Toast.makeText(CommissionArtikel.this, MenuArray, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void setTableRows(){
        Article artikelArray[];
        artikelArray = mapToArray();
        article = artikelArray[artikelZaehler-1];
        // Setzen der neuen Überschrift
        TextView artikelanzahlLabel = (TextView) findViewById(R.id.commission_artikelAnzahl_label);
        artikelanzahlLabel.setText("Artikel " + artikelZaehler + " von " + artikelGesamt);
        // Setzen der ganzen Zeilen
        TextView artikelCode = (TextView) findViewById(R.id.commission_artikel_artikel_code);
        artikelCode.setText(article.getCode());

        TextView artikelName = (TextView) findViewById(R.id.commission_artikel_artikel_name);
        artikelName.setText(article.getName());

        TextView lagerort = (TextView) findViewById(R.id.commission_artikel_artikel_storage);
        String text = String.valueOf(article.getStorageLocation().getCode());
        lagerort.setText(text);

        TextView lagerbestand = (TextView) findViewById(R.id.commission_artikel_artikel_soll);
        text = String.valueOf(article.getQuantityOnStock());
        lagerbestand.setText(text);


        TextView kommissionsMenge = (TextView) findViewById(R.id.commission_artikel_artikel_commession);
        text = String.valueOf(article.getQuantityOnCommit());
        kommissionsMenge.setText(text);
    }

    private Article[] mapToArray(){
        Article articleArray[] = new Article[artikelGesamt];
        int i = 0;
        for(Map.Entry<Integer,Article> entry : commission.getArticleHashMap().entrySet()){
            articleArray[i] = entry.getValue();
            i++;
        }
        return articleArray;
    }

    private void showToast(String text){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    // Verhindern das durch zurück Button kommission abgebrochen wird
    @Override
    public void onBackPressed() {
        showToast("Kommission kann nicht abgebrochen werden");
    }
}
