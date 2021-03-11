package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.SessionCookiesFilter;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.internal.CsrfUtil;

import javax.annotation.Nonnull;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static java.util.Objects.requireNonNull;

/**
 * JAX-RS 1.1 client filter that adds a CSRF token to every request.
 * <p>
 * All requests will send a CSRF token, whether or not these are actually CSRF protected. A CSRF token is obtained upon
 * the first request and cached for future use. In case a cached CSRF token is used and the response has a {@code 403}
 * status, then a new CSRF token is obtained. This allows dealing with the login operations where the session is
 * replaced by a new one or with session timeout scenarios.
 * <p>
 * A filter instance should be used by a single client. Additionally, it is assumed that each client is used for a
 * single user session. Moreover, the implementation is not thread-safe.
 * <p>
 * For this filter to work properly it is required that the auxiliary {@link SessionCookiesFilter} filter is also added
 * to the client, and <em>before</em> this one. If an authentication filter is used, it should also be placed
 * <em>before</em> this one.
 * <p>
 * Example:
 * <pre>
 * <code>
 * Client client = Client.create();
 * client.addFilter( new HTTPBasicAuthFilter( "user", "password" ) );
 * client.addFilter( new SessionCookiesFilter() );
 * client.addFilter( new CsrfTokenFilter( new URI( ".../csrf/token" ) ) );
 *
 * ClientResponse response = client
 *   .resource( "csrf-protected-endpoint" )
 *   .type( MediaType.APPLICATION_JSON )
 *   .get( ClientResponse.class );
 * </code>
 * </pre>
 */
public class CsrfTokenFilter extends ClientFilter {

  /**
   * The name of the query parameter on which to specify the URL of the protected service which is to be called.
   */
  static final String QUERY_PARAM_URL = "url";

  @Nonnull
  private final URI serviceUri;

  public CsrfTokenFilter( @Nonnull URI serviceUri ) {
    this.serviceUri = requireNonNull( serviceUri );
  }

  @Override
  public ClientResponse handle( ClientRequest request ) throws ClientHandlerException {

    // Get a token.
    ClientResponse tokenResponse = handleNext( createTokenClientRequest( request ) );
    if ( !CsrfUtil.isTokenResponseSuccessful( tokenResponse ) ) {
      // Something went wrong. Return the failed response.
      return tokenResponse;
    }

    CsrfToken token = CsrfUtil.readResponseToken( tokenResponse );
    if ( token != null ) {
      request.getHeaders().add( token.getHeader(), token.getToken() );
    }

    return handleNext( request );
  }

  @Nonnull
  private ClientRequest createTokenClientRequest( @Nonnull ClientRequest request ) {

    URI finalServiceUri = UriBuilder
      .fromUri( serviceUri )
      .queryParam( QUERY_PARAM_URL, request.getURI() )
      .build();

    ClientRequest tokenRequest = newClientRequest( finalServiceUri, HttpMethod.GET );
    tokenRequest.getProperties().putAll( request.getProperties() );
    return tokenRequest;
  }

  // For unit test stubbing.
  @Nonnull
  ClientRequest newClientRequest( @Nonnull URI uri, @Nonnull String method ) {
    return ClientRequest.create().build( uri, method );
  }

  // For unit test stubbing.
  // getNext is final and cannot be stubbed.
  @Nonnull
  ClientResponse handleNext( @Nonnull ClientRequest request ) {
    return getNext().handle( request );
  }
}
