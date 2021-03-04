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

package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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

  private List<NewCookie> createResponseCookieList() {
    return Arrays.asList(
      new NewCookie( "cookie1", "value1" ),
      new NewCookie( "cookie2", "value2" ),
      new NewCookie( "cookie3", "value3" ) );
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
    when( mockCookieHandler.get( eq( TEST_URI ), any() ) )
      .thenReturn( createCookieRequestHeadersMap() );

    @SuppressWarnings( "unchecked" )
    MultivaluedMap<String, Object> headers = (MultivaluedMap<String, Object>) mock( MultivaluedMap.class );

    ClientRequest request = mock( ClientRequest.class );
    when( request.getURI() ).thenReturn( TEST_URI );
    when( request.getHeaders() ).thenReturn( headers );

    ClientResponse response = mock( ClientResponse.class );
    when( response.getCookies() ).thenReturn( createResponseCookieList() );

    ClientHandler nextHandler = mock( ClientHandler.class );
    when( nextHandler.handle( any() ) ).thenReturn( response );

    SessionCookiesFilter filterSpy = spy( new SessionCookiesFilter( mockCookieHandler ) );
    doReturn( nextHandler ).when( filterSpy ).getNextNotFinal();

    // ---

    filterSpy.handle( request );

    // ---

    verify( mockCookieHandler, times( 1 ) )
      .get( eq( TEST_URI ), any() );

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

    when( mockCookieHandler.get( eq( TEST_URI ), any() ) )
      .thenReturn( cookieRequestHeadersMap );

    SessionCookiesFilter filter = new SessionCookiesFilter( mockCookieHandler );

    @SuppressWarnings( "unchecked" )
    MultivaluedMap<String, Object> headers = (MultivaluedMap<String, Object>) mock( MultivaluedMap.class );

    ClientRequest request = mock( ClientRequest.class );
    when( request.getURI() ).thenReturn( TEST_URI );
    when( request.getHeaders() ).thenReturn( headers );

    ClientResponse response = mock( ClientResponse.class );
    when( response.getCookies() ).thenReturn( createResponseCookieList() );

    ClientHandler nextHandler = mock( ClientHandler.class );
    when( nextHandler.handle( any() ) ).thenReturn( response );

    SessionCookiesFilter filterSpy = spy( new SessionCookiesFilter( mockCookieHandler ) );
    doReturn( nextHandler ).when( filterSpy ).getNextNotFinal();

    // ---

    filterSpy.handle( request );

    // ---

    verify( mockCookieHandler, times( 1 ) )
      .get( eq( TEST_URI ), any() );

    verify( headers, never() )
      .add( any( String.class ), any( Cookie.class ) );
  }

  @Test
  public void testFilterAddsResponseCookiesToCookieHandler() throws IOException {

    CookieHandler mockCookieHandler = mock( CookieHandler.class );

    SessionCookiesFilter filter = new SessionCookiesFilter( mockCookieHandler );

    ClientRequest request = mock( ClientRequest.class );
    when( request.getURI() ).thenReturn( TEST_URI );

    ClientResponse response = mock( ClientResponse.class );
    when( response.getCookies() ).thenReturn( createResponseCookieList() );

    ClientHandler nextHandler = mock( ClientHandler.class );
    when( nextHandler.handle( any() ) ).thenReturn( response );

    SessionCookiesFilter filterSpy = spy( new SessionCookiesFilter( mockCookieHandler ) );
    doReturn( nextHandler ).when( filterSpy ).getNextNotFinal();

    // ---

    ClientResponse actualResponse = filterSpy.handle( request );

    // ---

    assertSame( response, actualResponse );

    verify( mockCookieHandler, times( 1 ) )
      .put( eq( TEST_URI ), eq( createCookieResponseHeadersMap() ) );
  }

  @Test
  public void testFilterSupportsNoCookiesInTheResponseToAddToTheCookieHandler() throws IOException {

    CookieHandler mockCookieHandler = mock( CookieHandler.class );

    SessionCookiesFilter filter = new SessionCookiesFilter( mockCookieHandler );

    ClientRequest request = mock( ClientRequest.class );
    when( request.getURI() ).thenReturn( TEST_URI );

    List<NewCookie> emptyCookieList = new ArrayList<>();

    ClientResponse response = mock( ClientResponse.class );
    when( response.getCookies() ).thenReturn( emptyCookieList );

    ClientHandler nextHandler = mock( ClientHandler.class );
    when( nextHandler.handle( any() ) ).thenReturn( response );

    SessionCookiesFilter filterSpy = spy( new SessionCookiesFilter( mockCookieHandler ) );
    doReturn( nextHandler ).when( filterSpy ).getNextNotFinal();

    // ---

    filterSpy.handle( request );

    // ---

    verify( mockCookieHandler, never() )
      .put( eq( TEST_URI ), any() );
  }
}
