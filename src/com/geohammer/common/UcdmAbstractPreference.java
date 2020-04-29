/*
 *  SeaSeis - Seismic processing system for seabed (OBS) data
 *  CSeisLib - Seismic display library
 *  SeaView  - Seismic viewer prototype
 *  Copyright (C) 2006-2012  Bjorn Olofsson
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.geohammer.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

/**
 * Base class for user preferences/configuration file
 */
public abstract class UcdmAbstractPreference {
	public static final boolean IS_WINDOWS = ( java.io.File.separatorChar == '\\' );
	public static final String FILE_NAME_START = "preferences_";

	private static File _ucdmDirectory = null;
	private static File _homeDirectory;

	private PrintWriter _errorLogWriter;
	protected File _preferencesFile;
	protected File _errorFile;
	private Vector<String> _exceptionErrorList;

	public static File getUcdmDirectory() {
		return _ucdmDirectory;
	}
	public static boolean setUcdmDirectory(String folder) throws Exception {
		FileSystemView fsv = FileSystemView.getFileSystemView();
		_homeDirectory = fsv.getHomeDirectory();
		if( IS_WINDOWS ) {
			// Trick to get to C:\\Documents and Settings\\user_name :
			File dirFullPath = new File( _homeDirectory, "" );
			_homeDirectory = dirFullPath.getParentFile();
		}

		_ucdmDirectory = new File( _homeDirectory, folder );
		if( !_ucdmDirectory.exists() || !_ucdmDirectory.isDirectory() ) {
			if( !_ucdmDirectory.mkdir() ) {
				JOptionPane.showMessageDialog( null,
						"Error occurred when trying to create configuration directory\n" +
								_ucdmDirectory + "\nProgram will be terminated.",
								"", JOptionPane.ERROR_MESSAGE );
				throw new Exception( "Cannot create configuration directory" );
			}
			return true;
		}
		return false;
	}

	//--------------------------------------
	public UcdmAbstractPreference( String name ) {
		_preferencesFile = new File( getUcdmDirectory(), "pref_" + name + ".txt" );
		_exceptionErrorList = new Vector<String>();
		_errorFile = new File( getUcdmDirectory(), "pref_" + name + ".err" );
	}
	public boolean readPreferences() {
		if( _preferencesFile.exists() ) {
			readPreferences( _preferencesFile );
			return true;
		} else {
			return false;
		}
	}
	public void readPreferences( File file ) {
		openErrorLogWriter();
		_exceptionErrorList.clear();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader( file ) );
			readPreferences(reader);
			reader.close();
		} catch( IOException e ) {
			if( _errorLogWriter != null ) e.printStackTrace(_errorLogWriter);
			if( reader != null ) {
				try {
					reader.close();
				} catch( IOException exc ) {
					if( _errorLogWriter != null ) exc.printStackTrace(_errorLogWriter);
				}
			}
		}
		closeErrorLogWriter();
	}
	//----------------------------------------------
	public void writePreferences() {
		writePreferences( _preferencesFile );
	}
	public void writePreferences( File file ) {
		openErrorLogWriter();
		_exceptionErrorList.clear();

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter( file ) );
			writePreferences(writer);
			writer.close();
		} catch( IOException e ) {
			if( _errorLogWriter != null ) e.printStackTrace(_errorLogWriter);
			if( writer != null ) {
				try {
					writer.close();
				} catch( IOException exc ) {
					if( _errorLogWriter != null ) exc.printStackTrace(_errorLogWriter);
				}
			}
		}
		closeErrorLogWriter();
	}
	protected abstract void readPreferences(BufferedReader reader) throws IOException;
	protected abstract void writePreferences(BufferedWriter writer) throws IOException;
	//----------------------------------------------
	private void closeErrorLogWriter() {
		if( _errorLogWriter != null ) {
			_errorLogWriter.close();
			_errorLogWriter = null;
		}
	}
	//----------------------------------------------
	private void openErrorLogWriter() {
		try {
			_errorLogWriter = new PrintWriter( new BufferedWriter( new FileWriter( _errorFile ) ) );
		} catch( Exception e ) {
			if( _errorLogWriter != null ) _errorLogWriter = null;
		}
		_errorLogWriter.close();
		_errorLogWriter = null;
	}  

}
