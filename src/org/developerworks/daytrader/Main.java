package org.developerworks.daytrader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.developerworks.stocks.Stocks;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.ContentHandler;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import static android.view.Menu.NONE;

public class Main extends ListActivity {
	
	private static final int XML = 0;
	private static final int JSON = 1;
	private static final int PROTOBUF = 2;
	private int mode = XML; // default
	
	private Vector<Stock> recentStocks;
	private MyCursorAdapter myAdapter;
	private MyDbAdapter mDbHelper;
	private Cursor mainC;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.main);
        final EditText input = (EditText) findViewById(R.id.symbol);
        final TextView symbolsList = (TextView) findViewById(R.id.symList);
        final Button addButton = (Button) findViewById(R.id.addBtn);
        final Button dlButton = (Button) findViewById(R.id.dlBtn);
        //added buttons
        final Button refreshButton = (Button) findViewById(R.id.refreshBtn);
        final Button recentButton = (Button) findViewById(R.id.localBtn);
        addButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				String newSymbol = input.getText().toString();
				if (symbolsList.getText() == null || 
						symbolsList.getText().length() == 0){
					symbolsList.setText(newSymbol);
				} else {
					StringBuilder sb = new StringBuilder(symbolsList.getText());
					sb.append(",");
					sb.append(newSymbol);
					symbolsList.setText(sb.toString());
				}
				input.setText("");
			}
        });
        dlButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				String symList = symbolsList.getText().toString();
				String[] symbols = symList.split(",");
				symbolsList.setText("");
				switch (mode){
				case JSON :
					new StockJsonParser().execute(symbols);
					break;
				case PROTOBUF :
					new StockProtoBufParser().execute(symbols);
					break;
				default :
					new StockXmlParser().execute(symbols);
					break;
				}
			}
        });
    
        refreshButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//TODO refresh (download current from server)
				String[] s1 = { "dasd", "fdsfs", "rewrwet" };

				ArrayAdapter<String> adapter = 
					new ArrayAdapter<String>(Main.this, R.layout.stock, s1 );
				setListAdapter(adapter);
			}
        });
        recentButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//TODO recent (get from DB)
				Toast toast = Toast.makeText(getApplicationContext(), 
						"Recent Click", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL,0, 0);
				toast.show();
				
				mainC = mDbHelper.fetchAllData();
				//Cursor cr=null;
				myAdapter=new MyCursorAdapter(Main.this, mainC);
				setListAdapter(myAdapter);
				//myAdapter.notifyDataSetChanged();
		        
			}
        });
        
        mDbHelper = new MyDbAdapter(this);
        mDbHelper.open();
        
        mDbHelper.createStackRow("rak", "abcd","21");   
        mDbHelper.createStackRow("qqq", "wwwwww","33");   
    }

    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.addSubMenu(NONE, XML, NONE, "Use XML");
		menu.addSubMenu(NONE, JSON, NONE, "Use JSON");
		menu.addSubMenu(NONE, PROTOBUF, NONE, "Use ProtoBuf");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mode = item.getItemId();
		return true;
	}

	abstract class BaseStockParser extends AsyncTask<String, Integer, Stock[]>{
		String urlStr = "http://protostocks.appspot.com/stockbroker?format=";
		protected BaseStockParser(String format){
			urlStr += format;
		}
		
		private String makeUrlString(String... symbols) {
			StringBuilder sb = new StringBuilder(urlStr);
			for (int i=0;i<symbols.length;i++){
				sb.append("&stock=");
				sb.append(symbols[i]);
			}
			return sb.toString();
		}
		
		protected InputStream getData(String[] symbols) throws Exception{
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(new URI(makeUrlString(symbols)));
			request.addHeader("Accept-Encoding","gzip");
			HttpResponse response = client.execute(request);
			InputStream content = response.getEntity().getContent();
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")){
				content = new GZIPInputStream(content);
			}
			return content;
		}
		
		@Override
		protected void onPostExecute(Stock[] stocks){
			//for(int)
			ArrayAdapter<Stock> adapter = 
				new ArrayAdapter<Stock>(Main.this, R.layout.stock, stocks );
			setListAdapter(adapter);
		}	
	}

	private class StockProtoBufParser extends BaseStockParser{
		public StockProtoBufParser(){
			super("protobuf");
		}
	
		@Override
		protected Stock[] doInBackground(String... symbols) {
			Stock[] stocks = new Stock[symbols.length];
			try{
				Stocks.Portfolio portfolio = 
					Stocks.Portfolio.parseFrom(getData(symbols));
				for (int i=0;i<symbols.length;i++){
					stocks[i] = Stock.fromQuote(portfolio.getQuote(i));
				}
			} catch (Exception e){
				Log.e("DayTrader", "Exception getting ProtocolBuffer data", e);
			}
			return stocks;
		}
	}
	
	private class StockJsonParser extends BaseStockParser{
		public StockJsonParser(){
			super("json");
		}
		@Override
		protected Stock[] doInBackground(String... symbols) {
			Stock[] stocks = new Stock[symbols.length];
			try{
				StringBuilder json = new StringBuilder();
				BufferedReader reader = 
					new BufferedReader(new InputStreamReader(getData(symbols)));
				String line = reader.readLine();
				while (line != null){
					json.append(line);
					line = reader.readLine();
				}
				JSONObject jsonObj = new JSONObject(json.toString());
				JSONArray stockArray = jsonObj.getJSONArray("stocks");
				for (int i=0;i<stocks.length;i++){
					JSONObject object = 
						stockArray.getJSONObject(i).getJSONObject("stock");
					stocks[i] = new Stock(object.getString("symbol"), 
							object.getString("name"), 
							object.getDouble("price"));
				}
			} catch (Exception e){
				Log.e("DayTrader", "Exception getting JSON data", e);
			}
			return stocks;
		}
	}
	
	private class StockXmlParser extends BaseStockParser{
		public StockXmlParser(){
			super("xml");
		}
		
		@Override
		protected Stock[] doInBackground(String... symbols) {
			ArrayList<Stock> stocks = new ArrayList<Stock>(symbols.length);
			try{	
				ContentHandler handler = newHandler(stocks);
				Xml.parse(getData(symbols), Xml.Encoding.UTF_8, handler);
			} catch (Exception e){
				Log.e("DayTrader", "Exception getting XML data", e);
			}
			Stock[] array = new Stock[symbols.length];
			return stocks.toArray(array);
		}
		
		private ContentHandler newHandler(final ArrayList<Stock> stocks){
			RootElement root = new RootElement("stocks");
			Element stock = root.getChild("stock");
			final Stock currentStock = new Stock();
			stock.setEndElementListener(
				new EndElementListener(){
					public void end() {
						stocks.add((Stock) currentStock.clone());
					}
				}
			);
			stock.getChild("name").setEndTextElementListener(
				new EndTextElementListener(){
	    			public void end(String body) {
	    				currentStock.setName(body);
	    			}
	    		}
			);
			stock.getChild("symbol").setEndTextElementListener(
				new EndTextElementListener(){
					public void end(String body) {
						currentStock.setSymbol(body);
					}
				}
			);		
			stock.getChild("price").setEndTextElementListener(
				new EndTextElementListener(){
					public void end(String body) {
						currentStock.setPrice(Double.parseDouble(body));
					}
				}
			);		
			return root.getContentHandler();
		}
	}
}