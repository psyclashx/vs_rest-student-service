package entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Adresse {

    protected String strasse;
    protected String plz;
    protected String ort;

    // Konstruktor ohne Parameter ist zwingend erforderlich!
    public Adresse() {

    }
}
