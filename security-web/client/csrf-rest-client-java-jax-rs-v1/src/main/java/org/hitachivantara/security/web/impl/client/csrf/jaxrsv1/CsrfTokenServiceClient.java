/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2019-2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.SessionCookiesFilter;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.internal.CsrfUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * This class is a CSRF token service client based on JAX-RS 1.1 technology.
 * <p>
 * The CSRF token retrieval service is necessarily an HTTP service that works over a stateful HTTP connection. The state
 * of the HTTP connection is accomplished through the use of cookies and, specifically, using a `CookieHandler`.
 * <p>
 * This class allows full control on how a CSRF token is obtained and used. For a more convenient alternative, which
 * automatically manages token retrieval and inclusion in all requests made from a Jersey client for JAX-RS 1.1, see the
 * {@link CsrfTokenFilter} class.
 * <p>
 * Example:
 * <pre>
 * <code>
 * Client client = Client.create();
 * client.addFilter( new HTTPBasicAuthFilter( "user", "password" ) );
 * client.addFilter( new SessionCookiesFilter() );
 *
 * CsrfTokenServiceClient tokenClient = new CsrfTokenServiceClient(
 *   new URI( ".../csrf/token" ),
 *   client ) );
 *
 * CsrfToken token = tokenClient.getToken( new URI( "my/protected/service" ) );
 *
 * // Use the token on a following request.
 * WebResource.Builder builder = client
 *   .resource( "..." )
 *   .type( MediaType.APPLICATION_JSON );
 *
 * if ( token != null ) {
 *   builder.header( token.getHeader(), token.getToken() );
 * }
 *
 * ClientResponse response = builder.get( ClientResponse.class );
 *
 * </code>
 * </pre>
 */
public class CsrfTokenServiceClient {

  /**
   * The name of the query parameter on which to specify the URL of the protected service which is to be called.
   */
  static final String QUERY_PARAM_URL = "url";

  @Nonnull
  private final Client client;
  @Nonnull
  private final URI serviceUri;

  /**
   * Constructs a CSRF token service client with the given token service URI and JAX-RS client instance.
   * <p>
   * Cookie maintenance across requests is determinant to the usefulness of the obtained CSRF token, as the token is
   * associated with the current (or newly created) server session and the session is determined by a session cookie.
   * <p>
   * If the given client instance does not have a registered filter of type {@link SessionCookiesFilter}, one is
   * automatically registered which uses the default {@link java.net.CookieHandler}, as given by {@link
   * CookieHandler#getDefault()}.
   *
   * @param serviceUri The URI of the token service.
   * @param client     The JAX-RS client to use to call the CSRF token service.
   */
  public CsrfTokenServiceClient( @Nonnull URI serviceUri, @Nonnull Client client ) {

    requireNonNull( serviceUri );
    requireNonNull( client );

    this.serviceUri = serviceUri;
    this.client = client;

    if ( !CsrfUtil.hasClientFilter( client, SessionCookiesFilter.class ) ) {
      client.addFilter( new SessionCookiesFilter() );
    }
  }

  /**
   * Constructs a CSRF token service client with the given token service URI and cookie handler.
   * <p>
   * Cookie maintenance across requests is determinant to the usefulness of the obtained CSRF token, as the token is
   * associated with the current (or newly created) server session and the session is determined by a session cookie.
   * <p>
   * The specified cookie handler should be used to provide the session cookie when calling the protected service.
   *
   * @param serviceUri    The URI of the token service.
   * @param cookieHandler The cookie handler where to read and/or store the session cookie from.
   */
  public CsrfTokenServiceClient( @Nonnull URI serviceUri, @Nonnull CookieHandler cookieHandler ) {

    requireNonNull( serviceUri );

    this.serviceUri = serviceUri;
    this.client = Client.create();
    this.client.addFilter( new SessionCookiesFilter( cookieHandler ) );
  }

  /**
   * Constructs a CSRF token service client with the given token service URI and with a new cookie manager.
   * <p>
   * Cookie maintenance across requests is determinant to the usefulness of the obtained CSRF token, as the token is
   * associated with the current (or newly created) server session and the session is determined by a session cookie.
   * <p>
   * A new instance of {@link CookieManager} is used as cookie handler to maintain session cookies.
   *
   * @param serviceUri The URI of the token service.
   */
  public CsrfTokenServiceClient( @Nonnull URI serviceUri ) {
    this( serviceUri, new CookieManager() );
  }

  /**
   * Gets a CSRF token to access a given protected service.
   *
   * @param protectedServiceUri - The URI of the protected service for which a CSRF token is requested.
   * @return The CSRF token to use to access protected services; {@code null}, CSRF protection is disabled
   * or otherwise not required for the given URL.
   */
  @Nullable
  public CsrfToken getToken( @Nonnull URI protectedServiceUri ) {

    requireNonNull( protectedServiceUri );

    ClientResponse response = client.resource( serviceUri )
      .queryParam( QUERY_PARAM_URL, protectedServiceUri.toString() )
      .get( ClientResponse.class );

    if ( !CsrfUtil.isTokenResponseSuccessful( response ) ) {
      return null;
    }

    return CsrfUtil.readResponseToken( response );
  }
}
