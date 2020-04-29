package com.geohammer.common;

import java.util.ArrayList;

import javax.swing.JEditorPane;

public class CommonHtmlPage extends JEditorPane {

	public CommonHtmlPage() {
		super();
		setEditable(false);
		setContentType("text/html");
	}

	public String toHTML(String[][] data){
		StringBuilder sb = new StringBuilder();
		sb.append("<table>\n");
		for(int row = 0; row < data.length; row++){
			sb.append("\t<tr>\n");
			for(int col = 0; col < data[0].length; col++){
				sb.append("\t\t<td>" + data[row][col] + "</td>\n");
			}
			sb.append("\t</tr>\n");
		}
		sb.append("</table>");
		return sb.toString();
	}

//	public void addTable(ArrayList<float []> hList, ArrayList<float []> vList) {
//		int nCol = hList.size();
//		int nRow = hList.get(0).length;
//		StringBuilder sb = new StringBuilder();
//		sb.append("<table>\n");
//		for(int col = 0; col < nCol; col++){
//			sb.append("\t\t<th>" + "Horizontal_" + col + "</th>\n");
//			sb.append("\t\t<th>" + "Vertical_" + col  + "</th>\n");
//		}
//		for(int row = 0; row < nRow; row++){
//			sb.append("\t<tr>\n");
//			for(int col = 0; col < nCol; col++){
//				sb.append("\t\t<td>" + hList.get(col)[row] + "</td>\n");
//				sb.append("\t\t<td>" + vList.get(col)[row] + "</td>\n");
//			}
//			sb.append("\t</tr>\n");
//		}
//		sb.append("</table>");
//		setText("<html><body>"+sb.toString()+"</body></html>");
//	}
	
	public void addTable(ArrayList<float []> hList, ArrayList<float []> vList) {
		int nCol = hList.size();
		int nRow = -1;
		for(int i=0; i<hList.size(); i++) nRow = nRow>hList.get(i).length?nRow:hList.get(i).length;
		
		StringBuilder sb = new StringBuilder();
		sb.append("<table>\n");
		for(int col = 0; col < nCol; col++){
			sb.append("\t\t<th>" + "Horizontal_" + col + "</th>\n");
			sb.append("\t\t<th>" + "Vertical_" + col  + "</th>\n");
		}
		for(int row = 0; row < nRow; row++){
			sb.append("\t<tr>\n");
			for(int col = 0; col < nCol; col++){
				if(row<hList.get(col).length) {
					sb.append("\t\t<td>" + hList.get(col)[row] + "</td>\n");
					sb.append("\t\t<td>" + vList.get(col)[row] + "</td>\n");
				} else {
					sb.append("\t\t<td>" + " " + "</td>\n");
					sb.append("\t\t<td>" + " " + "</td>\n");
				}
			}
			sb.append("\t</tr>\n");
		}
		sb.append("</table>");
		setText("<html><body>"+sb.toString()+"</body></html>");
	}
	
	private String tableHeadToTxt() {
		String text = "";
		String big = getText();
		int from = 0;
		while ( true ) {
			// search for <th>...</th>
			final int tdStart = big.indexOf( "<th", from );
			if ( tdStart < 0 ) break; 
			from = tdStart + 3;
			final int tdEnd = big.indexOf( '>', from );
			if ( tdEnd < 0 ) break; 
			from = tdEnd + 1;
			final int startField = tdEnd + 1;
			final int slashTdStart = big.indexOf( "</th", from );
			int endField = Integer.MAX_VALUE;
			if ( slashTdStart >= 0 && slashTdStart < endField ) endField = slashTdStart; 
			if ( endField == Integer.MAX_VALUE ) break; 
			from = endField + 3;
			final int slashTdEnd = big.indexOf( '>', from );
			if ( slashTdEnd < 0 )  break; 
			String field = big.substring( startField, endField );
			field = field.replaceAll("\\r|\\n|\\t", "");
			text += field.trim()+" ";
			from = slashTdEnd + 1;
		}
		text += "\n";
		return text;		
	}
	
	public String tableToTxt() {
		String text = tableHeadToTxt();
		String big = getText();
		int from = 0;
		while ( true ) {
			// find <tr
			final int trStart = big.indexOf( "<tr", from );
			if ( trStart < 0 ) break; 
			from = trStart + 3;
			final int trEnd = big.indexOf( '>', from );
			if ( trEnd < 0 ) break; 
			while ( true )  {
				// search for <td>...</td>
				final int tdStart = big.indexOf( "<td", from );
				if ( tdStart < 0 ) break; 
				from = tdStart + 3;
				final int tdEnd = big.indexOf( '>', from );
				if ( tdEnd < 0 ) break; 
				from = tdEnd + 1;
				final int startField = tdEnd + 1;
				final int slashTdStart = big.indexOf( "</td", from );
				final int lookaheadTd = big.indexOf( "<td", from );
				final int lookaheadSlashTr = big.indexOf( "</tr", from );
				final int lookaheadTr = big.indexOf( "<tr", from );
				int endField = Integer.MAX_VALUE;
				if ( slashTdStart >= 0 && slashTdStart < endField ) endField = slashTdStart; 
				if ( lookaheadTd >= 0 && lookaheadTd < endField ) endField = lookaheadTd; 
				if ( lookaheadSlashTr >= 0 && lookaheadSlashTr < endField ) endField = lookaheadSlashTr; 
				if ( lookaheadTr >= 0 && lookaheadTr < endField ) endField = lookaheadTr; 
				if ( endField == Integer.MAX_VALUE ) break; 
				from = endField + 3;
				final int slashTdEnd = big.indexOf( '>', from );
				if ( slashTdEnd < 0 )  break; 
				String field = big.substring( startField, endField );
				//field = field.replace("\n", "").replace("\r", "").replace("\t", "");
				field = field.replaceAll("\\r|\\n|\\t", "");
				//System.out.print(field+" ");
				text += field.trim()+" ";
				from = slashTdEnd + 1;
				final int lookTd = big.indexOf( "<td", from );
				final int lookTr = big.indexOf( "<tr", from );
				if ( lookTr >= 0 && lookTr < lookTd || lookTd < 0 ) break; 
			}
			text += "\n";
		}
		return text;		
	}

}
