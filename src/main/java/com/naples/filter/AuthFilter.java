package com.naples.filter;

import java.lang.reflect.Method;

import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import java.security.Key;

import com.naples.helper.Error;
import com.naples.util.KeyUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

@Provider
public class AuthFilter implements ContainerRequestFilter {

  @Context
  private ResourceInfo resourceInfo;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    Method method = resourceInfo.getResourceMethod();

    if (requestContext.getMethod().equals("OPTIONS")) {
      // Do nothing
    } else if ( method.isAnnotationPresent(DenyAll.class) ) {
      requestContext.abortWith(
        Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build()
      );
    } else if ( !method.isAnnotationPresent(PermitAll.class) ) {
      String jsonWebToken = requestContext.getHeaderString("json-web-token");
      int userId = authenticate(jsonWebToken);
      if (userId == -1) {
        requestContext.abortWith(
          Response.status(401).entity(new Error("Unauthorized.")).type(MediaType.APPLICATION_JSON).build()
        );
      } else {
        requestContext.setProperty("userId", userId);
      }
    }
  }

  private int authenticate(String jsonWebToken) {
    String userIdStr;
    int userId = -1;
    Key key = KeyUtil.getKey();

    try {
      if (jsonWebToken != null && jsonWebToken.length() != 0 && !jsonWebToken.equals("null"))  {
        userIdStr = Jwts.parser().setSigningKey(key).parseClaimsJws(jsonWebToken).getBody().getSubject();
        userId = Integer.parseInt(userIdStr);
      }
    } catch(SignatureException e) {
      //e.printStackTrace();
    } catch(Exception e) {
      //e.printStackTrace();
    }

    return userId;
  }


}
