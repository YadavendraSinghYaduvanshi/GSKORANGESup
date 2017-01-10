package cpm.com.gskmtorange.gsk_dailyentry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cpm.com.gskmtorange.Database.GSKOrangeDB;
import cpm.com.gskmtorange.R;
import cpm.com.gskmtorange.constant.CommonString;
import cpm.com.gskmtorange.xmlGetterSetter.MSL_AvailabilityGetterSetter;
import cpm.com.gskmtorange.xmlGetterSetter.Stock_FacingGetterSetter;

public class Stock_FacingActivity extends AppCompatActivity {
    ExpandableListView expandableListView;
    TextView txt_stockFacingName;

    ArrayList<Stock_FacingGetterSetter> headerDataList;
    ArrayList<Stock_FacingGetterSetter> childDataList;
    List<Stock_FacingGetterSetter> hashMapListHeaderData;
    HashMap<Stock_FacingGetterSetter, List<Stock_FacingGetterSetter>> hashMapListChildData;
    List<Integer> checkHeaderArray = new ArrayList<>();

    ExpandableListAdapter adapter;
    GSKOrangeDB db;

    String categoryName, categoryId, Error_Message = "";

    String path = "", str = "", _pathforcheck = "", img1 = "", img2 = "";
    static int child_position = -1;
    boolean isDialogOpen = true;
    boolean checkflag = true;

    private SharedPreferences preferences;
    String store_id, visit_date, username, intime, date, keyAccount_id, class_id, storeType_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_stock_facing);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

            db = new GSKOrangeDB(this);
            db.open();

            expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
            //txt_stockFacingName = (TextView) findViewById(R.id.txt_stockFacingName);

            //preference data
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            store_id = preferences.getString(CommonString.KEY_STORE_ID, null);
            visit_date = preferences.getString(CommonString.KEY_DATE, null);
            date = preferences.getString(CommonString.KEY_DATE, null);
            username = preferences.getString(CommonString.KEY_USERNAME, null);
            intime = preferences.getString(CommonString.KEY_STORE_IN_TIME, "");
            keyAccount_id = preferences.getString(CommonString.KEY_KEYACCOUNT_ID, "");
            class_id = preferences.getString(CommonString.KEY_CLASS_ID, "");
            storeType_id = preferences.getString(CommonString.KEY_STORETYPE_ID, "");

            categoryName = getIntent().getStringExtra("categoryName");
            categoryId = getIntent().getStringExtra("categoryId");

            //txt_stockFacingName.setText(getResources().getString(R.string.title_activity_stock_facing));
            toolbar.setTitle(getResources().getString(R.string.title_activity_stock_facing));
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            prepareList();

            str = CommonString.FILE_PATH + _pathforcheck;

            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/

                    if (validateData(hashMapListHeaderData, hashMapListChildData)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Stock_FacingActivity.this);
                        builder.setMessage("Are you sure you want to save")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        db.open();

                                        if (db.checkStockAndFacingData(store_id, categoryId)) {
                                            db.updateStockAndFacing(store_id, categoryId, hashMapListHeaderData, hashMapListChildData);
                                            Snackbar.make(view, "Data has been updated", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        } else {
                                            db.InsertStock_Facing(store_id, categoryId, hashMapListHeaderData, hashMapListChildData);
                                            Snackbar.make(view, "Data has been saved", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            //Toast.makeText(getApplicationContext(), "Data has been saved", Toast.LENGTH_LONG).show();
                                        }

                                        finish();
                                        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Stock_FacingActivity.this);
                        builder.setMessage("Fill the value or fill 0 ")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });

            expandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    int lastItem = firstVisibleItem + visibleItemCount;

                    if (firstVisibleItem == 0) {
                        fab.setVisibility(View.VISIBLE);
                    } else if (lastItem == totalItemCount) {
                        fab.setVisibility(View.INVISIBLE);
                    } else {
                        fab.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onScrollStateChanged(AbsListView arg0, int arg1) {
                    InputMethodManager inputManager = (InputMethodManager) getApplicationContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (getCurrentFocus() != null) {
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        getCurrentFocus().clearFocus();
                    }

                    expandableListView.invalidateViews();
                }
            });

            // Listview Group click listener
            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    return false;
                }
            });

            // Listview Group expanded listener
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {
                    InputMethodManager inputManager = (InputMethodManager) getApplicationContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (getWindow().getCurrentFocus() != null) {
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        getCurrentFocus().clearFocus();
                    }
                }
            });

            // Listview Group collasped listener
            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {
                    InputMethodManager inputManager = (InputMethodManager) getApplicationContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (getWindow().getCurrentFocus() != null) {
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        getCurrentFocus().clearFocus();
                    }
                }
            });

            // Listview on child click listener
            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    return false;
                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void prepareList() {
        try {
            hashMapListHeaderData = new ArrayList<>();
            hashMapListChildData = new HashMap<>();

            //Header Data
            headerDataList = db.getStockAndFacingHeader_AfterSaveData(categoryId);
            if (!(headerDataList.size() > 0)) {
                headerDataList = db.getStockAndFacingHeaderData(categoryId);
            }

            if (headerDataList.size() > 0) {

                for (int i = 0; i < headerDataList.size(); i++) {
                    hashMapListHeaderData.add(headerDataList.get(i));

                    //Child Data
                    childDataList = db.getStockAndFacingSKU_AfterSaveData(categoryId, headerDataList.get(i).getBrand_id());
                    if (!(childDataList.size() > 0)) {
                        childDataList = db.getStockAndFacingSKUData(categoryId, headerDataList.get(i).getBrand_id());
                    }

                    hashMapListChildData.put(hashMapListHeaderData.get(i), childDataList);
                }
            }

            adapter = new ExpandableListAdapter(this, hashMapListHeaderData, hashMapListChildData);
            expandableListView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {
        private Context _context;
        private List<Stock_FacingGetterSetter> _listDataHeader;
        private HashMap<Stock_FacingGetterSetter, List<Stock_FacingGetterSetter>> _listDataChild;

        public ExpandableListAdapter(Context context, List<Stock_FacingGetterSetter> listDataHeader,
                                     HashMap<Stock_FacingGetterSetter, List<Stock_FacingGetterSetter>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            final Stock_FacingGetterSetter headerTitle = (Stock_FacingGetterSetter) getGroup(groupPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.item_stock_facing_header, null, false);
            }

            TextView txt_stockFaceupHeader = (TextView) convertView.findViewById(R.id.txt_stockFaceupHeader);
            LinearLayout lin_stockFaceupHeader = (LinearLayout) convertView.findViewById(R.id.lin_stockFaceupHeader);
            ImageView img_reference = (ImageView) convertView.findViewById(R.id.img_reference);
            ImageView img_camera1 = (ImageView) convertView.findViewById(R.id.img_camera1);
            ImageView img_camera2 = (ImageView) convertView.findViewById(R.id.img_camera2);
            ImageView img_edit = (ImageView) convertView.findViewById(R.id.img_edit);

            txt_stockFaceupHeader.setTypeface(null, Typeface.BOLD);
            txt_stockFaceupHeader.setText(headerTitle.getSub_category() + "-" + headerTitle.getBrand());

            img_camera1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //String date = new Date().toLocaleString().toString();
                    //String tempDate = new Date().toLocaleString().toString().replace(' ', '_').replace(',', '_').replace(':', '-');

                    _pathforcheck = "Stock_Cam1_" + store_id + "_" + visit_date.replace("/", "") + "_" + getCurrentTime().replace(":", "") + ".jpg";
                    child_position = groupPosition;
                    path = str + _pathforcheck;

                    startCameraActivity1(groupPosition);
                }
            });

            if (!img1.equalsIgnoreCase("")) {
                if (groupPosition == child_position) {
                    headerTitle.setImage1(img1);
                    img1 = "";
                }
            }

            if (headerTitle.getImage1().equals("")) {
                img_camera1.setBackgroundResource(R.mipmap.camera);
            } else {
                img_camera1.setBackgroundResource(R.mipmap.camera_done);
            }


            img_camera2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //String date = new Date().toLocaleString().toString();
                    //String tempDate = new Date().toLocaleString().toString().replace(' ', '_').replace(',', '_').replace(':', '-');

                    _pathforcheck = "Stock_Cam2_" + store_id + "_" + visit_date.replace("/", "") + "_" + getCurrentTime().replace(":", "") + ".jpg";
                    child_position = groupPosition;
                    path = str + _pathforcheck;

                    startCameraActivity2(groupPosition);
                }
            });

            if (!img2.equalsIgnoreCase("")) {
                if (groupPosition == child_position) {
                    headerTitle.setImage2(img2);
                    img2 = "";
                }
            }

            if (headerTitle.getImage2().equals("")) {
                img_camera2.setBackgroundResource(R.mipmap.camera);
            } else {
                img_camera2.setBackgroundResource(R.mipmap.camera_done);
            }


            if (!checkflag) {
                if (checkHeaderArray.contains(groupPosition)) {
                    txt_stockFaceupHeader.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    txt_stockFaceupHeader.setTextColor(getResources().getColor(R.color.black));
                }
            }

            return convertView;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            final Stock_FacingGetterSetter childData = (Stock_FacingGetterSetter) getChild(groupPosition, childPosition);
            ViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.item_stock_facing_child, null, false);

                holder = new ViewHolder();
                holder.cardView = (CardView) convertView.findViewById(R.id.card_view);
                holder.lin_category = (LinearLayout) convertView.findViewById(R.id.lin_category);

                holder.txt_skuName = (TextView) convertView.findViewById(R.id.txt_skuName);
                holder.ed_stock = (EditText) convertView.findViewById(R.id.ed_stock);
                holder.ed_facing = (EditText) convertView.findViewById(R.id.ed_facing);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.txt_skuName.setText(childData.getSku());

            if (childData.getCompany_id().equals("1")) {
                holder.txt_skuName.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            } else {
                holder.txt_skuName.setTextColor(getResources().getColor(R.color.black));
            }


            if (childData.getStock().equals("0")) {
                holder.ed_facing.setEnabled(false);
            } else {
                holder.ed_facing.setEnabled(true);
            }

            final ViewHolder finalHolder = holder;
            holder.ed_stock.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    final EditText caption = (EditText) v;
                    String edStock = caption.getText().toString();

                    if (!edStock.equals("")) {
                        String stock = edStock.replaceFirst("^0+(?!$)", "");
                        childData.setStock(stock);

                        if (edStock.equals("0")) {
                            childData.setFacing("0");

                            finalHolder.ed_facing.setEnabled(false);
                        } else {
                            childData.setFacing(childData.getFacing());
                            finalHolder.ed_facing.setEnabled(true);
                        }
                    } else {
                        childData.setStock("");
                        finalHolder.ed_facing.setEnabled(true);
                    }
                }
            });

            holder.ed_stock.setText(childData.getStock());

            holder.ed_facing.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    final EditText caption = (EditText) v;
                    final String edFaceup = caption.getText().toString().replaceFirst("^0+(?!$)", "");

                    if (!childData.getStock().equals("")) {
                        if (!edFaceup.equals("")) {
                            if (Integer.parseInt(edFaceup) <= Integer.parseInt(childData.getStock())) {
                                childData.setFacing(edFaceup);
                            } else {
                                if (isDialogOpen) {
                                    isDialogOpen = !isDialogOpen;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Stock_FacingActivity.this);
                                    builder.setMessage("Faceup can not be greater than stock value")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                    isDialogOpen = !isDialogOpen;
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }
                        } else {
                            childData.setFacing("");
                        }
                    } else {
                        if (isDialogOpen) {
                            isDialogOpen = !isDialogOpen;
                            AlertDialog.Builder builder = new AlertDialog.Builder(Stock_FacingActivity.this);
                            builder.setMessage("First fill the stock value")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            isDialogOpen = !isDialogOpen;
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                }
            });

            holder.ed_facing.setText(childData.getFacing());

            if (!checkflag) {
                boolean tempflag = false;

                if (holder.ed_stock.getText().toString().equals("")) {
                    holder.ed_stock.setBackgroundColor(getResources().getColor(R.color.white));
                    holder.ed_stock.setHintTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    holder.ed_stock.setHint("Empty");
                    tempflag = true;
                }

                if (holder.ed_facing.getText().toString().equals("")) {
                    holder.ed_facing.setBackgroundColor(getResources().getColor(R.color.white));
                    holder.ed_facing.setHintTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    holder.ed_facing.setHint("Empty");
                    tempflag = true;
                }

                if (tempflag) {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
                }
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    public class ViewHolder {
        EditText ed_stock, ed_facing;
        CardView cardView;
        TextView txt_skuName;
        LinearLayout lin_category;
    }

    boolean validateData(List<Stock_FacingGetterSetter> listDataHeader,
                         HashMap<Stock_FacingGetterSetter, List<Stock_FacingGetterSetter>> listDataChild) {
        boolean flag = true;
        checkHeaderArray.clear();

        for (int i = 0; i < listDataHeader.size(); i++) {
            String imagePath = listDataHeader.get(i).getImage1();
            String imagePath1 = listDataHeader.get(i).getImage2();

            for (int j = 0; j < listDataChild.get(listDataHeader.get(i)).size(); j++) {
                String stock = listDataChild.get(listDataHeader.get(i)).get(j).getStock();
                String faceup = listDataChild.get(listDataHeader.get(i)).get(j).getFacing();

                if (!imagePath.equals("") || !imagePath1.equals("")) {
                    if (!stock.equals("0")) {
                        if (stock.equals("") || faceup.equals("")) {
                            if (!checkHeaderArray.contains(i)) {
                                checkHeaderArray.add(i);
                            }

                            flag = false;
                            Error_Message = "Please fill all the data";
                            break;
                        }
                    } else {
                        if (stock.equals("")) {
                            if (!checkHeaderArray.contains(i)) {
                                checkHeaderArray.add(i);
                            }

                            flag = false;
                            Error_Message = "Please fill all the data";
                            break;
                        }
                    }
                } else {
                    if (!checkHeaderArray.contains(i)) {
                        checkHeaderArray.add(i);
                    }

                    flag = false;
                    Error_Message = "Please click either 1 image";
                    break;
                }
            }

            if (flag == false) {
                checkflag = false;
                break;
            } else {
                checkflag = true;
            }
        }
        //expListView.invalidate();
        adapter.notifyDataSetChanged();

        return checkflag;
    }

    private void startCameraActivity1(int position) {
        try {
            /*Log.e("Stock and Facing ", "startCameraActivity()");
            File file = new File(path);
            Uri outputFileUri = Uri.fromFile(file);

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(intent, position);*/

            Log.i("Stock & Facing ", "startCameraActivity()");
            File file = new File(path);
            Uri outputFileUri = Uri.fromFile(file);

            String defaultCameraPackage = "";
            final PackageManager packageManager = getPackageManager();
            List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (int n = 0; n < list.size(); n++) {
                if ((list.get(n).flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    /*Log.e("TAG", "Installed Applications  : " + list.get(n).loadLabel(packageManager).toString());
                    Log.e("TAG", "package name  : " + list.get(n).packageName);*/

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (list.get(n).loadLabel(packageManager).toString().equalsIgnoreCase("Camera")) {
                            defaultCameraPackage = list.get(n).packageName;
                            break;
                        }
                    } else {
                        if (list.get(n).loadLabel(packageManager).toString().equalsIgnoreCase("Gallery")) {
                            defaultCameraPackage = list.get(n).packageName;
                            break;
                        }
                    }
                }
            }

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            intent.setPackage(defaultCameraPackage);
            startActivityForResult(intent, 1);
            //startActivityForResult(intent, position);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCameraActivity2(int position) {
        try {
            /*Log.e("Stock and Facing ", "startCameraActivity()");
            File file = new File(path);
            Uri outputFileUri = Uri.fromFile(file);

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(intent, position);*/

            Log.i("Stock & Facing ", "startCameraActivity()");
            File file = new File(path);
            Uri outputFileUri = Uri.fromFile(file);

            String defaultCameraPackage = "";
            final PackageManager packageManager = getPackageManager();
            List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (int n = 0; n < list.size(); n++) {
                if ((list.get(n).flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    /*Log.e("TAG", "Installed Applications  : " + list.get(n).loadLabel(packageManager).toString());
                    Log.e("TAG", "package name  : " + list.get(n).packageName);*/

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (list.get(n).loadLabel(packageManager).toString().equalsIgnoreCase("Camera")) {
                            defaultCameraPackage = list.get(n).packageName;
                            break;
                        }
                    } else {
                        if (list.get(n).loadLabel(packageManager).toString().equalsIgnoreCase("Gallery")) {
                            defaultCameraPackage = list.get(n).packageName;
                            break;
                        }
                    }
                }
            }

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            intent.setPackage(defaultCameraPackage);
            startActivityForResult(intent, 2);
            //startActivityForResult(intent, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Stock & Facing", "resultCode: " + resultCode);
        /*switch (resultCode) {
            case 0:
                Log.e("Stock & Facing", "User cancelled");
                break;
            case -1:
                if (_pathforcheck != null && !_pathforcheck.equals("")) {
                    if (new File(str + _pathforcheck).exists()) {
                        img1 = _pathforcheck;
                        adapter.notifyDataSetChanged();
                        _pathforcheck = "";
                    }
                }
                break;
        }*/

        switch (requestCode) {
            case 1:
                if (resultCode == -1) {
                    if (_pathforcheck != null && !_pathforcheck.equals("")) {
                        if (new File(str + _pathforcheck).exists()) {
                            img1 = _pathforcheck;
                            adapter.notifyDataSetChanged();
                            _pathforcheck = "";
                        }
                    }
                } else {
                    Log.e("Stock & Facing", "User cancelled");
                }
                break;
            case 2:
                if (resultCode == -1) {
                    if (_pathforcheck != null && !_pathforcheck.equals("")) {
                        if (new File(str + _pathforcheck).exists()) {
                            img2 = _pathforcheck;
                            adapter.notifyDataSetChanged();
                            _pathforcheck = "";
                        }
                    }
                } else {
                    Log.e("Stock & Facing", "User cancelled");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getCurrentTime() {
        Calendar m_cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String cdate = formatter.format(m_cal.getTime());
       /* String intime = m_cal.get(Calendar.HOUR_OF_DAY) + ":"
                + m_cal.get(Calendar.MINUTE) + ":" + m_cal.get(Calendar.SECOND);*/

        return cdate;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
