/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;

import org.jdom.Element;
import org.jdom.Namespace;

public class FormattedText {

	private String text;

	private boolean bold;
	private boolean italic;
	private boolean underline;

	private Color textColor;

	private String fontFamily;
	private float fontSize;

	public FormattedText(String text, boolean bold, boolean italic,
			boolean underline, Color textColor, String fontFamily, float fontSize) {
		this.text = text;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.textColor = textColor;
		this.fontFamily = fontFamily;
		this.fontSize = fontSize;
	}

	public String getText() {
		return text;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public boolean isUnderline() {
		return underline;
	}

	public Color getTextColor() {
		return textColor;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public float getFontSize() {
		return fontSize;
	}

	public Element toElement(Namespace xslFoNs, Element le, FormattedText last) {
		Element cur = new Element("inline", xslFoNs);
		boolean change = false;
		if(bold && (!last.bold || le == null)) {
			cur.setAttribute("font-weight", "bold");
			change = true;
		}
		if(underline && (!last.underline || le == null)) {
			cur.setAttribute("text-decoration", "underline");
			change = true;
		}
		if(italic && (!last.italic || le == null)) {
			cur.setAttribute("font-style", "italic");
			change = true;
		}
		if(fontSize > 0 && fontSize > last.fontSize) {
			cur.setAttribute("font-size", ((int)fontSize) + "pt");
			change = true;
		}
		if(fontFamily != null && !fontFamily.equals(last.fontFamily)) {
			cur.setAttribute("font-family", "Arial");
			change = true;
		}
		if(!change && le != null) { 
			le.setText(le.getText() + text);
			return null;
		}
		
		
		cur.setText(text);
		return cur;
	}

}
