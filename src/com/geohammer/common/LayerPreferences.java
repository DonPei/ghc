package com.geohammer.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Preferences specific for seismic viewer application.
 * Preferences are saved when the application is quit, and reloaded during next start-up.
 * 
 */
public class LayerPreferences extends UcdmAbstractPreference {

	public static final String NODE_DIR_SEISMIC    = "<seismic_dir>";
	public static final String NODE_DIR_PROPERTIES = "<properties_dir>";
	public static final String NODE_DIR_SCREENDUMP = "<screendump_dir>";
	public static final String NODE_END            = "<end>";
	public static final String NODE_RECENT_FILES   = "<recent_files>";

	public static final String NODE_TT_FILE_NAME   = "<TravelTime_fileName>";
	public static final String NODE_VEL_FILE_NAME  = "<Velocity_fileName>";
	
	public static final String NODE_General    		= "<General>";
	
	private String _fileHeader = null;
	private String _dataDirectoryPath = System.getProperty("user.home");
	private String _screenDumpDirectoryPath;
	private String _propertiesDirectoryPath;
	private ArrayList<String> 	_recentFileList = null;
	private String 				_ttFileName 	= null;
	private String 				_velFileName 	= null;
	private String [] 			_general 		= new String[30];
	
	public LayerPreferences(String name) {
		super(name);
		_fileHeader = "### " + name + " preferences file ###";;
		_dataDirectoryPath = UcdmAbstractPreference.getUcdmDirectory().getAbsolutePath();
		_screenDumpDirectoryPath = UcdmAbstractPreference.getUcdmDirectory().getAbsolutePath();
		_propertiesDirectoryPath = UcdmAbstractPreference.getUcdmDirectory().getAbsolutePath();
	}
	
	public String getDataDirectory() 					{ return _dataDirectoryPath; }
	public String getPropertiesDirectory() 				{ return _propertiesDirectoryPath; }
	public String getScreenDumpDirectory() 				{ return _screenDumpDirectoryPath; }
	public String getTtFileName() 						{ return _ttFileName; }
	public String getVelFileName() 						{ return _velFileName; }
	public String getGeneral(int index)					{ return _general[index]; }
	public ArrayList<String> getRecentFileList() 		{ return _recentFileList; }
	public String getRecentFile() 						{ 
		if(_recentFileList==null || _recentFileList.size()==0) return System.getProperty("user.dir");
		//else return _recentFileList.get(_recentFileList.size()-1); 
		else return _recentFileList.get(0);
	}
	
	public void setDataDirectory( String dir ) 			{ _dataDirectoryPath = dir; }
	public void setPropertiesDirectory( String dir ) 	{ _propertiesDirectoryPath = dir; }
	public void setScreenDumpDirectory( String dir )	{ _screenDumpDirectoryPath = dir; }
	public void setRecentFileList( ArrayList<String> list ) { _recentFileList = list; }
	public void setTtFileName( String ttFileName )		{ _ttFileName = ttFileName; }
	public void setVelFileName( String velFileName )	{ _velFileName = velFileName; }
	public void setGeneral(int index, String v )		{ _general[index] = v; }
	
	public void addRecentFileList( String fileName ) { 
		if(_recentFileList==null) {_recentFileList=new ArrayList<String>(); }
		if(_recentFileList.size()>30) _recentFileList.remove(0);
		_recentFileList.add(fileName);
	}
	
	//----------------------------------------------
	protected void readPreferences(BufferedReader reader) throws IOException {
		String line;
		_recentFileList = new ArrayList<String>();
		while( (line = reader.readLine()) != null ) {
			if( line.equalsIgnoreCase(NODE_DIR_SEISMIC) ) {
				while( (line = reader.readLine()) != null && !line.trim().equalsIgnoreCase(NODE_END)) {
					_dataDirectoryPath = line.trim();
				}
			}
			else if( line.equalsIgnoreCase(NODE_DIR_SCREENDUMP) ) {
				while( (line = reader.readLine()) != null && !line.trim().equalsIgnoreCase(NODE_END)) {
					_screenDumpDirectoryPath = line.trim();
				}
			}
			else if( line.equalsIgnoreCase(NODE_DIR_PROPERTIES) ) {
				while( (line = reader.readLine()) != null && !line.trim().equalsIgnoreCase(NODE_END)) {
					_propertiesDirectoryPath = line.trim();
				}
			}
			else if( line.equalsIgnoreCase(NODE_TT_FILE_NAME) ) {
				while( (line = reader.readLine()) != null && !line.trim().equalsIgnoreCase(NODE_END)) {
					_ttFileName = line.trim();
				}
			}
			else if( line.equalsIgnoreCase(NODE_VEL_FILE_NAME) ) {
				while( (line = reader.readLine()) != null && !line.trim().equalsIgnoreCase(NODE_END)) {
					_velFileName = line.trim();
				}
			}
			else if( line.equalsIgnoreCase(NODE_General) ) {
				int k = 0;;
				while( (line = reader.readLine()) != null && !line.trim().equalsIgnoreCase(NODE_END)) {
					//System.out.println(line.trim());
					_general[k++] = line.trim();
				}
			}
			else if( line.equalsIgnoreCase(NODE_RECENT_FILES) ) {
				_recentFileList = new ArrayList<String>();
				while( (line = reader.readLine()) != null && !line.trim().equalsIgnoreCase(NODE_END)) {
					_recentFileList.add( line.trim() );
				}
			}

		}
	}
	protected void writePreferences( BufferedWriter writer ) throws IOException {
		writer.write(_fileHeader);
		writer.newLine();
		writer.newLine();
		writer.write(NODE_DIR_SEISMIC);
		writer.newLine();
		String dir = _dataDirectoryPath;
		if( dir != null ) {
			writer.write( dir );
			writer.newLine();
		}
		writer.write(NODE_END);
		writer.newLine();
		writer.write(NODE_DIR_SCREENDUMP);
		writer.newLine();
		dir = _screenDumpDirectoryPath;
		if( dir != null ) {
			writer.write( dir );
			writer.newLine();
		}
		writer.write(NODE_END);
		writer.newLine();
		writer.write(NODE_DIR_PROPERTIES);
		writer.newLine();
		dir = _propertiesDirectoryPath;
		if( dir != null ) {
			writer.write( dir );
			writer.newLine();
		}
		writer.write(NODE_END);
		writer.newLine();
		
		writer.write(NODE_TT_FILE_NAME);
		writer.newLine();
		dir = _ttFileName;
		if( dir != null ) {
			writer.write( dir );
			writer.newLine();
		} 
		writer.write(NODE_END);
		writer.newLine();
		
		writer.write(NODE_VEL_FILE_NAME);
		writer.newLine();
		dir = _velFileName;
		if( dir != null ) {
			writer.write( dir );
			writer.newLine();
		} 
		writer.write(NODE_END);
		writer.newLine();
		
		int k = 0;
		for( int i = 0; i < _general.length; i++ ) {
			if(_general[i]!=null) { k++; }
		}
		if(k>0) {
			writer.write(NODE_General);
			writer.newLine();
			for( int i = 0; i < _general.length; i++ ) {
				if(_general[i]!=null) { writer.write(_general[i] );	writer.newLine(); }
			}
			writer.write(NODE_END);
			writer.newLine();
		}
		
		writer.write(NODE_RECENT_FILES);
		writer.newLine();
		if( _recentFileList != null ) {
			for( int i = 0; i < _recentFileList.size(); i++ ) {
				writer.write( (String)_recentFileList.get(i) );
				writer.newLine();
			}
		}
		writer.write(NODE_END);
		writer.newLine();
		writer.newLine();
	}
}


