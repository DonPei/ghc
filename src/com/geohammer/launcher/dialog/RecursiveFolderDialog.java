package org.ucdm.launcher.dialog;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;
import org.ucdm.common.util.UiUtil;
import org.ucdm.das1.DasMsFileName;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.seg2.SEG2;
import org.ucdm.seg2.SEG2FileHeader;
import org.ucdm.seg2.SEG2StringBlock;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class RecursiveFolderDialog extends CommonDialog {

	private static final String START 	= "Extract";
    private static final String STOP 	= "Stop";
	private LauncherFrame  	_frame 					= null;
	private JTextField 		_sourcePathTF			= null;
	private JTextField 		_sourceDepthTF			= null;
	private JTextField 		_targetPathTF			= null;
	private int 			_sourceDepth			= Integer.MAX_VALUE;
	
	private boolean [] 		_enableFileCriterion	= null;
	private JTextField 	[]	_fileCriterionTF		= null;

	private JTextField 		_eventFolderTF			= null;
	private boolean 		_enableEventFolder		= false;
	private JTextField 	[]	_eventOriginTimeTF		= null;
	private double 	[]		_eventOriginTime		= new double[]{-4, -1};
	
	private int 		_iFile 		= 0; 
	// 0-copy dir only 1 copy both file and dir 2 delete file only 3 delete both file and dir 	

	private ArrayList<LocalDateTime> _goephoneEvent = null;
	
	private JLabel 				_msgLabel 			= new JLabel("ready...", JLabel.LEFT);
	private CalculatorTask 		_calcTask 			= null;
	private JButton 			_run  				= null;
	private boolean 			_cancel 			= false;
	

	public RecursiveFolderDialog(CommonFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 450);
		setEnableApplyButton(true);
		_frame 		= (LauncherFrame)aParent;
	}
	
	protected JTabbedPane createContents() {
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		String cwd = System.getProperty("user.dir");
		tabbedPane.addTab("  Basic   ", new LoadFilePanel(_frame.getFolderSourcePath(), _frame.getFolderTargetPath()));
		tabbedPane.addTab("  Advanced   ", new AdvancedPanel(_frame.getFolderEventPath()));
		
		return tabbedPane;
	}
	
//	protected boolean okAction() {
//		String sourcePath = _sourcePathTF.getText().trim();
//		if(sourcePath==null || sourcePath.isEmpty()) return false;
//		
//		File file = new File(sourcePath);
//		if(!file.exists()) return false;
//		
//		if(_iFile==0 || _iFile==1) {			//copy
//			String targetPath = _targetPathTF.getText().trim();
//			if(targetPath==null || targetPath.isEmpty()) return false;
//			
//			goephoneEvent = null;
//			if(_enableEventFolder) {
//				String eventFolder = _eventFolderTF.getText().trim();
//				if(eventFolder==null || eventFolder.isEmpty()) { }
//				else { setGeophoneEvent(eventFolder); }
//			}
//			
//			copyFileTree(sourcePath, targetPath);
//		} else if(_iFile==2 || _iFile==3) { //delete
//			deleteFileTree(sourcePath);
//		} else if(_iFile==4 || _iFile==5) { //list
//			listFileTree(sourcePath);
//		}
//		
//		return true;		
//	}
	public boolean startCal() {
		_cancel 			= false;
		
		String sourcePath = _sourcePathTF.getText().trim();
		if(sourcePath==null || sourcePath.isEmpty()) return false;
		
		File file = new File(sourcePath);
		if(!file.exists()) return false;
		_frame.setFolderSourcePath(sourcePath);
		
		_sourceDepth = Integer.parseInt(_sourceDepthTF.getText().trim());
		if(_sourceDepth<=0) return false;
		
		_calcTask = new CalculatorTask(sourcePath);
		_calcTask.execute();
		return true;
	}

	public void stopCal() {
		if (_calcTask != null  &&  !_calcTask.isDone()) {
			_cancel 			= true;
			_calcTask.cancel(false);
		} else {
			//dispose();
		}
	}

	@Override
	protected boolean okAction() { return true; }
	protected JComponent getCommandRow() {
		_run = new JButton(START);
		_run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (STOP.equals(cmd)) {
					stopCal();
					_run.setText(START);
				} else {
					startCal();
					_run.setText(STOP);
				}
			}
		});
		this.getRootPane().setDefaultButton( _run );
		List<JComponent> buttons = new ArrayList<JComponent>();
		buttons.add( _run );
		return UiUtil.getCommandRow( buttons );
	}
	
	private class CalculatorTask extends SwingWorker<String, String> {
		String _sourcePath = null;

		long _startTime;	// Get current starting time in milliseconds

		public CalculatorTask(String sourcePath) {	
			_sourcePath = sourcePath;
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			_startTime = System.currentTimeMillis(); 
		}

		@Override
		protected String doInBackground() {
			while (isCancelled()) return "Cancelled: "+getElapsedTime();
			if(_iFile==0 || _iFile==1) {			//copy
				String targetPath = _targetPathTF.getText().trim();
				if(targetPath==null || targetPath.isEmpty()) return null;

				_frame.setFolderTargetPath(targetPath);
				
				_goephoneEvent = null;
				if(_enableEventFolder) {
					String eventFolder = _eventFolderTF.getText().trim();
					if(eventFolder==null || eventFolder.isEmpty()) { }
					else { 
						setGeophoneEvent(eventFolder); 
						_frame.setFolderEventPath(eventFolder);
						
						_eventOriginTime[0] = Double.parseDouble(_eventOriginTimeTF[0].getText().trim());
						_eventOriginTime[1] = Double.parseDouble(_eventOriginTimeTF[1].getText().trim());
					}
				}
				
				copyFileTree(_sourcePath, targetPath);
			} else if(_iFile==2 || _iFile==3) { //delete
				deleteFileTree(_sourcePath);
			} else if(_iFile==4 || _iFile==5) { //list
				listFileTree(_sourcePath);
			}
			return "Successful: "+getElapsedTime();
		}

		public void update(String token) { publish(token); }
		protected void process(List<String>  tokens) {
			_msgLabel.setText(tokens.get(0));
		}

		@Override
		protected void done() {
			//_progBar.setValue( 100 );
			try {
				String token = get();
				if(token!=null) { _msgLabel.setText(token); }
			} catch(InterruptedException e) {
				System.out.println("Iterrupted while waiting for results.");
			} catch(ExecutionException e) {
				System.out.println("Error performing computation.");
			} catch (CancellationException e) {
				_cancel = true;
				_msgLabel.setText("User cancelled.");
				//System.out.println("User cancelled.");
			}	

			//_progBar.setString("Done");
			setCursor( null );
		}

		private String getElapsedTime() {
			long endTime = System.currentTimeMillis();
			return "finished with an elapsed time: " + toElapsedTime(endTime-_startTime);
		}
		private String toElapsedTime(long milliseconds) {
			double totalSeconds = (double) milliseconds / 1000.0;

			int hours   = (int) (totalSeconds/3600.0);
			int minutes = (int) ((totalSeconds-hours*3600)/60.0);
			int seconds = (int) (totalSeconds-hours*3600-minutes*60.0);

			return String.format("%d Hr %d Min %d Sec.\n", hours, minutes, seconds);
		}
	}

	public void setGeophoneEvent(String eventFolder) {
		_goephoneEvent = new ArrayList<LocalDateTime>();
		File[] listOfFiles = new File(eventFolder).listFiles();			
		for (int i=0; i<listOfFiles.length; i++) {
			String name = listOfFiles[i].getName();
			if (listOfFiles[i].isFile()) {
				if(FilenameUtils.isExtension(name, "dat")) {
					String baseName = FilenameUtils.getBaseName(name);
					String tString = baseName.substring(8, 9);
					int len = baseName.length();
					String zString = baseName.substring(len-1, len);
					if(tString.equalsIgnoreCase("T")&&zString.equalsIgnoreCase("Z")) {
						DasMsFileName dasMsFileName = new DasMsFileName(1, name);
						_goephoneEvent.add(dasMsFileName.getLocalDateTime());
					} else {
						String fileName = null;
						try {
							fileName = listOfFiles[i].getCanonicalPath();
						} catch (IOException e1) {
							return;
						}
						
						SEG2 seg2 = new SEG2(fileName);

						//String text = null;
						SEG2FileHeader fh = seg2.getSeg2FileHeader();
						//text = fh.toString();
						SEG2StringBlock sb = fh.getStringBlock();
						//text += sb.toString();
						
						String dayString = sb.getValue("ACQUISITION_DATE");
						String timeString = sb.getValue("ACQUISITION_TIME");
						//String nanoSec = sb.getValue("ACQUISITION_NANOSECONDS");
						
						DateTimeFormatter 	formatter 	= DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
						String tmpString = dayString.trim()+" "+timeString.trim();
						LocalDateTime localDateTime = LocalDateTime.parse(tmpString, formatter);
						_goephoneEvent.add(localDateTime);
					}
				} 
			} 
		}
	}
	
	private class AdvancedPanel extends JPanel {
		public AdvancedPanel(String eventCwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;
			JButton browseButton = null;
			
//			String [] moduleString = new String[]{"Copy the Files with Extension: ", 
//					"Copy the Files Contain Character: ", 
//					"Copy the Files Match Folder: "  };
			String [] moduleString = new String[]{"Copy the Files with Extension: ", 
					"Copy the Files Contain Character: "};
			int n = moduleString.length;
			_enableFileCriterion	= new boolean[n];
			for(int i=0; i<_enableFileCriterion.length; i++) _enableFileCriterion[i] = false;
			_fileCriterionTF		= new JTextField [] {new JTextField("xml"), new JTextField("") };
			JPanel modulePanel = new JPanel( new GridLayout(n, n, 5, 2));
			JCheckBox [] jCheckBoxs = new JCheckBox[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				jCheckBoxs[i] = new JCheckBox(moduleString[i], _enableFileCriterion[j]);
				jCheckBoxs[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						_enableFileCriterion[j] 	= ((JCheckBox)event.getSource()).isSelected(); 
					}
				});
				modulePanel.add(jCheckBoxs[i]);
				modulePanel.add(_fileCriterionTF[i]);
			}

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			JCheckBox jCheckBox = new JCheckBox("Copy Files According to Event Folder: ", _enableEventFolder);
			jCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					_enableEventFolder 	= ((JCheckBox)event.getSource()).isSelected(); 
				}
			});
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jCheckBox, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Copy Files whose time is advanced between: "), gbc);
			_eventOriginTimeTF		= new JTextField[]{ new JTextField(_eventOriginTime[0]+""), new JTextField(_eventOriginTime[1]+"")};
			gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_eventOriginTimeTF[0], gbc);
			gbc= new GridBagConstraints(2, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_eventOriginTimeTF[1], gbc);
			
			_eventFolderTF = new JTextField(eventCwd);
			browseButton = new JButton("Browse"); 
			browseButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String fileName = _frame.openDirectoryUsingJFileChooser(_eventFolderTF.getText()); 
					if(fileName==null) return;
					else {
						_eventFolderTF.setText(fileName);
					}
				}
			});
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_eventFolderTF, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browseButton, gbc);
			
//			iRow++;
//			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.9, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//			add(new JLabel(""), gbc);
		}
	}
	
	
	
	private class LoadFilePanel extends JPanel {
		public LoadFilePanel(String sourcePathCwd, String targetPathCwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;
			JButton browseButton = null;
			
			
//			String [] moduleString = new String[]{"Both File and Directory", "File Only" };
//			int n = moduleString.length;
//			JPanel modulePanel = new JPanel( new GridLayout(1, n, 5, 2));
//			JCheckBox [] jCheckBoxs = new JCheckBox[n];
//			for(int i=0; i<n; i++) {
//				final int j = i;
//				boolean a = j==0?_isCopyDir:_isCopyFile;
//				jCheckBoxs[i] = new JCheckBox(moduleString[i], a);
//				jCheckBoxs[i].addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent event) {
//						if(j==0) 		_isCopyDir 	= ((JCheckBox)event.getSource()).isSelected(); 
//						else if(j==1) 	_isCopyFile = ((JCheckBox)event.getSource()).isSelected();
//					}
//				});
//				modulePanel.add(jCheckBoxs[i]);
//			}
			
			String [] moduleString = new String[]{"Copy Directory from Source to Target", 
					"Copy Both File and Directory from Source to Target", 
					"Delete File in Source Path", 
					"Delete Both File and Directory in Source Path", 
					"List Directory in Source Path", 
					"List Both File and Directory in Source Path"
					
			};
			int n = moduleString.length;
			JPanel modulePanel = new JPanel( new GridLayout(n/2, 2, 5, 2));
			ButtonGroup moduleRadioGroup = new ButtonGroup();
			JRadioButton [] moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], _iFile==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) _iFile = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				modulePanel.add(moduleRadioButton[i]);
			}

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Source Root:"), gbc);

			gbc= new GridBagConstraints(2, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("the maximum number of source directory levels to visit:", JLabel.RIGHT), gbc);
			_sourceDepthTF = new JTextField(_sourceDepth+"");
			gbc= new GridBagConstraints(3, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_sourceDepthTF, gbc);
			
			_sourcePathTF = new JTextField(sourcePathCwd);
			browseButton = new JButton("Browse"); 
			browseButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String fileName = _frame.openDirectoryUsingJFileChooser(_sourcePathTF.getText()); 
					if(fileName==null) return;
					else {
						_sourcePathTF.setText(fileName);
					}
				}
			});
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_sourcePathTF, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browseButton, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Target Root:"), gbc);
			
			_targetPathTF = new JTextField(targetPathCwd);
			browseButton = new JButton("Browse"); 
			browseButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String fileName = _frame.openDirectoryUsingJFileChooser(_targetPathTF.getText()); 
					if(fileName==null) return;
					else {
						_targetPathTF.setText(fileName);
					}
				}
			});
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_targetPathTF, gbc);
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(browseButton, gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Status: "), gbc);
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_msgLabel, gbc);
		}
	}
	
	public void copyFileTree(String sourcePath, String targetPath) {
		try {
			Path source = Paths.get(sourcePath);
			Path target = Paths.get(targetPath);
//			Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
//					Integer.MAX_VALUE, new CopyDirectory(source, target));
			Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
					_sourceDepth, new CopyDirectory(source, target));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private class CopyDirectory extends SimpleFileVisitor<Path> {
		private int indentionLevel = 0;
		private Path source;
		private Path target;

		public CopyDirectory(Path source, Path target) {
			this.source = source;
			this.target = target;
		}
		
		
		public boolean withinRange(String dasFileName) {
			DasMsFileName dasMsFileName = new DasMsFileName(0, dasFileName);	
			double originTime0 = Math.abs(_eventOriginTime[0]);
			double originTime1 = Math.abs(_eventOriginTime[1]);
			for (int i=0; i<_goephoneEvent.size(); i++) {
				double timeDuration = (double)(dasMsFileName.calTimeDuration(_goephoneEvent.get(i)));
				//if(timeDuration>=0.0&&timeDuration<=2.0) return true;
				if(timeDuration>=originTime1&&timeDuration<=originTime0) return true;
			}
			return false;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
				throws IOException {
			//System.out.println("Copying " + source.relativize(file));
			if(_iFile==1) {		
				boolean isCopy = _enableFileCriterion[0]||_enableFileCriterion[1]||(_goephoneEvent!=null)?false:true;
				String a = file.getFileName().toString();
				if(_enableFileCriterion[0]) {
					String extension = _fileCriterionTF[0].getText().trim();
					if(extension==null || extension.isEmpty()) { }
					else {
						if (a.endsWith(extension)) {
							isCopy = true;
						}
					}
				}
				if(_enableFileCriterion[1]) {
					String containsString = _fileCriterionTF[1].getText().trim();
					if(containsString==null || containsString.isEmpty()) { }
					else {
						if (a.contains(containsString)) {
							isCopy = true;
						}
					}
				}
				if(_goephoneEvent!=null) {
					//String dasFileName = file.getFileName().toString();
					//System.out.println("dasFileName="+dasFileName);
					if (a.toLowerCase().endsWith("bin")) {
						if (withinRange(a)) {
							isCopy = true;
						}
					}
				}	
				
				if(isCopy) {
					try {
						Files.copy(file, target.resolve(source.relativize(file)));
						_calcTask.update(a);
					} catch(FileAlreadyExistsException e){
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path directory,
				BasicFileAttributes attributes) throws IOException {
			Path targetDirectory = target.resolve(source.relativize(directory));
			try {
				//System.out.println("Copying " + source.relativize(directory));
				Files.copy(directory, targetDirectory);
				_calcTask.update(source.relativize(directory).toString());
			} catch (FileAlreadyExistsException e) {
				if (!Files.isDirectory(targetDirectory)) {
					throw e;
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}
	
	public void deleteFileTree(String selectedFilePath ) {
		try {
			Files.walkFileTree(Paths.get(selectedFilePath), new DeleteDirectory());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private class DeleteDirectory extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
				throws IOException {
			//System.out.println("Deleting " + file.getFileName());
			Files.delete(file);
			_calcTask.update(file.getFileName().toString());
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path directory,
				IOException exception) throws IOException {
			if (exception == null) {
				//System.out.println("Deleting " + directory.getFileName());
				if(_iFile==3) {
					Files.delete(directory);
					_calcTask.update(directory.getFileName().toString());
				}
				return FileVisitResult.CONTINUE;
			} else {
				throw exception;
			}
		}
	}
	
	public void listFileTree(String selectedFilePath ) {
		Path path = Paths.get(selectedFilePath);
		ListFiles listFiles = new ListFiles();
		try {
			Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), _sourceDepth, listFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Using the SimpleFileVisitor class to traverse file systems
	private class ListFiles extends SimpleFileVisitor<Path> {
		private final int indentionAmount = 3;
		private int indentionLevel;

		public ListFiles() {
			indentionLevel = 0;
		}

		private void indent() {
			for (int i = 0; i < indentionLevel; i++) {
				System.out.print(' ');
			}
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
			indent();
			//if(_iFile==1) System.out.println("Visiting file:" + file.getFileName());
			if(_iFile==5) System.out.println(file.getFileName());
			//		    if (file.toString().endsWith(".java")) {
			//		        System.out.println(file.getFileName());
			//		      }
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path directory, IOException e)
				throws IOException {
			indentionLevel -= indentionAmount;
			indent();
			//System.out.println("Finished with the directory: " + directory.getFileName());
			//System.out.println(directory.getFileName());
			System.out.println("\n");
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path directory,
				BasicFileAttributes attributes) throws IOException {
			indent();
			//System.out.println("About to traverse the directory: " + directory.getFileName());
			System.out.println(directory.getFileName());
			indentionLevel += indentionAmount;
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
				throws IOException {
			System.out.println("A file traversal error ocurred");
			return super.visitFileFailed(file, exc);
		}
	}

}
