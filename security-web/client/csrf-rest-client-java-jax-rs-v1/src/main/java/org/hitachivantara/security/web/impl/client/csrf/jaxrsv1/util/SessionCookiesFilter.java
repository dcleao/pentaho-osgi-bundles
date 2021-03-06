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

package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.internal.CsrfUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A JAX-RS 1.1 (request and response) client filter which maintains cookies across requests.
 */
public class SessionCookiesFilter extends ClientFilter {

  @Nonnull
  private final CookieHandler cookieHandler;

  /**
   * Creates a filter which uses a cookie handler which is a new instance of {@link CookieManager}.
   */
  public SessionCookiesFilter() {
    this( null );
  }

  /**
   * Creates a filter which uses a given cookie handler.
   *
   * @param cookieHandler The cookie handler where to maintain the exchanged cookies.
   *                      When {@code null}, a new instance of {@link CookieManager} is used.
   */
  public SessionCookiesFilter( @Nullable CookieHandler cookieHandler ) {
    this.cookieHandler = cookieHandler != null ? cookieHandler : new CookieManager();
  }

  @Override
  public ClientResponse handle( ClientRequest request ) throws ClientHandlerException {

    // Add cookies in the cookie handler to the request.
    try {
      CsrfUtil.getCookieStreamForRequest( cookieHandler, request )
        .forEach( cookie -> request.getHeaders().add( HttpHeaders.COOKIE, cookie ) );
    } catch ( IOException e ) {
      throw new ClientHandlerException( "Could not add cookies to the request.", e );
    }

    ClientResponse response = getNextNotFinal().handle( request );

    // Add response cookies to the cookie handler.
    List<NewCookie> newCookies = response.getCookies();
    if ( newCookies.size() > 0 ) {
      List<String> newCookiesText = newCookies
        .stream()
        .map( NewCookie::toString )
        .filter( cookieText -> cookieText != null && !cookieText.isEmpty() )
        .collect( Collectors.toList() );

      if ( !newCookiesText.isEmpty() ) {
        Map<String, List<String>> responseHeadersMap = new HashMap<>();
        responseHeadersMap.put( HttpHeaders.SET_COOKIE, newCookiesText );

        try {
          cookieHandler.put( request.getURI(), responseHeadersMap );
        } catch ( IOException e ) {
          throw new ClientHandlerException( "Could not save response cookies.", e );
        }
      }
    }

    return response;
  }

  // getNext is final and cannot be stubbed.
  // this wrapper allows unit testing.
  ClientHandler getNextNotFinal() {
    return getNext();
  }
}
