package basic_auth_webapp;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;

import junit.framework.TestCase;

/**
 * Test basic authentication using the HTTP 'Authorization' header.
 * @author jeremym
 */
public class BasicAuthenticationClientTest extends TestCase {
    
    private static String USER = "jeremy";
    private static String WRONG_USER = "derpy";
    private static String PASSWORD = "ABCDE";
    private static String WRONG_PASSWORD = "FGHIJ";
    private static String TARGET = "http://localhost:8080/basic-auth-webapp/index.jsp";
    
    /**
     * Test valid username and password.
     */
    public void testAuth() {
        System.out.println("requesting page with user " + USER + " and " + PASSWORD);
        Client client = ClientBuilder.newClient().register(new BasicAuthenticator(USER, PASSWORD));
        WebTarget target = client.target(TARGET);
        target.request().get(String.class);
        System.out.println("request OK");
    }
    
    /**
     * Test valid username with wrong password.
     */
    public void testWrongPassword() {
        try {
            System.out.println("requesting page with user " + USER + " and " + WRONG_PASSWORD);
            Client client = ClientBuilder.newClient().register(new BasicAuthenticator(USER, WRONG_PASSWORD));
            WebTarget target = client.target(TARGET);
            target.request().get(String.class);
            throw new RuntimeException("This test was supposed to fail!");
        } catch (NotAuthorizedException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Test valid username with wrong password.
     */
    public void testWrongUsername() {
        try {
            System.out.println("requesting page with user " + WRONG_USER + " and " + PASSWORD);
            Client client = ClientBuilder.newClient().register(new BasicAuthenticator(WRONG_USER, PASSWORD));
            WebTarget target = client.target(TARGET);
            target.request().get(String.class);
            throw new RuntimeException("This test was supposed to fail!");
        } catch (NotAuthorizedException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Basic implementation of client side filter to insert user and password into Authorization header. 
     * @author jeremym
     */
    static class BasicAuthenticator implements ClientRequestFilter {
        
        String user;
        String password;
        
        BasicAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }
         
        @Override
        public void filter(ClientRequestContext request) throws IOException {
            request.getHeaders().add("Authorization", "Basic " + user + ":" + password);
        }
    }
}
