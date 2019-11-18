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

package org.pentaho.csrf.client.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.csrf.ICsrfService;
import org.pentaho.csrf.client.ICsrfClient;
import org.pentaho.csrf.client.ICsrfToken;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CsrfClientTest {

  private static URI TEST_CONTEXT_URL;
  private static URI TEST_PROTECTED_SERVICE_URL;
  private static final String TEST_CSRF_TOKEN = "test-token-value";
  private static final String TEST_CSRF_HEADER = "test-token-header";
  private static final String TEST_CSRF_PARAMETER = "test-token-param";
  private static final String COOKIE_HEADER = "Cookie";
  private static final String SET_COOKIE_HEADER = "Set-Cookie";

  static {
    try {
      TEST_CONTEXT_URL = new URI( "http://corp.com:8080/pentaho/api/csrf/service" );
      TEST_PROTECTED_SERVICE_URL = new URI( "http://mydomain.com:8080/pentaho/moneyTransfer" );
    } catch ( URISyntaxException e ) {
      e.printStackTrace();
    }
  }

  private Client mockClient;
  private WebTarget mockWebTarget;
  private Invocation.Builder mockInvocationBuilder;
  private Response mockResponse;
  private CookieHandler mockCookieHandler;

  // region Setup
  @Before
  public void setUp() throws IOException {
    mockClient = mock( Client.class );
    mockWebTarget = mock( WebTarget.class );
    mockInvocationBuilder = mock( Invocation.Builder.class );
    mockResponse = mock( Response.class );
    mockCookieHandler = mock( CookieHandler.class );

    // ---

    when( mockClient.target( any( URI.class ) ) ).thenReturn( mockWebTarget );
    when( mockWebTarget.queryParam( anyString(), anyObject() ) ).thenReturn( mockWebTarget );
    when( mockWebTarget.request() ).thenReturn( mockInvocationBuilder );
    when( mockInvocationBuilder.get() ).thenReturn( mockResponse );

    // ---

    when( mockResponse.getStatus() ).thenReturn( 204 );
    when( mockResponse.getCookies() ).thenReturn( createResponseCookieMap() );

    when( mockResponse.getHeaderString( eq( ICsrfService.RESPONSE_HEADER_HEADER ) ) )
      .thenReturn( TEST_CSRF_HEADER );
    when( mockResponse.getHeaderString( eq( ICsrfService.RESPONSE_HEADER_PARAM ) ) )
      .thenReturn( TEST_CSRF_PARAMETER );
    when( mockResponse.getHeaderString( eq( ICsrfService.RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( TEST_CSRF_TOKEN );

    // ---

    when( mockCookieHandler.get( any( URI.class ), anyObject() ) )
      .thenReturn( createCookieRequestHeadersMap() );
  }

  private Map<String, NewCookie> createResponseCookieMap() {

    Map<String, NewCookie> newCookieMap = new LinkedHashMap<>();

    newCookieMap.put( "cookie1", new NewCookie( "cookie1", "value1" ) );
    newCookieMap.put( "cookie2", new NewCookie( "cookie2", "value2" ) );
    newCookieMap.put( "cookie3", new NewCookie( "cookie3", "value3" ) );

    return newCookieMap;
  }

  private Map<String, List<String>> createCookieRequestHeadersMap() {

    Map<String, List<String>> cookieRequestHeadersMap = new HashMap<>();
    List<String> cookiesText = new ArrayList<>();

    cookiesText.add( "cookie1=value1" );
    cookiesText.add( "cookie2=value2" );
    cookiesText.add( "cookie3=value3" );

    cookieRequestHeadersMap.put( COOKIE_HEADER, cookiesText );

    return cookieRequestHeadersMap;
  }
  // endregion Setup

  @Test
  public void testGetTokenWhenResponseIsNot200Or204() {

    when( mockResponse.getStatus() ).thenReturn( 210 );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    ICsrfToken token = csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    assertNull( token );
  }

  @Test
  public void testGetTokenCallsCsrfService() {

    // Bail out asap.
    when( mockResponse.getStatus() ).thenReturn( 210 );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    verify( mockClient, times( 1 ) )
      .target( eq( TEST_CONTEXT_URL ) );

    verify( mockWebTarget, times( 1 ) )
      .queryParam( eq( ICsrfService.QUERY_PARAM_URL ), eq( TEST_PROTECTED_SERVICE_URL) );

    verify( mockWebTarget, times( 1 ) )
      .request();

    verify( mockInvocationBuilder, times( 1 ) )
      .get();
  }

  // region 200/204 with Token Present
  @Test
  public void testGetTokenWhenResponseIs200AndTokenIsPresent() {

    when( mockResponse.getStatus() ).thenReturn( 200 );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    ICsrfToken token = csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    assertNotNull( token );

    assertEquals( token.getHeader(), TEST_CSRF_HEADER );
    assertEquals( token.getParameter(), TEST_CSRF_PARAMETER );
    assertEquals( token.getToken(), TEST_CSRF_TOKEN );
  }

  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsPresent() {

    when( mockResponse.getStatus() ).thenReturn( 204 );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    ICsrfToken token = csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    assertNotNull( token );

    assertEquals( token.getHeader(), TEST_CSRF_HEADER );
    assertEquals( token.getParameter(), TEST_CSRF_PARAMETER );
    assertEquals( token.getToken(), TEST_CSRF_TOKEN );
  }
  // endregion

  // region 200/204 with Null or Empty Token
  @Test
  public void testGetTokenWhenResponseIs200AndTokenIsNotPresent() {

    when( mockResponse.getStatus() ).thenReturn( 200 );
    when( mockResponse.getHeaderString( eq( ICsrfService.RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( null );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    ICsrfToken token = csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    assertNull( token );
  }

  @Test
  public void testGetTokenWhenResponseIs200AndTokenIsEmpty() {

    when( mockResponse.getStatus() ).thenReturn( 200 );
    when( mockResponse.getHeaderString( eq( ICsrfService.RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( "" );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    ICsrfToken token = csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    assertNull( token );
  }

  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsNotPresent() {

    when( mockResponse.getStatus() ).thenReturn( 204 );
    when( mockResponse.getHeaderString( eq( ICsrfService.RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( null );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    ICsrfToken token = csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    assertNull( token );
  }

  @Test
  public void testGetTokenWhenResponseIs204AndTokenIsEmpty() {

    when( mockResponse.getStatus() ).thenReturn( 204 );
    when( mockResponse.getHeaderString( eq( ICsrfService.RESPONSE_HEADER_TOKEN ) ) )
      .thenReturn( "" );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    ICsrfToken token = csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    assertNull( token );
  }
  // endregion

  // region Cookie handling
  @Test
  public void testGetTokenAddsCookiesToRequest() throws IOException {

    CsrfClient csrfClient = new CsrfClient( mockClient );

    csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    verify( mockCookieHandler, times( 1 ) )
      .get( eq( TEST_CONTEXT_URL ), anyObject() );

    verify( mockInvocationBuilder, times( 1 ) )
      .cookie( eq( Cookie.valueOf( "cookie1=value1" ) ) );

    verify( mockInvocationBuilder, times( 1 ) )
      .cookie( eq( Cookie.valueOf( "cookie2=value2" ) ) );

    verify( mockInvocationBuilder, times( 1 ) )
      .cookie( eq( Cookie.valueOf( "cookie3=value3" ) ) );
  }

  @Test
  public void testGetTokenDoesNotAddCookiesToRequestWhenNoCookieHeader() throws IOException {

    Map<String, List<String>> cookieRequestHeadersMap = new HashMap<>();
    when( mockCookieHandler.get( any( URI.class ), anyObject() ) )
      .thenReturn( cookieRequestHeadersMap );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    verify( mockInvocationBuilder, never() )
      .cookie( any( Cookie.class ) );
  }

  @Test
  public void testGetTokenAddsResponseCookiesToCookieHandler() throws IOException {

    CsrfClient csrfClient = new CsrfClient( mockClient );

    csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    Map<String, List<String>> responseHeadersMap = new HashMap<>();

    List<String> newCookiesText = Arrays.asList(
        new NewCookie("cookie1", "value1").toString(),
        new NewCookie("cookie2", "value2").toString(),
        new NewCookie("cookie3", "value3").toString() );

    responseHeadersMap.put( SET_COOKIE_HEADER, newCookiesText );

    verify( mockCookieHandler, times( 1 ) )
      .put( eq( TEST_CONTEXT_URL ), eq( responseHeadersMap ) );
  }

  @Test
  public void testGetTokenDoesNotAddResponseCookiesToCookieHandlerWhenNoSetCookieHeader() throws IOException {

    Map<String, NewCookie> newCookieMap = new LinkedHashMap<>();
    when( mockResponse.getCookies() ).thenReturn( newCookieMap );

    CsrfClient csrfClient = new CsrfClient( mockClient );

    csrfClient.getToken( TEST_CONTEXT_URL, mockCookieHandler, TEST_PROTECTED_SERVICE_URL );

    verify( mockCookieHandler, never() )
      .put( eq( TEST_CONTEXT_URL ), any() );
  }
  // endregion
}
