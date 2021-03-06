package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.SessionCookiesFilter;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.internal.CsrfUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
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

  @Nonnull
  private final URI serviceUri;

  @Nullable
  private TokenHolder tokenHolder;

  private static class TokenHolder {
    @Nullable
    public final CsrfToken token;

    public TokenHolder( @Nullable CsrfToken token ) {
      this.token = token;
    }
  }

  public CsrfTokenFilter( @Nonnull URI serviceUri ) {
    this.serviceUri = requireNonNull( serviceUri );
  }

  @Override
  public ClientResponse handle( ClientRequest originalRequest ) throws ClientHandlerException {

    ClientResponse failedResponse = ensureTokenForRequest( originalRequest );
    if ( failedResponse != null ) {
      return failedResponse;
    }

    ClientResponse response = handleWithCurrentToken( originalRequest );

    if ( response.getStatus() == 403 && getCurrentToken() != null ) {
      // Retry with a fresh token.

      failedResponse = refreshTokenForRequest( originalRequest );
      if ( failedResponse != null ) {
        return failedResponse;
      }

      response = handleWithCurrentToken( originalRequest );
    }

    return response;
  }

  @Nullable
  private CsrfToken getCurrentToken() {
    return tokenHolder != null ? tokenHolder.token : null;
  }

  @Nullable
  private ClientResponse ensureTokenForRequest( @Nonnull ClientRequest request ) {
    if ( tokenHolder == null ) {
      // Try to get a token.
      ClientResponse tokenResponse = handleNext( createTokenClientRequest( request ) );
      if ( !CsrfUtil.isTokenResponseSuccessful( tokenResponse ) ) {
        // Something went wrong. Return the failed response.
        return tokenResponse;
      }

      tokenHolder = new TokenHolder( CsrfUtil.readResponseToken( tokenResponse ) );
    }

    return null;
  }

  @Nonnull
  private ClientRequest createTokenClientRequest( @Nonnull ClientRequest request ) {
    ClientRequest tokenRequest = newClientRequest( serviceUri, HttpMethod.GET );
    tokenRequest.getProperties().putAll( request.getProperties() );
    return tokenRequest;
  }

  // For unit test stubbing.
  @Nonnull
  ClientRequest newClientRequest( @Nonnull URI uri, @Nonnull String method ) {
    return ClientRequest.create().build( uri, method );
  }

  @Nonnull
  private ClientResponse handleWithCurrentToken( @Nonnull ClientRequest originalRequest ) {
    ClientRequest request = originalRequest.clone();

    CsrfToken token = getCurrentToken();
    if ( token != null ) {
      request.getHeaders().add( token.getHeader(), token.getToken() );
    }

    return handleNext( request );
  }

  @Nullable
  private ClientResponse refreshTokenForRequest( @Nonnull ClientRequest request ) {
    tokenHolder = null;
    return ensureTokenForRequest( request );
  }

  // getNext is final and cannot be stubbed.
  // this wrapper allows unit testing.
  @Nonnull
  ClientResponse handleNext( @Nonnull ClientRequest request ) {
    return getNext().handle( request );
  }
}
