package org.ucdm.launcher.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;
import org.ucdm.launcher.LauncherFrame;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;


//http://www.fredosaurus.com/notes-cpp/index.html

public class Jave2CConverterDialog extends CommonDialog {
	LauncherFrame  			_frame 		= null;
	ArrayList<String> 		_context 	= null;

	int						_iJavaToC 	= 0; // 0-C++ 1-C# 
	JTextField 				_sourceTF	= null;
	JTextField 				_targetTF	= null;

	int 					_sourceIndex = 22;
	int 					_targetIndex = 23;

	private String 			_className = null;

	public Jave2CConverterDialog(CommonFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(1000, 300);
		setEnableApplyButton(true);
		_frame 		= (LauncherFrame)aParent;
	}

	protected JPanel createContents() { 
		LoadFilePanel panel = new LoadFilePanel(_frame.getCwd(22), _frame.getCwd(23));
		return panel;		
	}

	protected boolean okAction() {
		String inputFileName = _sourceTF.getText().trim();
		String outputFolderName = _targetTF.getText().trim();
		if(_iJavaToC==0) {
			File input = new File(inputFileName);
			if(input.isDirectory()) {
				//String outputFolder = inputFileName.replaceFirst("java", "CPP");

			} else {
				String baseName 	= FilenameUtils.getBaseName(inputFileName);
				int ia = FilenameUtils.indexOfLastSeparator(outputFolderName);
				int ib = outputFolderName.length()-1;
				if(ib>ia) {
					outputFolderName += File.separator;
				}

				_className = baseName;

				String outputFileNameH = outputFolderName+baseName+".h";
				String outputFileNameCPP = outputFolderName+baseName+".cpp";

				//				String fullPath 	= FilenameUtils.getFullPath(inputFileName);
				//				String baseName 	= FilenameUtils.getBaseName(inputFileName);
				//				
				//				String outputFolder = fullPath.replaceFirst("java", "CPP");
				//				try {
				//					FileUtils.forceMkdir(new File(outputFolder+File.separator+"h"));
				//					FileUtils.forceMkdir(new File(outputFolder+File.separator+"cpp"));
				//				} catch (IOException e) {
				//				}
				//				String outputFileNameH = outputFolder+"h"+File.separator+baseName+".h";
				//				String outputFileNameCPP = outputFolder+"cpp"+File.separator+baseName+".cpp";

				//System.out.println(outputFileNameH+" "+outputFileNameCPP);
				_context = read(inputFileName);
				int n = _context.size();
				boolean [] isComments = new boolean[n];
				setCommentsLineAttribute(_context, isComments);
				int [] lineLevel = new int[n];
				setLineLevel(_context, lineLevel, isComments);

				writeH(_context, outputFileNameH, isComments, lineLevel);
				writeCPP(_context, outputFileNameCPP, isComments, lineLevel);
			}
		} else {

		}

		_frame.setCwd(22, inputFileName);
		_frame.setCwd(23, outputFolderName);

		return true;

	}

	public ArrayList<String> read(String fileName) { 
		String inputFileName = fileName;
		BufferedReader br 	= null;
		String line 		= null;

		ArrayList<String> list = new ArrayList<String>();
		try {
			br =  new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF8"));
			line = br.readLine(); 
			list.add(line.trim());
			while ((line = br.readLine()) != null) {
				list.add(line.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} 
		return list;
	} 
	
	public void writeCPP(ArrayList<String> context, String hFileName, boolean [] isComments, int [] lineLevel) { 
		String line = null;
		String a = null;
		int n = context.size();
		String [] typeNames = {"public", "private", "protected"};
		int p1 = 0; 
		int p2 = 0;
			
		BufferedWriter hBfw = null;
		
		try{
			hBfw = new BufferedWriter(new FileWriter(hFileName, false));
			String baseName 	= FilenameUtils.getBaseName(hFileName);
			
			hBfw.newLine();
			hBfw.write("#include <iostream>"); hBfw.newLine();
			hBfw.write("#include \""+_className+".h"+"\""); hBfw.newLine();
			hBfw.write("using namespace std;"); hBfw.newLine();
			hBfw.newLine();
			
			for(int i=0; i<n; i++) {
				line = context.get(i);
				if(isComments[i]) {
					//System.out.println(line);
					hBfw.write(line); hBfw.newLine();
					continue;
				}
				if(lineLevel[i]==2) {
					int ik = -1;
					for(int jk=0; jk<typeNames.length; jk++) {
						if(line.startsWith(typeNames[jk]))  ik=jk; 
					}
					if(ik==-1) {
						//System.out.println(line);
						hBfw.write(line.replace("[", " ").replace("]", "*")); hBfw.newLine();
					} else {
						p1 = 0;  p2 = 0;
						if(line.contains(_className)) {
							p1 = 0; p2 = line.indexOf(' ', p1);
							a = _className+"::"+line.substring(p2+1);
						} else {
							p1 = typeNames[ik].length(); p2 = line.indexOf(' ', p1+1);
							a = line.substring(p1+1,p2) + " "+_className+"::"+line.substring(p2+1);
						}					
						hBfw.write(a.replace("[", " ").replace("]", "*")); hBfw.newLine();
						//System.out.println(a);
					} 
					continue;
				}
				
				if(line.startsWith("package"))  {
					//System.out.println("//"+line);
					hBfw.write("//"+line); hBfw.newLine();
				} else if(line.startsWith("import"))  {
					
				} else {
					int ik = -1;
					for(int jk=0; jk<typeNames.length; jk++) {
						if(line.startsWith(typeNames[jk]))  ik=jk; 
					}
					if(ik==-1) {
						hBfw.write(line); hBfw.newLine();
						//System.out.println(line);
					} 
				}
			}
			hBfw.close();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "\ncppBfw File Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				if (hBfw != null) hBfw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} 
	} 

	

	public void writeH(ArrayList<String> context, String hFileName, boolean [] isComments, int [] lineLevel) { 
		ArrayList<String> hContext = new ArrayList<String>();
		ArrayList<Integer> hContextI = new ArrayList<Integer>();
		String line = null;
		String a = null;
		int n = context.size();
		String [] typeNames = {"public", "private", "protected"};
		int p1 = 0; 
		int p2 = 0;
		for(int i=0; i<n; i++) {
			line = context.get(i);
			if(isComments[i]) {
				//System.out.println(line);
				continue;
			}
			if(lineLevel[i]==2) {
				int ik = -1;
				for(int jk=0; jk<typeNames.length; jk++) {
					if(line.startsWith(typeNames[jk]))  ik=jk; 
				}
				if(ik==-1) {
					//System.out.println(line);
					hContext.add(line); hContextI.add(ik);
				} else {
					p1 = 0;  p2 = 0;
					if(line.contains(_className)) {
						p1 = 0; p2 = line.indexOf(' ', p1);
						//a = _className+"::"+line.substring(p2+1);
						a = line.substring(p2+1);
					} else {
						p1 = typeNames[ik].length(); p2 = line.indexOf(' ', p1+1);
						//a = line.substring(p1+1,p2) + " "+_className+"::"+line.substring(p2+1);
						a = line.substring(p1+1,p2) + " "+line.substring(p2+1);
					}					
					//System.out.println(line);
					//System.out.println(a);
					hContext.add(a); hContextI.add(ik);
				} 
				continue;
			}

			int ik = -1;
			for(int jk=0; jk<typeNames.length; jk++) {
				if(line.startsWith(typeNames[jk]))  ik=jk; 
			}
			if(ik==-1) {
				//System.out.println(line);
			} else {
				//System.out.println(line);
				if(line.contains(";")) {
					p1 = typeNames[ik].length();
					a = line.substring(p1+1);

					hContext.add(a); hContextI.add(ik);
				}
			} 
		}

		ArrayList<String> hPublic = new ArrayList<String>();
		ArrayList<String> hPrivate = new ArrayList<String>();
		ArrayList<String> hProtected = new ArrayList<String>();

		a = null;
		int ia = 0;
		for(int i=0; i<hContext.size(); i++) {
			line = hContext.get(i);
			if(hContextI.get(i)>=0) {
				if(a!=null) {
					p2 = a.indexOf('{', 0);
					if(p2>0) a = a.substring(0,p2) + "; ";
					//System.out.println(a);
					if(ia==0) hPublic.add(a);
					else if(ia==1) hPrivate.add(a);
					else if(ia==2) hProtected.add(a);
				}
				a = line;
				ia = hContextI.get(i);
			} else {
				a += line;
			}
			//System.out.println(hContextI.get(i)+" "+line);
		}
		if(a!=null) {
			p2 = a.indexOf('{', 0);
			if(p2>0) a = a.substring(0,p2) + "; ";
			//System.out.println(a);

			if(ia==0) hPublic.add(a);
			else if(ia==1) hPrivate.add(a);
			else if(ia==2) hProtected.add(a);
		}

//				if(hPublic.size()>0) {
//					System.out.println("\npublic: ");
//					for(int kk=0; kk<hPublic.size(); kk++) {
//						//a = hPublic.get(kk).replaceAll("[\\[\\]\"]", " A");
//						a = hPublic.get(kk).replace("[", " ").replace("]", "*");
//						System.out.println(a);
//					}
//				}
//				if(hPrivate.size()>0) {
//					System.out.println("\nprivate: ");
//					for(int kk=0; kk<hPrivate.size(); kk++) {
//						System.out.println(hPrivate.get(kk).replace("[", " ").replace("]", "*"));
//					}
//				}
//				if(hProtected.size()>0) {
//					System.out.println("protected: \n");
//					for(int kk=0; kk<hProtected.size(); kk++) {
//						System.out.println(hProtected.get(kk));
//					}
//				}


		BufferedWriter hBfw = null;

		try{
			hBfw = new BufferedWriter(new FileWriter(hFileName, false));
			String baseName 	= FilenameUtils.getBaseName(hFileName);

			hBfw.newLine();
			hBfw.write("#ifndef "+ baseName.toUpperCase() +"_H"); hBfw.newLine();
			hBfw.write("#define "+ baseName.toUpperCase() +"_H"); hBfw.newLine();			
			//hBfw.write("#pragma once"); hBfw.newLine();
			hBfw.write("#include <iostream>"); hBfw.newLine();
			hBfw.write("using namespace std;"); hBfw.newLine();
			hBfw.newLine();

			hBfw.write("class "+_className +"{"); hBfw.newLine();
			if(hPublic.size()>0) {
				hBfw.write("public: "); hBfw.newLine();
				for(int kk=0; kk<hPublic.size(); kk++) {
					hBfw.write(hPublic.get(kk).replace("[", " ").replace("]", "*")); hBfw.newLine();
				}
			}
			if(hPrivate.size()>0) {
				hBfw.write("private: "); hBfw.newLine();
				for(int kk=0; kk<hPrivate.size(); kk++) {
					hBfw.write(hPrivate.get(kk).replace("[", " ").replace("]", "*")); hBfw.newLine();
				}
			}
			if(hProtected.size()>0) {
				hBfw.write("protected: "); hBfw.newLine();
				for(int kk=0; kk<hProtected.size(); kk++) {
					hBfw.write(hProtected.get(kk).replace("[", " ").replace("]", "*")); hBfw.newLine();
				}
			}

			hBfw.write("};"); hBfw.newLine();
			hBfw.write("#endif"); hBfw.newLine();
			hBfw.close();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "\nhBfw File Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				if (hBfw != null) hBfw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} 
	} 


	public void write(ArrayList<String> context, String hFileName, boolean [] isComments, int [] lineLevel) { 

		ArrayList<String> hContext = new ArrayList<String>();
		ArrayList<Integer> hContextI = new ArrayList<Integer>();
		String line = null;
		String a = null;
		int n = context.size();
		String [] typeNames = {"public", "private", "protected"};
		int p1 = 0; 
		int p2 = 0;
		for(int i=0; i<n; i++) {
			line = context.get(i);
			if(isComments[i]) {
				//System.out.println(line);
				continue;
			}
			if(lineLevel[i]==2) {
				int ik = -1;
				for(int jk=0; jk<typeNames.length; jk++) {
					if(line.startsWith(typeNames[jk])) {
						ik=jk;
					}
				}
				if(ik==-1) {
					//System.out.println(line);
					hContext.add(line);
					hContextI.add(ik);
				} else {
					p1 = 0; 
					p2 = 0;
					if(line.contains(_className)) {
						p1 = 0;
						p2 = line.indexOf(' ', p1);
						a = _className+"::"+line.substring(p2+1);
					} else {
						p1 = typeNames[ik].length();
						p2 = line.indexOf(' ', p1+1);
						a = line.substring(p1+1,p2) + " "+_className+"::"+line.substring(p2+1);
					}					
					//System.out.println(line);
					//System.out.println(a);
					hContext.add(a);
					hContextI.add(ik);
				} 
				continue;
			}

			int ik = -1;
			for(int jk=0; jk<typeNames.length; jk++) {
				if(line.startsWith(typeNames[jk])) {
					ik=jk;
				}
			}
			if(ik==-1) {
				//System.out.println(line);
			} else {
				//System.out.println(line);
				if(line.contains(";")) {
					p1 = typeNames[ik].length();
					a = line.substring(p1+1);

					hContext.add(a);
					hContextI.add(ik);
				}
			}

		}

		for(int i=0; i<hContext.size(); i++) {
			line = hContext.get(i);
			if(hContextI.get(i)>=0) {
				if(a!=null) System.out.println(a);
				a = line;
			} else {
				a += line;
			}
			//System.out.println(hContextI.get(i)+" "+line);
		}

		BufferedWriter hBfw = null;
		BufferedWriter cppBfw = null;

		//		for (String line : context) { 
		//			a = parseLine(line);
		//			if(a!=null) System.out.println(a); 		
		//		}

		//		try{
		//			hBfw = new BufferedWriter(new FileWriter(hFileName, false));
		//			String baseName 	= FilenameUtils.getBaseName(hFileName);
		//			
		//			hBfw.newLine();
		//			hBfw.write("#ifndef "+ baseName.toUpperCase() +"_H"); hBfw.newLine();
		//			hBfw.write("#define "+ baseName.toUpperCase() +"_H"); hBfw.newLine();			
		//			//hBfw.write("#pragma once"); hBfw.newLine();
		//			hBfw.write("#include <iostream>"); hBfw.newLine();
		//			hBfw.write("using namespace std;"); hBfw.newLine();
		//			hBfw.newLine();
		//			
		//			hBfw.write("class "+_className +"{"); hBfw.newLine();
		//			
		//
		//			hBfw.write("};"); hBfw.newLine();
		//			hBfw.write("#endif"); hBfw.newLine();
		//			hBfw.close();
		//			
		//			
		//			
		//			cppBfw = new BufferedWriter(new FileWriter(cppFileName, false));
		//			
		//			cppBfw.close();
		//
		//
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//			JOptionPane.showMessageDialog(null, "\nhBfw File Format is not right!", 
		//					"Error", JOptionPane.ERROR_MESSAGE);
		//		} finally {
		//			try {
		//				if (hBfw != null) hBfw.close();
		//				if (cppBfw != null) cppBfw.close();
		//			} catch (IOException ex) {
		//				ex.printStackTrace();
		//			}
		//		} 
	} 

	private void setLineLevel(ArrayList<String> context, int [] lineLevel, boolean [] isComments) {
		String line = null;
		int n = lineLevel.length;
		for(int i=0; i<n; i++) lineLevel[i] = 0;
		int k = 0;
		int iLevel = 0;
		while(k<n) {	
			if(isComments[k]) {
				k++;
				continue;
			}
			line = context.get(k);
			if(line.contains("{")) {
				iLevel++;
				lineLevel[k] = iLevel;
				if(line.contains("}")) {
					iLevel--;
				} else {
					//multiple line
					line = context.get(k-1);
					if(line.startsWith("public")||line.startsWith("private")||line.startsWith("protected")) {
						lineLevel[k-1] = iLevel;
					}
					line = context.get(k-2);
					if(line.startsWith("public")||line.startsWith("private")||line.startsWith("protected")) {
						lineLevel[k-2] = iLevel;
					}
				}
			} else {
				if(line.contains("}")) {
					lineLevel[k] = iLevel;
					iLevel--;
				} 	
			}
			k++;
		}
	}


	private void setCommentsLineAttribute(ArrayList<String> context, boolean [] isComments) {
		String line = null;
		int n = isComments.length;
		for(int i=0; i<n; i++) isComments[i] = false;
		int k = 0;
		while(k<n) {			
			line = context.get(k);
			if(line.startsWith("//")) {
				isComments[k++] = true;
			} else {				
				if(line.startsWith("/*")) {	
					if(line.endsWith("*/")) {
						isComments[k++] = true;
					} else {
						do {
							isComments[k++] = true;
							line = context.get(k);
						} while(!line.endsWith("*/"));	
						isComments[k++] = true;
					}
				} else {
					k++;
				}
			}			
		}
	}

	private String parseLine(String line) {
		String delimiter = "[,]+";
		String [] sub = null;
		if(line.startsWith("import")) {
			//System.out.println(line);
			delimiter = "[ .;]+";
			sub = line.split(delimiter);
			String b = sub[sub.length-1];
			//System.out.println(Arrays.deepToString(sub));
			if(b.startsWith("*")) {
				return null;
			} else {
				return "#include \""+b+".h"+"\"";
			}
		} 
		if(line.startsWith("//")||line.startsWith("/*")||line.startsWith("*")) {
			//System.out.println("comments");
			return line;
		}

		if(line.startsWith("public")||line.startsWith("private")||line.startsWith("protected")) {
			if(line.contains(" class "))	{
				//System.out.println(" class ");
				delimiter = "[ ]+";
				sub = line.split(delimiter);
				_className = sub[2];
			}
			else if(line.contains("("))		{
				System.out.println("function");
			}
			else System.out.println("variable defination");
			return null;
		} 
		return line;
	}


	private class LoadFilePanel extends JPanel {
		public LoadFilePanel(String sourceCwd, String targetCwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;


			String [] moduleString = new String[]{"Jave to C++", "Jave to C#" };
			int n = moduleString.length;
			JPanel modulePanel = new JPanel( new GridLayout(1, n, 5, 2));
			ButtonGroup moduleRadioGroup = new ButtonGroup();
			JRadioButton [] moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], _iJavaToC==j);
				moduleRadioButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						if(((JRadioButton)event.getSource()).isSelected()) _iJavaToC = j; 
					}
				});
				moduleRadioGroup.add(moduleRadioButton[i]);
				modulePanel.add(moduleRadioButton[i]);
			}
			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Java Source File or Source Directory:"), gbc);

			_sourceTF = new JTextField(sourceCwd);
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_sourceTF, gbc);
			JButton jButton = new JButton("Browse");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
							new FileNameExtensionFilter("java (*.java)", "java") };

					String fileName = _frame.openFileAndDirectoryUsingJFileChooser(exts, _sourceTF.getText()); 
					if(fileName==null) return;
					else _sourceTF.setText(fileName);
				}
			});	
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jButton, gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Target Directory:"), gbc);

			_targetTF = new JTextField(targetCwd);
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_targetTF, gbc);
			jButton = new JButton("Browse");
			jButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
							new FileNameExtensionFilter("java (*.java)", "java") };

					String fileName = _frame.openDirectoryUsingJFileChooser(_targetTF.getText()); 
					if(fileName==null) return;
					else _targetTF.setText(fileName);
				}
			});	
			gbc= new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jButton, gbc);
		}
	}

}

