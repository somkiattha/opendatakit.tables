package yoonsung.odk.spreadsheet.Activity.importexport;

import java.io.File;
import java.util.Map;
import yoonsung.odk.spreadsheet.Database.TableList;
import yoonsung.odk.spreadsheet.csvie.CSVException;
import yoonsung.odk.spreadsheet.csvie.CSVImporter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * An activity for importing CSV files to a table.
 */
public class ImportCSVActivity extends IETabActivity {
	
	/** view IDs (for use in testing) */
	public static int NTNVAL_ID = 1;
	public static int TABLESPIN_ID = 2;
	public static int FILENAMEVAL_ID = 3;
	public static int IMPORTBUTTON_ID = 4;
	
	/* the list of table names */
	private String[] tableNames;
	/* the view for inputting the new table name (label and text field) */
	private View newTableViews;
	/* the text field for getting the new table name */
	private EditText ntnValField;
	/* the table name spinner */
	private Spinner tableSpin;
	/* the text field for getting the filename */
	private EditText filenameValField;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getView());
	}
	
	/**
	 * @return the view
	 */
	private View getView() {
		LinearLayout v = new LinearLayout(this);
		v.setOrientation(LinearLayout.VERTICAL);
		// adding the table spinner
		tableSpin = new Spinner(this);
		tableSpin.setId(TABLESPIN_ID);
		Map<String, String> tableMap = (new TableList()).getTableList();
		tableNames = new String[tableMap.size() + 1];
		tableNames[0] = "New Table";
		int counter = 1;
		for(String tableId : tableMap.keySet()) {
			tableNames[counter] = tableMap.get(tableId);
			counter++;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, tableNames);
		adapter.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		tableSpin.setAdapter(adapter);
		tableSpin.setSelection(0);
		tableSpin.setOnItemSelectedListener(new tableSpinListener());
		v.addView(tableSpin);
		// adding the new table name field
		LinearLayout ntn = new LinearLayout(this);
		ntn.setOrientation(LinearLayout.VERTICAL);
		TextView ntnLabel = new TextView(this);
		ntnLabel.setText("New Table Name:");
		ntn.addView(ntnLabel);
		ntnValField = new EditText(this);
		ntnValField.setId(NTNVAL_ID);
		ntnValField.setText("New Table");
		ntn.addView(ntnValField);
		newTableViews = ntn;
		v.addView(newTableViews);
		// adding the filename field
		LinearLayout fn = new LinearLayout(this);
		fn.setOrientation(LinearLayout.VERTICAL);
		TextView fnLabel = new TextView(this);
		fnLabel.setText("Filename:");
		fn.addView(fnLabel);
		filenameValField = new EditText(this);
		filenameValField.setId(FILENAMEVAL_ID);
		fn.addView(filenameValField);
		v.addView(fn);
		// adding the import button
		Button button = new Button(this);
		button.setId(IMPORTBUTTON_ID);
		button.setText("Import");
		button.setOnClickListener(new ButtonListener());
		v.addView(button);
		// wrapping in a scroll view
		ScrollView scroll = new ScrollView(this);
		scroll.addView(v);
		return scroll;
	}
	
	/**
	 * Attempts to import a CSV file.
	 */
	private void importSubmission() {
		String filename = filenameValField.getText().toString();
		File root = Environment.getExternalStorageDirectory();
		File file = new File(root.getPath() +
				"/data/data/yoonsung.odk.spreadsheet/files/" + filename);
		String tableName;
		int pos = tableSpin.getSelectedItemPosition();
		TableList tList = new TableList();
		if(pos == 0) {
			tableName = ntnValField.getText().toString();
		} else {
			tableName = tableNames[pos];
			if(!tList.isTableExist(tableName)) {
				notifyOfError("Table does not exist.");
				return;
			}
		}
		Handler iHandler = new ImporterHandler();
		ImporterThread iThread = new ImporterThread(iHandler, tableName, file,
				(pos == 0));
		showDialog(IMPORT_IN_PROGRESS_DIALOG);
		iThread.start();
	}
	
	/**
	 * A listener for the table name spinner. Adds or removes the "New Table"
	 * name field as necessary.
	 */
	private class tableSpinListener
			implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			if(position == 0) {
				newTableViews.setVisibility(View.VISIBLE);
			} else {
				newTableViews.setVisibility(View.GONE);
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}
	
	/**
	 * A listener for the import button. Calls importSubmission() on click.
	 */
	private class ButtonListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			importSubmission();
		}
	}
	
	private class ImporterThread extends Thread {
		
		private Handler mHandler;
		private String tableName;
		private File file;
		private boolean createTable;
		
		ImporterThread(Handler h, String tableName, File file,
				boolean createTable) {
			mHandler = h;
			this.tableName = tableName;
			this.file = file;
			this.createTable = createTable;
		}
		
		public void run() {
			CSVImporter importer = new CSVImporter();
			boolean success = true;
			String errorMsg = null;
			try {
				if(createTable) {
					importer.buildTable(tableName, file);
				} else {
					importer.importTable(tableName, file);
				}
			} catch(CSVException e) {
				success = false;
				errorMsg = e.getMessage();
			}
			Bundle b = new Bundle();
			b.putBoolean("success", success);
			if(!success) {
				b.putString("errmsg", errorMsg);
			}
			Message msg = mHandler.obtainMessage();
			msg.setData(b);
			mHandler.sendMessage(msg);
		}
		
	}
	
	private class ImporterHandler extends Handler {
		
		public void handleMessage(Message msg) {
			dismissDialog(IMPORT_IN_PROGRESS_DIALOG);
			Bundle b = msg.getData();
			if(b.getBoolean("success")) {
				showDialog(CSVIMPORT_SUCCESS_DIALOG);
			} else {
				notifyOfError("Error importing: " + b.getString("errorMsg"));
			}
		}
		
	}
	
}
