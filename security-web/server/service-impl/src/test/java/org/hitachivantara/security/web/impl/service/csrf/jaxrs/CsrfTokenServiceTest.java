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

package org.hitachivantara.security.web.impl.service.csrf.jaxrs;

import org.hitachivantara.security.web.api.model.csrf.CsrfConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { Response.class, Response.ResponseBuilder.class } )
public class CsrfTokenServiceTest {

  private HttpServletRequest mockRequest;
  private Response mockResponse;
  private Response.ResponseBuilder mockResponseBuilder;
  private CsrfTokenService csrfTokeResource;
  private CsrfConfiguration mockCsrfConfiguration;

  private static final String RESPONSE_HEADER_VALUE = "HEADER_NAME";
  private static final String RESPONSE_PARAM_VALUE = "PARAM_NAME";
  private static final String RESPONSE_TOKEN_VALUE = "TOKEN";

  @Before
  public void setUp() {
    PowerMockito.mockStatic( Response.class );

    mockResponseBuilder = PowerMockito.mock( Response.ResponseBuilder.class );
    mockResponse = PowerMockito.mock( Response.class );
    when( mockResponse.getStatus() )
      .thenReturn( HttpServletResponse.SC_NO_CONTENT );

    PowerMockito.when( Response.noContent() )
      .thenReturn( mockResponseBuilder );

    when( mockResponseBuilder.build() )
      .thenReturn( mockResponse );

    mockRequest = mock( HttpServletRequest.class );
    mockCsrfConfiguration = mock( CsrfConfiguration.class );
    when( mockCsrfConfiguration.isEnabled() ).thenReturn( true );

    csrfTokeResource = Mockito.spy( new CsrfTokenService( mockCsrfConfiguration ) );

    csrfTokeResource.request = mockRequest;
  }

  private static CsrfToken createToken() {

    CsrfToken token = mock( CsrfToken.class );

    when( token.getHeaderName() ).thenReturn( RESPONSE_HEADER_VALUE );
    when( token.getParameterName() ).thenReturn( RESPONSE_PARAM_VALUE );
    when( token.getToken() ).thenReturn( RESPONSE_TOKEN_VALUE );

    return token;
  }

  @Test
  public void testWhenCsrfIsDisabled() {

    when( mockCsrfConfiguration.isEnabled() ).thenReturn( false );

    Response response = csrfTokeResource.getToken( "url" );

    assertEquals( mockResponse, response );

    verify( mockResponseBuilder, times( 1 ) )
      .build();

    verify( mockResponseBuilder, times( 0 ) )
      .header( any( String.class ), any( String.class ) );
  }

  @Test
  public void testWhenNoCsrfTokenInRequest() {

    Response response = csrfTokeResource.getToken( "url" );

    assertEquals( mockResponse, response );

    verify( mockResponseBuilder, times( 1 ) )
      .build();

    verify( mockResponseBuilder, times( 0 ) )
      .header( any( String.class ), any( String.class ) );
  }

  @Test
  public void testWhenCsrfTokenThenCsrfResponseHeaders() {

    CsrfToken token = createToken();
    when( mockRequest.getAttribute( CsrfTokenService.REQUEST_ATTRIBUTE_NAME ) )
      .thenReturn( token );

    Response response = csrfTokeResource.getToken( "url" );

    assertEquals( mockResponse, response );

    verify( mockResponseBuilder, times( 1 ) )
      .build();

    verify( mockResponseBuilder, times( 1 ) )
      .header( CsrfTokenService.RESPONSE_HEADER_HEADER, RESPONSE_HEADER_VALUE );

    verify( mockResponseBuilder, times( 1 ) )
      .header( CsrfTokenService.RESPONSE_HEADER_PARAM, RESPONSE_PARAM_VALUE );

    verify( mockResponseBuilder, times( 1 ) )
      .header( CsrfTokenService.RESPONSE_HEADER_TOKEN, RESPONSE_TOKEN_VALUE );
  }

  @Test
  public void testStatusCodeNoContent() {

    CsrfToken token = createToken();
    when( mockRequest.getAttribute( CsrfTokenService.REQUEST_ATTRIBUTE_NAME ) )
      .thenReturn( token );

    Response response = csrfTokeResource.getToken( "url" );

    assertEquals( HttpServletResponse.SC_NO_CONTENT, response.getStatus() );
  }
}
