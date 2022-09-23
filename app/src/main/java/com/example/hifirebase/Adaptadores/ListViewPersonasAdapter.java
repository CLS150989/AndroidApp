package com.example.hifirebase.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.hifirebase.Models.Persona;
import com.example.hifirebase.R;

import java.util.ArrayList;

public class ListViewPersonasAdapter extends BaseAdapter {

    Context context;
    ArrayList<Persona> personaData;
    LayoutInflater layoutInflater;
    Persona peronsaModel;



    public ListViewPersonasAdapter(Context context, ArrayList<Persona> personaDate) {
        this.context = context;
        this.personaData = personaDate;
        layoutInflater = (LayoutInflater)
                context.getSystemService
                        (context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return personaData.size();
    }

    @Override
    public Object getItem(int position) {
        return personaData.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View rowView = convertView;
        if(rowView == null){
            rowView = layoutInflater.inflate(R.layout.lista_personas,
                    null, true);
        }
        TextView nombres = rowView.findViewById(R.id.nombres);
        TextView telefono = rowView.findViewById(R.id.telefono);
        TextView fecharegistro = rowView.findViewById(R.id.fechaRegistro);

        peronsaModel = personaData.get(position);
        nombres.setText(peronsaModel.getNombres());
        telefono.setText(peronsaModel.getTelefono());
        fecharegistro.setText(peronsaModel.getFecharegistro());

        return rowView;
    }
}
