package com.eyecue.inapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


//AppCompatActivity
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BuyCreditActivity extends Activity {

    android.support.v7.widget.RecyclerView myRecycle;
    IInAppBillingService mService;
    MyAdapter adapter;
    List<ItemInfo> itemInfoList;
//    String itemOrder[] = {"premium_upgrade", "gas", "1credit", "android.test.purchased", "2crdit", "5credit"};
    HashMap<String, Item> itemMap = new HashMap<String, Item>();
    Activity ctx;
    public static final int REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_buy_credit);

        ctx = this;

        itemInfoList = new ArrayList<ItemInfo>();
        initItemList(itemInfoList);

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        myRecycle = findViewById(R.id.purchase_list);
        myRecycle.setY(getScreenHeight() * 0.05f);
        myRecycle.getLayoutParams().height = (int)(getScreenHeight() * 0.75);
        myRecycle.setHasFixedSize(true);
        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(this);
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        adapter = new MyAdapter(itemInfoList, itemMap);
        myRecycle.setAdapter(adapter);
        myRecycle.setLayoutManager(MyLayoutManager);


    }

//    String itemOrder[] = {"premium_upgrade", "gas", "1credit", "android.test.purchased", "2crdit", "5credit"};
    void initItemList(List<ItemInfo> itemsList)
    {
        ItemInfo item;

        item = new ItemInfo("premium_upgrade", null);
        itemsList.add(item);

        item = new ItemInfo("gas", null);
        itemsList.add(item);

        item = new ItemInfo("1credit", null);
        itemsList.add(item);

        item = new ItemInfo("android.test.purchased", null);
        itemsList.add(item);

        item = new ItemInfo("2crdit", null);
        itemsList.add(item);

        item = new ItemInfo("5credit", null);
        itemsList.add(item);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    String token = jo.getString("purchaseToken");
                    Log.d("oooOri", "You have bought the " + sku);
                    consume(token);
                }
                catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    void consume(String token) {
        class consumeTask implements Runnable {
            String str;
            consumeTask(String token) { str = token; }
            public void run() {
                try {
                    int response = mService.consumePurchase(3, getPackageName(), str);
                    Log.d("oooOri", "consume response " + response);
                }
                catch (RemoteException e) {
                    Log.d("oooOri", "consume response " + e.toString());
                }
            }
        }
//        Thread t = new Thread(new OneShotTask(str));
//        t.start();
        AsyncTask.execute(new consumeTask(token));
    }

    void checkPurchaseItems() {
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            // Check response
            int responseCode = ownedItems.getInt("RESPONSE_CODE");

            // Get the list of purchased items
            ArrayList<String> purchaseDataList =
                    ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
            for (String purchaseData : purchaseDataList) {
                JSONObject o = new JSONObject(purchaseData);
                String purchaseToken = o.optString("token", o.optString("purchaseToken"));
                // Consume purchaseToken, handling any errors
                consume(purchaseToken);
            }
        }
        catch (Exception e) {

        }
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTextView;
        public ImageView image;
        public Button btn;
        public String id;
        public MyViewHolder(View v) {
            super(v);

            titleTextView = v.findViewById(R.id.title);
            image = v.findViewById(R.id.purchase_item_image);
            btn = v.findViewById(R.id.buy_button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Log.d("oooOri", "button pressed " + id);
                        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                                id, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                        startIntentSenderForResult(pendingIntent.getIntentSender(),
                                REQUEST_CODE, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                Integer.valueOf(0));
                    }
                    catch (Exception e) {

                    }
                }
            });
            Log.d("ooOri","size: " + v.getHeight());

        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        List<ItemInfo> listInfo;
        HashMap<String, Item> items;

        public MyAdapter(List<ItemInfo> list, HashMap<String, Item> itemsIn) {
            listInfo = list;
            items = itemsIn;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
            // create a new view
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.purchase_cell_layout, parent, false);
            int width = getScreenWidth()/4;
            view.getLayoutParams().width = width;

//            view.measure(View.MeasureSpec.makeMeasureSpec(view.getLayoutParams().width, View.MeasureSpec.EXACTLY),
//                    View.MeasureSpec.makeMeasureSpec(view.getLayoutParams().height, View.MeasureSpec.EXACTLY));

            MyViewHolder holder = new MyViewHolder(view);
//            holder.image.getLayoutParams().height = 100;
            Log.d("ooOri","size (1): " + holder.itemView.getHeight()+ ',' + view.getMeasuredHeight());
            return holder;
        }
        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            ItemInfo info = listInfo.get(position);

            String yourImageName = "credits10.png";

            InputStream imageStream = null;
            try {
                // get input stream
                int resId= getResources().getIdentifier(yourImageName, "drawable", "eyecue.qlone_basic_android");
                imageStream  =  ctx.getAssets().open(yourImageName);

                // load image as Drawable
                Drawable drawable= Drawable.createFromStream(imageStream, null);

//                Drawable drawable = getResources().getDrawable(resId);
                holder.image.setImageDrawable(drawable);
//                holder.image.setBackground(drawable);
            }
            catch(IOException ex) {

                return;
            }

//            int resId= getResources().getIdentifier(yourImageName, "drawable", "eyecue.qlone_basic_android");


            if(items.containsKey(info.ID)) {
                Item item = items.get(info.ID);
                holder.titleTextView.setText(item.title);
                holder.id = item.ID;
            }
            else {
                holder.titleTextView.setText(info.ID);
            }
        }
        @Override
        public int getItemCount() {
            return listInfo.size();
        }


    }





    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);

            ArrayList<String> skuList = new ArrayList<String> ();
            for(int i=0; i<itemInfoList.size(); i++) {
                ItemInfo info = itemInfoList.get(i);
                skuList.add(i,info.ID);
            }

            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
            try {
                Bundle skuDetails = mService.getSkuDetails(3,
                        getPackageName(), "inapp", querySkus);
                int response = skuDetails.getInt("RESPONSE_CODE");
                ArrayList<String> items = skuDetails.getStringArrayList("DETAILS_LIST");
                for (String itemStr : items) {
                    JSONObject object = new JSONObject(itemStr);
                    Item item = new Item();
                    item.description = object.getString("description");
                    item.ID = object.getString("productId");
                    item.price = object.getString("price");
                    item.title = object.getString("title");
                    itemMap.put(item.ID,item);

                }
                checkPurchaseItems();
                adapter.notifyDataSetChanged();
                Log.d("ooOri","size (1): " );
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    public int getScreenWidth() {

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.x;
    }

    public int getScreenHeight() {

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size.y;
    }

    public class Item {
        String ID;
        String type;
        String price;
        String title;
        String description;
    }
    public class ItemInfo {
        String ID;
        String imageName;

        public ItemInfo(String id, String image) {
            ID = id;
            imageName = image;
        }
    }

}
