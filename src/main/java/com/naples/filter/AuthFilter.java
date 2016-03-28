package com.naples.filter;

import java.lang.reflect.Method;

import javax.annotation.security.PermitAll;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.naples.helper.Error;

@Provider
public class AuthFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Method method = resourceInfo.getResourceMethod();

        if ( !method.isAnnotationPresent(PermitAll.class) ) {
            if (!authenticate("token")) requestContext.abortWith(
                Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build()
            );
        }
    }

    private boolean authenticate(String jsonWebToken) {
        boolean isAllowed = true;

        return isAllowed;
    }
}