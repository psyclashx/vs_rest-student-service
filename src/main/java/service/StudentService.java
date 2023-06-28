package service;

import app.OTHRestException;
import app.Server;
import com.hazelcast.core.ReplicatedMap;
import entity.Adresse;
import entity.Student;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static app.Server.*;

@Path("studentaffairs")  // alternativer Pfad: @Path("studentaffairs/students") --> ersetzt dann alle @Path-Annotationen unten
public class StudentService {

    private static AtomicInteger nextStudentId = new AtomicInteger(1);
    private static ConcurrentMap<Integer, Student> studentDb = new ConcurrentHashMap<>();

    @POST
    @Path("students")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Student matriculate(Student s) {

        s.setMatrikelNr(nextStudentId.getAndIncrement());
        studentDb.put(s.getMatrikelNr(), s);
        return s;


    }

    // @Produces, @Consumes: siehe beispielhaft oben!
    // kann für alle weiteren Methoden entsprechend angefügt werden.

    // Erlaubt @Produces mehrere Media-Types, so kann im Response-Header der Wert
    //    Accept: application/json    bzw.
    //    Accept: application/xml
    // angegeben werden.
    // Fehlt das "Accept", so verwendet der Server seinen konfigurierten Default-Media-Type.


    @DELETE
    @Path("students/{id}")
    public Student exmatriculate(@PathParam("id") int studentId) {

        if(studentDb.containsKey(studentId)) {
            Student geloescht = studentDb.remove(studentId);
            return geloescht;
        } else {
            throw new OTHRestException(404, "Student mit ID " + studentId + " ist nicht immatrikuliert");
        }

    }

    @GET
    @Path("students/{id}")
    public Student getStudentById(@PathParam("id") int studentId) {

        Student s = null;
        Connection c = null;

        //Replicated map students, zeigt auf das IMDG
        ReplicatedMap<Integer, Student> students = hazelcast.getReplicatedMap("students");
        //              key    value


        Student student = students.get(studentId);

        if(student != null) {
            System.out.println("Student in Data Grid gefunden: " + student);
            return student;
        }

        try {
            c = DriverManager.getConnection(DB_CONNECTION, DB_USERNAME, DB_PASSWORD);
            Statement statement = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String query = "SELECT vorname, nachname, strasse, ort FROM Student WHERE matrikelNr=" + studentId;
            ResultSet result = statement.executeQuery(query);
            if(result.first()) {
                s = new Student(
                        studentId,
                        result.getString("vorname"),
                        result.getString("nachname"),
                        new Adresse(
                                result.getString("strasse"),
                                result.getString("ort")
                        ));
            } else {
                throw new OTHRestException(404, "Student mit ID " + studentId + " ist nicht immatrikuliert");
            }
            c.close();

            students.put(s.getMatrikelNr(), s, 5L, TimeUnit.MINUTES);
            System.out.println("Student aus  Datenbank gelesen: " + s);


            return s;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @PUT
    @Path("students/{id}")
    public Student updateStudentAccount(@PathParam("id") int studentId, Student newData) {

        newData.setMatrikelNr(studentId);

        if(studentDb.containsKey(studentId)) {
            studentDb.put(studentId, newData);
            return newData;
        } else {
            throw new OTHRestException(404, "Student mit ID " + studentId + " ist nicht immatrikuliert");
        }

    }

    // Diese Methode hätte eigentlich folgende Annotationen:
    // @GET
    // @Path("students")
    // Diese sind jedoch identisch mit den Annotationen von getStudentsByRange (s. u.)
    public Collection<Student> getAllStudents() {

        return studentDb.values();

    }

    @GET
    @Path("students")
    public Collection<Student> getStudentsByRange(@QueryParam("from") int fromStudentId, @QueryParam("to") int toStudentId) {

        /* Beispiele für mögliche Resource-Pfade zum Aufruf dieser Methode:

              /restapi/studentaffairs/students?from=100&to=108
              /restapi/studentaffairs/students?from=100
              /restapi/studentaffairs/students?to=108
              /restapi/studentaffairs/students

              Die Angabe der Query-Parameter "from" und "to" ist also nicht zwingend erforderlich.
              Werden Sie weggelassen wird entsprechend der Wert 0 übergeben
         */
        if(fromStudentId == 0 && toStudentId == 0)
            return getAllStudents();
        else if(toStudentId == 0 && fromStudentId > 0)
            return studentDb.values()
                    .stream()
                    .filter( student ->
                            student.getMatrikelNr() >= fromStudentId)
                    .collect(Collectors.toSet());
        else
            return studentDb.values()
                   .stream()
                   .filter( student ->
                           student.getMatrikelNr() >= fromStudentId
                                   && student.getMatrikelNr() <= toStudentId)
                   .collect(Collectors.toSet());

    }
}
