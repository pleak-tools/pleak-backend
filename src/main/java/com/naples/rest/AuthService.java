package com.naples.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;

import org.hibernate.Session;
import org.hibernate.Filter;
import org.hibernate.HibernateException;

import com.naples.helper.Token;
import com.naples.util.HibernateUtil;
import com.naples.util.KeyUtil;
import com.naples.user.User;

@Path("/auth")
public class AuthService {

  @GET
  public Response check() {
    return Response.ok().build();
  }

  @POST
  @Path("/register")
  @DenyAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response register(User user) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    session.beginTransaction();

    try {
      user.createHashedPassword(user.getPassword());
      session.save(user);
      Key key = KeyUtil.getKey();
      String jwToken = Jwts.builder().setSubject(user.getId().toString())
                                     .claim("email", user.getEmail())
                                     .signWith(SignatureAlgorithm.HS512, key).compact();
      return Response.status(201).entity("Hello").header("JSON-Web-Token", jwToken).build();
    } catch(Exception e) {
      e.printStackTrace();
      return Response.status(409).entity("Nope").build();
    } finally {
      session.getTransaction().commit();
      session.close();
    }

  }

  @OPTIONS
  @Path("/login")
  @PermitAll
  public Response login() {
    return Response.ok().build();
  }

  @POST
  @Path("/login")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response login(User user) {

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
      if (dbUser.isCorrectPassword(user.getPassword())) {
        Key key = KeyUtil.getKey();
        String jwToken = Jwts.builder().setSubject(dbUser.getId().toString())
                                       .claim("email", dbUser.getEmail())
                                       .signWith(SignatureAlgorithm.HS512, key).compact();
        return Response.status(200).entity(new Token(jwToken)).build();
      } else {
        return Response.status(403).entity(new Error("Wrong password.")).build();
      }

    } catch(HibernateException e) {
      //e.printStackTrace();
      return Response.status(400).entity(new Error("Input not valid.")).build();
    } catch(Exception e) {
      //e.printStackTrace();
      return Response.status(400).entity(new Error("Server error.")).build();
    } finally {
      session.close();
    }

  }

  @GET
  @Path("/logout")
  public Response logout(@Context ContainerRequestContext crc) {
    int userId = (int) crc.getProperty("userId");
    Response resp = Response.status(200).entity(new Token("")).build();
    return resp;
  }

}
