package com.naples.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.annotation.security.PermitAll;

import org.hibernate.Session;
import org.hibernate.Filter;

import com.naples.helper.Token;
import com.naples.util.HibernateUtil;
import com.naples.user.User;

@Path("/user")
public class UserService {

  @POST
  @Path("/exists")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response exists(User user) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    session.beginTransaction();
    Filter filter = session.enableFilter("userFilterByEmail");
    filter.setParameter("userFilterParam", user.getEmail());

    try {
      filter.validate();

      List<User> users = (List<User>) session.createCriteria(User.class).list();
      if (users.size() == 0) {
        return Response.status(404).entity(new Error("User not found.")).build();
      }
      User dbUser = (User) users.get(0);

      return Response.ok().build();
    } catch(Exception e) {
      e.printStackTrace();
      return Response.status(409).entity("Server error.").build();
    } finally {
      session.getTransaction().commit();
      session.close();
    }

  }


}
