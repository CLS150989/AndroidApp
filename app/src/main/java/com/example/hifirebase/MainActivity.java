package com.example.hifirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hifirebase.Adaptadores.ListViewPersonasAdapter;
import com.example.hifirebase.Models.Persona;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

   private ArrayList<Persona> listPersona = new ArrayList<Persona>();
   ArrayAdapter<Persona> arrayAdapterPersona;
   ListViewPersonasAdapter listViewPersonasAdapter;
   LinearLayout linearLayoutEditar;
   ListView listViewPersonas;
   EditText inputNombre, inputTelefono;
   Button btnCancelar;

   Persona personaSeleccionada;
   FirebaseDatabase firebaseDatabase;
   DatabaseReference databaseReference;
   Button logOut;

    //variables para la instancia de Firebase
    // y para el listener dinámico de cualquier cambio inicio o cierre de sessión

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123 ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //obteniendo la instancia de Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // craando el  método listener dinámico de firebase para
        // inicio y cierre de sesión en Firebase
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //comprobando si el usuario está logeado
                if (user != null) {

                } else {
                    //despliegue de los proveedores de autenticación disponibles
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false).setTosUrl("privacy policy")
                            .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder()
                                    .build())).build(), RC_SIGN_IN);

                }

            }


        };






        inputNombre = findViewById(R.id.inputNombre);
        inputTelefono = findViewById(R.id.inputTelefono);
        btnCancelar = findViewById(R.id.btnCancelar);
        listViewPersonas = findViewById(R.id.ListViewPersonas);
        linearLayoutEditar = findViewById(R.id.linearLayoutEditar);



    listViewPersonas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            personaSeleccionada = (Persona) adapterView.getItemAtPosition(i);
            inputNombre.setText(personaSeleccionada.getNombres());
            inputTelefono.setText(personaSeleccionada.getTelefono());
            //hacer visible linar layout
            linearLayoutEditar.setVisibility(View.VISIBLE);

        }
    });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutEditar.setVisibility(View.GONE);
                personaSeleccionada = null;
            }
        });



        inicializarFirebase();
        listarPersonas();
    }


    //métodos para el estado de la actividad se deben escribir siempre fuera del método principal onCreate
    //método que informa sobre si la operación de startActivityResult ha sido exitosa
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN ){
            if(resultCode == RESULT_OK ){
                Toast.makeText(this, "Welcome...", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        }
    }





    private void inicializarFirebase(){
        FirebaseApp.initializeApp(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }


    //leyendo desde Firebase-RTDB
    private void listarPersonas(){
        databaseReference.child("Personas").orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listPersona.clear();
                for(DataSnapshot objSnapshot: snapshot.getChildren()){
                    Persona p = objSnapshot.getValue(Persona.class);
                    listPersona.add(p);
                }
                //iniciar adaptador proopio
                listViewPersonasAdapter = new ListViewPersonasAdapter(MainActivity.this, listPersona);
                listViewPersonas.setAdapter(listViewPersonasAdapter);

                /***
                arrayAdapterPersona = new ArrayAdapter<Persona>(
                        MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        listPersona
                 listViewPersonas.setAdapter(arrayAdapterPersona);
                ); **/

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Toast.makeText(MainActivity.this, "Not READING from RTDB", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



    //metodo para seleccion de menús
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String nombres = inputNombre.getText().toString();
        String telefono = inputTelefono.getText().toString();
        switch (item.getItemId()){
            case R.id.menu_agregar:
                insertar();
                break;
            case R.id.menu_guardar:
                if(personaSeleccionada != null){
                    if(validarInputs()==false){
                         Persona p = new Persona();
                         p.setIdpersona(personaSeleccionada.getIdpersona());
                         p.setNombres(nombres);
                         p.setTelefono(telefono);
                         p.setFecharegistro(personaSeleccionada.getFecharegistro());
                         p.setTimestamp(personaSeleccionada.getTimestamp());
                         databaseReference.child("Personas").child(p.getIdpersona()).setValue(p);
                         Toast.makeText(this, "Updated correctly", Toast.LENGTH_LONG).show();
                         linearLayoutEditar.setVisibility(View.GONE);
                         personaSeleccionada = null;
                    }
                }else{
                        Toast.makeText(this, "Select a person", Toast.LENGTH_LONG).show();
                }
            case R.id.menu_eliminar:
                if(personaSeleccionada != null){
                    Persona p2 = new Persona();
                    p2.setIdpersona(personaSeleccionada.getIdpersona());
                    databaseReference.child("Personas").child(p2.getIdpersona()).removeValue();
                    linearLayoutEditar.setVisibility(View.GONE);
                    personaSeleccionada = null;
                    Toast.makeText(this, "Contact eliminated correctly", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(this, "select a person to eliminate", Toast.LENGTH_LONG).show();

                }
            case R.id.menu_logOut:
                FirebaseAuth.getInstance().signOut();
        }
        return super.onOptionsItemSelected(item);
    }


    public void insertar() {
        AlertDialog.Builder  mBuilder = new AlertDialog.Builder(
                MainActivity.this
        );
        View mView = getLayoutInflater().inflate(R.layout.insertar, null);
        Button btnInsertar = (Button) mView.findViewById(R.id.btnInsertar);
        final EditText mInputNombres = (EditText) mView.findViewById(R.id.inputNombre);
        final EditText mInputTelefono = (EditText) mView.findViewById(R.id.inputTelefono);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        btnInsertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  String nombres = mInputNombres.getText().toString();
                  String telefono = mInputTelefono.getText().toString();
                  if(nombres.isEmpty() || nombres.length()<3){
                      showError(mInputNombres, "INVALID NAME (MIN. 3 CHARACTERS )");
                  }else if(telefono.isEmpty() || telefono.length()<3){
                      showError(mInputTelefono, "INVALID PHONE (MIN. 3 NUMBERS)");
                  }else{
                      Persona p = new Persona();
                      p.setIdpersona(UUID.randomUUID().toString());
                      p.setNombres(nombres);
                      p.setTelefono(telefono);
                      p.setFecharegistro(getFechaNormal(getFechaMilisegundos()));
                      p.setTimestamp(getFechaMilisegundos()* -1);
             databaseReference.child("Personas").child(p.getIdpersona()).setValue(p);
        Toast.makeText(MainActivity.this, "REGISTERED CORRECTLY", Toast.LENGTH_LONG).show();
                  dialog.dismiss();
                  }

            }
        });
    }

    public void showError(EditText input, String s){
          input.requestFocus();
          input.setError(s);
    }

    public long getFechaMilisegundos(){
        Calendar calendar = Calendar.getInstance();
        long tiempounix = calendar.getTimeInMillis();
        return tiempounix;

    }

    public String getFechaNormal(long fechamilisegundos ){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-5"));
        String fecha = sdf.format(fechamilisegundos);
        return fecha;
    }

    public  boolean validarInputs(){
        String nombre = inputNombre.getText().toString();
        String telefono = inputTelefono.getText().toString();
        if(nombre.isEmpty() || nombre.length() < 3 ){
            showError(inputNombre, "Invalid Name, min. 3 characters");
            return true;
        }else if(telefono.isEmpty() || telefono.length() < 3){
            showError(inputTelefono, "Invalid phone (min. 3 numbers)");
            return true;
        } else {
            return false;
        }
    }

    //Método onResume se usa cuando el usuario reanuda la actividad en la applicación
    @Override
    protected void onResume() {
        super.onResume();
        //Escuchando la actividad del usuario cuando otra actividad se pasa a primer plano
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }






    //método onPause se usa cuando otra actividad del usuario se antepone en primer plano
    @Override
    protected void onPause() {
        super.onPause();
        //verificar si el usuario sigue logueado
        if (mFirebaseAuth != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

    }




}