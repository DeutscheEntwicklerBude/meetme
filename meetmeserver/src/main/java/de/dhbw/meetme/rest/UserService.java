package de.dhbw.meetme.rest;

import de.dhbw.meetme.database.dao.UserDao;
import de.dhbw.meetme.domain.User;
import de.dhbw.meetme.domain.UuidId;
import groovy.lang.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
//@Produces({"application/json"}) // mime type
@Singleton
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  @Inject
  UserDao userDao;

  @Path("/list")
  @GET
  public Collection<User> list() {
    log.debug("List users");
    return userDao.list();
  }

  @Path("/get/{id}")
  @GET
  public User get(@PathParam("id") String id) {
    log.debug("Get user " + id);
    return userDao.get(UuidId.fromString(id));
  }

  @Path("/delete/{id}")
  @DELETE
  public void delete(@PathParam("id") String id) {
    log.debug("Delete user " + id);
    userDao.delete(UuidId.fromString(id));
  }

  @Path("/save")
  @POST
  public String save(@FormParam("regEmail") String regEmail, @FormParam("userName") String userName, @FormParam("registerPassword") String registerPassword, @PathParam("nationality") String nationality, @PathParam("description") String description){
    // prüfen ob alle Felder ausgefüllt sind...sollte eig schon bei dem Client geprüft worden sein
    if ( registerPassword == "" | regEmail == "" | userName == "" ){
      log.debug(userName + "tried to register but not all forms were filled");
      return "Bitte fülle alle Felder aus";
    }
    // prüfen ob der userName schon vergebe ist
    if (userDao.existCeckName(userName) == true)
    {
      log.debug(userName +"tried ro register but the userName is already in use");
      return "Leider existiert dein Username breits, suche dir bitte einen neuen aus";
    }
    //prüfen ob die Mail schon verwendent worden ist.
    if (userDao.existCeckMail(regEmail) == true)
    {
      log.debug(userName +"tried ro register but the regMail is already in use");
      return "Du hast mit der Mail bereits einen Account erstellt";
    }
    //userDao.persist(regEmail, userName, registerPassword);
    //userDao.persist(user);
    //log.debug("Save user " + user);
    return  "Hallo " + userName +" du wurdest erfolgereich registriert! :) deine Mail: " +  regEmail;
  }

}
