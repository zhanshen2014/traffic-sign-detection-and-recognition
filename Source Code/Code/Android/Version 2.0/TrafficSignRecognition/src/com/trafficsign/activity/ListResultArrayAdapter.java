package com.trafficsign.activity;

import java.util.ArrayList;

import com.trafficsign.activity.R;
import com.trafficsign.json.ResultInput;
import com.trafficsign.ultils.GlobalValue;
import com.trafficsign.ultils.HttpImageUtils;
import com.trafficsign.ultils.ImageUtils;
import com.trafficsign.ultils.Properties;
import com.trafficsign.ultils.MyInterface.IAsyncHttpImageListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListResultArrayAdapter extends ArrayAdapter<ResultInput> {
	Activity context = null;
	int layoutID;
	ArrayList<ResultInput> arr = null;

	public ListResultArrayAdapter(Activity context, int layoutID,
			ArrayList<ResultInput> list) {
		super(context, layoutID, list);
		this.context = context;
		this.layoutID = layoutID;
		this.arr = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			convertView = inflater.inflate(layoutID, null);
		}
		ResultInput resultInput = arr.get(position);
		TextView name = (TextView) convertView.findViewById(R.id.trafficName);
		TextView id = (TextView) convertView.findViewById(R.id.trafficID);
		ImageView img = (ImageView) convertView.findViewById(R.id.trafficImage);
		// set info
		name.setText(resultInput.getTrafficName());
		if (resultInput.getTrafficID() != null
				&& !"".equals(resultInput.getTrafficID())) {
			id.setText(resultInput.getTrafficID());
			String imagePath = GlobalValue.getAppFolder()
					+ Properties.MAIN_IMAGE_FOLDER + resultInput.getTrafficID()
					+ ".jpg";
			Bitmap bitmap = ImageUtils.convertToBimap(imagePath);
			img.setImageBitmap(bitmap);
		} else {
			id.setText("Kh�ng r�");
			img.setImageResource(R.drawable.no_image);
		}	

		return convertView;

	}

}
