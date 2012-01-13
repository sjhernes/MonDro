package net.sjux.android.mondro;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.*;
import java.io.*;
import java.net.*;

public class MonDroActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	private EditText result;
	private Valuta v;
    private Spinner from;
    private Spinner to;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setContentView(R.layout.main);
    	

        View buttonListener = findViewById(R.id.button1);
        result = (EditText) findViewById(R.id.editText1);
        result.setText("");
        
        from = (Spinner) this.findViewById(R.id.spinner1);
        to = (Spinner) this.findViewById(R.id.spinner2);
        
        
        v = new Valuta(getApplicationContext());
        
        Currency[] spinnerlist = v.getCurrNameAndAbbrevs();
        
        if(spinnerlist.length > 0){
        
    	super.onCreate(savedInstanceState);
        ArrayAdapter<Currency> spinnerArrayAdapter = new ArrayAdapter<Currency>(this,
        		android.R.layout.simple_spinner_item, spinnerlist);
        
        from.setAdapter(spinnerArrayAdapter);
        to.setAdapter(spinnerArrayAdapter);
        
        buttonListener.setOnClickListener(this);

        }
    }
    
    /** 
     * Listener for the apps only button for using and changing values to textview3
     * 
     */
    public void onClick(View s){
    	
    	TextView curr = (TextView) findViewById(R.id.textView3);
    	
    	
    	Currency f = (Currency) from.getSelectedItem();
        Currency t = (Currency) to.getSelectedItem();
        
        int amount = (result.getText().length() == 0 ? 1 : Integer.parseInt(result.getText().toString()));
        
        Double l = (f == t ? 1.0 : v.calculateValue(f, t)) * amount;

        String res = new StringBuilder(String.format("%.2f %s", l, t.abbrev)).toString();
        
    	curr.setText(res);

    }
}

/**
Methods to fetch, store and calculate currencies based on NOK, from file over http  
*/

class Currency{
	
	String name, abbrev, price, number;

	/** Constructor for currency
	 * 	
	 * 	@param String[] composed of {currency_abbrev, currency_name, currency_price, currency_amount_for_price}
	 *  */
	Currency(String[] a) {
		abbrev = a[0];
		name = a[1];
		price = a[2];
		number = a[3];
	}
	
	/** 
	 * Returns a string[] for the currency in same format as constructor
	 * */
	String[] toArray(){
		String[] rtrn = {abbrev, name, price, number };
		return rtrn;
	}
	
	/** for pretty printing of the currency 
	 * */
	public String toString(){
		return "" + name + " (" + abbrev + ")";
	}
	
}

class Valuta{

  // Our values in memory
  Currency[] currencies;


  /** Constructor, gets file from http and creates currencies */
  Valuta(Context cont){
	  
		// split by line
		String []a = getfile("http://sps.rr-research.no/valuta/kurser.txt", cont).split("\n");
		currencies = new Currency[a.length+1];
		
		String[] nok = {"NOK", "Norske Kroner", "100", "100"};
		
		currencies[0] = new Currency(nok);
		
		System.out.println(currencies.length);
		

		
		// split to currencyValues
		for(int i = 1; i < currencies.length; i++){
			currencies[i] = new Currency(a[i-1].split(":"));
		}
		for (Currency c : currencies)
			System.out.println(c.name);
  }

  /** Calculate the price for a one to one exchange of to currencies
   * 
   * @param currency to trade from
   * @param currency to trade to
   * */
  double calculateValue(Currency f, Currency t){
	  
	  if (f == t) return 1.0;
	
	double fromPriceRelative = Double.valueOf(f.price) / Double.valueOf(f.number);
	double toPriceRelative = Double.valueOf(t.price) / Double.valueOf(t.number);
	
	return fromPriceRelative / toPriceRelative;
  }

  /**
   * @return all currencies
   */
  
  Currency[] getCurrNameAndAbbrevs(){
	  return currencies;
  }
  
  /**
   * Get textfile from an url and return it as a string and save it locally, 
   * if there is no internet connection return it from local storage
   */
  
  String getfile(String url, Context cont){

	  byte[] buffer = new byte[1024];
	  String rtrn = "";
	  final String FILENAME = "Mondro_backup_file";
	  
	  ConnectivityManager cm = (ConnectivityManager) cont.getSystemService(Context.CONNECTIVITY_SERVICE);
	  
	  if(cm.getActiveNetworkInfo() == null)

		  try {
			  FileInputStream fis = cont.openFileInput(FILENAME);
			  
			  while (fis.read(buffer) != -1)
				  rtrn += buffer.toString();
			  fis.close();
		  } catch (IOException fe){}

	  else
		  
		  try{
		  
			  HttpURLConnection in = null;

			  in = (HttpURLConnection) new URL(url).openConnection();
			  
			  in.setRequestMethod("GET");
			  in.setDoOutput(true);
			  in.setReadTimeout(10000);

			  in.connect();
		  
			  BufferedReader read = new BufferedReader(new InputStreamReader(in.getInputStream()));
		  
			  String tmp;
		  
			  while((tmp = read.readLine()) != null)
				  rtrn += new String(tmp+'\n');

			  FileOutputStream fos = cont.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			  fos.write(rtrn.getBytes());
			  fos.close();
		  } catch (Exception e){}
	  
	  return rtrn;
  }

}
