package de.dhbw.meetme.rest;

import de.dhbw.meetme.database.Transaction;
import de.dhbw.meetme.database.dao.UserDao;
import de.dhbw.meetme.domain.User;
import de.dhbw.meetme.domain.UuidId;
import groovy.lang.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Collection;

/**
 *
 */
@Path("/api/user")
@Produces({"application/json"}) // mime type
@Singleton
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  @Inject
  UserDao userDao;
  Transaction transaction;

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
  public String save(@FormParam("regEmail") String email, @FormParam("userName") String username, @FormParam("password") String password, @FormParam("nation") String nation, @FormParam("description") String description) {
    User user = new User();
    user.setName(username);

    userDao.persist(user);
    //log.debug("Save user " + user);
    return "TEST";
  }

  /*@Path("/login")
  @GET

  public String login(@HeaderParam("Authorization") String authPara){

    log.debug("authPara: "+ authPara);
    final String authorization =  authPara; //httpRequest.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Basic")) {
      // Authorization: Basic base64credentials
      String base64Credentials = authorization.substring("Basic".length()).trim();
      String credentials = new String(Base64.getDecoder().decode(base64Credentials),
              Charset.forName("UTF-8"));
      // credentials = username:password
      final String[] values = credentials.split(":",2);
      log.debug("username "+ values[0]);
      log.debug("password "+ values[1]);
      User s= null;
      try{
       s= userDao.findByUserName(values[0]);}
      catch (Exception e ){
        log.debug("Fehler user gibt es nicht ");
        return "false";
      }
      log.debug("user"+ s);

      if (values[1].equalsIgnoreCase(s.getPassword())){
      return "true";}
      else return "false";
    }
    return "false";

  }*/



}
