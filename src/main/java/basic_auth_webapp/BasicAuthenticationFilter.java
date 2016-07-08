package basic_auth_webapp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jeremym
 */
public class BasicAuthenticationFilter implements Filter {

    private Map<String, String> userPasswords;

    /**
     * No op.
     */
    public void destroy() {
    }

    /**
     * Do filter operation which performs basic authorization. 
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();
                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String[] credentials = st.nextToken().split(":");
                        String username = credentials[0];
                        String password = credentials[1];
                        System.out.println("got username " + username + " with password " + password + " from auth header");
                        if (userPasswords.containsKey(username)) {
                            if (userPasswords.get(username).equals(password)) {
                                
                                System.out.println("password matches -- OK!");
                                
                                // ?????
                                request.getSession().setAttribute("userName", username);
                                
                                filterChain.doFilter(request, response);
                            } else {
                                response.sendError(401, "Incorrect password");
                            }
                        } else {
                            response.sendError(401, "Unknown username");
                        }
                    } catch (Exception e) {
                        response.sendError(401, "Bad authorization token");
                    }
                } else {
                    response.sendError(401, "Unsupported authentication scheme");
                }
            } else {
                response.sendError(401, "Invalid authorization header");
            }
        } else {
            response.sendRedirect("Unauthorized");
        }
    }

    /**
     * Initialize the filter by parsing the user conf file.
     * @param config the filter configuration
     */
    public void init(FilterConfig config) throws ServletException {
        String userConf = config.getInitParameter("user-conf");
        if (userConf != null) {
            userPasswords = parseUserConf(userConf);
        }
    }

    /**
     * Parse user conf file which has 'user:password' entries, one per line.
     * @param userConf the path to the user configuration file
     * @return map of username's to passwords
     */
    private Map<String, String> parseUserConf(String userConf) {
        Map<String, String> userTokens = new HashMap<String, String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(userConf))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    throw new RuntimeException("Bad user conf line: " + line);
                }
                System.out.println("adding user " + parts[0] + " with token " + parts[1] + " from conf file");
                if (userTokens.containsKey(parts[0])) {
                    throw new RuntimeException("User " + parts[0] + " is a duplicate.");
                }
                userTokens.put(parts[0], parts[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return userTokens;
    }
}
