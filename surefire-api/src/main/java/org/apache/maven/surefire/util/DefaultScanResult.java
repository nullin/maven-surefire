package org.apache.maven.surefire.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Kristian Rosenvold
 */
public class DefaultScanResult
    implements ScanResult
{
    private final List files;

    private static final String scanResultNo = "tc.";

    public DefaultScanResult( List files )
    {
        this.files = files;
    }

    public int size()
    {
        return files.size();
    }

    public String getClassName( int index )
    {
        return (String) files.get( index );
    }

    public void writeTo( Properties properties )
    {
        int size = files.size();
        for ( int i = 0; i < size; i++ )
        {
            properties.setProperty( scanResultNo + i, (String) files.get( i ) );
        }
    }

    public static DefaultScanResult from( Properties properties )
    {
        List result = new ArrayList();
        int i = 0;
        while ( true )
        {
            String item = properties.getProperty( scanResultNo + ( i++ ) );
            if ( item == null )
            {
                return new DefaultScanResult( result );
            }
            result.add( item );
        }
    }

    public boolean isEmpty()
    {
        return files.isEmpty();
    }

    public List getFiles()
    {
        return Collections.unmodifiableList( files );
    }

    public TestsToRun applyFilter( ScannerFilter scannerFilter, ClassLoader testClassLoader )
    {
        List result = new ArrayList();

        int size = size();
        for ( int i = 0; i < size; i++ )
        {
            String className = getClassName( i );

            Class testClass = loadClass( testClassLoader, className );

            if ( scannerFilter == null || scannerFilter.accept( testClass ) )
            {
                result.add( testClass );
            }
        }

        return new TestsToRun( result );
    }

    public List getClassesSkippedByValidation( ScannerFilter scannerFilter, ClassLoader testClassLoader )
    {
        List result = new ArrayList();

        int size = size();
        for ( int i = 0; i < size; i++ )
        {
            String className = getClassName( i );

            Class testClass = loadClass( testClassLoader, className );

            if ( scannerFilter != null && !scannerFilter.accept( testClass ) )
            {
                result.add( testClass );
            }
        }

        return result;
    }

    private static Class loadClass( ClassLoader classLoader, String className )
    {
        Class testClass;
        try
        {
            testClass = classLoader.loadClass( className );
        }
        catch ( ClassNotFoundException e )
        {
            throw new NestedRuntimeException( "Unable to create test class '" + className + "'", e );
        }
        return testClass;
    }


}
