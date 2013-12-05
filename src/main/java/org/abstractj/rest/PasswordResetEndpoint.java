package org.abstractj.rest;

import org.abstractj.api.ExpirationTime;
import org.abstractj.model.Token;
import org.abstractj.service.TokenService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

@Path("/")
public class PasswordResetEndpoint {

    @Inject
    private TokenService tokenService;
    @Inject
    private ExpirationTime expirationTime;

    @POST
    @Path("/forgot")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String forgot(String email) {

        Token token = null;
        int hours = 1;

        //Here of course we need to validate the e-mail against the database or PicketLink
        if (email != null || !email.isEmpty()) {
            ExpirationTime expirationTime = new ExpirationTime(hours);
            token = tokenService.generate(expirationTime);
        }
        //It' base64 encoded but also can be an Hex
        return uri(token.getId());
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void reset(@QueryParam("id") String id) {

        Token token = tokenService.findTokenById(id);

        //First we check if the token is valid
        if (tokenService.isValid(token)) {
            //If yes, we need to redirect use to the login page
            //After user update the password, disable that token
            tokenService.disable(id);
            Response.status(Response.Status.OK);
        } else {
            Response.status(Response.Status.BAD_REQUEST);
        }
    }


    private String uri(String id) {
        try {
            String url = loadProperties().getProperty("config.url");
            return String.format(url + "%s%s", "rest/reset?id=", URLEncoder.encode(id, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("META-INF/config.properties");
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return props;
    }
}