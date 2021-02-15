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
package org.hitachivantara.security.web.impl.model.csrf;

import org.hitachivantara.security.web.api.model.matcher.RequestMatcherConfiguration;
import org.hitachivantara.security.web.api.csrfold.CsrfProtectedRequestSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
public class CsrfGateFilterTest {

  private CsrfGateFilter csrfGateFilter;
  @Mock private CsrfTokenRepository csrfTokenRepository;
  @Mock private ICsrfConfiguration csrfConfigProvider;

  private FilterConfig filterConfig;

  @Before
  public void setUp() {
    csrfGateFilter = new CsrfGateFilter( csrfTokenRepository, csrfConfigProvider );
    filterConfig = new MockFilterConfig();
  }

  private CsrfProtectedRequestSet getNonEmptyCsrfConfiguration() {
    return new CsrfProtectedRequestSet(
      Collections.singletonList(
        new RequestMatcherConfiguration( "regex", "url" ) ) );
  }

  @Test
  public void testConstructor() {
    assertNotNull( csrfGateFilter.getInnerFilter() );
    // No way to test that csrfTokenRepository was the received repository.
  }

  @Test
  public void testDestroy() {

    // Coverage. No way to test that innerCsrfFilter.destroy() was called.

    csrfGateFilter.destroy();
  }

  @Test
  public void testSetAccessDeniedHandler() {

    // Coverage. No way to test that innerCsrfFilter.setAccessDeniedHandler( . ) was called.

    csrfGateFilter.setAccessDeniedHandler( Mockito.mock( AccessDeniedHandler.class ) );
  }

  @Test
  public void testInitWithCsrfProtectionDisabled() throws Exception {

    when( csrfConfigProvider.getCsrfConfiguration() ).thenReturn( new CsrfProtectedRequestSet() );

    assertFalse( csrfGateFilter.isInitialized() );

    csrfGateFilter.init( filterConfig );

    assertEquals( filterConfig, csrfGateFilter.getInnerFilter().getFilterConfig() );

    assertTrue( csrfGateFilter.isInitialized() );
    assertFalse( csrfGateFilter.isEnabled() );

    verify( csrfConfigProvider ).registerChangeListener( any( Runnable.class ) );
  }

  @Test
  public void testInitWithCsrfProtectionEnabled() throws Exception {

    when( csrfConfigProvider.getCsrfConfiguration() ).thenReturn( getNonEmptyCsrfConfiguration() );

    assertFalse( csrfGateFilter.isInitialized() );

    csrfGateFilter.init( filterConfig );

    assertEquals( filterConfig, csrfGateFilter.getInnerFilter().getFilterConfig() );

    assertTrue( csrfGateFilter.isInitialized() );
    assertTrue( csrfGateFilter.isEnabled() );
  }

  @Test
  public void testCsrfConfigurationChangeCausesReload() throws Exception {

    // Setup
    ArgumentCaptor<Runnable> listenerCapture = ArgumentCaptor.forClass( Runnable.class );

    doNothing().when( csrfConfigProvider ).registerChangeListener( listenerCapture.capture() );

    when( csrfConfigProvider.getCsrfConfiguration() ).thenReturn( getNonEmptyCsrfConfiguration() );

    csrfGateFilter.init( filterConfig );

    verify( csrfConfigProvider, times( 1 ) ).getCsrfConfiguration();

    assertTrue( csrfGateFilter.isInitialized() );
    assertTrue( csrfGateFilter.isEnabled() );

    // Test Simulate reload.
    listenerCapture.getValue().run();

    assertTrue( csrfGateFilter.isInitialized() );
    assertTrue( csrfGateFilter.isEnabled() );

    verify( csrfConfigProvider, times( 2 ) ).getCsrfConfiguration();
  }

  @Test
  public void testDoFilterWhenInitializedAndCsrfProtectionDisabled() throws Exception {

    // Setup
    when( csrfConfigProvider.getCsrfConfiguration() ).thenReturn( new CsrfProtectedRequestSet() );

    csrfGateFilter.init( filterConfig );

    assertTrue( csrfGateFilter.isInitialized() );
    assertFalse( csrfGateFilter.isEnabled() );

    // Test
    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );
    FilterChain filterChain = new MockFilterChain();

    csrfGateFilter.doFilter( mockRequest, mockResponse, filterChain );
  }

  @Test
  public void testDoFilterWhenInitializedAndCsrfProtectionEnabled() throws Exception {

    // Setup
    when( csrfConfigProvider.getCsrfConfiguration() ).thenReturn( getNonEmptyCsrfConfiguration() );

    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );
    FilterChain filterChain = new MockFilterChain();

    CsrfToken mockCsrfToken = mock( CsrfToken.class );
    when( mockCsrfToken.getParameterName() ).thenReturn( "_token_" );
    when( csrfTokenRepository.loadToken( mockRequest ) ).thenReturn( mockCsrfToken );

    csrfGateFilter.init( filterConfig );

    verify( csrfConfigProvider, times( 1 ) ).getCsrfConfiguration();

    assertTrue( csrfGateFilter.isInitialized() );
    assertTrue( csrfGateFilter.isEnabled() );

    // Test
    csrfGateFilter.doFilter( mockRequest, mockResponse, filterChain );

    verify( csrfConfigProvider, times( 1 ) ).getCsrfConfiguration();

    assertTrue( csrfGateFilter.isInitialized() );
    assertTrue( csrfGateFilter.isEnabled() );
  }

  @Test
  public void testDoFilterWhileReloadAndCsrfProtectionEnabled() throws Exception {

    // Setup
    when( csrfConfigProvider.getCsrfConfiguration() ).thenReturn( getNonEmptyCsrfConfiguration() );

    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );
    FilterChain filterChain = new MockFilterChain();

    CsrfToken mockCsrfToken = mock( CsrfToken.class );
    when( mockCsrfToken.getParameterName() ).thenReturn( "_token_" );
    when( csrfTokenRepository.loadToken( mockRequest ) ).thenReturn( mockCsrfToken );

    csrfGateFilter.init( filterConfig );

    verify( csrfConfigProvider, times( 1 ) ).getCsrfConfiguration();

    assertTrue( csrfGateFilter.isInitialized() );
    assertTrue( csrfGateFilter.isEnabled() );

    // Simulate to be within doInit(), in another thread...
    csrfGateFilter.setIsInitialized( false );

    // Test
    csrfGateFilter.doFilter( mockRequest, mockResponse, filterChain );

    verify( csrfConfigProvider, times( 2 ) ).getCsrfConfiguration();

    assertTrue( csrfGateFilter.isInitialized() );
    assertTrue( csrfGateFilter.isEnabled() );
  }
}
