package com.guilherme.openweather.activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.guilherme.openweather.R;
import com.guilherme.openweather.model.Coord;
import com.guilherme.openweather.model.Main;
import com.guilherme.openweather.model.Sys;
import com.guilherme.openweather.model.Weather;
import com.guilherme.openweather.model.WeatherRetrofit;
import com.guilherme.openweather.webservice.Conexao;
import com.guilherme.openweather.webservice.WeatherService;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


    private EditText et_city;
    private Button btn_sel;
    private TextView tv_temp;
    private TextView tv_city;
    private TextView tv_desc;
    private ProgressBar bar;
    private ImageView view;
    private TextView tv_rise;
    private TextView tv_set;
    private TextView tv_tempMax;
    private TextView tv_tempMin;
    private TextView tv_humidade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declaração de variáveis inserindo os botões
        tv_temp = findViewById(R.id.txt_temp);
        tv_tempMax = findViewById(R.id.txt_tempMax);
        tv_tempMin = findViewById(R.id.txt_tempMin);
        tv_city = findViewById(R.id.txt_city);
        tv_desc = findViewById(R.id.txt_desc);
        et_city = findViewById(R.id.etxt_city);
        btn_sel = findViewById(R.id.btn_city);
        bar = findViewById(R.id.progress);
        tv_rise = findViewById(R.id.txt_rise);
        tv_set = findViewById(R.id.txt_set);
        tv_humidade = findViewById(R.id.txt_humidade);
        view = findViewById(R.id.image);

        //Clique no botão para buscar a cidade
        btn_sel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                //Variável city recebe o valor digitado
                String city = et_city.getText().toString();

                //Executa a task
                JSONTask task = new JSONTask();
                task.execute(new String[]{city});

                //Modificações do visual

                bar.setVisibility(View.VISIBLE);

                //Move os botões para o topo da tela
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btn_sel.getLayoutParams();
                params.bottomToBottom = -1;
                btn_sel.requestLayout();

                ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) et_city.getLayoutParams();
                params1.bottomToBottom = -1;
                et_city.requestLayout();

            }

        });

    }

    // Classe assincrona para não interromper a UI Thread
    private class JSONTask extends AsyncTask<String, Void, WeatherRetrofit> {

        //Constantes para acesso de informações no openwheather
        final String API_BASE_URL = "http://api.openweathermap.org";
        final String KEY = "7c179a04ba87c9186a8952f181e5369a";
        final String UNITS = "metric";
        final String IMG_URL = "/img/w/";

        //Objeto da API Retrofit para fazer a chamada
        Retrofit retrofit;

        @Override
        protected WeatherRetrofit doInBackground(String... strings) {

            //Objeto para conter os dados do JSON
            WeatherRetrofit weatherRetrofit = null;

            //Objeto retrofit recebe a instância para fazer a chamada
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            //A cidade que foi passada como parâmetro é recuperada
            String cidade = strings[0];


            try {
                //Inicio da chamada, já prevendo uma excessao
                WeatherService weatherService = retrofit.create(WeatherService.class);
                Call<WeatherRetrofit> call = weatherService.getWeatherService(cidade, UNITS, KEY);
                Response<WeatherRetrofit> response = call.execute();
                if(response.isSuccessful()) {


                    weatherRetrofit = response.body();
                    weatherRetrofit.setResult(true);

                    List<Weather> list = weatherRetrofit.getWeather();
                    Weather weatherClass = list.get(0);
                    String icon = weatherClass.getIcon();

                    weatherRetrofit.setImagemData(new Conexao().getImage(icon));
                } else {

                    weatherRetrofit = new WeatherRetrofit();
                    weatherRetrofit.setResult(false);
                    return weatherRetrofit;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


            return weatherRetrofit;
        }


        @Override
        protected void onPostExecute(WeatherRetrofit weatherRetrofit) {
            super.onPostExecute(weatherRetrofit);

            if (weatherRetrofit.getName() != null && weatherRetrofit.getResult() == true) {


                Log.i("Teste", weatherRetrofit.getBase());

                //Recebe o openWeather como resultado e confere se esta ok
                //Insere o elemento
                if (weatherRetrofit.getImagemData() != null && weatherRetrofit.getImagemData().length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(weatherRetrofit.imagemData, 0, weatherRetrofit.imagemData.length);
                    view.setImageBitmap(bitmap);

                }

                Main main = weatherRetrofit.getMain();
                List<Weather> list = weatherRetrofit.getWeather();
                Weather weatherClass = list.get(0);

                Coord coord = weatherRetrofit.getCoord();
                Double lat = coord.getLat();
                Double lon = coord.getLon();

                Sys sys = weatherRetrofit.getSys();
                int rise = sys.getSunrise();
                int set = sys.getSunset();

                // coordenadas usando biblioteca externa
                Location location = new Location(lat,lon);
                SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "America/Sao_Paulo");
                String officialSunrise = calculator.getOfficialSunriseForDate(Calendar.getInstance());
                String officialSunset = calculator.getOfficialSunsetForDate(Calendar.getInstance());



                // Insere os dados nos elementos
                tv_city.setText(weatherRetrofit.getName());
                tv_temp.setText(main.getTemp() + "°");
                tv_tempMax.setText(main.getTempMax() + "°");
                tv_tempMin.setText(main.getTempMin() + "°");
                tv_desc.setText(weatherClass.getDescription());
                tv_humidade.setText(main.getHumidity() + "%");
                tv_rise.setText(officialSunrise);
                tv_set.setText(officialSunset);
                bar.setVisibility(View.GONE);
                tv_city.setVisibility(View.VISIBLE);
                tv_temp.setVisibility(View.VISIBLE);
                tv_tempMax.setVisibility(View.VISIBLE);
                tv_tempMin.setVisibility(View.VISIBLE);
                tv_desc.setVisibility(View.VISIBLE);
                tv_humidade.setVisibility(View.VISIBLE);
                tv_rise.setVisibility(View.VISIBLE);
                tv_set.setVisibility(View.VISIBLE);
                view.setVisibility(View.VISIBLE);


            }else {
                new AlertDialog.Builder(MainActivity.this).setTitle("Cidade incorreta").
                        setMessage("Cidade incorreta entre novamente").setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        bar.setVisibility(View.GONE);
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btn_sel.getLayoutParams();
                        params.bottomToBottom = ConstraintSet.PARENT_ID;
                        btn_sel.requestLayout();

                        ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) et_city.getLayoutParams();
                        params1.bottomToBottom = ConstraintSet.PARENT_ID;
                        et_city.requestLayout();
                    }
                }).show();

                tv_tempMax.setVisibility(View.GONE);
                tv_tempMin.setVisibility(View.GONE);
                tv_city.setVisibility(View.GONE);
                tv_desc.setVisibility(View.GONE);
                tv_rise.setVisibility(View.GONE);
                tv_set.setVisibility(View.GONE);
                tv_humidade.setVisibility(View.GONE);
                tv_temp.setVisibility(View.GONE);
                view.setVisibility(View.GONE);
            }
        }
    }
}
