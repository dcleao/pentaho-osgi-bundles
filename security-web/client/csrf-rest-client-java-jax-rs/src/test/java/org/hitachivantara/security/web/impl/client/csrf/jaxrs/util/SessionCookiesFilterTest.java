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
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.client.csrf.jaxrs.util;

import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionCookiesFilterTest {

  private static URI TEST_URI;

  static {
    try {
      TEST_URI = new URI( "http://corp.com:8080/pentaho/api/csrf/service" );
    } catch ( URISyntaxException e ) {
      e.printStackTrace();
    }
  }

  private Map<String, List<String>> createCookieRequestHeadersMap() {

    Map<String, List<String>> cookieRequestHeadersMap = new HashMap<>();

    List<String> cookiesText = new ArrayList<>();
    cookiesText.add( "cookie1=value1" );
    cookiesText.add( "cookie2=value2" );
    cookiesText.add( "cookie3=value3" );

    cookieRequestHeadersMap.put( HttpHeaders.COOKIE, cookiesText );

    return cookieRequestHeadersMap;
  }

  private Map<String, NewCookie> createResponseCookieMap() {

    Map<String, NewCookie> newCookieMap = new LinkedHashMap<>();

    newCookieMap.put( "cookie1", new NewCookie( "cookie1", "value1" ) );
    newCookieMap.put( "cookie2", new NewCookie( "cookie2", "value2" ) );
    newCookieMap.put( "cookie3", new NewCookie( "cookie3", "value3" ) );

    return newCookieMap;
  }

  private Map<String, List<String>> createCookieResponseHeadersMap() {

    Map<String, List<String>> responseHeadersMap = new HashMap<>();

    List<String> newCookiesText = Arrays.asList(
      new NewCookie( "cookie1", "value1" ).toString(),
      new NewCookie( "cookie2", "value2" ).toString(),
      new NewCookie( "cookie3", "value3" ).toString() );

    responseHeadersMap.put( HttpHeaders.SET_COOKIE, newCookiesText );

    return responseHeadersMap;
  }

  @Test
  public void testFilterAddsCookiesInTheCookieHandlerToTheRequest() throws IOException {

    CookieHandler mockCookieHandler = mock( CookieHandler.class );

    when( mockCookieHandler.get( eq( TEST_URI ), anyObject() ) )
      .thenReturn( createCookieRequestHeadersMap() );

    SessionCookiesFilter filter = new SessionCookiesFilter( mockCookieHandler );

    @SuppressWarnings( "unchecked" )
    MultivaluedMap<String, Object> headers = (MultivaluedMap<String, Object>) mock( MultivaluedMap.class );

    @SuppressWarnings( "unchecked" )
    MultivaluedMap<String, String> stringHeaders = (MultivaluedMap<String, String>) mock( MultivaluedMap.class );

    ClientRequestContext requestContext = mock( ClientRequestContext.class );
    when( requestContext.getUri() ).thenReturn( TEST_URI );
    when( requestContext.getHeaders() ).thenReturn( headers );
    when( requestContext.getStringHeaders() ).thenReturn( stringHeaders );

    // ---

    filter.filter( requestContext );

    // ---

    verify( mockCookieHandler, times( 1 ) )
      .get( eq( TEST_URI ), eq( stringHeaders ) );

    verify( headers, times( 1 ) )
      .add( eq( HttpHeaders.COOKIE ), eq( Cookie.valueOf( "cookie1=value1" ) ) );

    verify( headers, times( 1 ) )
      .add( eq( HttpHeaders.COOKIE ), eq( Cookie.valueOf( "cookie2=value2" ) ) );

    verify( headers, times( 1 ) )
      .add( eq( HttpHeaders.COOKIE ), eq( Cookie.valueOf( "cookie3=value3" ) ) );
  }

  @Test
  public void testFilterSupportsNoCookiesInTheCookieHandlerToAddToTheRequest() throws IOException {

    CookieHandler mockCookieHandler = mock( CookieHandler.class );

    Map<String, List<String>> cookieRequestHeadersMap = new HashMap<>();

    when( mockCookieHandler.get( eq( TEST_URI ), anyObject() ) )
      .thenReturn( cookieRequestHeadersMap );

    SessionCookiesFilter filter = new SessionCookiesFilter( mockCookieHandler );

    @SuppressWarnings( "unchecked" )
    MultivaluedMap<String, Object> headers = (MultivaluedMap<String, Object>) mock( MultivaluedMap.class );

    @SuppressWarnings( "unchecked" )
    MultivaluedMap<String, String> stringHeaders = (MultivaluedMap<String, String>) mock( MultivaluedMap.class );

    ClientRequestContext requestContext = mock( ClientRequestContext.class );
    when( requestContext.getUri() ).thenReturn( TEST_URI );
    when( requestContext.getHeaders() ).thenReturn( headers );
    when( requestContext.getStringHeaders() ).thenReturn( stringHeaders );

    // ---

    filter.filter( requestContext );

    // ---

    verify( mockCookieHandler, times( 1 ) )
      .get( eq( TEST_URI ), eq( stringHeaders ) );

    verify( headers, never() )
      .add( any( String.class ), any( Cookie.class ) );
  }

  @Test
  public void testFilterAddsResponseCookiesToCookieHandler() throws IOException {

    CookieHandler mockCookieHandler = mock( CookieHandler.class );

    SessionCookiesFilter filter = new SessionCookiesFilter( mockCookieHandler );

    ClientRequestContext requestContext = mock( ClientRequestContext.class );
    when( requestContext.getUri() ).thenReturn( TEST_URI );

    ClientResponseContext responseContext = mock( ClientResponseContext.class );
    when( responseContext.getCookies() ).thenReturn( createResponseCookieMap() );

    // ---

    filter.filter( requestContext, responseContext );

    // ---

    verify( mockCookieHandler, times( 1 ) )
      .put( eq( TEST_URI ), eq( createCookieResponseHeadersMap() ) );
  }

  @Test
  public void testFilterSupportsNoCookiesInTheResponseToAddToTheCookieHandler() throws IOException {

    CookieHandler mockCookieHandler = mock( CookieHandler.class );

    SessionCookiesFilter filter = new SessionCookiesFilter( mockCookieHandler );

    ClientRequestContext requestContext = mock( ClientRequestContext.class );
    when( requestContext.getUri() ).thenReturn( TEST_URI );

    Map<String, NewCookie> emptyCookieMap = new LinkedHashMap<>();
    ClientResponseContext responseContext = mock( ClientResponseContext.class );
    when( responseContext.getCookies() ).thenReturn( emptyCookieMap );

    // ---

    filter.filter( requestContext, responseContext );

    // ---

    verify( mockCookieHandler, never() )
      .put( eq( TEST_URI ), any() );
  }
}
