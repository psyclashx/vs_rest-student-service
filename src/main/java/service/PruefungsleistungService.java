package service;

import app.OTHRestException;
import entity.Pruefungsleistung;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("studentaffairs")
public class PruefungsleistungService {

    @Path("students/{id}/pruefungsleistungen")
    @POST
    public Pruefungsleistung pruefungsleistungEintragen(@PathParam("id") int matrikelNr, Pruefungsleistung pl) {

        Connection c = null;
        try {
            // Automatischen Transaktionsabschluss fuer jedes Statement ausschalten
            c.setAutoCommit(false);

            // ECTS-Anzahl fuer diese Pruefung laden
            PreparedStatement statement = c.prepareStatement("SELECT ects FROM Pruefung WHERE pruefungId=?");
            statement.setString(1, pl.getPruefungId()); // ersetzt das erste ? durch die pruefungId
            ResultSet result = statement.executeQuery();
            if (!result.first()) {
                // WebApplicationException bzw. der Response.Status (HTTP-Status) steuert die Rueckgabe-Status an den Client
                throw new OTHRestException(409, "Pruefung mit id " + pl.getPruefungId() + " not found");
            }
            int ects = result.getInt("ects");

            // ECTS gutschreiben
            statement = c.prepareStatement("UPDATE Student SET ects=ects+? WHERE matrikelNr=?");
            statement.setInt(1, ects);
            statement.setInt(2, matrikelNr);
            int affected = statement.executeUpdate(); // falsche MatrikelNr?!?
            if (affected != 1) {
                c.rollback();
                throw new OTHRestException(409, "ECTS-Summe konnte nicht erhöht werden");
            }

            // Note auf Gueltigkeit pruefen
            if (!(pl.getNote().equals("1.0") || pl.getNote().equals("1.3") ||
                    pl.getNote().equals("1.7") || pl.getNote().equals("2.0") || pl.getNote().equals("2.3") ||
                    pl.getNote().equals("2.7") || pl.getNote().equals("3.0") || pl.getNote().equals("3.3") ||
                    pl.getNote().equals("3.7") || pl.getNote().equals("4.0") ||
                    pl.getNote().equals("5.0"))) {
                c.rollback();
                throw new OTHRestException(409, "Falscher Notenwert");
            }

            // 1./2./3.-Versuch pruefen
            statement = c.prepareStatement("SELECT MAX(versuch), MIN(note) FROM Pruefungsleistung WHERE pruefungId=? AND matrikelNr=?");
            statement.setString(1, pl.getPruefungId());
            statement.setInt(2, matrikelNr);
            result = statement.executeQuery();
            if (result.first()) {
                short maxVersuch = result.getShort("MAX(versuch)");
                String minNote = result.getString("MIN(note)");
                if (maxVersuch > 2) {
                    c.rollback();
                    throw new OTHRestException(409, "Maximale Versuche überschritten");
                }
                if (maxVersuch >= pl.getVersuch()) {
                    c.rollback();
                    throw new OTHRestException(409, "Versuch bereits eingetragen");
                }
                if (minNote != null) {
                    if (Double.parseDouble(minNote) < 4.1) {
                        c.rollback();
                        throw new OTHRestException(409, "Bestandener Versuch bereits eingetragen");
                    }
                }
            }

            // Pruefungsleistung eintragen
            statement = c.prepareStatement("INSERT INTO Pruefungsleistung (id, pruefungId, matrikelNr, versuch, note) VALUES (NULL, ?, ?, ?, ?)");
            statement.setString(1, pl.getPruefungId());
            statement.setInt(2, matrikelNr);
            statement.setShort(3, pl.getVersuch());
            statement.setString(4, pl.getNote());
            statement.execute();

            // Automatisch vergebene ID lesen
            statement = c.prepareStatement("SELECT LAST_INSERT_ID()");
            result = statement.executeQuery();
            result.first();
            int id = result.getInt(1);

            // Pruefungsleistung-Objekt aktualisieren
            pl.setId(id);

            // Datenbanktransaktion abschliessen
            c.commit();

            return pl;

        } catch (SQLException ex) {
            try {
                c.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            throw new OTHRestException(500, ex.getMessage());
        } catch (OTHRestException ex) {
            try {
                c.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            throw ex;
        }


    }
}
