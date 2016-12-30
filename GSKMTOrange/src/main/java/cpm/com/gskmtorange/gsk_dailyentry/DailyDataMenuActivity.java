package cpm.com.gskmtorange.gsk_dailyentry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cpm.com.gskmtorange.R;
import cpm.com.gskmtorange.xmlGetterSetter.DailyDataMenuGetterSetter;

public class DailyDataMenuActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<DailyDataMenuGetterSetter> categoryList;
    DailyDataMenuAdapter adapter;
    TextView txt_categoryName;
    String categoryName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_data_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txt_categoryName = (TextView) findViewById(R.id.txt_categoryName);

        categoryName = getIntent().getStringExtra("categoryName");

        txt_categoryName.setText("Daily Data Menu - " + categoryName);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        categoryList = new ArrayList<>();

        DailyDataMenuGetterSetter data = new DailyDataMenuGetterSetter();
        data.setCategory_name("MSL Availability");
        data.setCategory_img(R.drawable.category);
        categoryList.add(data);

        data = new DailyDataMenuGetterSetter();
        data.setCategory_name("Stock & Facing");
        data.setCategory_img(R.drawable.category);
        categoryList.add(data);

        data = new DailyDataMenuGetterSetter();
        data.setCategory_name("T2P Compliance");
        data.setCategory_img(R.drawable.category);
        categoryList.add(data);

        data = new DailyDataMenuGetterSetter();
        data.setCategory_name("Additional Visibility");
        data.setCategory_img(R.drawable.category);
        categoryList.add(data);

        data = new DailyDataMenuGetterSetter();
        data.setCategory_name("Promo Compliance");
        data.setCategory_img(R.drawable.category);
        categoryList.add(data);

        data = new DailyDataMenuGetterSetter();
        data.setCategory_name("Competition Visibility");
        data.setCategory_img(R.drawable.category);
        categoryList.add(data);

        data = new DailyDataMenuGetterSetter();
        data.setCategory_name("Competition Promo");
        data.setCategory_img(R.drawable.category);
        categoryList.add(data);

        adapter = new DailyDataMenuAdapter(DailyDataMenuActivity.this, categoryList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
    }

    public class DailyDataMenuAdapter extends RecyclerView.Adapter<DailyDataMenuAdapter.MyViewHolder> {
        private LayoutInflater inflator;
        List<DailyDataMenuGetterSetter> list = Collections.emptyList();
        Context context;

        public DailyDataMenuAdapter(Context context, List<DailyDataMenuGetterSetter> list) {
            inflator = LayoutInflater.from(context);
            this.list = list;
            this.context = context;
        }

        @Override
        public DailyDataMenuAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
            View view = inflator.inflate(R.layout.daily_main_menu_row, parent, false);
            DailyDataMenuAdapter.MyViewHolder holder = new DailyDataMenuAdapter.MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(DailyDataMenuAdapter.MyViewHolder holder, int position) {
            final DailyDataMenuGetterSetter dailyData = list.get(position);

            holder.categoryName.setText(dailyData.getCategory_name());
            holder.categoryIcon.setImageResource(dailyData.getCategory_img());

            holder.lay_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dailyData.getCategory_name().equalsIgnoreCase("MSL Availability")) {
                        Intent intent = new Intent(DailyDataMenuActivity.this, MSL_AvailabilityActivity.class);
                        intent.putExtra("categoryName", dailyData.getCategory_name());
                        startActivity(intent);
                    } else if (dailyData.getCategory_name().equalsIgnoreCase("Stock & Facing")) {
                        Intent intent1 = new Intent(DailyDataMenuActivity.this, Stock_FacingActivity.class);
                        intent1.putExtra("categoryName", dailyData.getCategory_name());
                        startActivity(intent1);
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView categoryName;
            ImageView categoryIcon;
            LinearLayout lay_menu;

            public MyViewHolder(View itemView) {
                super(itemView);
                categoryName = (TextView) itemView.findViewById(R.id.categoryName);
                categoryIcon = (ImageView) itemView.findViewById(R.id.categoryIcon);
                lay_menu = (LinearLayout) itemView.findViewById(R.id.lay_menu);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
