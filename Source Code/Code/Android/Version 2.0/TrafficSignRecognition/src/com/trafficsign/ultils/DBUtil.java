package com.trafficsign.ultils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.sql.Date;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.reflect.TypeToken;
import com.trafficsign.activity.MainActivity;
import com.trafficsign.activity.R;
import com.trafficsign.json.CategoryJSON;
import com.trafficsign.json.FavoriteJSON;
import com.trafficsign.json.ResultDB;
import com.trafficsign.json.ResultInput;
import com.trafficsign.json.ResultJSON;
import com.trafficsign.json.ResultShortJSON;
import com.trafficsign.json.TrafficInfoJSON;
import com.trafficsign.json.TrafficInfoShortJSON;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class DBUtil {
	// Constant
	public static final int ACTIVE = 1;
	public static final int DEACTIVE = 0;
	public static final int NOT_EXIST = 2;

	// create folder for contain DB and inamge if not exist
	public static void initResource(InputStream dbIS, InputStream settingIS,
			Context ctx) {
		SharedPreferences sharedPreferences = ctx.getSharedPreferences(
				Properties.SHARE_PREFERENCE_SETTING, 0);
		String externalPath = Environment.getExternalStorageDirectory()
				.getPath() + "/";
		GlobalValue.initAppFolder(externalPath);

		File appFolder = new File(GlobalValue.getAppFolder());
		if (!appFolder.exists()) {
			appFolder.mkdir();
		}

		File saveImageFolder = new File(GlobalValue.getAppFolder()
				+ Properties.SAVE_IMAGE_FOLDER);
		if (!saveImageFolder.exists()) {
			saveImageFolder.mkdir();
		}

		File mainImageFolder = new File(GlobalValue.getAppFolder()
				+ Properties.MAIN_IMAGE_FOLDER);
		if (!mainImageFolder.exists()) {
			mainImageFolder.mkdir();
		}

		File dbFile = new File(GlobalValue.getAppFolder()
				+ Properties.DB_FILE_PATH);
		if (!dbFile.exists()) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(Properties.SHARE_PREFERENCE__KEY_TRAFFIC_SYNC, "");
			editor.commit();
			DBUtil.copyDB(dbIS, dbFile);
		}

		File settingFile = new File(GlobalValue.getAppFolder()
				+ Properties.SETTING_FILE_PATH);
		if (!settingFile.exists()) {
			DBUtil.copyDB(settingIS, settingFile);
		}

		try {
			GlobalValue.createInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if ("".equals(sharedPreferences.getString(
				Properties.SHARE_PREFERENCE__KEY_TRAFFIC_SYNC, ""))) {
			HttpDatabaseUtil httpDB = new HttpDatabaseUtil(ctx);
			httpDB.execute();
		}

	}

	// coppy file from res to folder in sdCard
	public static void copyDB(InputStream in, File dst) {
		try {
			OutputStream out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// check if category is exist
	public static boolean checkCategory(String catID) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		try {
			Cursor cursor = db.query("category", null, " `categoryID` LIKE '"
					+ catID + "'", null, null, null, null);
			if (cursor.moveToFirst()) {
				return true;
			}
			return false;
		} finally {
			db.close();
		}

	}

	// insert Category
	public static long insertCategory(CategoryJSON categoryJSON) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		ContentValues values = new ContentValues();
		values.put("categoryID", categoryJSON.getCategoryID());
		values.put("categoryName", categoryJSON.getCategoryName());
		Long dbReturn = db.insert("category", null, values);
		db.close();
		return dbReturn;
	}

	// check if traffic sign is exist
	public static boolean checkTraffic(String trafficID) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		try {
			Cursor cursor = db.query("trafficinformation", null,
					" `trafficID` LIKE '" + trafficID + "'", null, null, null,
					null);
			if (cursor.moveToFirst()) {
				db.close();
				return true;
			}
			db.close();
			return false;
		} finally {
			db.close();
		}

	}

	// insert traffic sign
	public static long insertTraffic(TrafficInfoJSON trafficInfoJSON) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		ContentValues values = new ContentValues();
		values.put("trafficID", trafficInfoJSON.getTrafficID());
		values.put("name", trafficInfoJSON.getName());
		values.put("image", trafficInfoJSON.getImage());
		values.put("categoryID", trafficInfoJSON.getCategoryID());
		values.put("information", trafficInfoJSON.getInformation());
		values.put("penaltyfee", trafficInfoJSON.getPenaltyfee());
		// insert to DB
		Long dbReturn = db.insert("trafficinformation", null, values);
		db.close();
		return dbReturn;
	}

	// update traffic sign
	public static int updateTraffic(TrafficInfoJSON trafficInfoJSON) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		try {
			ContentValues values = new ContentValues();
			values.put("name", trafficInfoJSON.getName());
			values.put("image", trafficInfoJSON.getImage());
			values.put("categoryID", trafficInfoJSON.getCategoryID());
			values.put("information", trafficInfoJSON.getInformation());
			values.put("penaltyfee", trafficInfoJSON.getPenaltyfee());
			// insert to DB
			int dbReturn = db.update("trafficinformation", values,
					" `trafficID` LIKE '" + trafficInfoJSON.getTrafficID()
							+ "'", null);
			return dbReturn;
		} finally {
			db.close();
		}

	}

	// get all category
	public static ArrayList<CategoryJSON> getAllCategory() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result (select * from "category")
		Cursor cursor = db
				.query("category", null, null, null, null, null, null);
		cursor.moveToFirst();
		ArrayList<CategoryJSON> listCategory = new ArrayList<CategoryJSON>();
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			// loop for get all category to list
			do {
				CategoryJSON temp = new CategoryJSON();
				temp.setCategoryID(cursor.getString(cursor
						.getColumnIndexOrThrow("categoryID")));
				temp.setCategoryName(cursor.getString(cursor
						.getColumnIndexOrThrow("categoryName")));
				listCategory.add(temp);
			} while (cursor.moveToNext());
		}

		db.close();
		return listCategory;

	}

	// get listTraffic by categoryID return arrayList trafficInfoShort
	public static ArrayList<TrafficInfoShortJSON> getTrafficByCategory(
			String categoryID) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result (SELECT * FROM
		// `trafficinformation` WHERE `categoryID` = ...)
		Cursor cursor = db.query("trafficinformation", null, " `categoryID` ="
				+ categoryID, null, null, null, null);
		ArrayList<TrafficInfoShortJSON> listTrafficInfoShortJSONs = new ArrayList<TrafficInfoShortJSON>();
		String imagePath = "";
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			do {

				TrafficInfoShortJSON temp = new TrafficInfoShortJSON();
				temp.setCategoryID(Integer.parseInt(cursor.getString(cursor
						.getColumnIndexOrThrow("categoryID"))));
				temp.setName(cursor.getString(cursor
						.getColumnIndexOrThrow("name")));
				temp.setTrafficID(cursor.getString(cursor
						.getColumnIndexOrThrow("trafficID")));
				/*
				 * get image Address by get path in properties class and plus
				 * with trafficID
				 */
				imagePath = GlobalValue.getAppFolder()
						+ Properties.MAIN_IMAGE_FOLDER + temp.getTrafficID()
						+ ".jpg";
				temp.setImage(imagePath);
				// add temp to list traffic
				listTrafficInfoShortJSONs.add(temp);
			} while (cursor.moveToNext());
		}

		db.close();
		return listTrafficInfoShortJSONs;
	}

	// get listTraffic by searchKey return arrayList trafficInfoShort
	public static ArrayList<TrafficInfoShortJSON> getTrafficByName(
			String searchKey) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result (SELECT * FROM
		// `trafficinformation` WHERE `name` LIKE '%...%')
		Cursor cursor = db.query("trafficinformation", null, " `name` LIKE '%"
				+ searchKey + "%'", null, null, null, null);
		ArrayList<TrafficInfoShortJSON> listTrafficInfoShortJSONs = new ArrayList<TrafficInfoShortJSON>();
		String imagePath = "";
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			do {

				TrafficInfoShortJSON temp = new TrafficInfoShortJSON();
				temp.setCategoryID(Integer.parseInt(cursor.getString(cursor
						.getColumnIndexOrThrow("categoryID"))));
				temp.setName(cursor.getString(cursor
						.getColumnIndexOrThrow("name")));
				temp.setTrafficID(cursor.getString(cursor
						.getColumnIndexOrThrow("trafficID")));
				/*
				 * get image Address by get path in properties class and plus
				 * with trafficID
				 */
				imagePath = GlobalValue.getAppFolder()
						+ Properties.MAIN_IMAGE_FOLDER + temp.getTrafficID()
						+ ".jpg";
				temp.setImage(imagePath);
				// add temp to list traffic
				listTrafficInfoShortJSONs.add(temp);
			} while (cursor.moveToNext());
		}

		db.close();
		return listTrafficInfoShortJSONs;
	}

	// get traffic details by traffic ID
	public static TrafficInfoJSON getTrafficDetail(String trafficID) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result (SELECT * FROM
		// `trafficinformation` WHERE `name` LIKE '%...%')
		Cursor cursor = db
				.query("trafficinformation", null, " `trafficID` LIKE '"
						+ trafficID + "'", null, null, null, null);
		TrafficInfoJSON trafficInfoJSON = new TrafficInfoJSON();
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			// get traffic details from cursor to Object
			trafficInfoJSON.setTrafficID(cursor.getString(cursor
					.getColumnIndexOrThrow("trafficID")));
			trafficInfoJSON.setName(cursor.getString(cursor
					.getColumnIndexOrThrow("name")));
			trafficInfoJSON.setCategoryID(Integer.parseInt(cursor
					.getString(cursor.getColumnIndexOrThrow("categoryID"))));
			trafficInfoJSON.setInformation(cursor.getString(cursor
					.getColumnIndexOrThrow("information")));
			trafficInfoJSON.setPenaltyfee(cursor.getString(cursor
					.getColumnIndexOrThrow("penaltyfee")));
			// set image Path
			String imagePath = GlobalValue.getAppFolder()
					+ Properties.MAIN_IMAGE_FOLDER
					+ trafficInfoJSON.getTrafficID() + ".jpg";
			trafficInfoJSON.setImage(imagePath);
		}
		db.close();
		return trafficInfoJSON;
	}

	// insert result to result table
	public static boolean addResult(ResultDB resultDB, String user) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// set values to insert
		ContentValues values = new ContentValues();
		values.put("resultID", resultDB.getResultID());
		values.put("uploadedImage", resultDB.getUploadedImage());
		values.put("listTraffic", resultDB.getLocate());
		values.put("creator", user);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		values.put("createDate", dateFormat.format(resultDB.getCreateDate()));
		values.put("isActive", true);
		if (db.insert("result", null, values) != -1) {
			db.close();
			return true;
		}
		db.close();
		return false;
	}

	// insert to result table for autosearch later
	public static boolean saveSearchInfo(String picturePath, String locateJSON,
			String user) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// set values to insert
		ContentValues values = new ContentValues();
		values.put("resultID", -1);
		values.put("uploadedImage", picturePath);
		values.put("listTraffic", locateJSON);
		values.put("creator", user);
		// get current datetime
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		Calendar cal = Calendar.getInstance();
		values.put("createDate", dateFormat.format(cal.getTime()));
		values.put("isActive", false);
		if (db.insert("result", null, values) != -1) {
			db.close();
			return true;
		}
		db.close();
		return false;
	}

	// get history from result table by ID
	public static ResultJSON viewHistory(int resultID) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result
		Cursor cursor = db.query("result", null, "`resultID` = " + resultID,
				null, null, null, null);
		ResultJSON output = new ResultJSON();
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			Type type = new TypeToken<ArrayList<ResultInput>>() {
			}.getType();
			// get result from cursor to Object
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.getDefault());
			int ID = cursor.getInt(cursor.getColumnIndexOrThrow("resultID"));
			output.setResultID(cursor.getInt(cursor
					.getColumnIndexOrThrow("resultID")));
			output.setUploadedImage(cursor.getString(cursor
					.getColumnIndexOrThrow("uploadedImage")));
			String jsonLocate = cursor.getString(cursor
					.getColumnIndexOrThrow("listTraffic"));
			output.setCreator(cursor.getString(cursor
					.getColumnIndexOrThrow("creator")));
			String tempDateString = cursor.getString(cursor
					.getColumnIndexOrThrow("createDate"));
			ArrayList<ResultInput> listResultInput = Helper.fromJson(
					jsonLocate, type);
			output.setListTraffic(listResultInput);
			Date tempDate = null;
			try {
				tempDate = new java.sql.Date(dateFormat.parse(tempDateString)
						.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			output.setCreateDate(tempDate);

		}
		db.close();
		return output;
	}

	// get autoSerch from result table for upload and excute search
	public static ArrayList<ResultDB> getSavedSearch() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result
		Cursor cursor = db.query("result", null, "`resultID` = -1", null, null,
				null, null);
		ArrayList<ResultDB> listResult = new ArrayList<ResultDB>();
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			// get result from cursor to Object
			do {
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

				ResultDB temp = new ResultDB();
				temp.setUploadedImage(cursor.getString(cursor
						.getColumnIndexOrThrow("uploadedImage")));
				temp.setLocate(cursor.getString(cursor
						.getColumnIndexOrThrow("listTraffic")));
				temp.setCreator(cursor.getString(cursor
						.getColumnIndexOrThrow("creator")));
				String tempDateString = cursor.getString(cursor
						.getColumnIndexOrThrow("createDate"));
				Date tempDate = null;
				try {
					tempDate = new java.sql.Date(dateFormat.parse(
							tempDateString).getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				temp.setCreateDate(tempDate);
				listResult.add(temp);
			} while (cursor.moveToNext());

		}
		db.close();
		return listResult;
	}

	// get history result table where isActive = true
	public static ArrayList<ResultShortJSON> listResult() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result
		Cursor cursor = db.query("result", null, "`isActive` = 1", null, null,
				null, null);
		ArrayList<ResultShortJSON> listResult = new ArrayList<ResultShortJSON>();
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			// get result from cursor to Object
			do {
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

				ResultShortJSON temp = new ResultShortJSON();
				temp.setResultID(cursor.getInt(cursor
						.getColumnIndexOrThrow("resultID")));
				String tempDateString = cursor.getString(cursor
						.getColumnIndexOrThrow("createDate"));
				Date tempDate = null;
				try {
					tempDate = new java.sql.Date(dateFormat.parse(
							tempDateString).getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				temp.setCreateDate(tempDate);
				listResult.add(temp);
			} while (cursor.moveToNext());

		}
		db.close();
		return listResult;
	}

	// get autoSerch from result table for upload and excute search
	public static ArrayList<ResultDB> listAllHistory() {
		ArrayList<ResultDB> listResult = new ArrayList<ResultDB>();
		
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result
		Cursor cursor = db.query("result", null, "`resultID` > 0", null, null,
				null, null);		
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			// get result from cursor to Object
			do {
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss", Locale.getDefault());

				ResultDB temp = new ResultDB();
				temp.setResultID(cursor.getInt(cursor
						.getColumnIndexOrThrow("resultID")));
				temp.setUploadedImage(cursor.getString(cursor
						.getColumnIndexOrThrow("uploadedImage")));
				temp.setLocate(cursor.getString(cursor
						.getColumnIndexOrThrow("listTraffic")));
				temp.setCreator(cursor.getString(cursor
						.getColumnIndexOrThrow("creator")));
				temp.setActive(true);
				int active = cursor.getInt(cursor
						.getColumnIndexOrThrow("isActive"));
				if (active == 0) {
					temp.setActive(false);
				}
				String tempDateString = cursor.getString(cursor
						.getColumnIndexOrThrow("createDate"));
				Date tempDate = null;
				try {
					tempDate = new java.sql.Date(dateFormat.parse(
							tempDateString).getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				temp.setCreateDate(tempDate);
				listResult.add(temp);
			} while (cursor.moveToNext());

		}
		db.close();
		return listResult;
	}

	// delete a row in result table
	public static int deleteSavedResult(String imagePath) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		int output = db.delete("result", "uploadedImage = ?",
				new String[] { imagePath });
		db.close();
		return output;
	}

	// delete all in result table
	public static int deleteAllResult() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		int output = db.delete("result", null, null);
		db.close();
		return output;
	}

	// delete result by resultID
	public static int deleteResult(int resultID) {
		ResultJSON resultJSON = new ResultJSON();
		resultJSON = DBUtil.viewHistory(resultID);
		if (resultJSON != null) {
			SQLiteDatabase db = SQLiteDatabase.openDatabase(
					GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
					SQLiteDatabase.OPEN_READWRITE);
			int output = db.delete("result", "`resultID` =" + resultID, null);
			db.close();
			if (output > 0) {
				String imageLocalPath = resultJSON.getUploadedImage();
				File imageFile = new File(imageLocalPath);
				try {
					imageFile.delete();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return output;
		}
		return 0;
	}

	// deactive result by resultID
	public static int deActivateResult(int resultID) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		ContentValues value = new ContentValues();
		value.put("isActive", false);
		int output = db
				.update("result", value, "`resultID` =" + resultID, null);
		db.close();
		return output;
	}

	// remove all in favorite table
	public static int removeAllFavorite() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		int output = db.delete("favorite", null, null);
		db.close();
		return output;
	}

	// deactivate favorite table by trafficID
	public static int deActivateFavorite(String trafficID) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		ContentValues value = new ContentValues();
		value.put("isActive", false);
		// get current datetime
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		Calendar cal = Calendar.getInstance();
		value.put("modifyDate", dateFormat.format(cal.getTime()));
		int output = db.update("favorite", value, "`trafficID` = '" + trafficID
				+ "'", null);
		db.close();
		return output;
	}

	// activate favorite table by trafficID
	public static int activateFavorite(String trafficID) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		ContentValues value = new ContentValues();
		value.put("isActive", true);
		// get current datetime
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		Calendar cal = Calendar.getInstance();
		value.put("modifyDate", dateFormat.format(cal.getTime()));
		int output = db.update("favorite", value, "`trafficID` = '" + trafficID
				+ "'", null);
		db.close();
		return output;
	}

	/*
	 * Add favorite Using trafficInfoShort, instead of FavoriteJSON because they
	 * are the same, moreover using trafficInfoShort can reuse module
	 * listTrafficArrayAdapter
	 */
	public static boolean addFavorite(TrafficInfoShortJSON input, String creator) {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		// set image Path
		String imagePath = GlobalValue.getAppFolder()
				+ Properties.MAIN_IMAGE_FOLDER + input.getTrafficID() + ".jpg";
		// set values to insert
		ContentValues values = new ContentValues();
		values.put("trafficID", input.getTrafficID());
		values.put("creator", creator);
		values.put("trafficName", input.getName());
		values.put("createDate", dateFormat.format(input.getModifyDate()));
		values.put("modifyDate", dateFormat.format(input.getModifyDate()));
		values.put("isActive", true);
		values.put("image", imagePath);
		// commit insert
		if (db.insert("favorite", null, values) != -1) {
			db.close();
			return true;
		}
		db.close();
		return false;
	}

	// get all favorite isActive = true
	public static ArrayList<TrafficInfoShortJSON> listFavorite() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		// create cursor to access query result
		Cursor cursor = db.query("favorite", null, "`isActive` = 1", null,
				null, null, null);
		ArrayList<TrafficInfoShortJSON> output = new ArrayList<TrafficInfoShortJSON>();
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			do {
				try {
					String tempDate = "";
					TrafficInfoShortJSON temp = new TrafficInfoShortJSON();
					temp.setName(cursor.getString(cursor
							.getColumnIndexOrThrow("trafficName")));
					temp.setTrafficID(cursor.getString(cursor
							.getColumnIndexOrThrow("trafficID")));
					temp.setImage(cursor.getString(cursor
							.getColumnIndexOrThrow("image")));
					tempDate = cursor.getString(cursor
							.getColumnIndexOrThrow("modifyDate"));
					Date modifyDate;

					modifyDate = new java.sql.Date(dateFormat.parse(tempDate)
							.getTime());

					temp.setModifyDate(modifyDate);
					// add temp to list favorite
					output.add(temp);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (cursor.moveToNext());
		}

		db.close();
		return output;
	}

	// get all favorite
	public static ArrayList<FavoriteJSON> listAllFavorite() {
		ArrayList<FavoriteJSON> listResult = new ArrayList<FavoriteJSON>();
		
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		// create cursor to access query result
		Cursor cursor = db
				.query("favorite", null, null, null, null, null, null);		
		// move cursor to first and check if cursor is null
		if (cursor.moveToFirst()) {
			do {
				try {
					String tempDate = "";
					FavoriteJSON temp = new FavoriteJSON();
					temp.setName(cursor.getString(cursor
							.getColumnIndexOrThrow("trafficName")));
					temp.setTrafficID(cursor.getString(cursor
							.getColumnIndexOrThrow("trafficID")));
					temp.setImage(cursor.getString(cursor
							.getColumnIndexOrThrow("image")));
					temp.setActive(cursor.getInt(cursor
							.getColumnIndexOrThrow("isActive")) > 0);
					tempDate = cursor.getString(cursor
							.getColumnIndexOrThrow("modifyDate"));
					Date modifyDate;

					modifyDate = new java.sql.Date(dateFormat.parse(tempDate)
							.getTime());

					temp.setModifyDate(modifyDate);
					// add temp to list favorite
					listResult.add(temp);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (cursor.moveToNext());
		}

		db.close();
		return listResult;
	}

	// // check favorite is exist or not
	// public static boolean checkFavorite(String trafficID) {
	// SQLiteDatabase db = SQLiteDatabase.openDatabase(
	// GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
	// SQLiteDatabase.OPEN_READWRITE);
	// // create cursor to access query result
	// Cursor cursor = db.query("favorite", null, "`trafficID` LIKE '"
	// + trafficID + "'", null, null, null, null);
	// if (cursor.moveToFirst()) {
	// db.close();
	// return true;
	// }
	// db.close();
	// return false;
	// }

	// check favorite status (isActive is true or false) return 1 if isActive =
	// true, 0 if isActive = false, 2 if not exist
	public static int checkFavoriteStatus(String trafficID) {
		int status = 2;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				GlobalValue.getAppFolder() + Properties.DB_FILE_PATH, null,
				SQLiteDatabase.OPEN_READWRITE);
		// create cursor to access query result
		Cursor cursor = db.query("favorite", null, "`trafficID` LIKE '"
				+ trafficID + "'", null, null, null, null);
		if (cursor.moveToFirst()) {
			status = cursor.getInt(cursor.getColumnIndexOrThrow("isActive"));

		}
		db.close();
		return status;
	}

	// run update traffic sign (must run in another thread)
	public static void updateTrafficsign(Context context) {
		// progress notification
		NotificationCompat.Builder mBuilder = null;
		NotificationManager mNotifyMgr = null;
		int mNotificationId = 100;
		if (context != null) {
			mBuilder = new NotificationCompat.Builder(context)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Cập nhật biển báo")
					.setContentText("Đang tải...");
			mBuilder.setOngoing(true);
			mNotifyMgr = (NotificationManager) context
					.getSystemService(context.NOTIFICATION_SERVICE);

			mNotifyMgr.notify(mNotificationId, mBuilder.build());
		}

		String urlGetListTraffic = GlobalValue.getServiceAddress()
				+ Properties.TRAFFIC_SEARCH_MANUAL + "?name=";
		String urlGetTrafficDetail = GlobalValue.getServiceAddress()
				+ Properties.TRAFFIC_TRAFFIC_VIEW + "?id=";
		String listTrafficJSON = HttpUtil.get(urlGetListTraffic);
		ArrayList<TrafficInfoShortJSON> listInfoShortJSONs = new ArrayList<TrafficInfoShortJSON>();
		Type typeListTrafficShort = new TypeToken<ArrayList<TrafficInfoShortJSON>>() {
		}.getType();
		listInfoShortJSONs = Helper.fromJson(listTrafficJSON,
				typeListTrafficShort);
		// get each traffic details and add to DB
		if (listInfoShortJSONs != null) {
			Log.e("number traffic", listInfoShortJSONs.size() + "");
			String urlGetTrafficDetailFull = "";
			long dbReturn; // variable to know db return
			for (int i = 0; i < listInfoShortJSONs.size(); i++) {
				if (mBuilder != null) {
					int percent = i * 100 / listInfoShortJSONs.size();
					if (percent > 100) {
						percent = 100;
					}
					mBuilder.setProgress(100, percent, false);
					mBuilder.setContentInfo(percent + "%");
					mBuilder.setOngoing(true);
					mNotifyMgr.notify(mNotificationId, mBuilder.build());
				}

				urlGetTrafficDetailFull = urlGetTrafficDetail
						+ listInfoShortJSONs.get(i).getTrafficID();
				// get traffic detail from service and parse json
				// TrafficInfoJson
				String trafficJSONString = HttpUtil
						.get(urlGetTrafficDetailFull);
				TrafficInfoJSON trafficInfoJSON = Helper.fromJson(
						trafficJSONString, TrafficInfoJSON.class);
				// add traffic to DB
				if (trafficInfoJSON != null) {
					if (DBUtil.checkTraffic(trafficInfoJSON.getTrafficID()) == false) {
						dbReturn = DBUtil.insertTraffic(trafficInfoJSON);
						Log.e("DB", dbReturn + " add");
					} else {
						// if traffic is already had, update traffic
						dbReturn = DBUtil.updateTraffic(trafficInfoJSON);
						Log.e("DB", dbReturn + " update");
					}
					String savePath = GlobalValue.getAppFolder()
							+ Properties.MAIN_IMAGE_FOLDER
							+ trafficInfoJSON.getTrafficID() + ".jpg";
					File image = new File(savePath);
					if (!image.exists()) {
						String imageLink = GlobalValue.getServiceAddress()
								+ trafficInfoJSON.getImage();
						if (HttpUtil.downloadImage(imageLink, savePath)) {
							Log.e("DB Image", savePath);
						}

					}
					Log.e("Number", String.valueOf(i + 1));
				}
			}// end for
			if (mBuilder != null) {
				mBuilder.setProgress(0, 0, false);
				mBuilder.setContentText("Hoàn thành");
				mBuilder.setContentInfo("");
				mBuilder.setOngoing(false);
				mNotifyMgr.notify(mNotificationId, mBuilder.build());
			}
		}
	}

}
