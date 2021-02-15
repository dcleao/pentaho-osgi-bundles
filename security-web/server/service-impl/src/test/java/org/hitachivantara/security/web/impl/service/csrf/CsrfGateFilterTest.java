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
package org.hitachivantara.security.web.impl.service.csrf;

import org.hitachivantara.security.web.api.model.csrf.CsrfConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { CsrfFilter.class, CsrfGateFilter.class } )
public class CsrfGateFilterTest {

  // Tested subject
  private CsrfGateFilter csrfGateFilter;

  // Mocks
  private CsrfFilter csrfSpringFilter;
  private CsrfTokenRepository csrfTokenRepository;
  private CsrfConfiguration csrfConfiguration;

  @Before
  public void setUp() throws Exception {

    csrfSpringFilter = PowerMockito.mock( CsrfFilter.class );
    csrfTokenRepository = PowerMockito.mock( CsrfTokenRepository.class );
    csrfConfiguration = PowerMockito.mock( CsrfConfiguration.class );

    PowerMockito.whenNew( CsrfFilter.class )
      .withArguments( any( CsrfTokenRepository.class ) )
      .thenReturn( csrfSpringFilter );

    csrfGateFilter = new CsrfGateFilter( csrfTokenRepository, csrfConfiguration );
  }

  @Test
  public void testConstructor() throws Exception {

    verifyNew( CsrfFilter.class ).withArguments( csrfTokenRepository );

    assertEquals( csrfSpringFilter, csrfGateFilter.getInnerFilter() );

    // Test that it was a method reference of Csrfconfiguration#isEnabled( RequestMatcher )
    // that was passed to CsrfFilter#setRequireCsrfProtectionMatcher( . ).

    ArgumentCaptor<RequestMatcher> captor = ArgumentCaptor.forClass( RequestMatcher.class );
    verify( csrfSpringFilter, times( 1 ) )
      .setRequireCsrfProtectionMatcher( captor.capture() );

    HttpServletRequest mockRequest = PowerMockito.mock( HttpServletRequest.class );

    RequestMatcher matcher = captor.getValue();
    matcher.matches( mockRequest );

    verify( csrfConfiguration, times( 1 ) )
      .isEnabled( mockRequest );
  }

  @Test
  public void testDestroy() {
    csrfGateFilter.destroy();
    verify( csrfSpringFilter, times( 1 ) )
      .destroy();
  }

  @Test
  public void testSetAccessDeniedHandler() {

    AccessDeniedHandler accessDeniedHandler = PowerMockito.mock( AccessDeniedHandler.class );
    csrfGateFilter.setAccessDeniedHandler( accessDeniedHandler );

    verify( csrfSpringFilter, times( 1 ) )
      .setAccessDeniedHandler( accessDeniedHandler );
  }

  @Test
  public void testInit() throws Exception {

    FilterConfig filterConfig = PowerMockito.mock( FilterConfig.class );

    csrfGateFilter.init( filterConfig );

    verify( csrfSpringFilter, times( 1 ) )
      .init( filterConfig );
  }

  @Test
  public void testDoFilter() throws Exception {

    HttpServletRequest mockRequest = PowerMockito.mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = PowerMockito.mock( HttpServletResponse.class );
    FilterChain filterChain = PowerMockito.mock( FilterChain.class );

    csrfGateFilter.doFilter( mockRequest, mockResponse, filterChain );

    verify( csrfSpringFilter, times( 1 ) )
      .doFilter( mockRequest, mockResponse, filterChain );
  }
}
