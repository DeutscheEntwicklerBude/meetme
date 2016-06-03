package de.dhbw.meetme.rest;

import de.dhbw.meetme.database.Transaction;
import de.dhbw.meetme.database.dao.*;
import de.dhbw.meetme.domain.*;
import groovy.lang.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by B-Ron on 24.09.2015.
 * This class handles all GPS-related interactions.
 */
@Path("api/admin")
@Produces({"application/json"}) // mime type
@Singleton
public class YoloAdmin {
   //bullshit class
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    UserDao userDao;
    @Inject
    LecturerDao lecturerDao;

    @Inject
    UrgentAppointmentDao urgentAppointmentDao;
    @Inject
    AppointmentDao appointmentDao;
    @Inject
    AppReplyDao appReplyDao;

    @Inject
    Transaction transaction;
/*
    @GET
    @Path("/setNations")
    //returns list of all GPS Locations saved in the database
    public String setNations(){
            transaction.begin();
        if(teamBoardDao.leaderTeamBoard().isEmpty()){
            TeamBoard d = new TeamBoard("german");
            TeamBoard n = new TeamBoard("notGerman");
            teamBoardDao.persist(d);
            teamBoardDao.persist(n);
            transaction.commit();
            return "german und notGerman wurden erstellt";
        }
        transaction.commit();
        return "es sind bereits Nationen in der Datenbank";

    }
*/
    @GET
    @Path("/test/{Fname}/{Lname}/{topic}/{lecturerMail}/{lecturerPw}")
    public Lecturers getTeamboard(@PathParam("Fname")String Fname, @PathParam("Lname") String Lname,@PathParam("topic") String topic,@PathParam("lecturerMail") String lecturerMail,@PathParam("lecturerPw") String lecturerPw ){
        transaction.begin();
        Lecturers lecturer = new Lecturers(Fname,Lname, topic,lecturerMail,lecturerPw, true);
        lecturerDao.persist(lecturer);
        transaction.commit();

        return lecturer;
    }

    @GET
    @Path("/appointment/{advisor}/{name}/{surname}/{email}/{topic}/{date}/{time}/{course}")
    //to schedule an appointment in advance
    //course muss angegeben werden
    public Response appointment(@PathParam("advisor") String lecturerName, @PathParam("name") String studentFName,@PathParam("surname") String studentLName, @PathParam("email") String studentMail, @PathParam("topic") String topic, @PathParam("date") String date, @PathParam("time") String proposedTime,@PathParam("course") String course){
        transaction.begin();
        if (course.equals("") || course == null){
            course = "N/A";
        }
        Appointment appointment = new Appointment(lecturerName, date, proposedTime, studentFName,studentLName, studentMail,course,topic);
        appointmentDao.persist(appointment);
        log.debug("Save appoinment " + appointment);
        transaction.commit();

        URI location = null;
        try {
            location = new java.net.URI("appointmentSuccess.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new WebApplicationException(Response.temporaryRedirect(location).build());
    }

    @GET
    @Path("/login/{email}/{password}")
    //verify the lecturer by checking mail and password
    public boolean login(@PathParam("email") String mail, @PathParam("password") String pw){
        Lecturers lecturer = lecturerDao.findLecturerMail(mail);
        if (lecturer.getLecturerPw().equals(pw)){
            return true;
        }
        else return false;

    }

    @GET
    @Path("/resetDatabase/{lecturerName}")
    public void resetDatabase(@PathParam("lecturerName") String lecturerName){
        transaction.begin();
        try
        {
        urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName).setProgressed(true);

            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.commit();
        }


    }

    @GET
    @Path("/availability/{setAvailability}/{lecturerName}")
    //set the availability ...boolean true for availabale or false for unavailable
    public boolean setAvailability(@PathParam("setAvailability") boolean availability,@PathParam("lecturerName") String lecturerName){
        transaction.begin();
        log.debug("Test1: " + availability);
        Lecturers lecturer = lecturerDao.findLecturer(lecturerName);
        log.debug("Test2: " + lecturerName);
        log.debug("Test3: " + lecturer.getLecturerLastname());
        lecturer.setLecturerAvailability(availability);
        log.debug("Test4: " + lecturer.isLecturerAvailability());
        transaction.commit();
        return lecturer.isLecturerAvailability();
    }

    @GET
    @Path("/{lecturerName}")
    // returns the DB entry of outstanding notifications
    //TODO verfiication wenn mehrere nachrichten ausstehen
    public UrgentAppointment getOpenUrgentAppointments(@PathParam("lecturerName") String lecturerName){
        transaction.begin();
        log.debug("Test1: " + lecturerName );
        if (urgentAppointmentDao.getOpenUrgentAppointment(lecturerName).isEmpty())
        {
            log.debug("urgent appointment" + lecturerName + ": no urgentAppointment");
            transaction.commit();
            return null;

        }
        else {
            //UrgentAppointment myUrgentAppointment =  urgentAppointmentDao.getOpenUrgentAppointment(lecturerName);


            log.debug("urgent appointment" + lecturerName+ " :" +urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName));

            transaction.commit();
            return urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName);
        }}

    @GET
    @Path("/urgentApp/{studentName}/{lecturerName}/{topic}/{studentMail}/{course}")
    public boolean requestUrgentApp(@PathParam("studentName") String studentName, @PathParam("lecturerName") String lecturerName, @PathParam("topic") String topic, @PathParam("studentMail")String studentMail, @PathParam("course")String course){
        transaction.begin();
        UrgentAppointment urgentAppointment = new UrgentAppointment(lecturerName,studentName,studentMail,course,topic,false);
        urgentAppointmentDao.persist(urgentAppointment);
        log.debug("urgent appointment" + lecturerName+ " :" +urgentAppointmentDao.getOpenUrgentAppointment(lecturerName)+ "wurde gespeichert");
        transaction.commit();
        return true;
    }
    @GET
    @Path("/AppReply/{studentName}/{lecturerName}/{reply}/{message}/{personalMessage}")
    public boolean replyToRequest(@PathParam("studentName") String studentName, @PathParam("lecturerName") String lecturerName, @PathParam("reply") String reply, @PathParam("message")String message,@PathParam("personalMessage")String pmessage){
        transaction.begin();
        AppReply appReply= new AppReply(lecturerName,message,pmessage,reply,false);
        appReplyDao.persist(appReply);
        urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName).setProgressed(true);
        transaction.commit();
        return true;
    }

}