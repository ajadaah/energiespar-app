package de.hska.rbmk.verbrauchsrechner;


import de.hska.rbmk.R;
import android.content.Context;
import android.content.Intent;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

public class MyActionProvider extends ActionProvider implements OnMenuItemClickListener {

	static final int LIST_LENGTH = 3;

	Context mContext;

	public MyActionProvider(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public View onCreateActionView() {
		TextView textView = new TextView(mContext);
		textView.setText("Pick");

		return null; // null
	}

	@Override
	public boolean onPerformDefaultAction() {
		return super.onPerformDefaultAction();
	}

	@Override
	public boolean hasSubMenu() {
		return true;
	}

	@Override
	public void onPrepareSubMenu(SubMenu subMenu) {
		subMenu.clear();

		String[] geraeteListe = mContext.getResources().getStringArray(R.array.GeraeteListe);
		String[] geraeteListeSymbole = mContext.getResources().getStringArray(R.array.GeraeteListeSymbole);
		
		// TODO schlechte submenu implementation 
		
		// Kühlschränke 
		subMenu.add(0, 0, 0, geraeteListe[0])
		.setIcon(mContext.getResources().getIdentifier(geraeteListeSymbole[0], "drawable", mContext.getPackageName()))
		.setOnMenuItemClickListener(this)
		.setIntent(new Intent(mContext, KuehlschraenkeActivity.class));
		
		// Waschmaschinen
//		subMenu.add(0, 1, 1, geraeteListe[1])
//		.setIcon(mContext.getResources().getIdentifier(geraeteListeSymbole[1], "drawable", mContext.getPackageName()))
//		.setOnMenuItemClickListener(this)
//		.setIntent(new Intent(mContext, WaschmaschinenActivity.class));
		
		// Spülmaschinen
		subMenu.add(0, 2, 2, geraeteListe[2])
		.setIcon(mContext.getResources().getIdentifier(geraeteListeSymbole[2], "drawable", mContext.getPackageName()))
		.setOnMenuItemClickListener(this)
		.setIntent(new Intent(mContext, SpuelmaschinenActivity.class));
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
    	mContext.startActivity(item.getIntent());
		return true;
	}
}