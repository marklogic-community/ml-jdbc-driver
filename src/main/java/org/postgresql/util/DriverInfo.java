/*
 * Copyright (c) 2017, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.util;

/**
 * Utility class with constants of Driver information.
 */
public final class DriverInfo {

  private DriverInfo() {
  }

  // Driver name
  public static final String DRIVER_NAME = "MarkLogic JDBC Driver";
  public static final String DRIVER_SHORT_NAME = "mlJDBC";
  //public static final String DRIVER_VERSION = "/*$mvn.project.property.parsedversion.osgiversion$*/";
  public static final String DRIVER_VERSION = "0.3";
  public static final String DRIVER_FULL_NAME = DRIVER_NAME + " " + DRIVER_VERSION;

  // Driver version
  public static final int MAJOR_VERSION = /*$mvn.project.property.parsedversion.majorversion+";"$*//*-*/ 0;
  public static final int MINOR_VERSION = /*$mvn.project.property.parsedversion.minorversion+";"$*//*-*/ 3;
  public static final int PATCH_VERSION = /*$mvn.project.property.parsedversion.incrementalversion+";"$*//*-*/ 0;

  // JDBC specification
  public static final String JDBC_VERSION = /*$mvn.project.property.jdbc.specification.version$*/ "4.2";
  private static final int JDBC_INTVERSION = /*$mvn.project.property.jdbc.specification.version.nodot+";"$*//*-*/ 42;
  public static final int JDBC_MAJOR_VERSION = JDBC_INTVERSION / 10;
  public static final int JDBC_MINOR_VERSION = JDBC_INTVERSION % 10;

}
