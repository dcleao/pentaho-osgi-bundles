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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.client.csrf.jaxrs;

import org.hitachivantara.security.web.impl.client.csrf.jaxrs.util.SessionCookiesFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.net.CookieHandler;
import java.net.URI;
import java.util.Objects;

/**
 * The `CsrfTokenServiceClient` class is a CSRF REST service JAX-RS client.
 * <p>
 * The CSRF token retrieval service is necessarily an HTTP service that works over a stateful HTTP connection. The state
 * of the HTTP connection is accomplished through the use of cookies and, specifically, using a `CookieHandler`.
 */
public class CsrfTokenServiceClient {

  /**
   * The name of the query parameter on which to specify the URL of the protected service which is to be called.
   */
  static final String QUERY_PARAM_URL = "url";

  /**
   * The name of the response header whose value is the name of the request header on which the value of the CSRF token
   * should be sent on requests to the protected service.
   */
  static final String RESPONSE_HEADER_HEADER = "X-CSRF-HEADER";

  /**
   * The name of the response header whose value is the name of the request parameter on which the value of the CSRF
   * token should be sent on requests to the protected service.
   * <p>
   * It is preferable to use a request header to send the CSRF token, if possible.
   */
  static final String RESPONSE_HEADER_PARAM = "X-CSRF-PARAM";

  /**
   * The name of the response header whose value is the CSRF token.
   */
  static final String RESPONSE_HEADER_TOKEN = "X-CSRF-TOKEN";

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

    Objects.requireNonNull( serviceUri );
    Objects.requireNonNull( client );

    this.serviceUri = serviceUri;
    this.client = client;

    if ( !client.getConfiguration().isRegistered( SessionCookiesFilter.class ) ) {
      client.register( new SessionCookiesFilter() );
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

    Objects.requireNonNull( serviceUri );

    this.serviceUri = serviceUri;
    this.client = ClientBuilder.newClient()
      .register( new SessionCookiesFilter( cookieHandler ) );
  }

  /**
   * Constructs a CSRF token service client with the given token service URI and with the default cookie handler.
   * <p>
   * Cookie maintenance across requests is determinant to the usefulness of the obtained CSRF token, as the token is
   * associated with the current (or newly created) server session and the session is determined by a session cookie.
   * <p>
   * The default {@link java.net.CookieHandler}, as given by {@link CookieHandler#getDefault()}
   * is used to maintain session cookies.
   *
   * @param serviceUri The URI of the token service.
   */
  public CsrfTokenServiceClient( @Nonnull URI serviceUri ) {
    this( serviceUri, CookieHandler.getDefault() );
  }

  /**
   * Gets a CSRF token to access a given protected service, if one needs to be used.
   * <p>
   * To make sure that the correct CSRF token is returned, this request and the one that then sends the token must be
   * performed on the same web server session.
   * <p>
   * To maintain the session cookie across requests, either specify a {@link Client} instance which is pre-configured to
   * achieve this or
   * <p>
   * To maintain the state of the HTTP connection the following is performed: - Cookies which are applicable to {@code
   * tokenServiceUrl} and are present in the cookie handler are added to the HTTP request. - Cookies which the server
   * sets in the HTTP response are added to the cookie handler.
   * <p>
   * When CSRF protection is disabled, or if the specified {@code protectedServiceUrl} is not CSRF protected, {@code
   * null} may be returned.
   *
   * @param protectedServiceUri - The URI of the protected service for which a CSRF token is requested.
   * @return The CSRF token to use to access the specified protected service; {@code null}, if a CSRF token need not be
   * sent.
   */
  @Nullable
  public CsrfToken getToken( URI protectedServiceUri ) {

    Objects.requireNonNull( protectedServiceUri );

    Invocation.Builder builder = client.target( serviceUri )
      .queryParam( QUERY_PARAM_URL, protectedServiceUri )
      .request();

    Response response = builder.get();

    // The response body should be empty, and return 204.
    // The relevant response is in the response headers.
    if ( response.getStatus() != 204 && response.getStatus() != 200 ) {
      return null;
    }

    // When CSRF protection is disabled, the token is not returned.
    String token = response.getHeaderString( RESPONSE_HEADER_TOKEN );
    if ( token == null || token.length() == 0 ) {
      return null;
    }

    String header = response.getHeaderString( RESPONSE_HEADER_HEADER );
    String parameter = response.getHeaderString( RESPONSE_HEADER_PARAM );

    return new CsrfToken( header, parameter, token );
  }
}
