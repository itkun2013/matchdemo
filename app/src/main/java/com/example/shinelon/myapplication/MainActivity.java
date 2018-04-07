package com.example.shinelon.myapplication;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.example.shinelon.myapplication.bean.DataBean;
import com.example.shinelon.myapplication.view.XListView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements XListView.IXListViewListener {

    private List<DataBean.ResultsBean> list = new ArrayList<>();
    private XListView xListView;
    private MainAdapter adapter;
    private int NUM = 10;        //最多十条数据
    private Handler handler =new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        xListView = (XListView) findViewById(R.id.xListView);

        //设置下拉和上拉选项可用....如果false不可用
        xListView.setPullRefreshEnable(true);
        xListView.setPullLoadEnable(true);

        //设置XListView的监听事件
        xListView.setXListViewListener(this);

        //获取网路数据的方法
        getDataFromNet();
    }

    /**
     * 刚开始的时候获取网络上的数据...添加到list集合,,,设置适配器
     * http://gank.io/api/data/Android/10/12
     */
    public void getDataFromNet() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    //路径
                    String path = "http://gank.io/api/data/Android/10/"+NUM;

                    //请求网络
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    //网络设置
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    //获取响应数据
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200){
                        InputStream inputStream = connection.getInputStream();
                        String json = streamToString(inputStream,"utf-8");
                        return json;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return "";
            }

            @Override
            protected void onPostExecute(String json) {
                //解析数据
                DataBean dataBean = new Gson().fromJson(json, DataBean.class);
                Log.i("onPostExecute","onPostExecute方法数据");
                //将这次的十条数据添加到集合
                list.addAll(dataBean.getResults());
                setAdapter();

                //数据加载展示完之后...停止加载
                xListView.stopLoadMore();
            }
        };

        //最后执行异步任务
        task.execute();
    }

    //设置适配器的方法
    public void setAdapter(){
        if (adapter == null){
            adapter = new MainAdapter(MainActivity.this, list);
            xListView.setAdapter(adapter);
        }else {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 下拉的时候调用的...刷新
     */
    @Override
    public void onRefresh() {
        NUM--;
        if (NUM>0){
            //获取数据
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshData();
                }
            },2000);


        }else{
            Toast.makeText(MainActivity.this,"没有最新数据了!",Toast.LENGTH_SHORT).show();
            xListView.stopRefresh();
        }
    }

    //刷新数据的方法
    private void refreshData() {
        AsyncTask<Void,Void,String>  task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    //路径
                    String path = "http://gank.io/api/data/Android/10/"+NUM;

                    //请求网络数据
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    //响应数据
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200){
                        InputStream inputStream = connection.getInputStream();
                        String json = streamToString(inputStream,"utf-8");
                        return json;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void onPostExecute(String json) {
                //解析数据
                DataBean dataBean = new Gson().fromJson(json, DataBean.class);

                //数据要添加到集合的最前边
                list.addAll(0,dataBean.getResults());
                setAdapter();

                //停止刷新
//                xListView.stopLoadMore();


                //设置本次刷新的时间
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String time = format.format(date);
                xListView.setRefreshTime(time);

                xListView.stopRefresh();
            }
        };
        task.execute();
    }

    /**
     * 上拉的时候调用的....加载
     */
    @Override
    public void onLoadMore() {
        NUM++;
        //请求网络数据
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDataFromNet();
            }
        },2000);

    }

    //Json数据解析的自定义方法
    private String streamToString(InputStream inputStream, String encode) {
        try {
            //转换流
            InputStreamReader streamReader = new InputStreamReader(inputStream, encode);
            //缓冲流
            BufferedReader reader = new BufferedReader(streamReader);
            //缓冲区
            StringBuilder builder = new StringBuilder();

            //读取数据
            String data = null;
            while ((data = reader.readLine())!=null){
                builder.append(data);
            }

            //关流，返回数据
            reader.close();
            return  builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
