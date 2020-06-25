package com.example.aplicacion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.Normalizer;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView Email;
    private TextView Uid;
    private Button logout;


    private static final int RECONOCEDOR_VOZ = 7;
    private TextView escuchando;
    private TextView respuesta;
    private ArrayList<Respuestas> respuest;
    private TextToSpeech leer;
    private final int REQUEST = 200;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Email = (TextView)findViewById(R.id.profileEmail);
        Uid = (TextView)findViewById(R.id.profileUid);
        mAuth = FirebaseAuth.getInstance();
        logout = (Button)findViewById(R.id.button_logout);
        user = mAuth.getCurrentUser();


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v==logout){
                    if (user != null) {
                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }
            }
        });

        if (user != null){
            String email = user.getEmail();
            String uid = user.getUid();
            Email.setText(email);
            Uid.setText(uid);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == RECONOCEDOR_VOZ){
            ArrayList<String> reconocido = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String escuchado = reconocido.get(0);
            escuchando.setText(escuchado);
            prepararRespuesta(escuchado);
        }
    }

    private void prepararRespuesta(String escuchado) {
        String normalizar = Normalizer.normalize(escuchado, Normalizer.Form.NFD);
        String sintilde = normalizar.replaceAll("[^\\p{ASCII}]", "");

        int resultado;
        String respuesta = respuest.get(0).getRespuestas();
        for (int i = 0; i < respuest.size(); i++) {
            resultado = sintilde.toLowerCase().indexOf(respuest.get(i).getCuestion());
            if(resultado != -1){
                respuesta = respuest.get(i).getRespuestas();

                //Llamar a método para las operaciones aritméticas.
                if(!operacion(respuest.get(i).getCuestion(), sintilde).equals("")){
                    //Se concatena la respuesta más el número.
                    respuesta=respuesta+operacion(respuest.get(i).getCuestion(), sintilde);
                }
            }
        }
        responder(respuesta);
    }

    //Crear método para realizar las operaciones aritméticas. (+ , - , * , /)

    private String operacion(String cuestion, String escuchado) {
        String rpta= "";

        if(cuestion.equals("mas") || cuestion.equals("menos") || cuestion.equals("por") || cuestion.equals("dividido")){
            rpta=operaciones(cuestion,escuchado);
        }
        return rpta;
    }

    private String operaciones(String operador, String numeros){

        String rpta= "";
        double respuesta=0;
        //Dividir la cadena por el operador.
        String[] numero=numeros.split(operador);

        double num1=obtenerNumero(numero[0]);
        double num2=obtenerNumero(numero[1]);


        String errorDivision="";


        switch (operador){
            case "mas":
                respuesta=num1+num2;
                break;

            case "menos":
                respuesta=num1-num2;
                break;

            case "por":
                respuesta=num1*num2;
                break;

            case "dividido":
                if(num1>0){
                    respuesta=num1/num2;
                }else {
                    errorDivision="ERROR: El primer número no puede ser menor a 0";
                }
                break;
        }
        rpta= String.valueOf(respuesta)+errorDivision;
        return rpta;
    }

    private double obtenerNumero(String cadena){
        double num;
        String n= "";
        //Recorrer el arreglo y me debe almacenar sólo los números.
        char[] numero=cadena.toCharArray();

        for (int i=0;i<numero.length;i++){
            if(Character.isDigit(numero[i])){
                n = n + String.valueOf(numero[i]);
            }
        }
        num=Double.parseDouble(n);
        return num;
    }


    private void responder(String respuestita) {
        respuesta.setText(respuestita);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            leer.speak(respuestita, TextToSpeech.QUEUE_FLUSH, null, null);
        }else {
            leer.speak(respuestita, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void inicializar(){
        escuchando = (TextView)findViewById(R.id.tvEscuchado);
        respuesta = (TextView)findViewById(R.id.tvRespuesta);
        respuest = proveerDatos();
        leer = new TextToSpeech(this, this);


    }

    public void hablar(View v){
        Intent hablar = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        hablar.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-CO");
        startActivityForResult(hablar, RECONOCEDOR_VOZ);
    }

    public ArrayList<Respuestas> proveerDatos(){
        ArrayList<Respuestas> respuestas = new ArrayList<>();

        respuestas.add(new Respuestas("defecto", "¡Aun no estoy programada para responder eso, lo siento!"));
        respuestas.add(new Respuestas("hola", "Hola, ¿que tal?"));
        respuestas.add(new Respuestas("adios", "Hasta pronto, que descanses"));
        respuestas.add(new Respuestas("chao", "Hasta pronto, que descanses"));
        respuestas.add(new Respuestas("estas ahi", "Si, acá estoy para ayudarte..."));

        respuestas.add(new Respuestas("como estas", "Muy bien, gracias por preguntar"));
        respuestas.add(new Respuestas("nombre", "Mis amigos me llaman Asistente Virtual"));
        respuestas.add(new Respuestas("llamas", "Mis amigos me llaman Asistente Virtual"));
        respuestas.add(new Respuestas("edad", "El tiempo pasa muy despacio en éste lugar"));
        respuestas.add(new Respuestas("vives", "¿En serio no sabes? Yo vivo aquí en tu celular"));
        respuestas.add(new Respuestas("pasatiempo", "Me gusta programar"));
        respuestas.add(new Respuestas("color", "Me gusta el color Azul"));
        respuestas.add(new Respuestas("empresa", "Mi empresa se llama CESDE"));


        //Operaciones aritméticas.
        respuestas.add(new Respuestas("mas ", "La respuesta de la suma es "));
        respuestas.add(new Respuestas("menos ", "La respuesta de la resta es "));
        respuestas.add(new Respuestas("por ", "La respuesta de la multiplicación es "));
        respuestas.add(new Respuestas("dividido ", "La respuesta de la división es "));



        return respuestas;
    }



    /*private final void validar() {
        if (Build.VERSION.SDK_INT > 21) {
            solicitarPermiso();
        }
    }*/

    /*private final void solicitarPermiso() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.RECORD_AUDIO"}, this.REQUEST);
        }
    }*/

    @Override
    public void onInit(int status) {

    }


}