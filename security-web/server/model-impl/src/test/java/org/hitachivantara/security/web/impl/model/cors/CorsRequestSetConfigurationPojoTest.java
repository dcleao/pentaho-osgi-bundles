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

package org.hitachivantara.security.web.impl.model.cors;

import org.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration;
import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CorsRequestSetConfigurationPojoTest {

  @Test
  public void testWhenConstructedWithNoArgsIsConcreteAndIsEnabledAndMatchesNone() {
    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    assertTrue( config.isEnabled() );
    assertFalse( config.isAbstract() );
    assertEquals( RequestMatcher.NONE, config.getRequestMatcher() );
  }

  @Test
  public void testWhenConstructedWithNoArgsHasNoOtherSpecifiedProperties() {
    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    assertNull( config.getName() );
    assertNull( config.getParentName() );

    assertNull( config.getAllowCredentials() );
    assertNull( config.getMaxAge() );
    assertNull( config.getAllowedHeaders() );
    assertNull( config.getAllowedMethods() );
    assertNull( config.getAllowedOrigins() );
    assertNull( config.getExposedHeaders() );
  }

  @Test
  public void testWhenConstructedWithArgsEnabledThenIsEnabledAndIsConcreteAndMatchesNone() {
    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo( true );

    assertTrue( config.isEnabled() );
    assertFalse( config.isAbstract() );
    assertEquals( RequestMatcher.NONE, config.getRequestMatcher() );
  }

  @Test
  public void testWhenConstructedWithArgsDisabledThenIsDisabledAndIsConcreteAndMatchesNone() {
    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo( false );

    assertFalse( config.isEnabled() );
    assertFalse( config.isAbstract() );
    assertEquals( RequestMatcher.NONE, config.getRequestMatcher() );
  }

  private CorsRequestSetConfiguration createConfiguration() {
    RequestMatcher matcher = httpServletRequest -> false;

    Set<String> allowedOrigins = new HashSet<>( Arrays.asList( "Foo1", "Bar1" ) );
    Set<String> allowedMethods = new HashSet<>( Arrays.asList( "Foo2", "Bar2" ) );
    Set<String> allowedHeaders = new HashSet<>( Arrays.asList( "Foo3", "Bar3" ) );
    Set<String> exposedHeaders = new HashSet<>( Arrays.asList( "Foo4", "Bar4" ) );
    Long maxAge = 1L;
    Boolean allowCredentials = Boolean.TRUE;

    return new CorsRequestSetConfiguration() {
      @Nullable @Override
      public String getName() {
        return "foo";
      }

      @Nullable @Override
      public String getParentName() {
        return "bar";
      }

      @Nonnull @Override
      public RequestMatcher getRequestMatcher() {
        return matcher;
      }

      @Override
      public boolean isEnabled() {
        return false;
      }

      @Override
      public boolean isAbstract() {
        return true;
      }

      @Nullable @Override
      public Set<String> getAllowedOrigins() {
        return allowedOrigins;
      }

      @Nullable @Override
      public Set<String> getAllowedMethods() {
        return allowedMethods;
      }

      @Nullable @Override
      public Set<String> getAllowedHeaders() {
        return allowedHeaders;
      }

      @Nullable @Override
      public Boolean getAllowCredentials() {
        return allowCredentials;
      }

      @Nullable @Override
      public Long getMaxAge() {
        return maxAge;
      }

      @Nullable @Override
      public Set<String> getExposedHeaders() {
        return exposedHeaders;
      }
    };
  }

  @Test
  public void testWhenConstructedWithArgOtherConfigThenAllValuesAreEqual() {

    CorsRequestSetConfiguration other = createConfiguration();

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo( other );

    assertEquals( other.getName(), config.getName() );
    assertEquals( other.getParentName(), config.getParentName() );
    assertEquals( other.getRequestMatcher(), config.getRequestMatcher() );
    assertEquals( other.isEnabled(), config.isEnabled() );
    assertEquals( other.isAbstract(), config.isAbstract() );
    assertEquals( other.getMaxAge(), config.getMaxAge() );
    assertEquals( other.getAllowCredentials(), config.getAllowCredentials() );
    assertEquals( other.getAllowedHeaders(), config.getAllowedHeaders() );
    assertEquals( other.getAllowedOrigins(), config.getAllowedOrigins() );
    assertEquals( other.getAllowedMethods(), config.getAllowedMethods() );
    assertEquals( other.getExposedHeaders(), config.getExposedHeaders() );
  }

  @Test
  public void testWhenConstructedWithArgOtherConfigThenEqualsIsTrue() {

    CorsRequestSetConfiguration other = createConfiguration();

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo( other );

    // noinspection SimplifiableAssertion
    assertTrue( config.equals( other ) );
  }

  @Test
  public void testSetNameWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setName( "FOO" );

    assertEquals( "FOO", config.getName() );
  }

  @Test
  public void testSetParentNameWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setParentName( "BAR" );

    assertEquals( "BAR", config.getParentName() );
  }

  @Test
  public void testSetEnabledWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setEnabled( true );

    assertTrue( config.isEnabled() );

    config.setEnabled( false );

    assertFalse( config.isEnabled() );
  }

  @Test
  public void testSetAbstractWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setAbstract( true );

    assertTrue( config.isAbstract() );

    config.setAbstract( false );

    assertFalse( config.isAbstract() );
  }

  @Test
  public void testSetRequestMatcherWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    RequestMatcher matcher = request -> true;
    config.setRequestMatcher( matcher );

    assertSame( matcher, config.getRequestMatcher() );

    config.setRequestMatcher( null );

    assertSame( RequestMatcher.NONE, config.getRequestMatcher() );
  }

  @Test
  public void testSetMaxAgeWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setMaxAge( 10L );

    assertEquals( config.getMaxAge(), new Long( 10L ) );

    config.setMaxAge( null );

    assertNull( config.getMaxAge() );
  }

  @Test
  public void testSetAllowCredentialsWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setAllowCredentials( false );

    //noinspection UnnecessaryBoxing,BooleanConstructorCall
    assertEquals( config.getAllowCredentials(), new Boolean( false ) );

    config.setAllowCredentials( true );

    //noinspection UnnecessaryBoxing,BooleanConstructorCall
    assertEquals( config.getAllowCredentials(), new Boolean( true ) );

    config.setAllowCredentials( null );

    assertNull( config.getAllowCredentials() );
  }

  @Test
  public void testSetAllowedOriginsWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setAllowedOrigins( new HashSet<>( Arrays.asList( "a", "b" ) ) );

    assertEquals( config.getAllowedOrigins(), new HashSet<>( Arrays.asList( "a", "b" ) ) );
  }

  @Test
  public void testSetAllowedHeadersWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setAllowedHeaders( new HashSet<>( Arrays.asList( "a", "b" ) ) );

    assertEquals( config.getAllowedHeaders(), new HashSet<>( Arrays.asList( "a", "b" ) ) );
  }

  @Test
  public void testSetAllowedMethodsWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setAllowedMethods( new HashSet<>( Arrays.asList( "a", "b" ) ) );

    assertEquals( config.getAllowedMethods(), new HashSet<>( Arrays.asList( "a", "b" ) ) );
  }

  @Test
  public void testSetExposedHeadersWorks() {

    CorsRequestSetConfigurationPojo config = new CorsRequestSetConfigurationPojo();

    config.setExposedHeaders( new HashSet<>( Arrays.asList( "a", "b" ) ) );

    assertEquals( config.getExposedHeaders(), new HashSet<>( Arrays.asList( "a", "b" ) ) );
  }
}
