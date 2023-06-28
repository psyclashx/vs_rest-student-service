package entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Pruefungsleistung {

    private String pruefungId;
    private String note;
    private short versuch;

    public String getPruefungId() {
        return pruefungId;
    }

    public void setPruefungId(String pruefungId) {
        this.pruefungId = pruefungId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public short getVersuch() {
        return versuch;
    }

    public void setVersuch(short versuch) {
        this.versuch = versuch;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;
}
