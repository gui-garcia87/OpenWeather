package com.guilherme.openweather.webservice;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Conexao {

    //Constantes para conexão
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static final String IMG_URL = "http://openweathermap.org/img/w/";
    private static final String KEY = "7c179a04ba87c9186a8952f181e5369a";

    //Método para conectar e retornar a string com os dados
    public String getWeatherData(String location)  {
        HttpURLConnection con = null ;
        InputStream is = null;
        int codeResponse;


        try {
            //Insere a url com a chave e faz a conexão passando o GET como método
            con = (HttpURLConnection) ( new URL(BASE_URL + location + "&appid=" + KEY)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Leitura da resposta
            codeResponse = con.getResponseCode();
            if(codeResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                StringBuffer buffer = new StringBuffer();
                is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null)
                    buffer.append(line + "\r\n");

                // Fecha a InputStream e disconecta
                is.close();
                con.disconnect();
                return buffer.toString();

            } else {
                is = con.getErrorStream();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Throwable t) {
            t.printStackTrace();
        } finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }

    //Método para retornar um array de bytes com os dadsos da imagem
    public byte[] getImage(String code) {
        HttpURLConnection con = null ;
        InputStream is = null;
        try {

            //Faz a conexão e passa a url com o código obtido
           HttpCall httpCall = new HttpCall(IMG_URL + code + ".png");
           HttpResponse response = httpCall.execute(HttpCall.Method.GET);

           is = response.getInputStream();

            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while ( is.read(buffer) != -1)
                baos.write(buffer);

            return baos.toByteArray();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }


}
