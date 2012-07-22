/*******************************************************************************
 * Copyright (c) 2012 Igor Fedorenko
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package com.ifedorenko.m2e.tychodev.internal.launching;

import static com.ifedorenko.m2e.tychodev.internal.launching.TychoITLaunchConfigurationDelegate.ATTR_TEST_TARGETPLATFORM;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;

public class TychoITLaunchConfigurationTab
    extends AbstractLaunchConfigurationTab
{
    private Combo combo;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        setControl( composite );
        composite.setLayout( new GridLayout( 4, false ) );

        Label lblTestTargetPlatform = new Label( composite, SWT.NONE );
        lblTestTargetPlatform.setToolTipText( "Many Tycho ITs still require local Eclipse installation as test target platform" );
        lblTestTargetPlatform.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1 ) );
        lblTestTargetPlatform.setText( "Test target platform" );

        combo = new Combo( composite, SWT.NONE );
        combo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        Button btnSelect = new Button( composite, SWT.NONE );
        btnSelect.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                DirectoryDialog d = new DirectoryDialog( getShell() );
                d.setText( "Select local Eclipse installation" );
                setLocation( d.open() );
            }
        } );
        btnSelect.setText( "Select..." );

        Button btnDefault = new Button( composite, SWT.NONE );
        btnDefault.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                setLocation( getDefaultTestTargetPlatform() );
            }
        } );
        btnDefault.setText( "Default" );
    }

    void setLocation( String location )
    {
        combo.setText( location );
        setDirty( true );
        updateLaunchConfigurationDialog();
    }

    @Override
    public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
    {
        String location = getDefaultTestTargetPlatform();
        configuration.setAttribute( ATTR_TEST_TARGETPLATFORM, location );
    }

    @Override
    public void initializeFrom( ILaunchConfiguration configuration )
    {
        try
        {
            combo.setText( configuration.getAttribute( ATTR_TEST_TARGETPLATFORM, getTestTargetPlatform( configuration ) ) );
        }
        catch ( CoreException e )
        {
            // ignored
        }
    }

    @Override
    public void performApply( ILaunchConfigurationWorkingCopy configuration )
    {
        if ( combo.getText() != null && combo.getText().trim().length() > 0 )
        {
            configuration.setAttribute( ATTR_TEST_TARGETPLATFORM, combo.getText().trim() );
        }
        else
        {
            configuration.removeAttribute( ATTR_TEST_TARGETPLATFORM );
        }
    }

    private String getTestTargetPlatform( ILaunchConfiguration configuration )
        throws CoreException
    {
        String location = configuration.getAttribute( ATTR_TEST_TARGETPLATFORM, (String) null );

        if ( location != null )
        {
            return location;
        }

        return getDefaultTestTargetPlatform();
    }

    private String getDefaultTestTargetPlatform()
    {
        Location platformLocation = Platform.getInstallLocation();

        if ( platformLocation == null )
        {
            return null;
        }

        URL url = platformLocation.getURL();

        if ( "file".equals( url.getProtocol() ) )
        {
            try
            {
                return new File( url.toURI() ).getAbsolutePath();
            }
            catch ( URISyntaxException e )
            {
                // ignored
            }
        }

        return null;
    }

    @Override
    public String getName()
    {
        return "Tycho IT";
    }
}