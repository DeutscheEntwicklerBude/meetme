package de.dhbw.meetme.rest;

import com.sun.xml.internal.bind.v2.TODO;
import de.dhbw.meetme.database.Transaction;
import de.dhbw.meetme.database.dao.AppReplyDao;
import de.dhbw.meetme.database.dao.AppointmentDao;
import de.dhbw.meetme.database.dao.LecturerDao;
import de.dhbw.meetme.database.dao.UrgentAppointmentDao;
import de.dhbw.meetme.domain.AppReply;
import de.dhbw.meetme.domain.Appointment;
import de.dhbw.meetme.domain.Lecturers;
import de.dhbw.meetme.domain.UrgentAppointment;
import groovy.lang.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 *
 */
@Path("api/notification")
@Produces({"application/json"}) // mime type
@Singleton
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    LecturerDao lecturerDao;
    @Inject
    AppointmentDao appointmentDao;
    @Inject
    UrgentAppointmentDao urgentAppointmentDao;
    @Inject
    AppReplyDao appReplyDao;

    @Inject
    Transaction transaction;





    @GET
    @Path("/{lecturerName}")
    // returns a redirect if the DB has a entry of outstanding notification
    //TODO verfiication wenn mehrere nachrichten ausstehen
    //tested
    public Response getOpenUrgentAppointments(@PathParam("lecturerName") String lecturerName){
        log.debug("Test1: " + lecturerName);
        transaction.begin();
        if (urgentAppointmentDao.getOpenUrgentAppointment(lecturerName).isEmpty())
        {
            log.debug("urgent appointment" + lecturerName + ": no urgentAppointment");
            transaction.commit();
            return null;

        }
        else {

            /*log.debug("urgent appointment" + lecturerName+ " :" +urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName));

            transaction.commit();
            return urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName);
            */
            log.debug("Es gibt urgent appointments: " + lecturerName );
            transaction.commit();
            URI location = null;
            try {
                location = new java.net.URI("showAppointments.html");

            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new WebApplicationException(Response.temporaryRedirect(location).build());
    }}
    @GET
    @Path("/urgentApp/{lecturerName}")
    // returns the DB entry of outstanding notifications
    //TODO verfiication wenn mehrere nachrichten ausstehen
    //tested
    public UrgentAppointment getOpenUrgentAppointmentsData(@PathParam("lecturerName") String lecturerName){
        transaction.begin();
        if (urgentAppointmentDao.getOpenUrgentAppointment(lecturerName).isEmpty())
        {
            log.debug("urgent appointment" + lecturerName + ": no urgentAppointment");
            transaction.commit();
            return null;
        }
        else {
            log.debug("urgent appointment" + lecturerName+ " :" +urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName));
            transaction.commit();
            return urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName);
        }}

    @GET
    @Path("/AppReply/redirect/{lecturerName}")
    // returns the redirect to the website so see the lecturers reply
    //TODO ok button um processed auf true zu setzen
    //
    public AppReply getOpenAppReplyRedirect(@PathParam("lecturerName") String lecturerName){
        URI location = null;
        transaction.begin();
        if (appReplyDao.getOpenAppReply(lecturerName)== null)
        {
            log.debug("AppReply" + lecturerName + ": no AppReply");
            transaction.commit();
            try {
                location = null;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {
            log.debug("AppReply" + lecturerName );

            transaction.commit();
            try {
                location = new java.net.URI("advisorInterface.html");   // new java.net.URI("loginPage.html");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        throw new WebApplicationException(Response.temporaryRedirect(location).build());
    }

    @GET
    @Path("/AppReply/{lecturerName}")
    // returns the DB entry of outstanding AppReplys
    //TODO ok button um processed auf true zu setzen
    //tested
    public AppReply getOpenAppReply(@PathParam("lecturerName") String lecturerName){
        transaction.begin();
        if (appReplyDao.getOpenAppReply(lecturerName)== null)
        {
            log.debug("AppReply" + lecturerName + ": no AppReply");
            transaction.commit();
            return null;

        }
        else {
            //UrgentAppointment myUrgentAppointment =  urgentAppointmentDao.getOpenUrgentAppointment(lecturerName);


            log.debug("AppReply" + lecturerName );
            AppReply myAppReply = appReplyDao.getOpenAppReply(lecturerName);
            appReplyDao.getOpenAppReply(lecturerName).setProcessed(true);
            log.debug( "Test1: "+ lecturerName+ " : "); //+appReplyDao.getOpenAppReply(lecturerName).isProcessed() );
            transaction.commit();
            return myAppReply;
        }}
     @POST
     @Path("/urgentApp")
     //tested
     public boolean requestUrgentApp(@FormParam("studentName") String studentName, @FormParam("lecturerName") String lecturerName, @FormParam("topic") String topic, @FormParam("studentMail")String studentMail, @FormParam("course")String course){
            transaction.begin();


            UrgentAppointment urgentAppointment = new UrgentAppointment(lecturerName,studentName,studentMail,course,topic,false);
             urgentAppointmentDao.persist(urgentAppointment);
            log.debug("urgent appointment" + lecturerName+ " :" +urgentAppointmentDao.getOpenUrgentAppointment(lecturerName)+ "wurde gespeichert");
            transaction.commit();
         return true;
        }

    @POST
    @Path("/AppReply")
    //tested
    public Response replyToRequest( @FormParam("lecturerName") String lecturerName, @FormParam("reply") String reply, @FormParam("message")String message,@FormParam("personalMessage")String pmessage)throws IOException{

        URI location = null;
        log.debug( "AppReplay beginn "+ lecturerName);
        log.debug( "LecturerName: "+ lecturerName + ", reply: "+ reply+", message: "+message+", pmessage: "+pmessage);

        if (lecturerName ==null )
        {
            log.debug( "AppReplay hat kein oder falschen lecturer");
            return null;
        }
        if ( reply ==null )
        {
            log.debug( "AppReplay reply wurde null gesetzt ");
            reply = null;
        }
        if ( message ==null )
        {
            log.debug( "AppReplay message wurde null gesetzt ");
            message = null;
        }
        else if ( message.equals("message1") )
        {
            log.debug( "AppReplay message wurde \"In 10 minutes\" gesetzt ");
            message = "In 10 minutes";
        }
        else if ( message.equals("message2") )
        {
            log.debug( "AppReplay message wurde \"In 1 h\" gesetzt ");
            message = "In 1 h";
        }
        else if ( message.equals("message3") )
        {
            log.debug( "AppReplay message wurde \"This afternoon\" gesetzt ");
            message = "This afternoon";
        }
        else if ( message.equals("message4") )
        {
            log.debug( "AppReplay message wurde \"The student should make an appointment\" gesetzt ");
            message = "The student should make an appointment";
        }

        if (pmessage==null){
          log.debug( "AppReplay pmessage wurde null gesetzt ");
          pmessage = null;}

        transaction.begin();
        AppReply appReply= new AppReply(lecturerName,message,pmessage,reply,false);
        appReplyDao.persist(appReply);
        urgentAppointmentDao.getOpenUrgentAppointment2(lecturerName).setProgressed(true);
        log.debug( "AppReplay erfolgreich");
        lecturerDao.findLecturer(lecturerName).setLecturerAvailability(false);
        transaction.commit();

        try {
            location = new java.net.URI("advisorInterface.html");   // new java.net.URI("loginPage.html");
        } catch (Exception e) {
            e.printStackTrace();
        }


    throw new WebApplicationException(Response.temporaryRedirect(location).build());
    }
/*
    @POST
    @Path("/urgentAppointment")
        public void appReq(@FormParam("lecturerName") String lecturerName, @FormParam("studentFirstname") String studentFirstname,@FormParam("studentLastname") String studentLastname, @FormParam("topic") String topic, @FormParam("course") String course, @FormParam("studentMail") String studentMail) {
        UrgentAppointment urgentAppointment = new UrgentAppointment(lecturerName, studentFirstname + " "+studentLastname, studentMail,course,topic,false);

        urgentAppointmentDao.persist(urgentAppointment);
        log.debug("Save urgentAppointment " + urgentAppointment);

    }

    @POST
    @Path("/appointment")
        public String appointment(@FormParam("lecturerName") String lecturerName, @FormParam("studentName") String studentName, @FormParam("studentMail") String studentMail, @FormParam("topic") String tobic, @FormParam("date") String date, @FormParam("proposedTime") String proposedTime,){
        Appointment appointment = new Appointment(lecturerName, date, proposedTime, studentName, studentMail);
        appointmentDao.persist(appointment);
        log.debug("Save appoinment " + appointment);
        return "save appointment";
        }

    @GET
    @Path("/availability/{lecturerName}")
        public boolean availability(@PathParam("lecturerName") String lecturerName){
        Lecturers lecturer = lecturerDao.findLecturer(lecturerName);
        return lecturer.isLecturerAvailability();
    }*/
}
