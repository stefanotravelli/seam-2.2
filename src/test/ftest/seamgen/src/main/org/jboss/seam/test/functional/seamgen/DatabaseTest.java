/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.test.functional.seamgen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Every test that uses database and needs import script to be executed should
 * extend this class.
 * 
 * @author Jozef Hartinger
 * 
 */
public class DatabaseTest extends SeleniumSeamGenTest
{

   /**
    * Execute import script against the database defined in import.sql
    */
   public void executeImportScript(InputStream is)
   {
      Connection conn = null;
      try
      {
         Class.forName(ftestProperties.getProperty("hibernate.connection.driver_class"));
         String url = ftestProperties.getProperty("hibernate.connection.url");
         conn = DriverManager.getConnection(url, ftestProperties.getProperty("hibernate.connection.username"), ftestProperties.getProperty("hibernate.connection.password"));
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         String line = reader.readLine();
         while (line != null)
         {
            if (!line.equals(new String())) // don't execute empty lines
            {
               conn.createStatement().execute(line);
            }
            line = reader.readLine();
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Database import script failed.", e);
      }
      finally
      {
         try
         {
            conn.close();
         }
         catch (Exception e)
         {
         }
      }
   }

}
