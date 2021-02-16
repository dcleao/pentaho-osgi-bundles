/*!
 *
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

package org.hitachivantara.security.web.impl.model.csrf;

import org.hitachivantara.security.web.api.model.csrf.CsrfRequestSetConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class AggregatedRequestSetCsrfConfigurationTest {

  private AggregatedRequestSetCsrfConfiguration configuration;

  @Before
  public void setUp() {
    configuration = new AggregatedRequestSetCsrfConfiguration();
  }

  @Test
  public void testWhenJustConstructedThenIsGloballyEnabled() {
    assertTrue( configuration.isEnabled() );
  }

  @Test
  public void testWhenJustConstructedThenIsEnabledForAllRequests() {

    HttpServletRequest request = mock( HttpServletRequest.class );

    assertTrue( configuration.isEnabled( request ) );
  }

  @Test
  public void testWhenSetDisabledAndHasNoRequestSetConfigsThenIsGloballyDisabled() {

    configuration.setEnabled( false );

    assertFalse( configuration.isEnabled() );
  }

  @Test
  public void testWhenSetDisabledAndHasNoRequestSetConfigsThenIsDisabledForAllRequests() {

    configuration.setEnabled( false );

    HttpServletRequest request = mock( HttpServletRequest.class );

    assertFalse( configuration.isEnabled( request ) );
  }

  // region Sample configuration
  private static final String PROTECTED_URL_A = "A";
  private static final String PROTECTED_URL_B = "B";
  private static final String UNPROTECTED_URL = "C";

  private static HttpServletRequest createMockRequest( String url ) {

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );

    when( mockRequest.getRequestURI() ).thenReturn( url );

    return mockRequest;
  }

  private static CsrfRequestSetConfiguration createSampleRequestSetConfig( String url ) {
    return new CsrfRequestSetConfigurationPojo( request -> request.getRequestURI().equals( url ) );
  }

  private static List<CsrfRequestSetConfiguration> getSampleConfigs() {

    ArrayList<CsrfRequestSetConfiguration> list = new ArrayList<>();

    list.add( createSampleRequestSetConfig( PROTECTED_URL_A ) );
    list.add( createSampleRequestSetConfig( PROTECTED_URL_B ) );

    return list;
  }
  // endregion

  @Test
  public void testWhenSetDisabledAndHasRequestSetConfigsThenIsGloballyDisabled() {

    configuration.setEnabled( false );
    configuration.setRequestSetConfigurations( getSampleConfigs() );

    assertFalse( configuration.isEnabled() );
  }

  @Test
  public void testWhenSetDisabledAndHasRequestSetConfigsThenIsDisabledForOtherwiseProtectedRequests() {

    configuration.setEnabled( false );
    configuration.setRequestSetConfigurations( getSampleConfigs() );

    HttpServletRequest protectedRequest = createMockRequest( PROTECTED_URL_A );

    assertFalse( configuration.isEnabled( protectedRequest ) );
  }

  @Test
  public void testWhenSetDisabledAndHasRequestSetConfigsThenIsDisabledForUnprotectedRequests() {

    configuration.setEnabled( false );
    configuration.setRequestSetConfigurations( getSampleConfigs() );

    HttpServletRequest unprotectedRequest = createMockRequest( UNPROTECTED_URL );

    assertFalse( configuration.isEnabled( unprotectedRequest ) );
  }

  @Test
  public void testWhenSetDisabledAndHasRequestSetConfigsAndThenRenableThenTheRequestSetConfigsAreMaintained() {

    configuration.setEnabled( false );
    configuration.setRequestSetConfigurations( getSampleConfigs() );
    configuration.setEnabled( true );

    HttpServletRequest protectedRequest = createMockRequest( PROTECTED_URL_A );

    assertTrue( configuration.isEnabled( protectedRequest ) );

    HttpServletRequest unprotectedRequest = createMockRequest( UNPROTECTED_URL );

    assertFalse( configuration.isEnabled( unprotectedRequest ) );
  }

  @Test
  public void testWhenSetEnabledAndHasRequestSetConfigsThenIsGloballyDisabled() {

    configuration.setEnabled( true );
    configuration.setRequestSetConfigurations( getSampleConfigs() );

    assertTrue( configuration.isEnabled() );
  }

  @Test
  public void testWhenSetEnabledAndHasRequestSetConfigsThenIsEnabledForProtectedRequests() {

    configuration.setEnabled( true );
    configuration.setRequestSetConfigurations( getSampleConfigs() );

    HttpServletRequest protectedRequest = createMockRequest( PROTECTED_URL_A );

    assertTrue( configuration.isEnabled( protectedRequest ) );

    protectedRequest = createMockRequest( PROTECTED_URL_B );

    assertTrue( configuration.isEnabled( protectedRequest ) );
  }

  @Test
  public void testWhenSetEnabledAndHasRequestSetConfigsThenIsDisabledForUnprotectedRequests() {

    configuration.setEnabled( true );
    configuration.setRequestSetConfigurations( getSampleConfigs() );

    HttpServletRequest unprotectedRequest = createMockRequest( UNPROTECTED_URL );

    assertFalse( configuration.isEnabled( unprotectedRequest ) );
  }

  @Test
  public void testWhenAddingANewProtectedRequestToTheConfigItBecomesProtected() {

    List<CsrfRequestSetConfiguration> configs = getSampleConfigs();

    configuration.setRequestSetConfigurations( configs );

    HttpServletRequest unprotectedRequest = createMockRequest( UNPROTECTED_URL );

    assertFalse( configuration.isEnabled( unprotectedRequest ) );

    CsrfRequestSetConfiguration newConfig = createSampleRequestSetConfig( UNPROTECTED_URL );
    configs.add( newConfig );

    assertFalse( configuration.isEnabled( unprotectedRequest ) );

    configuration.requestSetConfigurationDidBind( newConfig );

    assertTrue( configuration.isEnabled( unprotectedRequest ) );
    assertTrue( configuration.isEnabled( createMockRequest( PROTECTED_URL_A ) ) );
  }

  @Test
  public void testWhenRemovingAProtectedRequestFromTheConfigItBecomesUnprotected() {

    List<CsrfRequestSetConfiguration> configs = getSampleConfigs();

    configuration.setRequestSetConfigurations( configs );

    HttpServletRequest protectedRequest = createMockRequest( PROTECTED_URL_B );

    assertTrue( configuration.isEnabled( protectedRequest ) );

    @SuppressWarnings( "OptionalGetWithoutIsPresent" )
    CsrfRequestSetConfiguration removeConfig = configs.stream()
      .filter( config -> config.getRequestMatcher().test( protectedRequest ) )
      .findFirst()
      .get();

    configs.remove( removeConfig );

    assertTrue( configuration.isEnabled( protectedRequest ) );

    configuration.requestSetConfigurationWillUnbind( removeConfig );

    assertFalse( configuration.isEnabled( protectedRequest ) );
    assertTrue( configuration.isEnabled( createMockRequest( PROTECTED_URL_A ) ) );
  }
}
