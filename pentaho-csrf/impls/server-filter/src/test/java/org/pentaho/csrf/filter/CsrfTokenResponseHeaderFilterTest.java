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

package org.pentaho.csrf.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.verification.VerificationMode;
import org.pentaho.csrf.ICsrfProtectionDefinitionProvider;
import org.pentaho.csrf.ICsrfService;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

@RunWith( PowerMockRunner.class )
@PrepareForTest( CsrfUtil.class )
public class CsrfTokenResponseHeaderFilterTest {

  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private ICsrfProtectionDefinitionProvider mockCsrfProtectionDefinitionProvider;
  private CsrfTokenResponseHeaderFilter filter;
  private FilterChain filterChain;

  private static final String RESPONSE_HEADER_VALUE = "HEADER_NAME";
  private static final String RESPONSE_PARAM_VALUE = "PARAM_NAME";
  private static final String RESPONSE_TOKEN_VALUE = "TOKEN";

  @Before
  public void setUp() {

    this.mockRequest = mock( HttpServletRequest.class );
    this.mockResponse = mock( HttpServletResponse.class );
    this.mockCsrfProtectionDefinitionProvider = mock( ICsrfProtectionDefinitionProvider.class );

    this.filter = spy( new CsrfTokenResponseHeaderFilter( this.mockCsrfProtectionDefinitionProvider ) );
    this.filterChain = new MockFilterChain();
  }

  private static CsrfToken createToken() {

    CsrfToken token = mock( CsrfToken.class );

    when( token.getHeaderName() ).thenReturn( RESPONSE_HEADER_VALUE );
    when( token.getParameterName() ).thenReturn( RESPONSE_PARAM_VALUE );
    when( token.getToken() ).thenReturn( RESPONSE_TOKEN_VALUE );

    return token;
  }

  public void testWhenNoCsrfTokenInRequest() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    verify( mockResponse, never() ).setHeader( anyString(), anyString() );
  }

  @Test
  public void testWhenCsrfTokenThenCsrfResponseHeaders() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    when( this.mockRequest.getAttribute( CsrfTokenResponseHeaderFilter.REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    verify( mockResponse, once() ).setHeader( eq( ICsrfService.RESPONSE_HEADER_HEADER ), eq( RESPONSE_HEADER_VALUE ) );
    verify( mockResponse, once() ).setHeader( eq( ICsrfService.RESPONSE_HEADER_PARAM ), eq( RESPONSE_PARAM_VALUE ) );
    verify( mockResponse, once() ).setHeader( eq( ICsrfService.RESPONSE_HEADER_TOKEN ), eq( RESPONSE_TOKEN_VALUE ) );
  }

  @Test
  public void testWhenCsrfTokenAndCorsOriginAllowedThenCorsResponseHeaders() throws Exception {
    Set<String> allowOrigins = new HashSet<>();
    allowOrigins.add( "test-origin" );

    when( this.mockCsrfProtectionDefinitionProvider.getCorsAllowOrigins() ).thenReturn( allowOrigins );

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    when( this.mockRequest.getAttribute( CsrfTokenResponseHeaderFilter.REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );
    when( this.mockRequest.getHeader( CsrfUtil.ORIGIN_HEADER ) ).thenReturn( "test-origin" );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    verify( this.mockResponse, once() ).setHeader( eq( CsrfUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( "test-origin" ) );
    verify( this.mockResponse, once() ).setHeader( eq( CsrfUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
    verify( this.mockResponse, once() ).setHeader( eq( CsrfUtil.CORS_EXPOSE_HEADERS_HEADER ), eq( CsrfUtil.CORS_EXPOSED_HEADERS ) );
  }

  @Test
  public void testWhenCsrfTokenAndCorsOriginNotAllowedThenNoCorsResponseHeaders() throws Exception {
    Set<String> allowOrigins = new HashSet<>();
    allowOrigins.add( "test-origin" );

    when( this.mockCsrfProtectionDefinitionProvider.getCorsAllowOrigins() ).thenReturn( allowOrigins );

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    when( this.mockRequest.getAttribute( CsrfTokenResponseHeaderFilter.REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );
    when( this.mockRequest.getHeader( "origin" ) ).thenReturn( "test-origin2" );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    verify( this.mockResponse, never() ).setHeader( eq( CsrfUtil.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( CsrfUtil.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( CsrfUtil.CORS_EXPOSE_HEADERS_HEADER ), anyString() );
  }

  @Test
  public void testStatusCodeNoContent() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    when( this.mockRequest.getAttribute( CsrfTokenResponseHeaderFilter.REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    verify( mockResponse, once() ).setStatus( HttpServletResponse.SC_NO_CONTENT );
  }

  private VerificationMode once() {
    return times( 1 );
  }
}
