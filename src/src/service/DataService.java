package service;

import model.DatiStatici;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Bene;
import model.Movimento;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DataService {
    private static final String BENI_FILE = "beni.json";
    private static final String MOVIMENTI_FILE = "movimenti.json";
    private static final String DATI_STATICI_FILE = "dati_statici.json";
    private Gson gson;

    public DataService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<Bene> caricaBeni() {
        try {
            File file = new File(BENI_FILE);
            if (!file.exists()) {
                List<Bene> beniDefault = new ArrayList<>();
                beniDefault.add(new Bene("Primer ML50", "lt"));
                beniDefault.add(new Bene("Lame corrisoglia sentra+ ottone", "ml"));
                salvaBeni(beniDefault);
                return beniDefault;
            }
            FileReader reader = new FileReader(file);
            java.lang.reflect.Type listType = new TypeToken<ArrayList<Bene>>() {
            }.getType();
            List<Bene> beni = gson.fromJson(reader, listType);
            reader.close();
            return beni == null ? new ArrayList<>() : beni;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void salvaBeni(List<Bene> beni) {
        try {
            FileWriter writer = new FileWriter(BENI_FILE);
            gson.toJson(beni, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Movimento> caricaMovimenti() {
        try {
            File file = new File(MOVIMENTI_FILE);
            if (!file.exists()) return new ArrayList<>();
            FileReader reader = new FileReader(file);
            java.lang.reflect.Type listType = new TypeToken<ArrayList<Movimento>>() {
            }.getType();
            List<Movimento> movimenti = gson.fromJson(reader, listType);
            reader.close();
            return movimenti == null ? new ArrayList<>() : movimenti;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void salvaMovimenti(List<Movimento> movimenti) {
        try {
            FileWriter writer = new FileWriter(MOVIMENTI_FILE);
            gson.toJson(movimenti, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public model.DatiStatici caricaDatiStatici() {
        try {
            File file = new File(DATI_STATICI_FILE);
            if (!file.exists()) {
                DatiStatici dati = new DatiStatici();
                salvaDatiStatici(dati);
                return dati;
            }
            FileReader reader = new FileReader(file);
            DatiStatici dati = gson.fromJson(reader, DatiStatici.class);
            reader.close();
            return dati == null ? new DatiStatici() : dati;
        } catch (Exception e) {
            e.printStackTrace();
            return new DatiStatici();
        }
    }

    public void salvaDatiStatici(DatiStatici dati) {
        try {
            FileWriter writer = new FileWriter(DATI_STATICI_FILE);
            gson.toJson(dati, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
